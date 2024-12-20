public class Managers {
    //утилитарный класс
    //подбирать нужную реализацию TaskManager и возвращать объект правильного типа
    //Добавьте в служебный класс Managers статический метод HistoryManager getDefaultHistory.
    // Он должен возвращать объект InMemoryHistoryManager — историю просмотров.

    //метод возвращает объет класса, наследующего интерфейс TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }


}
