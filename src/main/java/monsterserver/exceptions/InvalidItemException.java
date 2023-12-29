package monsterserver.exceptions;

public class InvalidItemException extends RuntimeException{
    public InvalidItemException(String message) {
        super(message);
    }

    public InvalidItemException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidItemException(Throwable cause) { super(cause); }
}
