package api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerAddTaskException;
import exception.TaskNotFoundException;
import model.Epic;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(path, exchange.getRequestMethod());
        try {
            switch (endpoint) {
                case GET_EPICS:
                    handleGetEpics(exchange);
                    break;
                case GET_EPIC:
                    handleGetEpicPerId(exchange);
                    break;
                case GET_EPIC_SUBTASKS:
                    handleGetEpicSubtasks(exchange);
                case POST_EPIC:
                    handlePostEpic(exchange);
                    break;
                case DELETE_EPIC:
                    handleDeleteEpic(exchange);
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

        if (method.equals("GET") && pathParts.length == 2 && pathParts[1].equals("epics")) {
            return Endpoint.GET_EPICS;
        }
        if (method.equals("GET") && pathParts.length == 3 && pathParts[1].equals("epics")) {
            return Endpoint.GET_EPIC;
        }
        if (method.equals("GET") && pathParts.length == 4 && pathParts[1].equals("epics")
            && pathParts[3].equals("subtasks")) {
            return Endpoint.GET_EPIC_SUBTASKS;
        }
        if (method.equals("POST") && pathParts.length == 2 && pathParts[1].equals("epics")){
            return Endpoint.POST_EPIC;
        }
        if (method.equals("DELETE") && pathParts.length == 3 && pathParts[1].equals("epics")) {
            return Endpoint.DELETE_EPIC;
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        List<Epic> epicsList= taskManager.getAllEpics();

        String jsonString = gson.toJson(epicsList);
        sendText(exchange, jsonString, 200);
    }

    private void handleGetEpicPerId(HttpExchange exchange) throws IOException {
        int epicId = getTaskId(exchange.getRequestURI().getPath());
        Optional<Epic> epicOptional = taskManager.getEpic(epicId);
        if (epicOptional.isEmpty()) {
            throw new TaskNotFoundException(String.format("Не найдена задача с id %s.", epicId));
        } else {
            String jsonString = gson.toJson(epicOptional.get());
            sendText(exchange, jsonString, 200);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        int epicId = getTaskId(exchange.getRequestURI().getPath());
        List<Subtask> subtasksList =  taskManager.getEpicSubtasks(epicId);
        String jsonString = gson.toJson(subtasksList);
        sendText(exchange, jsonString, 200);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        Epic epic = parseTask(exchange.getRequestBody());
        int epicId = epic.getId();
        if (epicId == 0) {
            epic = taskManager.createEpic(epic);
        } else {
            epic = taskManager.updateEpic(epic);
        }
        String jsonString = gson.toJson(epic);
        sendText(exchange, jsonString, 201);
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        int epicId = getTaskId(exchange.getRequestURI().getPath());
        Epic epic = taskManager.deleteEpicPerId(epicId);
        String jsonString = gson.toJson(epic);
        sendText(exchange, jsonString, 200);
    }

    private Epic parseTask(InputStream requestBody) throws IOException {
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(body, Epic.class);
    }
}


