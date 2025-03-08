import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Менеджер задач не инициализирован.");
        assertNotNull(taskManager.getAllTasks(), "В менеджере задач не инициализирован контейнер для задач.");
        assertNotNull(taskManager.getAllEpics(), "В менеджере задач не инициализирован контейнер для эпиков.");
        assertNotNull(taskManager.getAllSubtasks(), "В менеджере задач не инициализирован контейнер для подзадач.");
        assertNotNull(taskManager.getHistory(), "В менеджере задач не инициализирован контейнер для хранения истории.");
    }

    @Test
    void shouldReturnHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();

        assertNotNull(history, "В менеджере задач не инициализирован объект истории.");
        assertNotNull(history.getHistory(), "В менеджере задач не инициализирован контейнер для хранения задач.");
    }
}