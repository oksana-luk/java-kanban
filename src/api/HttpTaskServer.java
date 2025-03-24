package api;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.*;
import service.FileBackedTaskManager;
//import service.Managers;
import service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    protected TaskManager taskManager;
    protected HttpServer httpServer;
    protected Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = createGson();
        httpServer = createHttpServer(this.taskManager, gson);
    }

    public static void main(String[] args) throws IOException {
        //TaskManager taskManager = Managers.getDefault();
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(new File("data.csv"));
        HttpTaskServer taskServer = new HttpTaskServer(taskManager);
        taskServer.startHttpServer();
    }

    public static HttpServer createHttpServer(TaskManager taskManager, Gson gson) throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress("127.0.0.1", 8080), 0);

        httpServer.createContext("/tasks", new TasksHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicsHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));

        return httpServer;
    }

    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }

    public void startHttpServer() {
        httpServer.start();
    }

    public void stopHttpServer() {
        httpServer.stop(0);
    }
}
