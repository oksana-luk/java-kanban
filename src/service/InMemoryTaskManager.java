package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks;
    private Map<Integer, Epic> epics;
    private Map<Integer, Subtask> subtasks;
    private HistoryManager historyManager;
    private int counter;


    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counter = 0;
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task createTask(Task task) {
        Task newTask = new Task(task.getName(), task.getDescription(), task.getStatus());
        newTask.setId(++counter);
        tasks.put(newTask.getId(), newTask);
        task.setId(newTask.getId());
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = new Epic(epic.getName(), epic.getDescription());
        newEpic.setId(++counter);
        epics.put(newEpic.getId(), newEpic);
        epic.setId(newEpic.getId());
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            //System.out.println("Не найден эпик, подзадача не создана.");
            return null;
        }
        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId());
        newSubtask.setId(++counter);
        subtasks.put(newSubtask.getId(), newSubtask);

        epic.addSubtasksId(newSubtask.getId());
        updateEpicStatus(epic);
        subtask.setId(newSubtask.getId());
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);

        //зафиксировать просмотр
        if (task != null) {
            historyManager.addTask(task);

            task = new Task(task.getName(), task.getDescription(), task.getStatus());
            task.setId(id);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);

        //зафиксировать просмотр
        if (epic != null) {
            historyManager.addTask(epic);

            TaskStatus status = epic.getStatus();
            ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
            epic = new Epic(epic.getName(), epic.getDescription());
            epic.setId(id);
            epic.setStatus(status);
            epic.setSubtasksIds(subtasksIds);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        //зафиксировать просмотр
        if (subtask != null) {
            historyManager.addTask(subtask);

            subtask = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId());
            subtask.setId(id);
        }
        return subtask;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            //System.out.println("Нет задач!");
            return new ArrayList<>();
        }
        return new ArrayList<>(tasks.values());
     }


    @Override
    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()) {
            //System.out.println("Нет эпиков!");
            return new ArrayList<>();
        }
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        if (subtasks.isEmpty()) {
            //System.out.println("Нет подзадач!");
            return new ArrayList<>();
        }
        return new ArrayList<>(subtasks.values());
    }

    //сценарии для более простой демонстрации возможностей программы
    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> subtaskByEpic = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            //System.out.println("Не найден эпик.");
            return subtaskByEpic;
        }
       ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
            //System.out.println("У эпика нет подзадач!");
            return subtaskByEpic;
        }
        for (Integer subtaskId : subtasksIds) {
            subtaskByEpic.add(subtasks.get(subtaskId));
        }
        return subtaskByEpic;
    }

    @Override
    public void deleteAllTasks() {
        historyManager.removeTasks(getAllTasks());
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        historyManager.removeTasks(getAllEpics());
        historyManager.removeTasks(getAllSubtasks());
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        historyManager.removeTasks(getAllSubtasks());
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtaskId();
            updateEpicStatus(epic);
        }
    }

    @Override
    public boolean updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            //System.out.println("Задача не найдена.");
            return false;
        }

        tasks.put(task.getId(), task);
        return true;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            //System.out.println("Epic не найден.");
            return false;
        }
        Epic currentEpic = epics.get(epic.getId());
        currentEpic.setName(epic.getName());
        currentEpic.setDescription(epic.getDescription());

        return true;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            //System.out.println("Подзадача не найдена.");
            return false;
        }
        Subtask currentSubtask = subtasks.get(subtask.getId());
        if (currentSubtask.getEpicId() != subtask.getEpicId()) {
            //System.out.println("Перенос в другой Эпик невозможен.");
            return false;
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(currentSubtask.getEpicId());
        updateEpicStatus(epic);
        return true;
    }

    @Override
    public Task deleteTaskPerId(int id) {
        historyManager.remove(id);
        return tasks.remove(id);
    }

    @Override
    public Epic deleteEpicPerId(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
        }
        return epic;
    }

    @Override
    public Subtask deleteSubtaskPerId(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.deleteSubtaskId(id);

        updateEpicStatus(epic);
        historyManager.remove(id);
        return subtasks.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        if (epic == null) {
            return;
        }
        ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        int statusDone, statusNew, statusInProgress;
        statusDone = 0; statusNew = 0; statusInProgress = 0;
        for (Integer subtaskId : subtasksIds) {
            Subtask subtask = subtasks.get(subtaskId);
            switch (subtask.getStatus()) {
                case TaskStatus.NEW :
                    statusNew++;
                    break;
                case TaskStatus.DONE:
                    statusDone++;
                    break;
                case IN_PROGRESS:
                    statusInProgress++;
                    break;
            }
        }
        if (statusNew == 0 && statusInProgress == 0 && statusDone > 0) {
            epic.setStatus(TaskStatus.DONE);
        } else if (statusInProgress > 0 || statusDone > 0) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
           epic.setStatus(TaskStatus.NEW);
        }
    }
}
