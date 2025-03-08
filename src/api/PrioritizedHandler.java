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

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(path, exchange.getRequestMethod());
        try {
            if (Objects.requireNonNull(endpoint) == Endpoint.GET_PRIORITIZED) {
                handleGetPrioritizedTasks(exchange);
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

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        String jsonString = gson.toJson(prioritizedTasks);
        sendText(exchange, jsonString, 200);
    }

    private Endpoint getEndpoint(String requestPath, String method) {
        String[] pathParts = requestPath.split("/");

        if (method.equals("GET") && pathParts.length == 2 && pathParts[1].equals("prioritized")) {
            return Endpoint.GET_PRIORITIZED;
        }
        return Endpoint.UNKNOWN;
    }
}

