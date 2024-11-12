import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.nio.file.Paths;
import java.nio.file.Files;

public class CreateDict {
    public static void main(String[] args) {
        try {
            System.out.printf("Getting allowed Words...");
            String[] allowedWords = Files.readString(Paths.get("./words.txt"))
                    .split("\n");
            for (int i = 0; i < allowedWords.length; i++) {
                allowedWords[i] = allowedWords[i].trim().toLowerCase();
            }
            System.out.printf("Allowed Words: %d Checking for words in unigram_freq.csv...", allowedWords.length);
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(Paths.get("./unigram_freq.csv").toString()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Pattern pattern = Pattern.compile("(.+),(\\d+).*$");
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        System.out.printf("Checking if word with frequency is allowed...");
                        if (Arrays.asList(allowedWords).contains(matcher.group(1))) {
                            System.out.printf("Word is allowed, writing to dict.txt... %s", matcher.group(1));
                            try (FileWriter writer = new FileWriter(
                                    Paths.get("dict.txt").toString(), true)) {
                                writer.write(matcher.group(1) + "\t" + matcher.group(2) + "\n");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
