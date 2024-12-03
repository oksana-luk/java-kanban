import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;
    private static int counter;

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

    public Subtask createSubtask(Subtask subtask, int epicId) {
        subtask.setId(++counter);
        subtask.setEpicId(epicId);

        Epic epic = epics.get(epicId);
        epic.addSubtasksId(counter);

        subtasks.put(subtask.getId(), subtask);

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

    public void printAllTasks() {
        if (tasks.isEmpty()){
            System.out.println("Нет задач!");
            return;
        }
        for (Integer id : tasks.keySet()) {
            System.out.println(tasks.get(id));
        }
     }

    public void printAllEpics() {
        if (epics.isEmpty()){
            System.out.println("Нет эпиков!");
            return;
        }
        for (Integer id : epics.keySet()) {
            System.out.println(epics.get(id));
        }
    }

    public void printAllSubtasks() {
        if (subtasks.isEmpty()){
            System.out.println("Нет подзадач!");
            return;
        }
        for (Integer id : subtasks.keySet()) {
            System.out.println(subtasks.get(id));
        }
    }

    public void printEpicsSubtasks(Epic epic) {
        ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
            System.out.println("У эпика нет подзадач!");
        }
        for (Integer subtaskId : subtasksIds) {
            Subtask subtask = subtasks.get(subtaskId);
            System.out.println(subtask);
        }
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
        for (Integer id : epics.keySet()) {
            Epic epic = epics.get(id);
            epic.deleteAllSubtaskId();
            updateEpicStatus(epic);
        }
    }

    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        updateEpicStatus(epic);
        return subtask;
    }

    public Task deleteTaskPerId(int id) {
        Task task = tasks.get(id);
        tasks.remove(id);
        return task;
    }

    public Epic deleteEpicPerId(int id) {
        Epic epic = epics.get(id);
        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
        return epic;
    }

    public Subtask deleteSubtaskPerId(int id) {
        Subtask subtask = subtasks.get(id);

        Epic epic = epics.get(subtask.getEpicId());
        epic.deleteSubtaskId(id);

        updateEpicStatus(epic);
        return subtask;
    }

    public ArrayList<Integer> getEpicsSubtasks(Epic epic) {
        return epic.getSubtasksIds();
    }

    private void updateEpicStatus(Epic epic) {
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
