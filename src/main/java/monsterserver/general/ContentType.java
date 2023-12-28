package monsterserver.general;

public enum ContentType {
    PLAIN_TEXT("text/plain"),
    HTML("text/html"),
    JSON("application/json");

    public final String type;

    ContentType(String s) {
        this.type = s;
    }
}
