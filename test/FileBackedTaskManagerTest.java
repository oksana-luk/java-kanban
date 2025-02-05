import static org.junit.jupiter.api.Assertions.*;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {

    @Test
    void shouldLoadFromEmptyFile() throws IOException {
        File data = File.createTempFile("tskmn", ".csv");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(data);

        assertTrue(fileBackedTaskManager.getAllTasks().isEmpty(), "Ошибка при загрузке из пустого файла.");
        assertTrue(fileBackedTaskManager.getAllEpics().isEmpty(), "Ошибка при загрузке из пустого файла.");
        assertTrue(fileBackedTaskManager.getAllSubtasks().isEmpty(), "Ошибка при загрузке из пустого файла.");
    }

    @Test
    void shouldSaveEmptyAndNotEmptyFile() throws IOException {
        File data = File.createTempFile("tskmn", ".csv");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(data);

        Task task = createDefaultTask();
        fileBackedTaskManager.createTask(task);
        Epic epic = createDefaultEpic();
        epic = fileBackedTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        subtask = fileBackedTaskManager.createSubtask(subtask);

        List<String> allLines = Files.readAllLines(data.toPath());
        assertEquals(allLines.size(), 4, "Не все задачи попали в файл.");

        assertFalse(allLines.get(0).isEmpty());
        assertFalse(allLines.get(1).isEmpty());
        assertFalse(allLines.get(2).isEmpty());
        assertFalse(allLines.get(3).isEmpty());

        fileBackedTaskManager.deleteTaskPerId(task.getId());
        fileBackedTaskManager.deleteSubtaskPerId(subtask.getId());
        fileBackedTaskManager.deleteEpicPerId(epic.getId());

        allLines = Files.readAllLines(data.toPath());
        assertEquals(allLines.size(), 1, "Файл не обновился после удаления задач");
        assertFalse(allLines.get(0).isEmpty());
    }

    @Test
    void shouldLoadFromNotEmptyFile() {
        File file = new File("data_test_shouldLoadFromNotEmptyFile.csv");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(fileBackedTaskManager.getAllTasks().size(), 3, "Не восстановлены задачи.");
        assertEquals(fileBackedTaskManager.getAllEpics().size(), 3, "Не востановлены эпики.");
        assertEquals(fileBackedTaskManager.getAllSubtasks().size(), 7, "Не восстановлены подзадачи.");
        assertEquals(fileBackedTaskManager.getEpic(4).getSubtasksIds().size(), 4, "В эпике не восстановлены подзадачи.");
    }

    @Test
    void shouldInitializeCounterOfIds() {
        File file = new File("data_shouldInitializeCounterOfIds.csv");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file);

        Task task = createDefaultTask();
        task = fileBackedTaskManager.createTask(task);
        assertNotEquals(task.getId(), 1, "Счетчик идентификаторов не восстановлен или восстановлен неверно.");
        assertEquals(task.getId(), 14, "Идентификатор задачи неверный.");

        fileBackedTaskManager.deleteTaskPerId(task.getId());
    }

    @Test
    void shouldUpdateInfoInFile() throws IOException {
        File data = File.createTempFile("tskmn", ".csv");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(data);

        Task task = createDefaultTask();
        fileBackedTaskManager.createTask(task);
        Epic epic = createDefaultEpic();
        epic = fileBackedTaskManager.createEpic(epic);
        Subtask subtask = createDefaultSubtask(epic.getId());
        subtask = fileBackedTaskManager.createSubtask(subtask);

        List<String> allLinesBefore = Files.readAllLines(data.toPath());
        assertEquals(allLinesBefore.size(), 4);

        task.setName("UpdatedTask");
        epic.setName("UpdatedEpic");
        subtask.setName("UpdatedSubtask");
        fileBackedTaskManager.updateTask(task);
        fileBackedTaskManager.updateEpic(epic);
        fileBackedTaskManager.updateSubtask(subtask);

        List<String> allLinesAfter = Files.readAllLines(data.toPath());
        assertEquals(allLinesAfter.size(), 4);
        assertNotEquals(allLinesBefore.get(1), allLinesAfter.get(1), "Информация о задаче не изменилась.");
        assertNotEquals(allLinesBefore.get(2), allLinesAfter.get(2), "Информация об эпике не изменилась.");
        assertNotEquals(allLinesBefore.get(3), allLinesAfter.get(3), "Информация о подзадаче не изменилась.");
    }
}
