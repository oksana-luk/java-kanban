package api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(path, exchange.getRequestMethod());
        try {
            if (Objects.requireNonNull(endpoint) == Endpoint.GET_HISTORY) {
                handleGetHistory(exchange);
            } else {
                sendResponse(exchange, "Method not allowed.", 405);
            }
        } catch (DateTimeParseException | JsonSyntaxException e) {
            sendResponse(exchange, e.getMessage(), 400);
        } catch (Exception e) {
            sendResponse(exchange, e.getMessage(), 500);
        } finally {
            exchange.close();
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        String jsonString = gson.toJson(history);
        sendText(exchange, jsonString, 200);
    }

    private Endpoint getEndpoint(String requestPath, String method) {
        String[] pathParts = requestPath.split("/");

        if (method.equals("GET") && pathParts.length == 2 && pathParts[1].equals("history")) {
            return Endpoint.GET_HISTORY;
        }
        return Endpoint.UNKNOWN;
    }
}

