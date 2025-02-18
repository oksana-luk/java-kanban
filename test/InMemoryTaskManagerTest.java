import static org.junit.jupiter.api.Assertions.*;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManagerTest {
    private InMemoryTaskManager memoryTaskManager;

    @BeforeEach
    void beforeEach() {
        memoryTaskManager = (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    void shouldAddTaskSetId() {
        Task task = createDefaultTask();

        ArrayList<Task> tasksBefore = memoryTaskManager.getAllTasks();
        Task newTask = memoryTaskManager.createTask(task);
        ArrayList<Task> tasksAfter = memoryTaskManager.getAllTasks();

        assertTrue(tasksBefore.isEmpty(), "Изначальный список задач не пуст.");
        assertFalse(tasksAfter.isEmpty(), "Задача не добавилась в список задач.");
        assertEquals(newTask, tasksAfter.getFirst(), "Задачи не совпали.");
        assertNotEquals(0, newTask.getId(), "Идентификатор не был сгенерирован.");
    }

    @Test
    void shouldAddTaskWithoutChange() {
        Task task = createDefaultTask();
        Task newTask = memoryTaskManager.createTask(task);

        assertEquals(task.getName(), newTask.getName(), "Не совпали имена задач.");
        assertEquals(task.getDescription(), newTask.getDescription(), "Не совпали описания задач.");
        assertEquals(task.getStatus(), newTask.getStatus(), "Не совпали статусы задач.");
        assertEquals(task.getStartTime(), newTask.getStartTime(), "Не совпала дата начала задачи.");
        assertEquals(task.getDuration(), newTask.getDuration(), "Не совпада длительность задачи");
     }

    @Test
    void shouldAddEpicSetIdSetStatusNew() {
        Epic epic = createDefaultEpic();

        ArrayList<Epic> epicBefore = memoryTaskManager.getAllEpics();
        Epic newEpic = memoryTaskManager.createEpic(epic);
        ArrayList<Epic> epicsAfter = memoryTaskManager.getAllEpics();

        assertTrue(epicBefore.isEmpty(), "Изначальный список эпиков не пуст.");
        assertFalse(epicsAfter.isEmpty(), "Эпик не добавился в список задач.");
        assertEquals(newEpic, epicsAfter.getFirst(), "Эпики не совпали.");
        assertNotEquals(0, newEpic.getId(), "Идентификатор не был сгенерирован.");
        assertEquals(TaskStatus.NEW, newEpic.getStatus(),  "Не определился статус у эпика.");
    }

    @Test
    void shouldAddEpicWithoutChange() {
        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);

        assertEquals(epic.getName(), newEpic.getName(), "Не совпали имена эпиков.");
        assertEquals(epic.getDescription(), newEpic.getDescription(), "Не совпали описания эпиков.");
        assertEquals(epic.getStatus(), newEpic.getStatus(), "Не совпали статусы эпиков.");
        assertNull(epic.getStartTime(), "Не совпала дата начала.");
        assertEquals(epic.getDuration(), Duration.ZERO, "Не совпада длительность.");
    }

        @Test
    void shouldAddSubtaskSetId() {
        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(newEpic.getId());


        ArrayList<Subtask> subtasksBefore = memoryTaskManager.getAllSubtasks();
        Subtask newSubtask = memoryTaskManager.createSubtask(subtask);
        ArrayList<Subtask> subtasksAfter = memoryTaskManager.getAllSubtasks();

        assertTrue(subtasksBefore.isEmpty(), "Изначальный список подзадач не пуст.");
        assertFalse(subtasksAfter.isEmpty(), "Подзадача не добавилась в список задач.");
        assertEquals(newSubtask, subtasksAfter.getFirst(), "Подзадачи не совпали.");
        assertNotEquals(0, newSubtask.getId(),  "Идентификатор не был сгенерирован.");
    }

    @Test
    void shouldAddSubtaskWithoutChange() {
        Subtask subtask = createDefaultSubtaskInEpic();
        Subtask newSubtask = memoryTaskManager.createSubtask(subtask);

        assertEquals(subtask.getName(), newSubtask.getName(), "Не совпали имена подзадач.");
        assertEquals(subtask.getDescription(), newSubtask.getDescription(), "Не совпали описания подзадач.");
        assertEquals(subtask.getStatus(), newSubtask.getStatus(), "Не совпали статусы подзадач.");
        assertEquals(subtask.getStartTime(), newSubtask.getStartTime(), "Не совпала дата начала задачи.");
        assertEquals(subtask.getDuration(), newSubtask.getDuration(), "Не совпада длительность задачи");
    }

    @Test
    void shouldAddSubtaskInEpicAndChangeEpicsStatus() {
        Epic newEpic = createDefaultEpic();
        newEpic = memoryTaskManager.createEpic(newEpic);
        Subtask subtask = createDefaultSubtask(newEpic.getId());

        Subtask newSubtask = memoryTaskManager.createSubtask(subtask);
        newEpic = memoryTaskManager.getEpic(newEpic.getId());
        ArrayList<Integer> subtasksId = newEpic.getSubtasksIds();

        assertTrue(subtasksId.contains(newSubtask.getId()), "Подзадача не добавлена в эпик.");
        assertEquals(TaskStatus.NEW, newEpic.getStatus(), "Статус эпика определен некорректно.");

        subtask.setStatus(TaskStatus.IN_PROGRESS);
        memoryTaskManager.updateSubtask(subtask);

        newEpic = memoryTaskManager.getEpic(newEpic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, newEpic.getStatus(), "Статус эпика определен некорректно.");

        subtask.setStatus(TaskStatus.DONE);
        memoryTaskManager.updateSubtask(subtask);
        newEpic = memoryTaskManager.getEpic(newEpic.getId());

        assertEquals(TaskStatus.DONE, newEpic.getStatus(), "Статус эпика определен некорректно.");

        Subtask subtask2 = createDefaultSubtask(newEpic.getId());
        memoryTaskManager.createSubtask(subtask2);
        newEpic = memoryTaskManager.getEpic(newEpic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, newEpic.getStatus(), "Статус эпика определен некорректно.");
    }

    @Test
    void shouldReturnTaskById() {
        assertNull(memoryTaskManager.getTask(1));

        Task task = createDefaultTask();

        Task newTask = memoryTaskManager.createTask(task);
        Task currentTask = memoryTaskManager.getTask(newTask.getId());

        assertEquals(newTask, currentTask, "Задачи не совпали.");
    }

    @Test
    void shouldReturnEpicById() {
        assertNull(memoryTaskManager.getEpic(1));

        Epic epic = createDefaultEpic();

        Epic newEpic = memoryTaskManager.createEpic(epic);
        Epic currentEpic = memoryTaskManager.getEpic(newEpic.getId());

        assertEquals(newEpic, currentEpic, "Эпики не совпали.");
    }

    @Test
    void shouldReturnSubtaskById() {
        assertNull(memoryTaskManager.getSubtask(1));

        Subtask newSubtask = createDefaultSubtaskInEpic();

        Subtask currentSubtask = memoryTaskManager.getSubtask(newSubtask.getId());

        assertEquals(newSubtask, currentSubtask, "Подзадачи не совпали.");
    }

    @Test
    void shouldReturnEpicsSubtasks() {
        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);
        int epicId = newEpic.getId();

        ArrayList<Subtask> subtasks = memoryTaskManager.getEpicSubtasks(epicId);

        assertTrue(subtasks.isEmpty());

        Subtask subtask = createDefaultSubtask(epicId);
        memoryTaskManager.createSubtask(subtask);
        ArrayList<Subtask> subtasks2 = memoryTaskManager.getEpicSubtasks(epicId);

        assertFalse(subtasks2.isEmpty(), "Список подзадач эпика пуст.");
        assertEquals(subtask, subtasks2.getFirst(), "Подзадачи не совпали.");
    }

    @Test
    void shouldChangeNameDescriptionStatusOfTask() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        Task task = createDefaultTask();
        Task newTask = memoryTaskManager.createTask(task);

        newTask.setName(expectedName);
        newTask.setDescription(expectedDescription);
        newTask.setStatus(TaskStatus.IN_PROGRESS);

        assertTrue(memoryTaskManager.updateTask(newTask), "Задачи не обновлена.");

        Task updatedTask = memoryTaskManager.getTask(newTask.getId());

        assertEquals(expectedName, updatedTask.getName(), "Имена задач не совпали.");
        assertEquals(expectedDescription, updatedTask.getDescription(), "Описания задач не совпали.");
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus(), "Статусы задач не совпали.");
    }

    @Test
    void shouldChangeNameDescriptionOfEpic() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);

        newEpic.setName(expectedName);
        newEpic.setDescription(expectedDescription);

        assertTrue(memoryTaskManager.updateEpic(newEpic), "Эпик не обновлен.");

        Task updatedEpic = memoryTaskManager.getEpic(newEpic.getId());

        assertEquals(expectedName, updatedEpic.getName(), "Имена эпиков не совпали.");
        assertEquals(expectedDescription, updatedEpic.getDescription(), "Описания эпиков не совпали.");
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus(), "Статусы эпиков не совпали.");
    }

    @Test
    void shouldChangeNameDescriptionStatusOfSubtask() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        Subtask subtask = createDefaultSubtaskInEpic();
        Subtask newSubtask = memoryTaskManager.createSubtask(subtask);

        newSubtask.setName(expectedName);
        newSubtask.setDescription(expectedDescription);
        newSubtask.setStatus(TaskStatus.IN_PROGRESS);

        assertTrue(memoryTaskManager.updateSubtask(newSubtask), "Подзадача не обновлена.");

        Task updatedSubtask = memoryTaskManager.getSubtask(newSubtask.getId());

        assertEquals(expectedName, updatedSubtask.getName(), "Имена подзадач не совпали.");
        assertEquals(expectedDescription, updatedSubtask.getDescription(), "Описания подзадач не совпали.");
        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getStatus(), "Статусы подзадач не совпали.");
    }

    @Test
    void shouldNotChangeNameDescriptionStatusOfTaskWithSetters() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        Task task = createDefaultTask();
        task = memoryTaskManager.createTask(task);

        task.setName(expectedName);
        task.setDescription(expectedDescription);
        task.setStatus(TaskStatus.IN_PROGRESS);

        Task taskInTaskManager = memoryTaskManager.getTask(task.getId());

        assertNotEquals(expectedName, taskInTaskManager.getName(), "Поле имя доступно для изменения.");
        assertNotEquals(expectedDescription, taskInTaskManager.getDescription(), "Поле описание доступно для изменения.");
        assertNotEquals(TaskStatus.IN_PROGRESS, taskInTaskManager.getStatus(), "Поле статус доступно для изменения.");
    }

    @Test
    void shouldNotChangeNameDescriptionOfEpicWithSetters() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        Epic epic = createDefaultEpic();
        epic = memoryTaskManager.createEpic(epic);

        epic.setName(expectedName);
        epic.setDescription(expectedDescription);

        Epic epicInTaskManager = memoryTaskManager.getEpic(epic.getId());

        assertNotEquals(expectedName, epicInTaskManager.getName(), "Поле имя доступно для изменения.");
        assertNotEquals(expectedDescription, epicInTaskManager.getDescription(), "Поле описание доступно для изменения.");
        assertNotEquals(TaskStatus.IN_PROGRESS, epicInTaskManager.getStatus(), "Поле статус доступно для изменения.");
    }

    @Test
    void shouldNotChangeNameDescriptionStatusOfSubtaskWithSetters() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        Subtask subtask = createDefaultSubtaskInEpic();

        subtask.setName(expectedName);
        subtask.setDescription(expectedDescription);
        subtask.setStatus(TaskStatus.IN_PROGRESS);

        Subtask subtaskInTaskManager = memoryTaskManager.getSubtask(subtask.getId());

        assertNotEquals(expectedName, subtaskInTaskManager.getName(), "Поле имя доступно для изменения.");
        assertNotEquals(expectedDescription, subtaskInTaskManager.getDescription(), "Поле описание доступно для изменения.");
        assertNotEquals(TaskStatus.IN_PROGRESS, subtaskInTaskManager.getStatus(), "Поле статус доступно для изменения.");
    }

    @Test
    void shouldReturnHistoryWithoutDuplicates() {
        Task task = createDefaultTask();
        memoryTaskManager.createTask(task);
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        memoryTaskManager.createSubtask(subtask);

        for (int i = 0; i < 4; i++){
            memoryTaskManager.getTask(task.getId());
         }

        for (int i = 0; i < 4; i++){
            memoryTaskManager.getEpic(epic.getId());
         }

        for (int i = 0; i < 4; i++){
            memoryTaskManager.getSubtask(subtask.getId());
        }

        List<Task> history = memoryTaskManager.getHistory();
        assertEquals(task, history.get(0), "Первая задача истории не совпала.");
        assertEquals(epic, history.get(1), "Вторая задача истории не совпала.");
        assertEquals(subtask, history.get(2), "Третья задача истории не совпала.");
    }

    @Test
    void shouldReturnHistoryWithOneTwoThreeTasks() {
        Task task1 = createDefaultTask();
        memoryTaskManager.createTask(task1);
        Task task2 = createDefaultTask();
        memoryTaskManager.createTask(task2);
        Task task3 = createDefaultTask();
        memoryTaskManager.createTask(task3);

        List<Task> historyBefore = memoryTaskManager.getHistory();
        assertTrue(historyBefore.isEmpty());

        memoryTaskManager.getTask(task1.getId());
        List<Task> historyAfter1 = memoryTaskManager.getHistory();
        assertFalse(historyAfter1.isEmpty());
        assertEquals(task1, historyAfter1.get(0), "Первая задача истории не совпала.");

        memoryTaskManager.getTask(task2.getId());
        List<Task> historyAfter2 = memoryTaskManager.getHistory();
        assertFalse(historyAfter2.isEmpty());
        assertEquals(task1, historyAfter2.get(0), "Первая задача истории не совпала.");
        assertEquals(task2, historyAfter2.get(1), "Вторая задача истории не совпала.");

        memoryTaskManager.getTask(task3.getId());
        List<Task> historyAfter3 = memoryTaskManager.getHistory();
        assertFalse(historyAfter3.isEmpty());
        assertEquals(task1, historyAfter3.get(0), "Первая задача истории не совпала.");
        assertEquals(task2, historyAfter3.get(1), "Вторая задача истории не совпала.");
        assertEquals(task3, historyAfter3.get(2), "Третья задача истории не совпала.");
    }

    @Test
    void shouldNotBeInHistoryTaskAfterRemoving() {
        Task task = createDefaultTask();
        memoryTaskManager.createTask(task);
        Epic defaultEpic = createDefaultEpic();
        Epic epic = memoryTaskManager.createEpic(defaultEpic);
        Subtask subtask1 = createDefaultSubtask(epic.getId());
        memoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = createDefaultSubtaskInEpic();

        memoryTaskManager.getTask(task.getId());
        memoryTaskManager.getEpic(epic.getId());
        memoryTaskManager.getSubtask(subtask1.getId());
        memoryTaskManager.getSubtask(subtask2.getId());

        List<Task> historyBefore = memoryTaskManager.getHistory();
        assertEquals(historyBefore.size(), 4, "Размер истории не верный.");
        assertEquals(task, historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(epic, historyBefore.get(1), "Вторая задача истории не совпала.");
        assertEquals(subtask1, historyBefore.get(2), "Третья задача истории не совпала.");
        assertEquals(subtask2, historyBefore.get(3), "Четвёртая задача истории не совпала.");

        memoryTaskManager.deleteTaskPerId(task.getId());
        List<Task> historyBefore1 = memoryTaskManager.getHistory();
        assertEquals(historyBefore1.size(), 3, "Размер истории не верный.");
        assertFalse(historyBefore1.contains(task), "Задача не была удалена из истории.");
        assertEquals(epic, historyBefore1.get(0), "Первая задача истории не совпала.");
        assertEquals(subtask1, historyBefore1.get(1), "Вторая задача истории не совпала.");
        assertEquals(subtask2, historyBefore1.get(2), "Третья задача истории не совпала.");

        memoryTaskManager.deleteEpicPerId(epic.getId());
        List<Task> historyBefore2 = memoryTaskManager.getHistory();
        assertEquals(historyBefore2.size(), 1, "Размер истории не верный.");
        assertFalse(historyBefore2.contains(epic), "Эпик не был удален из истории.");
        assertFalse(historyBefore2.contains(subtask1), "Подзадача эпика не была удалена из истории.");
        assertEquals(subtask2, historyBefore2.get(0), "Первая задача истории не совпала.");


        memoryTaskManager.deleteSubtaskPerId(subtask2.getId());
        List<Task> historyBefore3 = memoryTaskManager.getHistory();
        assertTrue(historyBefore3.isEmpty(), "Подзадача не была удалена из истории.");
    }

    @Test
    void shouldNotBeInHistoryTasksAfterRemovingAll() {
        Task[] tasks = new Task[3];
        for (int i = 0; i < 3; i++){
            tasks[i] = createDefaultTask();
            memoryTaskManager.createTask(tasks[i]);
            memoryTaskManager.getTask(tasks[i].getId());
        }

        Epic epic = createDefaultEpic();
        epic = memoryTaskManager.createEpic(epic);
        memoryTaskManager.getEpic(epic.getId());

        List<Task> historyBefore = memoryTaskManager.getHistory();
        assertEquals(historyBefore.size(), 4, "Размер истории не верный.");
        assertEquals(tasks[0], historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(epic, historyBefore.get(3), "Четвёртая задача истории не совпала.");

        memoryTaskManager.deleteAllTasks();
        List<Task> historyAfter = memoryTaskManager.getHistory();
        assertEquals(historyAfter.size(), 1, "Размер истории не верный.");
        assertEquals(epic, historyAfter.get(0), "Первая задача истории не совпала.");
    }

    @Test
    void shouldNotBeInHistoryEpicsAfterRemovingAll() {
        Epic[] epics = new Epic[3];
        Subtask[] subtasks = new Subtask[3];
        for (int i = 0; i < 3; i++){
            epics[i] = createDefaultEpic();
            epics[i] = memoryTaskManager.createEpic(epics[i]);
            subtasks[i] = createDefaultSubtask(epics[i].getId());
            subtasks[i] = memoryTaskManager.createSubtask(subtasks[i]);
            memoryTaskManager.getEpic(epics[i].getId());
            memoryTaskManager.getSubtask(subtasks[i].getId());
        }

        Task task = createDefaultTask();
        task = memoryTaskManager.createTask(task);
        memoryTaskManager.getTask(task.getId());

        List<Task> historyBefore = memoryTaskManager.getHistory();
        assertEquals(historyBefore.size(), 7, "Размер истории не верный.");
        assertEquals(epics[0], historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(subtasks[0], historyBefore.get(1), "Вторая задача истории не совпала.");
        assertEquals(task, historyBefore.get(6), "Последняя задача истории не совпала.");

        memoryTaskManager.deleteAllEpics();

        List<Task> historyAfter = memoryTaskManager.getHistory();
        assertEquals(historyAfter.size(), 1, "Размер истории не верный.");
        assertEquals(task, historyAfter.get(0), "Первая задача истории не совпала.");
    }

    @Test
    void shouldNotBeInHistorySubtasksAfterRemovingAll() {
        Subtask[] subtasks = new Subtask[3];
        for (int i = 0; i < 3; i++){
            subtasks[i] = createDefaultSubtaskInEpic();
            memoryTaskManager.getSubtask(subtasks[i].getId());
        }

        Task task = createDefaultTask();
        task = memoryTaskManager.createTask(task);
        memoryTaskManager.getTask(task.getId());

        List<Task> historyBefore = memoryTaskManager.getHistory();
        assertEquals(historyBefore.size(), 4, "Размер истории не верный.");
        assertEquals(subtasks[0], historyBefore.get(0), "Первая задача истории не совпала.");
        assertEquals(task, historyBefore.get(3), "Четвёртая задача истории не совпала.");

        memoryTaskManager.deleteAllSubtasks();
        List<Task> historyAfter = memoryTaskManager.getHistory();
        assertEquals(historyAfter.size(), 1, "Размер истории не верный.");
        assertEquals(task, historyAfter.get(0), "Первая задача истории не совпала.");
    }

    @Test
    void shouldNotCreateEpicInEpic() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        subtask.setId(epic.getId());
        memoryTaskManager.createSubtask(subtask);

        assertNotEquals(epic.getId(), subtask.getId(), "Создан эпик в эпике.");
    }

    @Test
    void shouldNotCreateSubtaskInSubtask() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        memoryTaskManager.createSubtask(subtask);
        Subtask subtask1 = createDefaultSubtask(subtask.getId());
        Subtask subtask2 = memoryTaskManager.createSubtask(subtask1);
        assertNull(subtask2, "Создана подзадача в подзадаче.");
    }

    @Test
    void shouldBeEqualSubtaskIfSameId() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);

        Subtask subtask = createDefaultSubtask(epic.getId());
        memoryTaskManager.createSubtask(subtask);

        Subtask otherSubtask = new Subtask("otherSubtask", "description", TaskStatus.NEW, epic.getId(),
                                LocalDateTime.now(), Duration.ofMinutes(48));
        otherSubtask.setId(subtask.getId());

        assertEquals(subtask, otherSubtask, "Подзадачи не равны.");
    }

    @Test
    void shouldBeEqualEpicIfSameId() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);

        Epic otherEpic = new Epic("otherEpic", "description");
        otherEpic.setId(epic.getId());

        assertEquals(epic, otherEpic, "Эпики не равны.");
    }

    @Test
    void shouldBeEqualTaskIfSameId() {
        Task task = createDefaultTask();
        memoryTaskManager.createTask(task);

        Task otherTask = new Task("otherTask", "description", TaskStatus.NEW, LocalDateTime.now(),
                            Duration.ofMinutes(130));
        otherTask.setId(task.getId());

        assertEquals(task, otherTask, "Задачи не равны.");
    }

    @Test
    void historyIsIndependentOfTask() {
        Task task = createDefaultTask();
        memoryTaskManager.createTask(task);
        memoryTaskManager.getTask(task.getId());

        Task taskBefore = memoryTaskManager.getHistory().getFirst();
        Task taskAfter = memoryTaskManager.getTask(task.getId());

        taskAfter.setDescription("newDescription");
        taskAfter.setName("newName");
        taskAfter.setStatus(TaskStatus.IN_PROGRESS);

        assertTrue(memoryTaskManager.updateTask(taskAfter), "Задача не обновлена.");
        assertEquals(taskBefore.getId(), taskBefore.getId(), "Идентификаторы не совпали.");
        assertNotEquals(taskBefore.getName(), taskAfter.getName(), "Имя задач совпало.");
        assertNotEquals(taskBefore.getDescription(), taskAfter.getDescription(), "Описание задач совпало.");
        assertNotEquals(taskBefore.getStatus(), taskAfter.getStatus(), "Статус задач совпал.");
    }

    @Test
    void shouldReplaceGivenIdOnGenerated() {
        Task task = createDefaultTask();
        Epic epic = createDefaultEpic();
        Subtask subtask = createDefaultSubtaskInEpic();

        int id = 500;

        task.setId(id);
        epic.setId(id + 1);
        subtask.setId(id + 2);

        Task newTask = memoryTaskManager.createTask(task);
        Epic newEpic = memoryTaskManager.createEpic(epic);
        Subtask newSubtask = memoryTaskManager.createSubtask(subtask);

        assertNotEquals(id, newTask.getId(), "Идентификатор задачи не изменился.");
        assertNotEquals(id + 1, newEpic.getId(), "Идентификатор эпика не изменился.");
        assertNotEquals(id + 2, newSubtask.getId(), "Идентификатор подзадачи не изменился.");
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = createDefaultTask();
        memoryTaskManager.createTask(task);

        ArrayList<Task> tasks = memoryTaskManager.getAllTasks();
        assertTrue(tasks.contains(task), "Задача не добавилась.");

        memoryTaskManager.deleteTaskPerId(task.getId());
        tasks = memoryTaskManager.getAllTasks();

        assertFalse(tasks.contains(task), "Задача не удалилась.");
    }

    @Test
    void shouldDeleteEpicByIdAndHisSubtasks() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        memoryTaskManager.createSubtask(subtask);

        ArrayList<Epic> epics = memoryTaskManager.getAllEpics();
        ArrayList<Subtask> epicSubtasks = memoryTaskManager.getEpicSubtasks(epic.getId());

        assertTrue(epics.contains(epic), "Эпик не добавился.");
        assertTrue(epicSubtasks.contains(subtask), "Подзадача в эпик не добавилась.");

        memoryTaskManager.deleteEpicPerId(epic.getId());
        epics = memoryTaskManager.getAllEpics();
        epicSubtasks = memoryTaskManager.getEpicSubtasks(epic.getId());

        assertFalse(epics.contains(epic), "Эпик не удалился.");
        assertFalse(epicSubtasks.contains(subtask), "Подзадача в эпике не удалилась.");
    }

    @Test
    void shouldDeleteSubtaskByIdAndOutOfEpic() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        memoryTaskManager.createSubtask(subtask);

        ArrayList<Subtask> epicSubtasks = memoryTaskManager.getEpicSubtasks(epic.getId());

        assertTrue(epicSubtasks.contains(subtask), "Подзадача в эпик не добавилась.");

        memoryTaskManager.deleteSubtaskPerId(subtask.getId());
        epicSubtasks = memoryTaskManager.getEpicSubtasks(epic.getId());

        assertFalse(epicSubtasks.contains(subtask), "Подзадача в эпике не удалилась.");
    }

    @Test
    void shouldCalculateEpicsDurationOnBasisOfSubtasksDuration() {
        Epic epic = createDefaultEpic();
        memoryTaskManager.createEpic(epic);

        assertNull(epic.getStartTime());
        assertEquals(epic.getDuration(), Duration.ZERO);

        Subtask subtask =  new Subtask("name", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 01, 1, 13, 30), Duration.ofMinutes(30));
        memoryTaskManager.createSubtask(subtask);
        epic = memoryTaskManager.getEpic(epic.getId());

        assertEquals(epic.getStartTime(), subtask.getStartTime());
        assertEquals(epic.getDuration(), subtask.getDuration());
        assertEquals(epic.getEndTime(), subtask.getStartTime().plus(subtask.getDuration()));

        Subtask secondSubtask = new Subtask("name2", "description2", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 01, 2, 13, 30), Duration.ofMinutes(20));
        memoryTaskManager.createSubtask(secondSubtask);
        Subtask thirdSubtask = new Subtask("name3", "description3", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 01, 2, 13, 30), Duration.ofMinutes(1));
        memoryTaskManager.createSubtask(thirdSubtask);
        epic = memoryTaskManager.getEpic(epic.getId());

        assertEquals(epic.getStartTime(), subtask.getStartTime());
        assertEquals(epic.getDuration(), Duration.ofMinutes(51));
        assertEquals(epic.getEndTime(), secondSubtask.getStartTime().plus(secondSubtask.getDuration()));
    }

    Task createDefaultTask() {
        return new Task("task", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(55));
    }

    Epic createDefaultEpic() {
        return new Epic("epic", "description");
    }

    Subtask createDefaultSubtask(int epicId) {
        return new Subtask("subtask", "description", TaskStatus.NEW, epicId, LocalDateTime.now(),
                Duration.ofMinutes(120));
    }

    Subtask createDefaultSubtaskInEpic() {
        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);

        Subtask subtask = createDefaultSubtask(newEpic.getId());

        return memoryTaskManager.createSubtask(subtask);
    }

}
