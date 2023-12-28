package monsterserver.exceptions;

public class AccessRightsTooLowException extends RuntimeException {

    public AccessRightsTooLowException(String message) {
        super(message);
    }

    public AccessRightsTooLowException(String message, Throwable cause) {
        super(message, cause);
    }
    public AccessRightsTooLowException(Throwable cause) {
        super(cause);
    }

}
