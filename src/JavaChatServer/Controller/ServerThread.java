package JavaChatServer.Controller;

import JavaChatServer.Model.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main sever thread
 * Created by david on 3/29/17.
 */
public class ServerThread implements Runnable {

    private int port = 6667;

    private String serverName = "JavaChatServer";

    private Server server;

    public ServerThread(Server server) {
        this.server = server;
    }

    public void run() {
        System.out.println("Starting server");


        System.out.println("Creating message broadcaster thread");
        new Thread(server.getBroadcaster()).start();


        System.out.println("Listening for clients on port " + port);
        ServerSocket serverSocket = null;

        // Create a server socket to begin listening on the port
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // keep accepting new clients and spawn new threads to handle them

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
                System.out.println("New client (" + n + ") accepted.");
                ClientHandler handler = new ClientHandler(clientSocket, n, server);
                new Thread(handler).start();
                n++;
            }

        }

    }
}
