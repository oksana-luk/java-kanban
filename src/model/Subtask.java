package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, TaskStatus status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicId = subtask.getEpicId();
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        String result =  "model.Subtask{" +
                "name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length=" + description.length();
        } else {
            result = result + ", description=null";
        }
        result = result + ", id=" + id +
                ", epicId=" + epicId  +
                ", status=" + status;
        if (startTime != null) {
            result = result + ", startTime=" + startTime;
        } else {
            result = result + ", startTime=null";
        }
        result = result + ", duration=" + Objects.toString(duration.toString(), "null") + '}';
        return result;
    }
}
