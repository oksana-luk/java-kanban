package service;

import model.Task;
import model.Epic;
import model.Subtask;

import java.util.ArrayList;
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
            taskCopy = new Subtask(task.getName(), task.getDescription(), task.getStatus(), ((Subtask) task).getEpicId());
        } else if (task instanceof Epic) {
            taskCopy = new Epic(task.getName(), task.getDescription());
            taskCopy.setStatus(task.getStatus());
        } else {
            taskCopy = new Task(task.getName(), task.getDescription(), task.getStatus());
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

class TasksLinkedList<T> {
    public Node<T> head;
    public Node<T> tail;
    public int size = 0;

    public Node<T> linkLast(T data) {
        Node<T> currentNode = new Node<>(data);
        if (head == null) {
            head = currentNode;
        } else if (tail == null) {
            tail = currentNode;
            currentNode.prev = head;
            head.next = currentNode;
        } else {
            Node<T> lastNode = tail;
            tail = currentNode;
            lastNode.next = currentNode;
            currentNode.prev = lastNode;
        }
        size++;
        return currentNode;
    }

    public List<T> getHistory() {
        if (head == null) {
            return new ArrayList<>();
        }

        List<T> history = new ArrayList<>();
        Node<T> currentNode = head;
        while(currentNode != null) {
            history.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return history;
    }

    public void removeNode(Node<T> node) {
        Node<T> previousNode = node.prev;
        Node<T> nextNode = node.next;
        if (size == 1) {
            head = null;
            tail = null;
        } else if (size == 2) {
            if (node == head) {
               head = nextNode;
               tail = null;
               nextNode.prev = null;
            } else { //node == tail
               tail = null;
               previousNode.next = null;
            }
        } else {
            if (node == head) {
                head = nextNode;
                nextNode.prev = null;
            } else if (node == tail) {
                tail = previousNode;
                previousNode.next = null;
            } else {
                previousNode.next = nextNode;
                nextNode.prev = previousNode;
            }
        }
        size--;
    }
}

class Node<T> {
    public T data;
    public Node<T> next;
    public Node<T> prev;

    public Node(T data) {
        this.data = data;
    }
}

