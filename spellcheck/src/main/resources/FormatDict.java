import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.nio.file.Paths;

public class FormatDict {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get("./spellcheck/src/main/resources/unigram_freq.csv").toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("([a-zA-Z]+),(\\d+).*$");
                Matcher matcher = pattern.matcher(line);  
                while(matcher.find()) {
                    try (FileWriter writer = new FileWriter(Paths.get("./spellcheck/src/main/resources/dict.txt").toString(), true)) {
                        writer.write(matcher.group(1) +" "+ matcher.group(2) + "\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    
}
