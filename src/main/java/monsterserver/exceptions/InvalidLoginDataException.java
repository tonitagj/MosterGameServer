package monsterserver.exceptions;

public class InvalidLoginDataException extends RuntimeException {
    public InvalidLoginDataException(String message) {
        super(message);
    }

    public InvalidLoginDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLoginDataException(Throwable cause) { super(cause); }
}
