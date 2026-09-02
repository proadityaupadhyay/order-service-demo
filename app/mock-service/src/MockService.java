import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Canned stand-in for a downstream backend. Not part of the CI/CD pipeline --
 * a local convenience only. Run with: java src/MockService.java
 */
public class MockService {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/inventory", exchange -> {
            String body = "{\"sku\":\"stub\",\"available\":true,\"quantity\":42}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("mock-service listening on port 9090");
    }
}
