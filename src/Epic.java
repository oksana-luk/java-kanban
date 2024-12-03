import java.util.ArrayList;

public class Epic extends Task{
    private ArrayList<Integer> subtasksIds;

    public Epic(String name, String description) {
        super(name, description);
        subtasksIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    @Override
    public String toString() {
        String result =  "Epic{" +
                "name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length=" + description.length();
        } else {
            result = result + ", description=null";
        }
        result = result + ", id=" + id +
                ", subtasksIds=" + subtasksIds.toString() +
                 ", status=" + status +
                '}';
        return result;
    }

    public void addSubtasksId(int subtasksId) {
        subtasksIds.add(subtasksId);
    }

    public void deleteSubtaskId(int subtaskId) {
        Integer id = (Integer) subtaskId;
        subtasksIds.remove(id);
    }

    public void deleteAllSubtaskId() {
        subtasksIds.clear();
    }

}
