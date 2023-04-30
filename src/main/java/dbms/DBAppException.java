package dbms;

public class DBAppException extends Exception {
    public DBAppException(String message) {
        super(message);
    }

    public DBAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
