package wtf.color.spellcheck;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.index.Term;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCheckService {
    private Directory index;

    public SpellCheckService() throws IOException {
        this.index = new ByteBuffersDirectory();
        buildIndex(Paths.get("src/main/resources/dict.txt").toString());
    }

    private void buildIndex(String filePath) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Document doc = new Document();

                Pattern pattern = Pattern.compile("([a-zA-Z]+)\t(\\d+).*$");
                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) {
                    continue;
                }
                Float frequency = Float.parseFloat(matcher.group(2));
                TextField wordField = new TextField("word", matcher.group(1), Field.Store.YES);
                FloatDocValuesField frequencyField = new FloatDocValuesField("frequency",
                        (float) Math.log(frequency + 1));
                StoredField storedFrequencyField = new StoredField("frequency", frequency);

                doc.add(storedFrequencyField);
                doc.add(wordField);
                doc.add(frequencyField);

                writer.addDocument(doc);
            }
        }
        writer.close();
    }

    public String findClosestMatch(String word) throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(index));

        Query exactMatchQuery = new TermQuery(new Term("word", word.trim()));
        DoubleValuesSource boostSource = DoubleValuesSource.fromFloatField("frequency");
        FunctionScoreQuery boostedExactQuery = new FunctionScoreQuery(exactMatchQuery, boostSource);

        TopDocs exactMatchDocs = searcher.search(boostedExactQuery, 1);

        if (exactMatchDocs.totalHits.value() > 0) {
            StoredFields storedFields = searcher.storedFields();
            return storedFields.document(exactMatchDocs.scoreDocs[0].doc).get("word");
        }

        FuzzyQuery query = new FuzzyQuery(new Term("word", word), 2);
        FunctionScoreQuery boostedQuery = new FunctionScoreQuery(query, boostSource);

        TopDocs results = searcher.search(boostedQuery, 10);
        Document bestMatch = null;

        if (results.totalHits.value() > 0) {
            StoredFields storedFields = searcher.storedFields();
            float maxScore = 0;
            for (ScoreDoc doc : results.scoreDocs) {
                Document document = storedFields.document(doc.doc);
                IndexableField frequencyField = document.getField("frequency");
                int frequency = (frequencyField != null) ? frequencyField.numericValue().intValue() : 0;
                if (doc.score >= maxScore && frequency > 0) {
                    System.out.println(
                            "word: " + document.get("word") + " score: " + doc.score + " frequency: " + frequency);
                    maxScore = doc.score;
                    bestMatch = document;
                    break;
                }
            }

            if (bestMatch == null) {
                return null;
            }
            return bestMatch.get("word");
        }

        return null;
    }
}
