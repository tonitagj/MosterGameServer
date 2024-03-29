package monsterserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {

        int serverPort = 10001;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(serverPort);
            while (true){
                Socket socket = serverSocket.accept();

                ClientThread clientThread = new ClientThread(socket);

                new Thread(clientThread).start();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
