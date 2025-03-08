package api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerAddTaskException;
import exception.TaskNotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(path, exchange.getRequestMethod());
        try {
            switch (endpoint) {
                case GET_SUBTASKS:
                    handleGetSubtasks(exchange);
                    break;
                case GET_SUBTASK:
                    handleGetSubtaskPerId(exchange);
                    break;
                case POST_SUBTASK:
                    handlePostSubtask(exchange);
                    break;
                case DELETE_SUBTASK:
                    handleDeleteSubtask(exchange);
                default:
                    sendResponse(exchange, "Method not allowed.", 405);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        } catch (ManagerAddTaskException e) {
            sendResponse(exchange, e.getMessage(), 406);
        } catch (TaskNotFoundException e) {
            sendResponse(exchange, e.getMessage(), 404);
        } catch (DateTimeParseException | JsonSyntaxException e) {
            sendResponse(exchange, e.getMessage(), 400);
        } catch (Exception e) {
            sendResponse(exchange, e.getMessage(), 500);
        } finally {
            exchange.close();
        }
    }

    private Endpoint getEndpoint(String requestPath, String method) {
        String[] pathParts = requestPath.split("/");

        if (method.equals("GET") && pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            return Endpoint.GET_SUBTASKS;
        }
        if (method.equals("GET") && pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            return Endpoint.GET_SUBTASK;
        }
        if (method.equals("POST") && pathParts.length == 2 && pathParts[1].equals("subtasks")){
            return Endpoint.POST_SUBTASK;
        }
        if (method.equals("DELETE") && pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            return Endpoint.DELETE_SUBTASK;
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> allSubtasks= taskManager.getAllSubtasks();

        String jsonString = gson.toJson(allSubtasks);
        sendText(exchange, jsonString, 200);
    }

    private void handleGetSubtaskPerId(HttpExchange exchange) throws IOException {
        int subtaskId = getTaskId(exchange.getRequestURI().getPath());
        Optional<Subtask> subtaskOptional = taskManager.getSubtask(subtaskId);
        if (subtaskOptional.isEmpty()) {
            throw new TaskNotFoundException(String.format("Не найдена задача с id %s.", subtaskId));
        } else {
            String jsonString = gson.toJson(subtaskOptional.get());
            sendText(exchange, jsonString, 200);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        Subtask subtask = parseTask(exchange.getRequestBody());
        int epicId = subtask.getId();
        if (epicId == 0) {
            subtask = taskManager.createSubtask(subtask);
        } else {
            subtask = taskManager.updateSubtask(subtask);
        }
        String jsonString = gson.toJson(subtask);
        sendText(exchange, jsonString, 201);
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        int subtaskId = getTaskId(exchange.getRequestURI().getPath());
        Subtask subtask = taskManager.deleteSubtaskPerId(subtaskId);
        String jsonString = gson.toJson(subtask);
        sendText(exchange, jsonString, 200);
    }

    private Subtask parseTask(InputStream requestBody) throws IOException {
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(body, Subtask.class);
    }
}
