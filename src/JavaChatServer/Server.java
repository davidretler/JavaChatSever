package JavaChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by david on 3/29/17.
 */
public class Server implements Runnable {

    private int port = 4444;


    public void run() {
        System.out.println("Starting server");

        ServerSocket serverSocket = null;

        // Create a server socket to begin listening on the port
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // keep accepting new clients and spawn new threads to handle them
        System.out.println("Listening for clients on port " + port);

        int n = 0; // keep track of number of clients
        while (true) {

            Socket clientSocket = null;

            // get a socket connection
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Exception occured while trying to accept a new client");
            }

            if (clientSocket != null) {
                new Thread(new ClientHandler(clientSocket, n)).start();
                n++;
            }

        }

    }
}
