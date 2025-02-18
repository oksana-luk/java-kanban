package service;

import model.Task;
import model.Epic;
import model.Subtask;

import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private HashMap<Integer, Node<Task>> tasksIDHashMap = new HashMap<>();
    private TasksLinkedList<Task> listNode = new TasksLinkedList<>();

    @Override
    public List<Task> getHistory() {
        return listNode.getHistory();
    }

    @Override
    public void addTask(Task task) {
        Task taskCopy;
        if (task instanceof Subtask) {
            taskCopy = new Subtask(task.getName(), task.getDescription(), task.getStatus(), ((Subtask) task).getEpicId(),
                        task.getStartTime(), task.getDuration());
        } else if (task instanceof Epic) {
            taskCopy = new Epic(task.getName(), task.getDescription());
            taskCopy.setStatus(task.getStatus());
        } else {
            taskCopy = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getStartTime(), task.getDuration());
        }
        taskCopy.setId(task.getId());
        remove(task.getId());
        Node<Task> node = listNode.linkLast(taskCopy);
        tasksIDHashMap.put(taskCopy.getId(), node);
    }

    @Override
    public void remove(int id) {
        Node<Task> node = tasksIDHashMap.get(id);
        if (node != null) {
            listNode.removeNode(node);
            tasksIDHashMap.remove(id);
        }
    }

    @Override
    public void removeTasks(List<? extends Task> tasks) {
        for (Task task : tasks) {
            remove(task.getId());
        }
    }
}

