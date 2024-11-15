package wtf.color.spellcheck;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCheckServer {
    public static HttpServer createServer(int port) throws IOException {
        SpellCheckService spellChecker = new SpellCheckService();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/spellcheck", new SpellCheckHandler(spellChecker));
        server.setExecutor(null);
        return server;
    }

    static class SpellCheckHandler implements HttpHandler {
        private SpellCheckService spellChecker;

        public SpellCheckHandler(SpellCheckService spellChecker) {
            this.spellChecker = spellChecker;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                String requestText = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

                Pattern pattern = Pattern.compile("(\\w+)(\\W*)");
                Matcher matcher = pattern.matcher(requestText);

                StringBuilder result = new StringBuilder();
                while (matcher.find()) {
                    String word = matcher.group(1);
                    String punctuation = matcher.group(2);

                    String corrected;
                    try {
                        corrected = spellChecker.findClosestMatch(word.toLowerCase());
                    } catch (Exception e) {
                        corrected = "Error: " + e.getMessage();
                    }
                    if (corrected == null) {
                        corrected = word;
                    } else {
                        CaseCorrector caseCorrector = new CaseCorrector();
                        corrected = caseCorrector.applyOriginalCasePattern(word, corrected);
                    }

                    // Append the transformed word and the original punctuation
                    result.append(corrected).append(punctuation);
                }

                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, result.toString().getBytes().length);

                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(result.toString().getBytes());
                responseBody.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = createServer(port);
        server.start();
        System.out.println("Server started on port " + port);
    }
}
