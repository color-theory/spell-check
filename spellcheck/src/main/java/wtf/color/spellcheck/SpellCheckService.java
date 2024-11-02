package wtf.color.spellcheck;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.index.Term;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

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
                doc.add(new TextField("word", line.trim(), Field.Store.YES));
                writer.addDocument(doc);
            }
        }
        writer.close();
    }

    public String findClosestMatch(String word) throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(index));
        Query query = new FuzzyQuery(new Term("word", word), 1);

        TopDocs results = searcher.search(query, 1);
        if (results.totalHits.value() > 0) {
            StoredFields storedFields = searcher.storedFields();
            Document doc = storedFields.document(results.scoreDocs[0].doc);
            return doc.get("word");
        }
        return null;
    }
}
