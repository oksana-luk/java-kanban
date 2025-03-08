import api.HttpTaskServer;
import api.LocalDateTimeTypeAdapter;
import com.google.gson.Gson;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerSubtasksTest {

    private final TaskManager taskManager = Managers.getDefault();
    private final HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.createGson();
    private Subtask subtask;
    private Epic epic;

    public HttpTaskServerSubtasksTest() throws IOException {
    }

    @BeforeEach
    void beforeEach() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();
        httpTaskServer.startHttpServer();
    }

    @AfterEach
    void afterEach() {
        httpTaskServer.stopHttpServer();
    }

    @Test
    void shouldReturnSubtasksList() throws IOException, InterruptedException {
        epic = taskManager.createEpic(new Epic("name", "description"));
        subtask = new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(1));
        subtask = taskManager.createSubtask(subtask);
        String subtaskJson = gson.toJson(List.of(subtask));

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), subtaskJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldReturnSubtaskPerId() throws IOException, InterruptedException {
        epic = taskManager.createEpic(new Epic("name", "description"));
        subtask = new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(1));
        subtask = taskManager.createSubtask(subtask);
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", subtask.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), subtaskJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldAddSubtask() throws IOException, InterruptedException {
        epic = taskManager.createEpic(new Epic("name", "description"));
        String name = "Test subtask";
        String description = "This subtask is very important";
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        subtask = new Subtask(name, description, TaskStatus.NEW, epic.getId(), startTime, Duration.ofMinutes(1));
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Subtask> subtaskFromManager = taskManager.getAllSubtasks();

        assertNotNull(subtaskFromManager, "Задачи не возвращаются");
        assertEquals(1, subtaskFromManager.size(), "Некорректное количество задач");
        assertEquals(name, subtaskFromManager.getFirst().getName(), "Некорректное имя задачи");
        assertEquals(description, subtaskFromManager.getFirst().getDescription(), "Некорректное описание задачи");
        assertEquals(startTime, subtaskFromManager.getFirst().getStartTime(), "Некорректая дата начала");
        assertEquals(Duration.ofMinutes(1), subtaskFromManager.getFirst().getDuration(), "Некорректная длительность");
        assertEquals(TaskStatus.NEW, subtaskFromManager.getFirst().getStatus(), "Некорректный статус");
    }

    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        epic = taskManager.createEpic(new Epic("name", "description"));
        subtask = new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now().plusMonths(1), Duration.ofMinutes(1));
        subtask = taskManager.createSubtask(subtask);
        String subtaskJsonBefore = gson.toJson(subtask);

        subtask.setName("Updated task");
        subtask.setDescription("Updated description");
        subtask.setStartTime(LocalDateTime.now().plusMonths(1));
        subtask.setDuration(Duration.ofMinutes(2));
        subtask.setStatus(TaskStatus.IN_PROGRESS);

        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertNotEquals(subtaskJsonBefore, response.body(), "Данные задачи не обновлены");
        assertEquals(subtaskJson, response.body(), "Данные задачи не обновлены");
    }

    @Test
    void shouldDeleteSubtaskPerId() throws IOException, InterruptedException {
        epic = taskManager.createEpic(new Epic("name", "description"));
        subtask = new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now().plusMonths(1), Duration.ofMinutes(1));
        subtask = taskManager.createSubtask(subtask);
        String subtaskJson = gson.toJson(subtask);

        assertFalse(taskManager.getAllSubtasks().isEmpty());

        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", subtask.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(subtaskJson, response.body(), "Тело ответа не корректное");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Задача не удалена");
    }

    @Test
    void testNotAllowedMethod() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testNotFound() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Неверный код ответа");

        url = URI.create(String.format("http://localhost:8080/subtasks/%s", "f"));
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testJsonSyntaxException() throws IOException, InterruptedException {
        String gsonString = gson.toJson(",");
        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gsonString))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testDateTimeParseException() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        Task task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                startTime, Duration.ofMinutes(1));
        String taskJson = gson.toJson(task);

        DateTimeFormatter oldDtf = new LocalDateTimeTypeAdapter().getDateTimeFormatter();
        DateTimeFormatter newDtf = DateTimeFormatter.ofPattern("yy MM dd HH mm ss");

        taskJson = taskJson.replace(startTime.format(oldDtf), startTime.format(newDtf));
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testNotAcceptable() throws IOException, InterruptedException {
        epic = taskManager.createEpic(new Epic("name", "description"));
        subtask = new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 3, 10, 12, 30), Duration.ofMinutes(10));
        taskManager.createSubtask(subtask);
        subtask = new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 3, 10, 12, 35), Duration.ofMinutes(10));
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Неверный код ответа");
    }
}



