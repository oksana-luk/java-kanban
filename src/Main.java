import exception.ManagerAddTaskException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.FileBackedTaskManager;
import service.InMemoryTaskManager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException {
        //FileBackedTaskManager taskManager = new FileBackedTaskManager(new File("data.csv"));
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(new File("data.csv"));

        Task task1 = taskManager.createTask(new Task("Запись к врачу", "Записаться к другому терапевту",
                TaskStatus.NEW, LocalDateTime.of(2025, 02, 15, 12, 0), Duration.ofMinutes(40)));
        try {
            Task task2 = taskManager.createTask(new Task("Купить хлебницу", "", TaskStatus.IN_PROGRESS,
                    LocalDateTime.of(2025, 02, 15, 12, 30), Duration.ofMinutes(55)));
        } catch (ManagerAddTaskException e) {
            System.out.println(e.getMessage());
        }
        Task task3 = taskManager.createTask(new Task("Водафон", "Просмотреть новые тарифы", TaskStatus.DONE,
                LocalDateTime.of(2025, 02, 17, 12, 15), Duration.ofMinutes(25)));

        Epic epic1 = taskManager.createEpic(new Epic("Подарки на НГ", "Заказать подарки на новый год онлайн"));
        Epic epic2 = taskManager.createEpic(new Epic("Украсить дом к НГ", ""));
        Epic epic3 = taskManager.createEpic(new Epic("Обустройство дома", ""));

        Subtask subtask11 = taskManager.createSubtask(new Subtask("Маленькие подарки детям",
                "Заказать подарки на тему лапки сквиш и еще что-то для 4 детей одинаковое", TaskStatus.NEW,
                epic1.getId(), LocalDateTime.of(2025, 02, 18, 13, 15), Duration.ofMinutes(60)));
        Subtask subtask12 = taskManager.createSubtask(new Subtask("Подароки взрослым",
                "Заказать на тему странные штучки по мальним ценам для взрослых на 4 человека", TaskStatus.NEW,
                epic1.getId(), LocalDateTime.of(2025, 02, 19, 18, 15), Duration.ofMinutes(80)));
        Subtask subtask13 = taskManager.createSubtask(new Subtask("Подарки в родителям",
                "Зказать на озон подарки родственникам с доставкой по почтеб м б на вайлдберриз", TaskStatus.NEW,
                epic1.getId(), LocalDateTime.of(2025, 02, 20, 23, 0), Duration.ofMinutes(70)));
        try {
            Subtask subtask14 = taskManager.createSubtask(new Subtask("Сладкие подарки детям",
                    "Заказать на амазон подарки детям сладкие на 2 человек", TaskStatus.NEW, epic1.getId(),
                    LocalDateTime.of(2025, 02, 21, 0, 0), Duration.ofMinutes(30)));
        } catch (ManagerAddTaskException e) {
            System.out.println(e.getMessage());;
        }
        Subtask subtask21 = taskManager.createSubtask(new Subtask("Купить елку", "", TaskStatus.NEW,
                epic2.getId(), LocalDateTime.of(2025, 02, 22, 18, 0), Duration.ofMinutes(120)));
        Subtask subtask22 = taskManager.createSubtask(new Subtask("купить шарики", "Заказать на озон", TaskStatus.IN_PROGRESS,
                epic2.getId(), LocalDateTime.of(2025, 02, 23, 14, 40), Duration.ofMinutes(20)));
        Subtask subtask23 = taskManager.createSubtask(new Subtask("купить гирлянды", "Заказать на вайлдберриз", TaskStatus.DONE,
                epic2.getId(), LocalDateTime.of(2025, 02, 24, 15, 15), Duration.ofMinutes(25)));

        InMemoryTaskManager.printAllTasks(taskManager);
//        for (Task task : taskManager.getPrioritizedTasks()) {
//            System.out.println(task);
//        }

//        System.out.println("_____".repeat(5) + "delete task id 2");
//        System.out.println("_____".repeat(5) + "delete epic id 4");
//        System.out.println("_____".repeat(5) + "delete leer epic id 6 and subtask id 12");

//        taskManager.deleteTaskPerId(2);
//        taskManager.deleteEpicPerId(4);
//        taskManager.deleteEpicPerId(6);
//        taskManager.deleteSubtaskPerId(12);

        for (Task task : taskManager.getPrioritizedTasks()) {
            System.out.println(task);
        }

        //InMemoryTaskManager.printAllTasks(taskManager);
    }
}

