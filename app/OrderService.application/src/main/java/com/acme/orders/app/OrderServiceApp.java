package com.acme.orders.app;

import com.acme.orders.module.OrderCalculator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Stand-in runnable for what a real BWCE application would expose.
 *
 * Two config layers, matching the repo's design:
 *  - Build-time / design-time: configuration/{env}.substvar is baked into
 *    the jar as classpath resource "design-time.substvar" by the Maven
 *    profile selected in the Jenkinsfile based on branch.
 *  - Deploy-time / runtime: deploy/config/{env}/application.properties is
 *    passed to the container via `docker run --env-file` and read here from
 *    the process environment.
 */
public class OrderServiceApp {

    private static double discountRate = 0.0;

    public static void main(String[] args) throws Exception {
        discountRate = readDesignTimeDiscountRate();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String envName = System.getenv().getOrDefault("APP_ENV", "unknown");
        String logLevel = System.getenv().getOrDefault("LOG_LEVEL", "INFO");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", exchange -> respond(exchange, 200, "OK"));

        server.createContext("/info", exchange -> respond(exchange, 200,
                "{\"env\":\"" + envName + "\",\"discountRate\":" + discountRate
                        + ",\"logLevel\":\"" + logLevel + "\",\"appVersion\":\"1.1\"}"));

        server.createContext("/order", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                respond(exchange, 405, "method not allowed");
                return;
            }
            double subtotal = parseSubtotal(exchange.getRequestURI().getQuery());
            double total = new OrderCalculator().calculateTotal(subtotal, discountRate);
            respond(exchange, 200, "{\"subtotal\":" + subtotal + ",\"discountRate\":" + discountRate
                    + ",\"total\":" + total + "}");
        });

        server.start();
        System.out.println("OrderService listening on port " + port + " (env=" + envName
                + ", discountRate=" + discountRate + ")");
    }

    private static double parseSubtotal(String query) {
        if (query == null) {
            return 0.0;
        }
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "subtotal".equals(kv[0])) {
                return Double.parseDouble(kv[1]);
            }
        }
        return 0.0;
    }

    private static double readDesignTimeDiscountRate() {
        try (InputStream in = OrderServiceApp.class.getClassLoader()
                .getResourceAsStream("design-time.substvar")) {
            if (in == null) {
                return 0.0;
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            NodeList vars = doc.getElementsByTagName("globalVariable");
            for (int i = 0; i < vars.getLength(); i++) {
                Element var = (Element) vars.item(i);
                String name = textOf(var, "name");
                if ("OrderService/DiscountRate".equals(name)) {
                    return Double.parseDouble(textOf(var, "value"));
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Failed to read design-time.substvar, defaulting discountRate=0.0: " + e);
            return 0.0;
        }
    }

    private static String textOf(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
