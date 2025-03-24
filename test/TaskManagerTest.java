import exception.ManagerAddTaskException;
import exception.TaskNotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;

    public abstract TaskManager getTaskManager();

    @Test
    void shouldAddTaskSetId() {
        List<Task> tasksBefore = taskManager.getAllTasks();
        task = taskManager.createTask(createDefaultTask());
        List<Task> tasksAfter = taskManager.getAllTasks();

        assertTrue(tasksBefore.isEmpty(), "Изначальный список задач не пуст.");
        assertFalse(tasksAfter.isEmpty(), "Задача не добавилась в список задач.");
        assertEquals(task, tasksAfter.getFirst(), "Задачи не совпали.");
        assertNotEquals(0, task.getId(), "Идентификатор не был сгенерирован.");
    }

    @Test
    void shouldAddTaskWithoutChange() {
        task = createDefaultTask();
        Task newTask = taskManager.createTask(task);

        assertEquals(task.getName(), newTask.getName(), "Не совпали имена задач.");
        assertEquals(task.getDescription(), newTask.getDescription(), "Не совпали описания задач.");
        assertEquals(task.getStatus(), newTask.getStatus(), "Не совпали статусы задач.");
        assertEquals(task.getStartTime(), newTask.getStartTime(), "Не совпала дата начала задачи.");
        assertEquals(task.getDuration(), newTask.getDuration(), "Не совпада длительность задачи");
    }

    @Test
    void shouldAddEpicSetIdSetStatusNew() {
        List<Epic> epicBefore = taskManager.getAllEpics();
        epic = taskManager.createEpic(createDefaultEpic());
        List<Epic> epicsAfter = taskManager.getAllEpics();

        assertTrue(epicBefore.isEmpty(), "Изначальный список эпиков не пуст.");
        assertFalse(epicsAfter.isEmpty(), "Эпик не добавился в список задач.");
        assertEquals(epic, epicsAfter.getFirst(), "Эпики не совпали.");
        assertNotEquals(0, epic.getId(), "Идентификатор не был сгенерирован.");
        assertEquals(TaskStatus.NEW, epic.getStatus(),  "Не определился статус у эпика.");
    }

    @Test
    void shouldAddEpicWithoutChange() {
        epic = createDefaultEpic();
        Epic newEpic = taskManager.createEpic(epic);

        assertEquals(epic.getName(), newEpic.getName(), "Не совпали имена эпиков.");
        assertEquals(epic.getDescription(), newEpic.getDescription(), "Не совпали описания эпиков.");
        assertEquals(epic.getStatus(), newEpic.getStatus(), "Не совпали статусы эпиков.");
        assertNull(epic.getStartTime(), "Не совпала дата начала.");
        assertEquals(epic.getDuration(), newEpic.getDuration(), "Не совпада длительность.");
    }

    @Test
    void shouldAddSubtaskSetId() {
        List<Subtask> subtasksBefore = taskManager.getAllSubtasks();
        subtask = taskManager.createSubtask(createDefaultSubtaskInEpic());
        List<Subtask> subtasksAfter = taskManager.getAllSubtasks();

        assertTrue(subtasksBefore.isEmpty(), "Изначальный список подзадач не пуст.");
        assertFalse(subtasksAfter.isEmpty(), "Подзадача не добавилась в список задач.");
        assertEquals(subtask, subtasksAfter.getFirst(), "Подзадачи не совпали.");
        assertNotEquals(0, subtask.getId(),  "Идентификатор не был сгенерирован.");
    }

    @Test
    void shouldAddSubtaskWithoutChange() {
        subtask = createDefaultSubtaskInEpic();
        Subtask newSubtask = taskManager.createSubtask(subtask);

        assertEquals(subtask.getName(), newSubtask.getName(), "Не совпали имена подзадач.");
        assertEquals(subtask.getDescription(), newSubtask.getDescription(), "Не совпали описания подзадач.");
        assertEquals(subtask.getStatus(), newSubtask.getStatus(), "Не совпали статусы подзадач.");
        assertEquals(subtask.getStartTime(), newSubtask.getStartTime(), "Не совпала дата начала задачи.");
        assertEquals(subtask.getDuration(), newSubtask.getDuration(), "Не совпада длительность задачи");
    }

    @Test
    void shouldNotCreateEpicInEpic() {
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = createDefaultSubtask(epic.getId());
        subtask.setId(epic.getId());
        taskManager.createSubtask(subtask);

        assertNotEquals(epic.getId(), subtask.getId(), "Создан эпик в эпике.");
    }

    @Test
    void shouldNotCreateSubtaskInSubtask() {
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = createDefaultSubtask(epic.getId());
        subtask = taskManager.createSubtask(subtask);
        Subtask subtask2 = createDefaultSubtask(subtask.getId());
        assertThrows(ManagerAddTaskException.class, () -> taskManager.createSubtask(subtask2), "Создана подзадача в подзадаче.");
    }

    @Test
    void shouldBeEqualSubtaskIfSameId() {
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = taskManager.createSubtask(createDefaultSubtask(epic.getId()));

        Subtask otherSubtask = new Subtask("otherSubtask", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(48));
        otherSubtask.setId(subtask.getId());

        assertEquals(subtask, otherSubtask, "Подзадачи не равны.");
    }

    @Test
    void shouldBeEqualEpicIfSameId() {
        epic = taskManager.createEpic(createDefaultEpic());
        Epic otherEpic = new Epic("otherEpic", "description");
        otherEpic.setId(epic.getId());

        assertEquals(epic, otherEpic, "Эпики не равны.");
    }

    @Test
    void shouldBeEqualTaskIfSameId() {
        task = taskManager.createTask(createDefaultTask());
        Task otherTask = new Task("otherTask", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(130));
        otherTask.setId(task.getId());

        assertEquals(task, otherTask, "Задачи не равны.");
    }

    @Test
    void shouldReplaceGivenIdOnGenerated() {
        task = createDefaultTask();
        epic = createDefaultEpic();

        int id = 500;

        task.setId(id);
        epic.setId(id + 1);

        task = taskManager.createTask(task);
        epic = taskManager.createEpic(epic);

        subtask = createDefaultSubtask(epic.getId());
        subtask.setId(id + 2);
        subtask = taskManager.createSubtask(subtask);

        assertNotEquals(id, task.getId(), "Идентификатор задачи не изменился.");
        assertNotEquals(id + 1, epic.getId(), "Идентификатор эпика не изменился.");
        assertNotEquals(id + 2, subtask.getId(), "Идентификатор подзадачи не изменился.");
    }

    @Test
    void shouldReturnTaskById() {
        Optional<Task> optTask = taskManager.getTask(1);
        assertFalse(optTask.isPresent());

        task = taskManager.createTask(createDefaultTask());
        Optional<Task> returnedTaskOptional = taskManager.getTask(task.getId());

        assertTrue(returnedTaskOptional.isPresent(), "Не найдена задача по Id.");
        assertEquals(task, returnedTaskOptional.get(), "Задачи не совпали.");
    }

    @Test
    void shouldReturnEpicById() {
        Optional<Epic> optEpic = taskManager.getEpic(1);
        assertFalse(optEpic.isPresent());

        epic = taskManager.createEpic(createDefaultEpic());
        Optional<Epic> optReturnedEpic = taskManager.getEpic(epic.getId());

        assertTrue(optReturnedEpic.isPresent(), "Эпик не найден по Id.");
        assertEquals(epic, optReturnedEpic.get(), "Эпики не совпали.");
    }

    @Test
    void shouldReturnSubtaskById() {
        Optional<Subtask> optionalSubtask = taskManager.getSubtask(1);
        assertFalse(optionalSubtask.isPresent());

        subtask = taskManager.createSubtask(createDefaultSubtaskInEpic());
        Optional<Subtask> optReturnedSubtask = taskManager.getSubtask(subtask.getId());

        assertTrue(optReturnedSubtask.isPresent(), "Не найдена подзадача по Id.");
        assertEquals(subtask, optReturnedSubtask.get(), "Подзадачи не совпали.");
    }

    @Test
    void shouldReturnEpicsSubtasks() {
        epic = taskManager.createEpic(createDefaultEpic());
        int epicId = epic.getId();

        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);

        assertTrue(subtasks.isEmpty());

        subtask = taskManager.createSubtask(createDefaultSubtask(epicId));
        List<Subtask> subtasks2 = taskManager.getEpicSubtasks(epicId);

        assertFalse(subtasks2.isEmpty(), "Список подзадач эпика пуст.");
        assertEquals(subtask, subtasks2.getFirst(), "Подзадачи не совпали.");
    }

    @Test
    void shouldChangeNameDescriptionStatusOfTask() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        task = taskManager.createTask(createDefaultTask());

        task.setName(expectedName);
        task.setDescription(expectedDescription);
        task.setStatus(TaskStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> taskManager.updateTask(task), "Задачи не обновлена.");

        Optional<Task> updatedTaskOptional = taskManager.getTask(task.getId());
        assertTrue(updatedTaskOptional.isPresent(), "Не найдена задача по Id.");

        assertEquals(expectedName, updatedTaskOptional.get().getName(), "Имена задач не совпали.");
        assertEquals(expectedDescription, updatedTaskOptional.get().getDescription(), "Описания задач не совпали.");
        assertEquals(TaskStatus.IN_PROGRESS, updatedTaskOptional.get().getStatus(), "Статусы задач не совпали.");
    }

    @Test
    void shouldChangeNameDescriptionOfEpic() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        epic = taskManager.createEpic(createDefaultEpic());
        epic.setName(expectedName);
        epic.setDescription(expectedDescription);

        assertDoesNotThrow(() -> taskManager.updateEpic(epic), "Эпик не обновлен.");

        Optional<Epic> optUpdatedEpic = taskManager.getEpic(epic.getId());
        assertTrue(optUpdatedEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(expectedName, optUpdatedEpic.get().getName(), "Имена эпиков не совпали.");
        assertEquals(expectedDescription, optUpdatedEpic.get().getDescription(), "Описания эпиков не совпали.");
        assertEquals(TaskStatus.NEW, optUpdatedEpic.get().getStatus(), "Статусы эпиков не совпали.");
    }

    @Test
    void shouldChangeNameDescriptionStatusOfSubtask() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        subtask = taskManager.createSubtask(createDefaultSubtaskInEpic());
        subtask.setName(expectedName);
        subtask.setDescription(expectedDescription);
        subtask.setStatus(TaskStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> taskManager.updateSubtask(subtask), "Подзадача не обновлена.");

        Optional<Subtask> optUpdatedSubtask = taskManager.getSubtask(subtask.getId());
        assertTrue(optUpdatedSubtask.isPresent(), "Не найдена подзадача по Id.");

        assertEquals(expectedName, optUpdatedSubtask.get().getName(), "Имена подзадач не совпали.");
        assertEquals(expectedDescription, optUpdatedSubtask.get().getDescription(), "Описания подзадач не совпали.");
        assertEquals(TaskStatus.IN_PROGRESS, optUpdatedSubtask.get().getStatus(), "Статусы подзадач не совпали.");
    }

    @Test
    void shouldNotChangeNameDescriptionStatusOfTaskWithSetters() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        task = taskManager.createTask(createDefaultTask());
        task.setName(expectedName);
        task.setDescription(expectedDescription);
        task.setStatus(TaskStatus.IN_PROGRESS);

        Optional<Task> optTaskInTaskManager = taskManager.getTask(task.getId());
        assertTrue(optTaskInTaskManager.isPresent(), "Не найдена задача по Id.");

        assertNotEquals(expectedName, optTaskInTaskManager.get().getName(), "Поле имя доступно для изменения.");
        assertNotEquals(expectedDescription, optTaskInTaskManager.get().getDescription(), "Поле описание доступно для изменения.");
        assertNotEquals(TaskStatus.IN_PROGRESS, optTaskInTaskManager.get().getStatus(), "Поле статус доступно для изменения.");
    }

    @Test
    void shouldNotChangeNameDescriptionOfEpicWithSetters() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        epic = taskManager.createEpic(createDefaultEpic());
        epic.setName(expectedName);
        epic.setDescription(expectedDescription);

        Optional<Epic> optEpicInTaskManager = taskManager.getEpic(epic.getId());
        assertTrue(optEpicInTaskManager.isPresent(), "Эпик не найден по Id.");

        assertNotEquals(expectedName, optEpicInTaskManager.get().getName(), "Поле имя доступно для изменения.");
        assertNotEquals(expectedDescription, optEpicInTaskManager.get().getDescription(), "Поле описание доступно для изменения.");
        assertNotEquals(TaskStatus.IN_PROGRESS, optEpicInTaskManager.get().getStatus(), "Поле статус доступно для изменения.");
    }

    @Test
    void shouldNotChangeNameDescriptionStatusOfSubtaskWithSetters() {
        String expectedName = "newName";
        String expectedDescription = "newDescription";

        subtask = taskManager.createSubtask(createDefaultSubtaskInEpic());
        subtask.setName(expectedName);
        subtask.setDescription(expectedDescription);
        subtask.setStatus(TaskStatus.IN_PROGRESS);

        Optional<Subtask> optSubtaskInTaskManager = taskManager.getSubtask(subtask.getId());
        assertTrue(optSubtaskInTaskManager.isPresent(), "Эпик не найден по Id.");

        assertNotEquals(expectedName, optSubtaskInTaskManager.get().getName(), "Поле имя доступно для изменения.");
        assertNotEquals(expectedDescription, optSubtaskInTaskManager.get().getDescription(), "Поле описание доступно для изменения.");
        assertNotEquals(TaskStatus.IN_PROGRESS, optSubtaskInTaskManager.get().getStatus(), "Поле статус доступно для изменения.");
    }

    @Test
    void shouldDeleteTaskById() {
        task = taskManager.createTask(createDefaultTask());

        List<Task> tasks = taskManager.getAllTasks();
        assertTrue(tasks.contains(task), "Задача не добавилась.");

        taskManager.deleteTaskPerId(task.getId());
        tasks = taskManager.getAllTasks();

        assertFalse(tasks.contains(task), "Задача не удалилась.");
    }

    @Test
    void shouldDeleteEpicByIdAndHisSubtasks() {
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = taskManager.createSubtask(createDefaultSubtask(epic.getId()));

        List<Epic> epics = taskManager.getAllEpics();
        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertTrue(epics.contains(epic), "Эпик не добавился.");
        assertTrue(epicSubtasks.contains(subtask), "Подзадача в эпик не добавилась.");

        taskManager.deleteEpicPerId(epic.getId());
        epics = taskManager.getAllEpics();
        assertThrows(TaskNotFoundException.class, () -> taskManager.getEpicSubtasks(epic.getId()), "Эпик не удалился.");
        assertFalse(epics.contains(epic), "Эпик не удалился.");
    }

    @Test
    void shouldDeleteSubtaskByIdAndOutOfEpic() {
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = taskManager.createSubtask(createDefaultSubtask(epic.getId()));

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertTrue(epicSubtasks.contains(subtask), "Подзадача в эпик не добавилась.");

        taskManager.deleteSubtaskPerId(subtask.getId());
        epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertFalse(epicSubtasks.contains(subtask), "Подзадача в эпике не удалилась.");
    }

    @Test
    void shouldAddSubtaskInEpicAndChangeEpicsStatus() {
        epic = taskManager.createEpic(createDefaultEpic());
        Subtask subtask1 = taskManager.createSubtask(createDefaultSubtask(epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(createDefaultSubtask(epic.getId()));
        Optional<Epic> optEpic = taskManager.getEpic(epic.getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        ArrayList<Integer> subtasksId = optEpic.get().getSubtasksIds();

        assertTrue(subtasksId.contains(subtask1.getId()), "Подзадача не добавлена в эпик.");
        assertTrue(subtasksId.contains(subtask2.getId()), "Подзадача не добавлена в эпик.");
        assertEquals(subtask1.getStatus(), TaskStatus.NEW);
        assertEquals(subtask2.getStatus(), TaskStatus.NEW);
        assertEquals(optEpic.get().getStatus(), TaskStatus.NEW, "Статус эпика определен некорректно.");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        optEpic = taskManager.getEpic(optEpic.get().getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(subtask1.getStatus(), TaskStatus.IN_PROGRESS);
        assertEquals(subtask2.getStatus(), TaskStatus.NEW);
        assertEquals(optEpic.get().getStatus(), TaskStatus.IN_PROGRESS, "Статус эпика определен некорректно.");

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        optEpic = taskManager.getEpic(optEpic.get().getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(subtask1.getStatus(), TaskStatus.DONE);
        assertEquals(subtask2.getStatus(), TaskStatus.NEW);
        assertEquals(optEpic.get().getStatus(), TaskStatus.IN_PROGRESS, "Статус эпика определен некорректно.");

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        optEpic = taskManager.getEpic(optEpic.get().getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(subtask1.getStatus(), TaskStatus.DONE);
        assertEquals(subtask2.getStatus(), TaskStatus.DONE);
        assertEquals(optEpic.get().getStatus(), TaskStatus.DONE, "Статус эпика определен некорректно.");

        Subtask subtask3 = createDefaultSubtask(epic.getId());
        taskManager.createSubtask(subtask3);
        optEpic = taskManager.getEpic(optEpic.get().getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(subtask1.getStatus(), TaskStatus.DONE);
        assertEquals(subtask2.getStatus(), TaskStatus.DONE);
        assertEquals(subtask3.getStatus(), TaskStatus.NEW);
        assertEquals(optEpic.get().getStatus(), TaskStatus.IN_PROGRESS, "Статус эпика определен некорректно.");
    }

    @Test
    void shouldCalculateEpicsDurationOnBasisOfSubtasksDuration() {
        epic = taskManager.createEpic(createDefaultEpic());

        assertNull(epic.getStartTime());
        assertNull(epic.getDuration());

        Subtask subtask =  new Subtask("name", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 1, 13, 30), Duration.ofMinutes(30));
        subtask = taskManager.createSubtask(subtask);
        Optional<Epic> optEpic = taskManager.getEpic(epic.getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(optEpic.get().getStartTime(), subtask.getStartTime());
        assertEquals(optEpic.get().getDuration(), subtask.getDuration());
        assertEquals(optEpic.get().getEndTime(), subtask.getStartTime().plus(subtask.getDuration()));

        Subtask secondSubtask = new Subtask("name2", "description2", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 1, 11, 0), Duration.ofMinutes(20));
        taskManager.createSubtask(secondSubtask);
        optEpic = taskManager.getEpic(epic.getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(optEpic.get().getStartTime(), secondSubtask.getStartTime());
        assertEquals(optEpic.get().getDuration(), Duration.ofMinutes(50));
        assertEquals(optEpic.get().getEndTime(), subtask.getStartTime().plus(subtask.getDuration()));

        Subtask thirdSubtask = new Subtask("name3", "description3", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 2, 13, 30), Duration.ofMinutes(1));
        thirdSubtask = taskManager.createSubtask(thirdSubtask);
        optEpic = taskManager.getEpic(epic.getId());
        assertTrue(optEpic.isPresent(), "Эпик не найден по Id.");

        assertEquals(optEpic.get().getStartTime(), secondSubtask.getStartTime());
        assertEquals(optEpic.get().getDuration(), Duration.ofMinutes(51));
        assertEquals(optEpic.get().getEndTime(), thirdSubtask.getStartTime().plus(thirdSubtask.getDuration()));
    }

    @Test
    void shouldNotAddTaskSubtaskWithNotCorrectTime() {
        epic = createDefaultEpic();
        epic = taskManager.createEpic(epic);

        task = new Task("task", "description", TaskStatus.NEW, null, null);
        subtask = new Subtask("task", "description", TaskStatus.NEW, epic.getId(), null, null);

        assertNotNull(taskManager.createTask(task), "Не создана задача с пустой датой начала и без длительности.");
        assertNotNull(taskManager.createSubtask(subtask), "Не создана подзадача с пустой датой начала и без длительности.");

        Task task1 = new Task("task", "description", TaskStatus.NEW,
                LocalDateTime.of(2025, 1, 1, 18, 20), Duration.ofMinutes(50));
        task1 = taskManager.createTask(task1);

        task = new Task("task", "description", TaskStatus.NEW,
                LocalDateTime.of(2025, 1, 1, 18, 40), Duration.ofMinutes(1));
        assertThrows(ManagerAddTaskException.class, () -> taskManager.createTask(task), "Создана задача пересекающаяся по времени.");

        subtask = new Subtask("task", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 1, 18, 0), Duration.ofMinutes(25));
        assertThrows(ManagerAddTaskException.class, () -> taskManager.createSubtask(subtask), "Создана задача пересекающаяся по времени.");

        task = new Task("task", "description", TaskStatus.NEW,
                LocalDateTime.of(2025, 1, 1, 19, 10), Duration.ofMinutes(10));
        assertDoesNotThrow(() -> taskManager.createTask(task), "Не создана задача, начинающаяся во время окончания предыдущей задачи.");

        subtask = new Subtask("task", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 1, 1, 19, 20), Duration.ofMinutes(1));
        assertDoesNotThrow(() -> taskManager.createSubtask(subtask), "Не создана задача, начинающаяся во время окончания предыдущей задачи.");
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        epic = createDefaultEpic();
        epic = taskManager.createEpic(epic);

        List<Task> taskList = taskManager.getPrioritizedTasks();
        assertTrue(taskList.isEmpty());

        Task task1 = new Task("task", "description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(60));
        Task task2 = new Task("task", "description", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 18, 20), Duration.ofMinutes(50));
        Subtask subtask1 = new Subtask("task", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2024, 6, 8, 2, 9, 42), Duration.ofMinutes(1));
        Subtask subtask2 = new Subtask("task", "description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2024, 3, 5, 23, 18, 15), Duration.ofMinutes(10));

        task1 = taskManager.createTask(task1);
        task2 = taskManager.createTask(task2);
        subtask1 = taskManager.createSubtask(subtask1);
        subtask2 = taskManager.createSubtask(subtask2);

        taskList = taskManager.getPrioritizedTasks();

        assertEquals(taskList.getFirst(), task2);
        assertEquals(taskList.get(1), subtask2);
        assertEquals(taskList.get(2), subtask1);
        assertEquals(taskList.getLast(), task1);
    }

    Task createDefaultTask() {
        int minutes = new Random().nextInt(1000);
        return new Task("task", "description", TaskStatus.NEW, LocalDateTime.now().minusMinutes(minutes),
                Duration.ofNanos(1));
    }

    Epic createDefaultEpic() {
        return new Epic("epic", "description");
    }

    Subtask createDefaultSubtask(int epicId) {
        int minutes = new Random().nextInt(1000);
        return new Subtask("subtask", "description", TaskStatus.NEW, epicId, LocalDateTime.now().minusMinutes(minutes),
                Duration.ofNanos(1));
    }

    Subtask createDefaultSubtaskInEpic() {
        Epic epic = taskManager.createEpic(createDefaultEpic());
        return createDefaultSubtask(epic.getId());
    }
}
