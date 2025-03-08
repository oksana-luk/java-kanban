import api.HttpTaskServer;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerPrioritizedTest {

    private final TaskManager taskManager = Managers.getDefault();
    private final HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.createGson();

    public HttpTaskServerPrioritizedTest() throws IOException {
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
    void shouldReturnPrioritizedTasksList() throws IOException, InterruptedException {
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());

        Epic epic = taskManager.createEpic(new Epic("name", "description"));
        Task task = taskManager.createTask(new Task("Test task", "This task is very important", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(1)));
        Subtask subtask = taskManager.createSubtask(new Subtask("Test subtask", "This subtask is very important", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now().plusMonths(1), Duration.ofMinutes(1)));

        assertFalse(taskManager.getPrioritizedTasks().isEmpty());

        String prioritizedJson = gson.toJson(List.of(task, subtask));

        URI url = URI.create("http://localhost:8080/prioritized/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), prioritizedJson, "Данные истории не совпадают");
    }

    @Test
    void shouldReturnEmptyPrioritizedTasksList() throws IOException, InterruptedException {
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());

        String prioritizedJson = gson.toJson(List.of());

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(response.body(), prioritizedJson, "Данные истории не совпадают");
    }

    @Test
    void testNotAllowedMethod() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Неверный код ответа");
    }
}




