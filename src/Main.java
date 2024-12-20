import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = taskManager.createTask(new Task("Запись к психотерапевту", "Записать Веронику к другому психотерапевту", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Купить хлебницу", "", TaskStatus.IN_PROGRESS));
        Task task3 = taskManager.createTask(new Task("Водафон", "Просмотреть новые тарифы", TaskStatus.DONE));

        System.out.println(taskManager.getAllTasks());

        Epic epic1 = taskManager.createEpic(new Epic("Подарки на НГ", "Заказать подарки на новый год онлайн"));
        Epic epic2 = taskManager.createEpic(new Epic("Украсить дом к НГ", ""));
        Epic epic3 = taskManager.createEpic(new Epic("Обустройство дома", ""));


        Subtask subtask11 = taskManager.createSubtask(new Subtask("Маленькие подарки детям", "Заказать подарки на тему лапки сквиш и еще что-то для 4 детей одинаковое", TaskStatus.NEW, epic1.getId()));
        Subtask subtask12 = taskManager.createSubtask(new Subtask("Подароки взрослым", "Заказать на тему странные штучки по мальним ценам для взрослых на 4 человека", TaskStatus.NEW, epic1.getId()));
        Subtask subtask13 = taskManager.createSubtask(new Subtask("Подарки в россии", "Зказать на озон подарки родственникам с доставкой по почтеб м б на вайлдберриз", TaskStatus.NEW, epic1.getId()));
        Subtask subtask14 = taskManager.createSubtask(new Subtask("Сладкие подарки детям", "Заказать на амазон подарки детям сладкие на 2 человек", TaskStatus.NEW, epic1.getId()));


        Subtask subtask21 = taskManager.createSubtask(new Subtask("Купить елку", "", TaskStatus.NEW, epic2.getId()));
        Subtask subtask22 = taskManager.createSubtask(new Subtask("купить шарики", "В красном кресте", TaskStatus.IN_PROGRESS, epic2.getId()));
        Subtask subtask23 = taskManager.createSubtask(new Subtask("купить гирлянды", "В красном кресте", TaskStatus.DONE, epic2.getId()));


        Subtask subtask31 = taskManager.createSubtask(new Subtask("Посмотреть лампу в ванную", "На кляйнанцайген", TaskStatus.DONE, epic3.getId()));
        Subtask subtask32 = taskManager.createSubtask(new Subtask("Стулья на кухню", "Со спинками и не дороже 40 евро за 4 стула", TaskStatus.DONE, epic3.getId()));
        Subtask subtask33 = taskManager.createSubtask(new Subtask("Вытяжка на кухню", "В красном кресте", TaskStatus.DONE, epic3.getId()));

        System.out.println("_____".repeat(5));

        System.out.println(taskManager.getAllEpics());

        System.out.println("_____".repeat(5));

        System.out.println(taskManager.getAllSubtasks());

        System.out.println("_____".repeat(5));

        System.out.println(taskManager.getHistory());

        taskManager.getTask(1);
        taskManager.getEpic(4);

        taskManager.getSubtask(7);
        taskManager.getTask(2);
        taskManager.getEpic(5);
        taskManager.getSubtask(8);
        taskManager.getTask(3);
        taskManager.getEpic(6);
        taskManager.getSubtask(9);
        taskManager.getSubtask(10);
        taskManager.getSubtask(10);
        taskManager.getSubtask(10);

        System.out.println(taskManager.getHistory());


    }
}

