package service;

import java.util.ArrayList;
import java.util.List;

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
        while (currentNode != null) {
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
