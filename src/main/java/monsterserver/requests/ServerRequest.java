package monsterserver.requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerRequest {
    private String method;

    private String path;

    private String host;

    private String port;
    private String body;
    private List<String> pathParts;
    public static final String AUTHORIZATION_TOKEN_HEADER = "Authorization";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";

    public static final String HEADER_NAME_VALUE_SEPARATOR = ":";
    private Map<String, String> headers = new HashMap<>();


    public int getContentLength(){
        final String header = headers.get(CONTENT_LENGTH_HEADER);
        if(header == null){
            return 0;
        }
        return Integer.parseInt(header);
    }

    public ServerRequest(){

    }

    public void parseFromBufferedReader(BufferedReader br) throws IOException {
        String line = br.readLine();

        if(line != null){
            String[] splitFirstLine = line.split(" ");

            if(splitFirstLine[0] == null){
                return;
            }
            setMethod(splitFirstLine[0]);
            setPathname(splitFirstLine[1]);

            line = br.readLine();
            while(!line.isEmpty()){
                final String[] split = line.split(HEADER_NAME_VALUE_SEPARATOR, 2);
                headers.put(split[0], split[1].trim());
                line = br.readLine();
            }

            if(getContentLength() > 0){
                char[] charBuffer = new char[getContentLength()];
                //read the bufferd reader from 0 to the end of body and write it in the charBuffer
                br.read(charBuffer, 0, getContentLength());

                this.body = new String(charBuffer);

            }
        }

    }

    public String getAuthorizationTokenHeader() {
        final String header = headers.get(AUTHORIZATION_TOKEN_HEADER);
        return header;
    }
    public void setAuthorizationTokenHeader(String token) { headers.put(AUTHORIZATION_TOKEN_HEADER, token); }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    // todo refactor path parsing
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public void setPathname(String pathname) {
        this.path = pathname;
        String[] stringParts = pathname.split("/");
        this.pathParts = new ArrayList<>();
        for (String part :stringParts)
        {
            if (part != null &&
                    part.length() > 0)
            {
                this.pathParts.add(part);
            }
        }
    }

    public List<String> getPathParts() {
        return pathParts;
    }

    public void setPathParts(List<String> pathParts) {
        this.pathParts = pathParts;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
