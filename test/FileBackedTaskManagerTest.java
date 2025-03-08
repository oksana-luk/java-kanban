import static org.junit.jupiter.api.Assertions.*;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import service.FileBackedTaskManager;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    protected File data = null;

    @Override
    public FileBackedTaskManager getTaskManager() {
        initFile();
        return new FileBackedTaskManager(data);
    }

    @BeforeEach
    void beforeEach() {
        taskManager = getTaskManager();
    }

    private void initFile() {
        try{
            data = File.createTempFile("taskManager_", ".csv");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл.");
        }
    }

    @Test
    void shouldLoadFromEmptyFile() {
        taskManager = FileBackedTaskManager.loadFromFile(data);

        assertTrue(taskManager.getAllTasks().isEmpty(), "Ошибка при загрузке из пустого файла.");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Ошибка при загрузке из пустого файла.");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Ошибка при загрузке из пустого файла.");
    }

    @Test
    void shouldSaveEmptyAndNotEmptyFile() throws IOException {
        taskManager = FileBackedTaskManager.loadFromFile(data);

        task = taskManager.createTask(createDefaultTask());
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = taskManager.createSubtask(createDefaultSubtask(epic.getId()));

        List<String> allLines = Files.readAllLines(data.toPath());

        assertEquals(allLines.size(), 4, "Не все задачи попали в файл.");
        assertFalse(allLines.get(0).isEmpty());
        assertFalse(allLines.get(1).isEmpty());
        assertFalse(allLines.get(2).isEmpty());
        assertFalse(allLines.get(3).isEmpty());

        taskManager.deleteTaskPerId(task.getId());
        taskManager.deleteSubtaskPerId(subtask.getId());
        taskManager.deleteEpicPerId(epic.getId());
        allLines = Files.readAllLines(data.toPath());

        assertEquals(allLines.size(), 1, "Файл не обновился после удаления задач");
        assertFalse(allLines.getFirst().isEmpty());
    }

    @Test
    void shouldLoadFromNotEmptyFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(data));
        bw.write(new StringBuilder().append("id,type,name,status,startTime,description,startTime,duration,epic\n")
                                    .append("1,TASK,Запись к врачу,NEW,Записаться к другому терапевту,15.02.2025 12:00:00,20,\n")
                                    .append("3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,\n")
                                    .append("5,EPIC,Украсить дом к НГ,IN_PROGRESS,,22.02.2025 18:00:00,2765,\n")
                                    .append("11,SUBTASK,Купить елку,NEW,,22.02.2025 18:00:00,120,5\n")
                                    .append("13,SUBTASK,купить гирлянды,DONE,Заказать на вайлдберриз,24.02.2025 15:15:00,25,5\n")
                                    .toString());
        bw.close();

        taskManager = FileBackedTaskManager.loadFromFile(data);
        assertEquals(taskManager.getAllTasks().size(), 2, "Не восстановлены задачи.");
        assertEquals(taskManager.getAllEpics().size(), 1, "Не востановлены эпики.");
        assertEquals(taskManager.getAllSubtasks().size(), 2, "Не восстановлены подзадачи.");

        Optional<Task> taskOptional = taskManager.getTask(1);
        assertTrue(taskOptional.isPresent());
        task = taskOptional.get();
        assertInstanceOf(Task.class, task);
        assertEquals(task.getName(), "Запись к врачу");
        assertEquals(task.getStatus(), TaskStatus.NEW);
        assertEquals(task.getDescription(), "Записаться к другому терапевту");
        assertEquals(task.getStartTime(), LocalDateTime.of(2025, 2, 15, 12, 0));
        assertEquals(task.getDuration(), Duration.ofMinutes(20));

        Optional<Epic> epicOptional = taskManager.getEpic(5);
        assertTrue(epicOptional.isPresent(), "");
        epic = epicOptional.get();
        assertEquals(epic.getSubtasksIds().size(), 2, "В эпике не восстановлены подзадачи.");

        Optional<Subtask> subtaskOptional = taskManager.getSubtask(11);
        assertTrue(subtaskOptional.isPresent());
        subtask = subtaskOptional.get();
        assertEquals(subtask.getEpicId(), 5);
    }

    @Test
    void shouldInitializeCounterOfIds() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(data));
        bw.write(new StringBuilder().append("id,type,name,status,startTime,description,startTime,duration,epic\n")
                                    .append("1,TASK,Запись к врачу,NEW,Записаться к другому терапевту,15.02.2025 12:00:00,20,\n")
                                    .append("3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,\n")
                                    .append("5,EPIC,Украсить дом к НГ,IN_PROGRESS,,22.02.2025 18:00:00,2765,\n")
                                    .append("11,SUBTASK,Купить елку,NEW,,22.02.2025 18:00:00,120,5\n")
                                    .append("13,SUBTASK,купить гирлянды,DONE,Заказать на вайлдберриз,24.02.2025 15:15:00,25,5\n")
                                    .toString());
        bw.close();

        taskManager = FileBackedTaskManager.loadFromFile(data);
        task = taskManager.createTask(createDefaultTask());

        assertNotEquals(task.getId(), 1, "Счетчик идентификаторов не восстановлен или восстановлен неверно.");
        assertEquals(task.getId(), 14, "Идентификатор задачи неверный.");
    }

    @Test
    void shouldUpdateInfoInFile() throws IOException {
        task = taskManager.createTask(createDefaultTask());
        epic = taskManager.createEpic(createDefaultEpic());
        subtask = taskManager.createSubtask(createDefaultSubtask(epic.getId()));

        List<String> allLinesBefore = Files.readAllLines(data.toPath());
        assertEquals(allLinesBefore.size(), 4);

        task.setName("UpdatedTask");
        epic.setName("UpdatedEpic");
        subtask.setName("UpdatedSubtask");
        taskManager.updateTask(task);
        taskManager.updateEpic(epic);
        taskManager.updateSubtask(subtask);

        List<String> allLinesAfter = Files.readAllLines(data.toPath());
        assertEquals(allLinesAfter.size(), 4);
        assertNotEquals(allLinesBefore.get(1), allLinesAfter.get(1), "Информация о задаче не изменилась.");
        assertNotEquals(allLinesBefore.get(2), allLinesAfter.get(2), "Информация об эпике не изменилась.");
        assertNotEquals(allLinesBefore.get(3), allLinesAfter.get(3), "Информация о подзадаче не изменилась.");
    }

    @Test
    void shouldDeleteOutOfFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(data));
        bw.write(new StringBuilder().append("id,type,name,status,startTime,description,startTime,duration,epic\n")
                .append("1,TASK,Запись к врачу,NEW,Записаться к другому терапевту,15.02.2025 12:00:00,20,\n")
                .append("3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,\n")
                .append("5,EPIC,Украсить дом к НГ,IN_PROGRESS,,22.02.2025 18:00:00,2765,\n")
                .append("11,SUBTASK,Купить елку,NEW,,22.02.2025 18:00:00,120,5\n")
                .append("13,SUBTASK,купить гирлянды,DONE,Заказать на вайлдберриз,24.02.2025 15:15:00,25,5\n")
                .toString());
        bw.close();

        taskManager = FileBackedTaskManager.loadFromFile(data);

        List<String> allLinesBefore = Files.readAllLines(data.toPath());

        assertEquals(allLinesBefore.size(), 6, "Не все задачи попали в файл.");
        assertEquals(allLinesBefore.get(2), "3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,");

        task = taskManager.deleteTaskPerId(3);

        List<String> allLinesAfter = Files.readAllLines(data.toPath());

        assertEquals(allLinesAfter.size(), 5, "Не все задачи попали в файл.");
        assertNotEquals(allLinesAfter.get(2), "3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,");
    }
}
