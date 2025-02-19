package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks;
    protected Map<Integer, Epic> epics;
    protected Map<Integer, Subtask> subtasks;
    private HistoryManager historyManager;
    protected int counter;
    protected Set<Task> prioritizedTasks = new TreeSet<>(new Comparator<Task>() {

        @Override
        public int compare(Task o1, Task o2) {
            int result = -1;

            if (o1.getStartTime() != null && o2.getStartTime() != null) {
                if (o1.getStartTime().isAfter(o2.getStartTime()))
                    result = 1;
                else if (o1.getStartTime().equals(o2.getStartTime()) && o1.equals(o2))
                    result = 0;
            }
            return result;
        }
    });

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counter = 0;
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task createTask(Task task) {
        if (!isTaskPeriodCorrect(task)) {
            return null;
        }
        Task newTask = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getStartTime(),
                        task.getDuration());
        newTask.setId(++counter);
        tasks.put(newTask.getId(), newTask);
        task.setId(newTask.getId());
        prioritizedTasks.add(task);
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
        if (!isTaskPeriodCorrect(subtask)) {
            return null;
        }
        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                                subtask.getEpicId(), subtask.getStartTime(), subtask.getDuration());
        newSubtask.setId(++counter);
        subtasks.put(newSubtask.getId(), newSubtask);

        epic.addSubtasksId(newSubtask.getId());
        updateEpicStatus(epic);
        updateEpicDuration(epic);
        subtask.setId(newSubtask.getId());
        prioritizedTasks.add(subtask);
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);

        //зафиксировать просмотр
        if (task != null) {
            historyManager.addTask(task);

            task = new Task(task);
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

            epic = new Epic(epic);
            epic.setId(id);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        //зафиксировать просмотр
        if (subtask != null) {
            historyManager.addTask(subtask);

            subtask = new Subtask(subtask);
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
    public static void printAllTasks(TaskManager manager) {
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
        prioritizedTasks.removeAll(getAllTasks());
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        historyManager.removeTasks(getAllEpics());
        historyManager.removeTasks(getAllSubtasks());
        prioritizedTasks.removeAll(getAllSubtasks());
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        historyManager.removeTasks(getAllSubtasks());
        prioritizedTasks.removeAll(getAllSubtasks());
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtaskId();
            updateEpicStatus(epic);
            updateEpicDuration(epic);
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
            for (Integer subtaskId : epic.getSubtasksIds()) {
                prioritizedTasks.remove(subtasks.get(subtaskId));
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

    protected void updateEpicDuration(Epic epic) {
        if (epic == null) {
            return;
        }
        if (epic.getSubtasksIds().isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
        }
        LocalDateTime maxEndTime = LocalDateTime.MIN;
        LocalDateTime minStartTime = LocalDateTime.MAX;
        Duration duration = Duration.ZERO;
        for (Integer subtaskId : epic.getSubtasksIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStartTime().isBefore(minStartTime)) {
                minStartTime = subtask.getStartTime();
            }
            LocalDateTime subtaskEndTime = subtask.getStartTime().plus(subtask.getDuration());
            if (subtaskEndTime.isAfter(maxEndTime)) {
                maxEndTime = subtaskEndTime;
            }
            duration = duration.plus(subtask.getDuration());
        }
        epic.setStartTime(minStartTime);
        epic.setEndTime(maxEndTime);
        epic.setDuration(duration);
    }

    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    protected static boolean isIntervalsOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime start2 = task2.getStartTime();
        if (start1 == null || start2 == null)
            return false;
        LocalDateTime end1 = task1.getStartTime().plus(task1.getDuration());
        LocalDateTime end2 = task2.getStartTime().plus(task2.getDuration());
        LocalDateTime start = (start1.isAfter(start2)) ? start1 : start2;
        LocalDateTime end = (end1.isBefore(end2)) ? end1 : end2;
        return end.isAfter(start);
    }

    protected boolean isTaskPeriodCorrect(Task incomTask) {
        List<Task> listTasksOverlap = prioritizedTasks
                .stream()
                .filter(task -> isIntervalsOverlap(incomTask, task))
                .toList();
        return listTasksOverlap.isEmpty();
    }
}
