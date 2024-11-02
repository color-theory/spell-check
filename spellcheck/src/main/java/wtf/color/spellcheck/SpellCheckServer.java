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
    public static void main(String[] args) throws IOException {
        HttpServer server = createServer(8080);
        server.start();
        System.out.println("Server started on port 8080");
    }

    public static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/echo", new EchoHandler());
        server.setExecutor(null);
        return server;
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                String requestText = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, requestText.getBytes().length);

                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(requestText.getBytes());
                responseBody.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}
