import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    }


    @Test
    void shouldAddSubtaskInEpicAndChangeEpicsStatus() {
        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(newEpic.getId());

        Subtask newSubtask = memoryTaskManager.createSubtask(subtask);
        ArrayList<Integer> subtasksId = newEpic.getSubtasksIds();

        assertTrue(subtasksId.contains(newSubtask.getId()), "Подзадача не добавлена в эпик.");
        assertEquals(TaskStatus.NEW, newEpic.getStatus(), "Статус эпика определен некорректно.");

        subtask.setStatus(TaskStatus.IN_PROGRESS);
        memoryTaskManager.updateSubtask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, newEpic.getStatus(), "Статус эпика определен некорректно.");

        subtask.setStatus(TaskStatus.DONE);
        memoryTaskManager.updateSubtask(subtask);

        assertEquals(TaskStatus.DONE, newEpic.getStatus(), "Статус эпика определен некорректно.");

        Subtask subtask2 = createDefaultSubtask(newEpic.getId());
        memoryTaskManager.createSubtask(subtask2);

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
    void shouldReturnHistoryOfLastTenTasks() {
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
        assertEquals(task, history.get(1), "Вторая задача истории не совпала.");
        assertEquals(epic, history.get(2), "Третья задача истории не совпала.");
        assertEquals(epic, history.get(3), "Четвертая задача истории не совпала.");
        assertEquals(epic, history.get(5), "Шестая задача истории не совпала.");
        assertEquals(subtask, history.get(6), "Седьмая задача истории не совпала.");
        assertEquals(subtask, history.get(9), "Десятая задача истории не совпала.");
        assertEquals(10, history.size(), "В истории превышено количество элементов.");
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

        Subtask otherSubtask = new Subtask("otherSubtask", "description", TaskStatus.NEW, epic.getId());
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

        Task otherTask = new Task("otherTask", "description", TaskStatus.NEW);
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

    Task createDefaultTask() {
        return new Task("task", "description", TaskStatus.NEW);
    }

    Epic createDefaultEpic() {
        return new Epic("epic", "description");
    }

    Subtask createDefaultSubtask(int epicId) {
        return new Subtask("subtask", "description", TaskStatus.NEW, epicId);
    }

    Subtask createDefaultSubtaskInEpic() {
        Epic epic = createDefaultEpic();
        Epic newEpic = memoryTaskManager.createEpic(epic);

        Subtask subtask = createDefaultSubtask(newEpic.getId());

        return memoryTaskManager.createSubtask(subtask);
    }

}
