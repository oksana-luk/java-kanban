import api.HttpTaskServer;

import api.LocalDateTimeTypeAdapter;
import model.Task;
import model.TaskStatus;
import service.Managers;
import service.TaskManager;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest {

    private final TaskManager taskManager = Managers.getDefault();
    private final HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.createGson();
    private Task task;

    public HttpTaskServerTasksTest() throws IOException {
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
    void shouldReturnTaskList() throws IOException, InterruptedException {
        task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(1));
        task = taskManager.createTask(task);
        String taskJson = gson.toJson(List.of(task));

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), taskJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldReturnTaskPerId() throws IOException, InterruptedException {
        task = new Task("Test task", "This task is very important", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(1));
        task = taskManager.createTask(task);
        String taskJson = gson.toJson(task);

        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", task.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), taskJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldAddTask() throws IOException, InterruptedException {
        String name = "Test task";
        String description = "This task is very important";
        LocalDateTime startTime = LocalDateTime.now().withNano(0);
        task = new Task(name, description, TaskStatus.NEW, startTime, Duration.ofMinutes(1));
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(name, tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        assertEquals(description, tasksFromManager.getFirst().getDescription(), "Некорректное описание задачи");
        assertEquals(startTime, tasksFromManager.getFirst().getStartTime(), "Некорректая дата начала");
        assertEquals(Duration.ofMinutes(1), tasksFromManager.getFirst().getDuration(), "Некорректная длительность");
        assertEquals(TaskStatus.NEW, tasksFromManager.getFirst().getStatus(), "Некорректный статус");
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                LocalDateTime.now().plusMonths(1), Duration.ofMinutes(1));
        task = taskManager.createTask(task);
        String taskJsonBefore = gson.toJson(task);

        task.setName("Updated task");
        task.setDescription("Updated description");
        task.setStartTime(LocalDateTime.now().plusMonths(1));
        task.setDuration(Duration.ofMinutes(2));
        task.setStatus(TaskStatus.IN_PROGRESS);

        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertNotEquals(taskJsonBefore, response.body(), "Данные задачи не обновлены");
        assertEquals(taskJson, response.body(), "Данные задачи не обновлены");
    }

    @Test
    void shouldDeleteTaskPerId() throws IOException, InterruptedException {
        task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                LocalDateTime.now().plusMonths(1), Duration.ofMinutes(1));
        task = taskManager.createTask(task);
        String taskJson = gson.toJson(task);

        assertFalse(taskManager.getAllTasks().isEmpty());

        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", task.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(taskJson, response.body(), "Тело ответа не корректное");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Задача не удалена");
    }

    @Test
    void testNotAllowedMethod() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testNotFound() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Неверный код ответа");

        url = URI.create(String.format("http://localhost:8080/tasks/%s", "f"));
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
        URI url = URI.create("http://localhost:8080/tasks/");
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
        task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                startTime, Duration.ofMinutes(1));
        String taskJson = gson.toJson(task);

        DateTimeFormatter oldDtf = new LocalDateTimeTypeAdapter().getDateTimeFormatter();
        DateTimeFormatter newDtf = DateTimeFormatter.ofPattern("yy MM dd HH mm ss");

        taskJson = taskJson.replace(startTime.format(oldDtf), startTime.format(newDtf));
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testNotAcceptable() throws IOException, InterruptedException {
        task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 10, 12, 30), Duration.ofMinutes(10));
        taskManager.createTask(task);
        task = new Task("Test task", "This task is very important", TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 10, 12, 35), Duration.ofMinutes(10));
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Неверный код ответа");
    }
}


