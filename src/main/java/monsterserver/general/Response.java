package monsterserver.general;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Response {
    private int status;
    private String message;
    private String contentType;
    private String content;

    public Response(HttpStatus httpStatus, ContentType contentType, String content) {
        this.status = httpStatus.code;
        this.message = httpStatus.message;
        this.contentType = contentType.type;
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }

    public String getResponse() {
        return "HTTP/1.1 "+ this.status + " " + this.message + "\r\n" +
                "Content-Length: " + this.content.getBytes().length + "\r\n" +
                "\r\n" +
                this.content +
                "\r\n" + "\r\n";

        //return "HTTP/1.1 " + this.status + " " + this.message + "\r\n" +
        //        "Cache-Control: max-age=0\r\n" +
        //        "Connection: close\r\n" +
        //        "Date: " + localDatetime + "\r\n" +
        //        "Expires: " + localDatetime + "\r\n" +
        //        "Content-Type: " + this.contentType + "\r\n" +
        //        "Content-Length: " + this.content.length() + "\r\n" +
        //        "\r\n" +
        //        this.content;
    }
}
