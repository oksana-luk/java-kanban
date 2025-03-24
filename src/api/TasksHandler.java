package api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerAddTaskException;
import exception.TaskNotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(path, exchange.getRequestMethod());
        try {
            switch (endpoint) {
                case GET_TASKS:
                    handleGetTasks(exchange);
                    break;
                case GET_TASK:
                    handleGetTaskPerId(exchange);
                    break;
                case POST_TASK:
                    handlePostTask(exchange);
                    break;
                case DELETE_TASK:
                    handleDeleteTask(exchange);
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

        if (method.equals("GET") && pathParts.length == 2 && pathParts[1].equals("tasks")) {
            return Endpoint.GET_TASKS;
        }
        if (method.equals("GET") && pathParts.length == 3 && pathParts[1].equals("tasks")) {
            return Endpoint.GET_TASK;
        }
        if (method.equals("POST") && pathParts.length == 2 && pathParts[1].equals("tasks")) {
            return Endpoint.POST_TASK;
        }
        if (method.equals("DELETE") && pathParts.length == 3 && pathParts[1].equals("tasks")) {
            return Endpoint.DELETE_TASK;
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> taskList = taskManager.getAllTasks();

        String jsonString = gson.toJson(taskList);
        sendText(exchange, jsonString, 200);
    }

    private void handleGetTaskPerId(HttpExchange exchange) throws IOException {
        int taskId = getTaskId(exchange.getRequestURI().getPath());
        Optional<Task> taskOptional = taskManager.getTask(taskId);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundException(String.format("Не найдена задача с id %s.", taskId));
        } else {
            String jsonString = gson.toJson(taskOptional.get());
            sendText(exchange, jsonString, 200);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        Task task = parseTask(exchange.getRequestBody());
        int taskId = task.getId();
        if (taskId == 0) {
            task = taskManager.createTask(task);
        } else {
            task = taskManager.updateTask(task);
        }
        String jsonString = gson.toJson(task);
        sendText(exchange, jsonString, 201);
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        int taskId = getTaskId(exchange.getRequestURI().getPath());
        Task task = taskManager.deleteTaskPerId(taskId);
        String jsonString = gson.toJson(task);
        sendText(exchange, jsonString, 200);
    }

    private Task parseTask(InputStream requestBody) throws IOException {
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(body, Task.class);
    }
}
