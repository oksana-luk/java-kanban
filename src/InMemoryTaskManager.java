import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;
    private HistoryManager historyManager;
    private int counter;


    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counter = 0;
        this.historyManager = historyManager;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(++counter);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++counter);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Не найден эпик, подзадача не создана.");
            return null;
        }
        subtask.setId(++counter);
        subtasks.put(subtask.getId(), subtask);

        epic.addSubtasksId(subtask.getId());
        updateEpicStatus(epic);
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);

        //зафиксировать просмотр
        if (task != null) {
            historyManager.addTask(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);

        //зафиксировать просмотр
        if (epic != null) {
            historyManager.addTask(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        //зафиксировать просмотр
        if (subtask != null) {
            historyManager.addTask(subtask);
        }
        return subtask;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()){
            System.out.println("Нет задач!");
            return new ArrayList<>();
        }
        return new ArrayList<>(tasks.values());
     }


    @Override
    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()){
            System.out.println("Нет эпиков!");
            return new ArrayList<>();
        }
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        if (subtasks.isEmpty()){
            System.out.println("Нет подзадач!");
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
            System.out.println("Не найден эпик.");
            return subtaskByEpic;
        }
       ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
            System.out.println("У эпика нет подзадач!");
            return subtaskByEpic;
        }
        for (Integer subtaskId : subtasksIds) {
            subtaskByEpic.add(subtasks.get(subtaskId));
        }
        return subtaskByEpic;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtaskId();
            updateEpicStatus(epic);
        }
    }

    @Override
    public boolean updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Задача не найдена.");
            return false;
        }

        tasks.put(task.getId(), task);
        return true;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Epic не найден.");
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
            System.out.println("Подзадача не найдена.");
            return false;
        }
        Subtask currentSubtask = subtasks.get(subtask.getId());
        if (currentSubtask.getEpicId() != subtask.getEpicId()) {
            System.out.println("Перенос в другой Эпик невозможен.");
            return false;
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(currentSubtask.getEpicId());
        updateEpicStatus(epic);
        return true;
    }

    @Override
    public Task deleteTaskPerId(int id) {
        return tasks.remove(id);
    }

    @Override
    public Epic deleteEpicPerId(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
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
        return subtasks.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
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
