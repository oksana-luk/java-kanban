package service;

import model.*;
import exception.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File data;

    public FileBackedTaskManager(File data) {
        this.data = data;
    }

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(data)) {
            writer.write("id,type,name,status,startTime,description,startTime,duration,epic\n");
            writeCollection(writer, tasks.values());
            writeCollection(writer, epics.values());
            writeCollection(writer, subtasks.values());
        } catch (IOException e) {
            throw new ManagerSaveException("Program experienced an error trying to save in file.");
        }
    }

    private String toString(Task task) {
        StringBuilder result = new StringBuilder();
        TaskType taskType = TaskType.valueOf(task.getClass().getSimpleName().toUpperCase());
        result.append(String.format("%d,%s,%s,%s,%s,%s,%d,", task.getId(), taskType, task.getName(), task.getStatus(),
                        task.getDescription(), formatDateTime(task.getStartTime()), task.getDuration().toMinutes()));
        if (task instanceof Subtask) {
            result.append(((Subtask) task).getEpicId());
        }
        result.append("\n");
        return result.toString();
    }

    private void writeCollection(Writer writer, Collection<? extends Task> tasks) throws ManagerSaveException {
        tasks.forEach(task -> {
            try {
                writer.write(toString(task));
            } catch (IOException e) {
                throw new ManagerSaveException("Program experienced an error trying to save in a file.");
            }
        });
    }

    public static FileBackedTaskManager loadFromFile(File file) throws ManagerBackupException {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        try {
            List<String> allLines = Files.readAllLines(file.toPath());
            Optional<Integer> idOptional = allLines
                    .stream()
                    .map(line -> fromString(taskManager, line))
                    .filter(Objects::nonNull)
                    .map(Task::getId)
                    .max(Integer::compareTo);

            taskManager.counter = idOptional.orElse(0);
            taskManager.getAllEpics().forEach(taskManager::updateEpicDuration);
        } catch (IOException e) {
            throw new ManagerBackupException("Program experienced an error trying to initialize from a file.");
        }
        return taskManager;
    }

    private static Task fromString(FileBackedTaskManager taskManager, String s) {
        String[] data = s.split(",", -1);
        if (data.length != 8) {
            return null;
        }

        int id = Integer.parseInt(data[0]);
        TaskType type = TaskType.valueOf(data[1]);
        String name = data[2];
        TaskStatus status = TaskStatus.valueOf(data[3]);
        String description = data[4];
        LocalDateTime startTime = (data[5].isEmpty()) ? null : LocalDateTime.parse(data[5], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        Duration duration = Duration.ofMinutes(Long.parseLong(data[6]));

        if (type == TaskType.TASK) {
            Task task = new Task(name, description, status, startTime, duration);
            task.setId(id);
            taskManager.tasks.put(task.getId(), task);
            return task;
        } else if (type == TaskType.EPIC) {
            Epic epic = new Epic(name, description);
            epic.setStatus(status);
            epic.setId(id);
            taskManager.epics.put(epic.getId(), epic);
            return epic;
        } else if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(data[7]);
            Subtask subtask = new Subtask(name, description, status, epicId, startTime, duration);
            subtask.setId(id);
            taskManager.subtasks.put(subtask.getId(), subtask);

            Epic epic = taskManager.epics.get(epicId);
            epic.addSubtasksId(subtask.getId());
            return subtask;
        }
        return null;
    }

    @Override
    public Task createTask(Task task)  {
        Task creadtedTask =  super.createTask(task);
        save();
        return creadtedTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public boolean updateTask(Task task) {
        boolean successfully = super.updateTask(task);
        if (successfully) {
            save();
        }
        return successfully;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean successfully = super.updateEpic(epic);
        if (successfully) {
            save();
        }
        return successfully;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean successfully = super.updateSubtask(subtask);
        if (successfully) {
            save();
        }
        return successfully;
    }

    @Override
    public Task deleteTaskPerId(int id) {
        Task deletedTask = super.deleteTaskPerId(id);
        save();
        return deletedTask;
    }

    @Override
    public Epic deleteEpicPerId(int id) {
        Epic detetedEpic = super.deleteEpicPerId(id);
        save();
        return detetedEpic;
    }

    @Override
    public Subtask deleteSubtaskPerId(int id) {
        Subtask deletedSubtask =  super.deleteSubtaskPerId(id);
        save();
        return deletedSubtask;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}


