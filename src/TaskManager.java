import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;
    private int counter;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counter = 0;
    }

    public Task createTask(Task task) {
        task.setId(++counter);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(++counter);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Не найден эпик, подзадача не создана.");
            return subtask;
        }
        subtask.setId(++counter);
        subtasks.put(subtask.getId(), subtask);

        epic.addSubtasksId(subtask.getId());
        updateEpicStatus(epic);
        return subtask;
    }

    public Task findTaskPerId(int id) {
        return tasks.get(id);
    }

    public Epic findEpicPerId(int id) {
        return epics.get(id);
    }

    public Subtask findSubtaskPerId(int id) {
        return subtasks.get(id);
    }

    public ArrayList<Task> printAllTasks() {
        if (tasks.isEmpty()){
            System.out.println("Нет задач!");
            return new ArrayList<>();
        }
        return new ArrayList<>(tasks.values());
     }


    public ArrayList<Epic> printAllEpics() {
        if (epics.isEmpty()){
            System.out.println("Нет эпиков!");
            return new ArrayList<>();
        }
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> printAllSubtasks() {
        if (subtasks.isEmpty()){
            System.out.println("Нет подзадач!");
            return new ArrayList<>();
        }
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getSubtaskByEpic (int epicId) {
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

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtaskId();
            updateEpicStatus(epic);
        }
    }

    public boolean updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Задача не найдена.");
            return false;
        }
        Task currentTask = tasks.get(task.getId());
        currentTask.setName(task.getName());
        currentTask.setDescription(task.getDescription());
        currentTask.setStatus(task.getStatus());
        return true;
    }

    public boolean updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Epic не найден.");
            return false;
        }
        Epic currentEpic = epics.get(epic.getId());
        currentEpic.setName(epic.getName());
        currentEpic.setDescription(epic.getDescription());

        updateEpicStatus(epic);
        return true;
    }

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
        currentSubtask.setName(subtask.getName());
        currentSubtask.setDescription(subtask.getDescription());
        currentSubtask.setStatus(subtask.getStatus());

        Epic epic = epics.get(currentSubtask.getEpicId());
        updateEpicStatus(epic);
        return true;
    }

    public Task deleteTaskPerId(int id) {
        return tasks.remove(id);
    }

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

    public Subtask deleteSubtaskPerId(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return subtask;
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.deleteSubtaskId(id);

        updateEpicStatus(epic);
        return subtasks.remove(id);
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
