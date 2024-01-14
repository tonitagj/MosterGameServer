package monsterserver.httpFunc;

public enum HttpStatus {
    OK(200, "OK"),
    CREATED(201, "CREATED"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    CONFLICT(409, "Conflict"),
    INTERNAL_SERVER_ERROR(500, "Internal server.Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented");
    public final int code;
    public final String message;

    HttpStatus(int code, String s) {
        this.code = code;
        this.message = s;
    }
}
