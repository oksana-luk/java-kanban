package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    Optional<Task> getTask(int id);

    Optional<Epic> getEpic(int id);

    Optional<Subtask> getSubtask(int id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    List<Subtask> getEpicSubtasks(int epicId);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    Task deleteTaskPerId(int id);

    Epic deleteEpicPerId(int id);

    Subtask deleteSubtaskPerId(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
