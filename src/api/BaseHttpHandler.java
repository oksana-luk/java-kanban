package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    protected final Gson gson;
    protected final TaskManager taskManager;

    BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    public void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.getResponseBody().flush();
        exchange.close();
    }

    public void sendNotFound(HttpExchange exchange) throws IOException {
        ErrorResponse response = new ErrorResponse(404, exchange.getRequestURI().getPath(), "Not Found");
        String jsonString = gson.toJson(response);
        byte[] resp = jsonString.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.getResponseBody().flush();
        exchange.close();
    }

    public void sendResponse(HttpExchange exchange, String text, int code) throws IOException {
        ErrorResponse response = new ErrorResponse(code, exchange.getRequestURI().getPath(), text);
        String jsonString = gson.toJson(response);
        byte[] resp = jsonString.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(code, resp.length);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.getResponseBody().write(resp);
        exchange.getResponseBody().flush();
        exchange.close();
    }

    public int getTaskId(String requestPath) {
        String[] pathParts = requestPath.split("/");
        return Integer.parseInt(pathParts[2]);
    }
}


