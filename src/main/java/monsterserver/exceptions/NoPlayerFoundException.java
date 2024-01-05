package monsterserver.exceptions;

public class NoPlayerFoundException extends RuntimeException {
    public NoPlayerFoundException(String message) {
        super(message);
    }

    public NoPlayerFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPlayerFoundException(Throwable cause) { super(cause); }
}
