package learning.temporal.demo.lighthouse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class LighthouseWebMain {
    private static final int PORT = 8080;
    private static WorkflowClient client;

    public static void main(String[] args) throws Exception {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        client = WorkflowClient.newInstance(service);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT), 0);
        server.createContext("/", LighthouseWebMain::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            service.shutdown();
        }));

        System.out.println("The Last Lighthouse UI is ready at http://localhost:" + PORT);
        new CountDownLatch(1).await();
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            if ((path.equals("/") || path.equals("/index.html")) && method.equals("GET")) {
                servePage(exchange);
            } else if (path.equals("/api/games") && method.equals("POST")) {
                startGame(exchange);
            } else if (path.startsWith("/api/games/")) {
                handleGameAction(exchange, path, method);
            } else {
                respond(exchange, 404, "application/json", "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            respond(exchange, 500, "application/json", jsonError(e.getMessage()));
        }
    }

    private static void servePage(HttpExchange exchange) throws IOException {
        try (var page = LighthouseWebMain.class.getResourceAsStream("/lighthouse.html")) {
            if (page == null) {
                respond(exchange, 500, "text/plain; charset=utf-8", "Missing lighthouse.html resource");
                return;
            }
            respond(exchange, 200, "text/html; charset=utf-8",
                    new String(page.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void startGame(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String player = form.getOrDefault("player", "Keeper").trim();
        if (player.isEmpty()) player = "Keeper";

        String id = "lighthouse-" + UUID.randomUUID();
        LighthouseWorkflow game = client.newWorkflowStub(
                LighthouseWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(id)
                        .setTaskQueue(LighthouseWorkerMain.TASK_QUEUE)
                        .build());
        WorkflowClient.start(game::play, player);
        respond(exchange, 201, "application/json", "{\"id\":" + jsonString(id) + "}");
    }

    private static void handleGameAction(HttpExchange exchange, String path, String method) throws IOException {
        String[] parts = path.substring("/api/games/".length()).split("/");
        if (parts.length == 0 || parts[0].isBlank()) {
            respond(exchange, 400, "application/json", "{\"error\":\"Workflow ID required\"}");
            return;
        }

        LighthouseWorkflow game = client.newWorkflowStub(LighthouseWorkflow.class, parts[0]);
        if (parts.length == 1 && method.equals("GET")) {
            String status = game.status();
            long deadline = game.roomDeadlineEpochMillis();
            String result = "{\"status\":" + jsonString(status)
                    + ",\"deadline\":" + deadline + "}";
            respond(exchange, 200, "application/json", result);
        } else if (parts.length == 2 && method.equals("POST") && parts[1].equals("answer")) {
            String answer = parseForm(exchange).getOrDefault("answer", "").trim();
            if (answer.isEmpty()) {
                respond(exchange, 400, "application/json", "{\"error\":\"Enter an answer\"}");
                return;
            }
            game.submitAnswer(answer);
            respond(exchange, 202, "application/json", "{\"ok\":true}");
        } else if (parts.length == 2 && method.equals("POST") && parts[1].equals("hint")) {
            game.requestHint();
            respond(exchange, 202, "application/json", "{\"ok\":true}");
        } else {
            respond(exchange, 404, "application/json", "{\"error\":\"Unknown game action\"}");
        }
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isBlank()) continue;
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length > 1
                    ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private static String jsonError(String message) {
        return "{\"error\":" + jsonString(message == null ? "Request failed" : message) + "}";
    }

    private static String jsonString(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) escaped.append(String.format("\\u%04x", (int) c));
                    else escaped.append(c);
                }
            }
        }
        return escaped.append('"').toString();
    }

    private static void respond(HttpExchange exchange, int code, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(code, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
