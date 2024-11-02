package wtf.color.spellcheck;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

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

                String corrected;
                try{
                    corrected = spellChecker.findClosestMatch(requestText);
                } catch (Exception e) {
                    corrected = "Error: " + e.getMessage();
                }
                if (corrected == null) {
                    corrected = "No suggestions found";
                }
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, corrected.getBytes().length);

                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(corrected.getBytes());
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
