package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtasksIds;
    protected LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW, null, null);
        subtasksIds = new ArrayList<>();
    }

    public Epic(Epic epic) {
        super(epic);
        this.subtasksIds = epic.getSubtasksIds();
        this.endTime = epic.endTime;
    }

    public ArrayList<Integer> getSubtasksIds() {
        return new ArrayList<>(subtasksIds);
    }

    @Override
    public String toString() {
        String result =  "model.Epic{" +
                "name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length=" + description.length();
        } else {
            result = result + ", description=null";
        }
        result = result + ", id=" + id +
                ", subtasksIds=" + subtasksIds.toString() +
                 ", status=" + status;
        if (startTime != null) {
            result = result + ", startTime=" + startTime;
        } else {
            result = result + ", startTime=null";
        }
        result = result + ", duration=" + Objects.toString(duration, "null") + '}';
        return result;
    }

    public void addSubtasksId(int subtasksId) {
        subtasksIds.add(subtasksId);
    }

    public void deleteSubtaskId(int subtaskId) {
        subtasksIds.remove(Integer.valueOf(subtaskId));
    }

    public void deleteAllSubtaskId() {
        subtasksIds.clear();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
