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

public class HttpTaskServerEpicsTest {

    private final TaskManager taskManager = Managers.getDefault();
    private final HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.createGson();
    private Epic epic;

    public HttpTaskServerEpicsTest() throws IOException {
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
    void shouldReturnEpicList() throws IOException, InterruptedException {
        epic = new Epic("Test epic", "This epic is very important");
        epic = taskManager.createEpic(epic);
        String epicJson = gson.toJson(List.of(epic));

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), epicJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldReturnEpicPerId() throws IOException, InterruptedException {
        epic = new Epic("Test epic", "This epic is very important");
        epic = taskManager.createEpic(epic);
        String epicJson = gson.toJson(epic);

        URI url = URI.create(String.format("http://localhost:8080/epics/%d", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), epicJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldReturnEpicsSubtasksPerId() throws IOException, InterruptedException {
        epic = new Epic("Test epic", "This epic is very important");
        epic = taskManager.createEpic(epic);

        Subtask subtask1 = taskManager.createSubtask(new Subtask("name", "description", TaskStatus.NEW,
                epic.getId(), LocalDateTime.now(), Duration.ofMinutes(10)));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("name", "description", TaskStatus.NEW,
                epic.getId(), LocalDateTime.now().plusMonths(1), Duration.ofMinutes(10)));

        String taskJson = gson.toJson(List.of(subtask1, subtask2));

        URI url = URI.create(String.format("http://localhost:8080/epics/%d/subtasks", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), taskJson, "Данные задачи не совпадают");
    }

    @Test
    void shouldAddEpic() throws IOException, InterruptedException {
        String name = "Test epic";
        String description = "This epic is very important";
        epic = new Epic(name, description);
        String taskJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Epic> epicsFromManager = taskManager.getAllEpics();

        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals(name, epicsFromManager.getFirst().getName(), "Некорректное имя задачи");
        assertEquals(description, epicsFromManager.getFirst().getDescription(), "Некорректное описание задачи");
    }

    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        epic = new Epic("Test epic", "This epic is very important");
        epic = taskManager.createEpic(epic);
        String epicJsonBefore = gson.toJson(epic);

        epic.setName("Updated epic");
        epic.setDescription("Updated description");

        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertNotEquals(epicJsonBefore, response.body(), "Данные задачи не обновлены");
        assertEquals(epicJson, response.body(), "Данные задачи не обновлены");
    }

    @Test
    void shouldDeleteEpicPerId() throws IOException, InterruptedException {
        epic = new Epic("Test epic", "This epic is very important");
        epic = taskManager.createEpic(epic);
        String epicJson = gson.toJson(epic);

        assertFalse(taskManager.getAllEpics().isEmpty());

        URI url = URI.create(String.format("http://localhost:8080/epics/%d", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(epicJson, response.body(), "Тело ответа не корректное");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Задача не удалена");
    }

    @Test
    void testNotAllowedMethod() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Неверный код ответа");
    }

    @Test
    void testNotFound() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/epics/%d", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Неверный код ответа");

        url = URI.create(String.format("http://localhost:8080/epics/%s", "f"));
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
        URI url = URI.create("http://localhost:8080/epics/");
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
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Неверный код ответа");
    }
}



