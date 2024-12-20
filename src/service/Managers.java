package service;

public class Managers {
    //утилитарный класс
    //подбирать нужную реализацию service.TaskManager и возвращать объект правильного типа
    //Добавьте в служебный класс service.Managers статический метод service.HistoryManager getDefaultHistory.
    // Он должен возвращать объект service.InMemoryHistoryManager — историю просмотров.

    //метод возвращает объет класса, наследующего интерфейс service.TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }


}
