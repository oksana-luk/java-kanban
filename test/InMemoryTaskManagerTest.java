import static org.junit.jupiter.api.Assertions.*;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import java.util.List;
import java.util.Optional;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    public InMemoryTaskManager getTaskManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    void beforeEach() {
        taskManager = getTaskManager();
    }

    @Test
    void shouldReturnHistoryWithoutDuplicates() {
        task = taskManager.createTask(createDefaultTask());
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = taskManager.createSubtask(createDefaultSubtask(epic.getId()));

        for (int i = 0; i < 4; i++){
            taskManager.getTask(task.getId());
         }
        for (int i = 0; i < 4; i++){
            taskManager.getEpic(epic.getId());
         }
        for (int i = 0; i < 4; i++){
            taskManager.getSubtask(subtask.getId());
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(task, history.get(0), "Первая задача истории не совпала.");
        assertEquals(epic, history.get(1), "Вторая задача истории не совпала.");
        assertEquals(subtask, history.get(2), "Третья задача истории не совпала.");
    }

    @Test
    void shouldReturnHistoryWithOneTwoThreeTasks() {
        Task task1 = taskManager.createTask(createDefaultTask());
        Task task2 = taskManager.createTask(createDefaultTask());
        Task task3 = taskManager.createTask(createDefaultTask());

        List<Task> historyBefore = taskManager.getHistory();

        assertTrue(historyBefore.isEmpty());

        taskManager.getTask(task1.getId());
        List<Task> historyAfter1 = taskManager.getHistory();

        assertFalse(historyAfter1.isEmpty());
        assertEquals(task1, historyAfter1.getFirst(), "Первая задача истории не совпала.");

        taskManager.getTask(task2.getId());
        List<Task> historyAfter2 = taskManager.getHistory();

        assertFalse(historyAfter2.isEmpty());
        assertEquals(task1, historyAfter2.getFirst(), "Первая задача истории не совпала.");
        assertEquals(task2, historyAfter2.get(1), "Вторая задача истории не совпала.");

        taskManager.getTask(task3.getId());
        List<Task> historyAfter3 = taskManager.getHistory();

        assertFalse(historyAfter3.isEmpty());
        assertEquals(task1, historyAfter3.getFirst(), "Первая задача истории не совпала.");
        assertEquals(task2, historyAfter3.get(1), "Вторая задача истории не совпала.");
        assertEquals(task3, historyAfter3.get(2), "Третья задача истории не совпала.");
    }

    @Test
    void shouldNotBeInHistoryTaskAfterRemoving() {
        task = taskManager.createTask(createDefaultTask());
        epic = taskManager.createEpic(createDefaultEpic());
        Subtask subtask1 = createDefaultSubtask(epic.getId());
        subtask1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = createDefaultSubtaskInEpic();
        subtask2 = taskManager.createSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());
        List<Task> historyBefore = taskManager.getHistory();

        assertEquals(historyBefore.size(), 4, "Размер истории не верный.");
        assertEquals(task, historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(epic, historyBefore.get(1), "Вторая задача истории не совпала.");
        assertEquals(subtask1, historyBefore.get(2), "Третья задача истории не совпала.");
        assertEquals(subtask2, historyBefore.get(3), "Четвёртая задача истории не совпала.");

        taskManager.deleteTaskPerId(task.getId());
        List<Task> historyBefore1 = taskManager.getHistory();

        assertEquals(historyBefore1.size(), 3, "Размер истории не верный.");
        assertFalse(historyBefore1.contains(task), "Задача не была удалена из истории.");
        assertEquals(epic, historyBefore1.get(0), "Первая задача истории не совпала.");
        assertEquals(subtask1, historyBefore1.get(1), "Вторая задача истории не совпала.");
        assertEquals(subtask2, historyBefore1.get(2), "Третья задача истории не совпала.");

        taskManager.deleteEpicPerId(epic.getId());
        List<Task> historyBefore2 = taskManager.getHistory();

        assertEquals(historyBefore2.size(), 1, "Размер истории не верный.");
        assertFalse(historyBefore2.contains(epic), "Эпик не был удален из истории.");
        assertFalse(historyBefore2.contains(subtask1), "Подзадача эпика не была удалена из истории.");
        assertEquals(subtask2, historyBefore2.getFirst(), "Первая задача истории не совпала.");

        taskManager.deleteSubtaskPerId(subtask2.getId());
        List<Task> historyBefore3 = taskManager.getHistory();

        assertTrue(historyBefore3.isEmpty(), "Подзадача не была удалена из истории.");
    }

    @Test
    void shouldNotBeInHistoryTasksAfterRemovingAll() {
        Task[] tasks = new Task[3];
        for (int i = 0; i < 3; i++){
            tasks[i] = taskManager.createTask(createDefaultTask());
            taskManager.getTask(tasks[i].getId());
        }
        epic = taskManager.createEpic(createDefaultEpic());
        taskManager.getEpic(epic.getId());
        List<Task> historyBefore = taskManager.getHistory();

        assertEquals(historyBefore.size(), 4, "Размер истории не верный.");
        assertEquals(tasks[0], historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(epic, historyBefore.get(3), "Четвёртая задача истории не совпала.");

        taskManager.deleteAllTasks();
        List<Task> historyAfter = taskManager.getHistory();

        assertEquals(historyAfter.size(), 1, "Размер истории не верный.");
        assertEquals(epic, historyAfter.getFirst(), "Первая задача истории не совпала.");
    }

    @Test
    void shouldNotBeInHistoryEpicsAfterRemovingAll() {
        Epic[] epics = new Epic[3];
        Subtask[] subtasks = new Subtask[3];
        for (int i = 0; i < 3; i++){
            epics[i] = taskManager.createEpic(createDefaultEpic());
            subtasks[i] = taskManager.createSubtask(createDefaultSubtask(epics[i].getId()));
            taskManager.getEpic(epics[i].getId());
            taskManager.getSubtask(subtasks[i].getId());
        }
        task = taskManager.createTask(createDefaultTask());
        taskManager.getTask(task.getId());
        List<Task> historyBefore = taskManager.getHistory();

        assertEquals(historyBefore.size(), 7, "Размер истории не верный.");
        assertEquals(epics[0], historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(subtasks[0], historyBefore.get(1), "Вторая задача истории не совпала.");
        assertEquals(task, historyBefore.get(6), "Последняя задача истории не совпала.");

        taskManager.deleteAllEpics();
        List<Task> historyAfter = taskManager.getHistory();

        assertEquals(historyAfter.size(), 1, "Размер истории не верный.");
        assertEquals(task, historyAfter.getFirst(), "Первая задача истории не совпала.");
    }

    @Test
    void shouldNotBeInHistorySubtasksAfterRemovingAll() {
        Subtask[] subtasks = new Subtask[3];
        epic = taskManager.createEpic(createDefaultEpic());

        for (int i = 0; i < 3; i++){
            subtasks[i] =  taskManager.createSubtask(createDefaultSubtask(epic.getId()));
            taskManager.getSubtask(subtasks[i].getId());
        }

        task = taskManager.createTask(createDefaultTask());
        taskManager.getTask(task.getId());
        List<Task> historyBefore = taskManager.getHistory();

        assertEquals(historyBefore.size(), 4, "Размер истории не верный.");
        assertEquals(subtasks[0], historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(task, historyBefore.get(3), "Четвёртая задача истории не совпала.");

        taskManager.deleteAllSubtasks();
        List<Task> historyAfter = taskManager.getHistory();

        assertEquals(historyAfter.size(), 1, "Размер истории не верный.");
        assertEquals(task, historyAfter.getFirst(), "Первая задача истории не совпала.");
    }

    @Test
    void historyIsIndependentOfTask() {
        task = taskManager.createTask(createDefaultTask());
        taskManager.getTask(task.getId());

        Task taskBefore = taskManager.getHistory().getFirst();
        Optional<Task> optTaskAfter = taskManager.getTask(task.getId());
        assertTrue(optTaskAfter.isPresent(), "");
        Task taskAfter = optTaskAfter.get();

        taskAfter.setDescription("newDescription");
        taskAfter.setName("newName");
        taskAfter.setStatus(TaskStatus.IN_PROGRESS);

        assertTrue(taskManager.updateTask(taskAfter), "Задача не обновлена.");
        assertEquals(taskBefore.getId(), taskAfter.getId(), "Идентификаторы не совпали.");
        assertNotEquals(taskBefore.getName(), taskAfter.getName(), "Имя задач совпало.");
        assertNotEquals(taskBefore.getDescription(), taskAfter.getDescription(), "Описание задач совпало.");
        assertNotEquals(taskBefore.getStatus(), taskAfter.getStatus(), "Статус задач совпал.");
    }
}
