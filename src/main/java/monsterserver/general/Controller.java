package monsterserver.general;

import monsterserver.requests.ServerRequest;

public interface Controller {
    public Response handleRequest(ServerRequest serverRequest);


}
