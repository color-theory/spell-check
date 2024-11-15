package wtf.color.spellcheck;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpellCheckServerTest {
    private static HttpServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = SpellCheckServer.createServer(8080);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void testResponse() throws IOException {
        String inputText = "Henlo, Worlf!";
        String response = sendPostRequest("http://localhost:8080/spellcheck", inputText);
        System.out.println(response);
        assertEquals("Hello, World!", response);
    }

    private String sendPostRequest(String urlString, String body) throws IOException {
        URL url;
        try {
            url = new URI(urlString).toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + urlString, e);
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (var br = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        connection.disconnect();
        return response.toString();
    }
}