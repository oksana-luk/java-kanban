import static org.junit.jupiter.api.Assertions.*;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import service.FileBackedTaskManager;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
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
    void shouldLoadFromNotEmptyFile() throws IOException {
        //File file = new File("data_test_shouldLoadFromNotEmptyFile.csv");
        File file = File.createTempFile("tskmn", ".csv");


        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("id,type,name,status,startTime,description,startTime,duration,epic\n" +
                "1,TASK,Запись к врачу,NEW,Записаться к другому терапевту,15.02.2025 12:00:00,20,\n" +
                "3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,\n" +
                "5,EPIC,Украсить дом к НГ,IN_PROGRESS,,22.02.2025 18:00:00,2765,\n" +
                "11,SUBTASK,Купить елку,NEW,,22.02.2025 18:00:00,120,5\n" +
                "13,SUBTASK,купить гирлянды,DONE,Заказать на вайлдберриз,24.02.2025 15:15:00,25,5\n");
        bw.close();

        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(fileBackedTaskManager.getAllTasks().size(), 2, "Не восстановлены задачи.");
        assertEquals(fileBackedTaskManager.getAllEpics().size(), 1, "Не востановлены эпики.");
        assertEquals(fileBackedTaskManager.getAllSubtasks().size(), 2, "Не восстановлены подзадачи.");

        Task task = fileBackedTaskManager.getTask(1);
        assertTrue(task instanceof Task);
        assertEquals(task.getName(), "Запись к врачу");
        assertEquals(task.getStatus(), TaskStatus.NEW);
        assertEquals(task.getDescription(), "Записаться к другому терапевту");
        assertEquals(task.getStartTime(), LocalDateTime.of(2025, 02, 15, 12, 0));
        assertEquals(task.getDuration(), Duration.ofMinutes(20));

        Epic epic = fileBackedTaskManager.getEpic(5);
        assertEquals(epic.getSubtasksIds().size(), 2, "В эпике не восстановлены подзадачи.");

        Subtask subtask = fileBackedTaskManager.getSubtask(11);
        assertEquals(subtask.getEpicId(), 5);
    }

    @Test
    void shouldInitializeCounterOfIds() throws IOException {
        //File file = new File("data_test_shouldLoadFromNotEmptyFile.csv");
        File file = File.createTempFile("tskmn", ".csv");


        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("id,type,name,status,startTime,description,startTime,duration,epic\n" +
                "1,TASK,Запись к врачу,NEW,Записаться к другому терапевту,15.02.2025 12:00:00,20,\n" +
                "3,TASK,Водафон,DONE,Просмотреть новые тарифы,17.02.2025 12:15:00,25,\n" +
                "5,EPIC,Украсить дом к НГ,IN_PROGRESS,,22.02.2025 18:00:00,2765,\n" +
                "11,SUBTASK,Купить елку,NEW,,22.02.2025 18:00:00,120,5\n" +
                "13,SUBTASK,купить гирлянды,DONE,Заказать на вайлдберриз,24.02.2025 15:15:00,25,5\n");
        bw.close();

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
