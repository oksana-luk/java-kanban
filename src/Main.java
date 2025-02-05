import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.FileBackedTaskManager;
import service.InMemoryTaskManager;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        //FileBackedTaskManager taskManager = new FileBackedTaskManager(new File("data.csv"));
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(new File("data.csv"));

//        Task task1 = taskManager.createTask(new Task("Запись к врачу", "Записаться к другому терапевту", TaskStatus.NEW));
//        Task task2 = taskManager.createTask(new Task("Купить хлебницу", "", TaskStatus.IN_PROGRESS));
//        Task task3 = taskManager.createTask(new Task("Водафон", "Просмотреть новые тарифы", TaskStatus.DONE));
//
//        Epic epic1 = taskManager.createEpic(new Epic("Подарки на НГ", "Заказать подарки на новый год онлайн"));
//        Epic epic2 = taskManager.createEpic(new Epic("Украсить дом к НГ", ""));
//        Epic epic3 = taskManager.createEpic(new Epic("Обустройство дома", ""));
//
//        Subtask subtask11 = taskManager.createSubtask(new Subtask("Маленькие подарки детям", "Заказать подарки на тему лапки сквиш и еще что-то для 4 детей одинаковое", TaskStatus.NEW, epic1.getId()));
//        Subtask subtask12 = taskManager.createSubtask(new Subtask("Подароки взрослым", "Заказать на тему странные штучки по мальним ценам для взрослых на 4 человека", TaskStatus.NEW, epic1.getId()));
//        Subtask subtask13 = taskManager.createSubtask(new Subtask("Подарки в родителям", "Зказать на озон подарки родственникам с доставкой по почтеб м б на вайлдберриз", TaskStatus.NEW, epic1.getId()));
//        Subtask subtask14 = taskManager.createSubtask(new Subtask("Сладкие подарки детям", "Заказать на амазон подарки детям сладкие на 2 человек", TaskStatus.NEW, epic1.getId()));
//
//        Subtask subtask21 = taskManager.createSubtask(new Subtask("Купить елку", "", TaskStatus.NEW, epic2.getId()));
//        Subtask subtask22 = taskManager.createSubtask(new Subtask("купить шарики", "Заказать на озон", TaskStatus.IN_PROGRESS, epic2.getId()));
//        Subtask subtask23 = taskManager.createSubtask(new Subtask("купить гирлянды", "Заказать на вайлдберриз", TaskStatus.DONE, epic2.getId()));

        InMemoryTaskManager.printAllTasks(taskManager);

        System.out.println("_____".repeat(5) + "delete task id 2");
        System.out.println("_____".repeat(5) + "delete epic id 4");
        System.out.println("_____".repeat(5) + "delete leer epic id 6 and subtask id 12");

        taskManager.deleteTaskPerId(2);
        taskManager.deleteEpicPerId(4);
        taskManager.deleteEpicPerId(6);
        taskManager.deleteSubtaskPerId(12);

        InMemoryTaskManager.printAllTasks(taskManager);
    }
}

