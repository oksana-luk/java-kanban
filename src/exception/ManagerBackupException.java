package exception;

public class ManagerBackupException extends  RuntimeException {
    public ManagerBackupException() {
    }

    public ManagerBackupException(String message) {
        super(message);
    }
}
