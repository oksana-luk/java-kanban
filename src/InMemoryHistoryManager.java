import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final int HISTORY_CAPACITY = 10;
    //При создании менеджера заведите список для хранения просмотренных задач.
    private ArrayList<Task> history = new ArrayList<>();

    //возвращаемый список задач может содержать объект одного из трёх типов задач
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    //данные в полях менеджера будут обновляться при вызове методов-просмотров
    //выбрать тип, являющийся общим родителем обоих классов
    @Override
    public void addTask(Task task) {
        if (history.size() == HISTORY_CAPACITY) {
            history.removeFirst();
        }

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
        history.add(taskCopy);
    }
}
