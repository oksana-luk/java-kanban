package service;

import exception.ManagerAddTaskException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks;
    protected Map<Integer, Epic> epics;
    protected Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;
    protected int counter;
    protected Set<Task> prioritizedTasks = new TreeSet<>((o1, o2) -> {
        int result = -1;
        if (!isTaskPeriodFilled(o1) || !isTaskPeriodFilled(o2) || o1.getStartTime().isAfter(o2.getStartTime()))
            result = 1;
        else if (o1.getStartTime().equals(o2.getStartTime()))
            result = 0;
        return result;
    });

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counter = 0;
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task createTask(Task task) throws ManagerAddTaskException {
        if (isTaskPeriodFilled(task) && !isTaskPeriodCorrect(task)) {
            throw new ManagerAddTaskException("Задача не создана, пересекается по периоду с другой задачей.");
        }
        Task newTask = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getStartTime(),
                task.getDuration());
        newTask.setId(++counter);
        tasks.put(newTask.getId(), newTask);
        task.setId(newTask.getId());
        if (isTaskPeriodFilled(task)) {
            prioritizedTasks.add(newTask);
        }
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
    public Subtask createSubtask(Subtask subtask) throws ManagerAddTaskException {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new ManagerAddTaskException("Подзадача не создана, потому что не найдена ее родительская задача.");
        }
        if (isTaskPeriodFilled(subtask) && !isTaskPeriodCorrect(subtask)) {
            throw new ManagerAddTaskException("Подзадача не создана, пересекается по периоду с другой задачей.");
        }
        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                subtask.getEpicId(), subtask.getStartTime(), subtask.getDuration());
        newSubtask.setId(++counter);
        subtasks.put(newSubtask.getId(), newSubtask);

        epic.addSubtasksId(newSubtask.getId());
        updateEpicStatus(epic);
        updateEpicDuration(epic);
        subtask.setId(newSubtask.getId());
        if (isTaskPeriodFilled(subtask)) {
            prioritizedTasks.add(newSubtask);
        }
        return subtask;
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);

        //зафиксировать просмотр
        if (tasks.get(id) != null) {
            historyManager.addTask(task);

            task = new Task(task);
            task.setId(id);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);

        //зафиксировать просмотр
        if (epic != null) {
            historyManager.addTask(epic);

            epic = new Epic(epic);
            epic.setId(id);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        //зафиксировать просмотр
        if (subtask != null) {
            historyManager.addTask(subtask);

            subtask = new Subtask(subtask);
            subtask.setId(id);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        if (subtasks.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(subtasks.values());
    }

    //сценарии для более простой демонстрации возможностей программы
    public static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getAllTasks().forEach(System.out::println);
        System.out.println("Эпики:");
        manager.getAllEpics().forEach(epic -> {
            System.out.println(epic);
            manager.getEpicSubtasks(epic.getId()).stream().map(task -> "--> " + task).forEach(System.out::println);
        });
        System.out.println("Подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        System.out.println("История:");
        manager.getHistory().forEach(System.out::println);
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> subtaskByEpic = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return subtaskByEpic;
        }
        ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
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
        getAllTasks().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        historyManager.removeTasks(getAllEpics());
        historyManager.removeTasks(getAllSubtasks());
        getAllSubtasks().forEach(prioritizedTasks::remove);
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        historyManager.removeTasks(getAllSubtasks());
        getAllSubtasks().forEach(prioritizedTasks::remove);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.deleteAllSubtaskId();
            updateEpicStatus(epic);
            updateEpicDuration(epic);
        });
    }

    @Override
    public boolean updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return false;
        }
        Task oldTask = tasks.get(task.getId());
        if (task.getStartTime() != oldTask.getStartTime() || task.getDuration() != oldTask.getDuration()
                && prioritizedTasks.contains(task)) {
            prioritizedTasks.remove(task);
            if (isTaskPeriodCorrect(task)) {
                prioritizedTasks.add(task);
            }
        }
        tasks.put(task.getId(), task);
        return true;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
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
            return false;
        }
        Subtask currentSubtask = subtasks.get(subtask.getId());
        if (currentSubtask.getEpicId() != subtask.getEpicId()) {
            return false;
        }
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (subtask.getStartTime() != oldSubtask.getStartTime() || subtask.getDuration() != oldSubtask.getDuration()
                && prioritizedTasks.contains(subtask)) {
            prioritizedTasks.remove(subtask);
            if (isTaskPeriodCorrect(subtask)) {
                prioritizedTasks.add(subtask);
            }
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(currentSubtask.getEpicId());
        updateEpicStatus(epic);
        updateEpicDuration(epic);
        return true;
    }

    @Override
    public Task deleteTaskPerId(int id) {
        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
        return tasks.remove(id);
    }

    @Override
    public Epic deleteEpicPerId(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            epic.getSubtasksIds().forEach(subtaskId -> {
                prioritizedTasks.remove(subtasks.get(subtaskId));
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
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
        updateEpicDuration(epic);
        historyManager.remove(id);
        prioritizedTasks.remove(subtask);
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
        List<TaskStatus> statuses = subtasksIds
                .stream()
                .map(id -> subtasks.get(id))
                .map(Task::getStatus)
                .toList();
        boolean includeNewTask = statuses.stream().anyMatch(status -> status == TaskStatus.NEW);
        boolean includeInProgressTask = statuses.stream().anyMatch(status -> status == TaskStatus.IN_PROGRESS);
        boolean includeDoneTask = statuses.stream().anyMatch(status -> status == TaskStatus.DONE);

        if (!includeNewTask && !includeInProgressTask && includeDoneTask) {
            epic.setStatus(TaskStatus.DONE);
        } else if (includeInProgressTask || includeDoneTask) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }

    protected void updateEpicDuration(Epic epic) {
        if (epic == null) {
            return;
        }
        if (epic.getSubtasksIds().isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(null);
        }
        List<Subtask> epicSubtasks = epic.getSubtasksIds()
                .stream()
                .map(id -> subtasks.get(id))
                .toList();
        LocalDateTime startTime = epicSubtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime endTime = epicSubtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        Duration duration = epicSubtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration((duration == Duration.ZERO) ? null : duration);
    }

    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    protected static boolean isIntervalsOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime end2 = task2.getEndTime();
        LocalDateTime start = (start1.isAfter(start2)) ? start1 : start2;
        LocalDateTime end = (end1.isBefore(end2)) ? end1 : end2;
        return end.isAfter(start);
    }

    protected boolean isTaskPeriodCorrect(Task incomingTask) {
        return !getPrioritizedTasks()
                .stream()
                .filter(task -> isTaskPeriodFilled(task))
                .anyMatch(task -> isIntervalsOverlap(incomingTask, task));
    }

    protected boolean isTaskPeriodFilled(Task incomimgTask) {
        return incomimgTask.getStartTime() != null && incomimgTask.getDuration() != null;
    }
}
