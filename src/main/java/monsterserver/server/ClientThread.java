package monsterserver.server;

import monsterserver.general.Controller;
import monsterserver.general.Response;
import monsterserver.general.Router;
import monsterserver.general.UserController;
import monsterserver.requests.ServerRequest;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable{
    private Socket clientSocket = null;
    private BufferedReader bufferedReader;

    ClientThread(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
    }


    @Override
    public void run() {

        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            ServerRequest serverRequest = new ServerRequest();
            serverRequest.parseFromBufferedReader(this.bufferedReader);

            if(serverRequest.getPath() == null){
                clientSocket.close();
                return;
            }

            Router router = new Router();
            router = RouterManager.registerRoutes(router);

            Controller controller = router.resolve(serverRequest.getPathParts().get(0));

            Response response = controller.handleRequest(serverRequest);


            outputStream.write(response.getResponse().getBytes());

            outputStream.flush();

        } catch (IOException e) {
            System.out.println("Error in server.ClientThread: " + e.getMessage());
        } finally {
            if (clientSocket != null) {
                try {
                    //close the client socket or perform any cleanup if needed
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
