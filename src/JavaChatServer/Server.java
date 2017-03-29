package JavaChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by david on 3/29/17.
 */
public class Server {

    private int port = 4444;


    public void start() {
        System.out.println("Starting server");

        try (
                // start listening on socker
                ServerSocket serverSocket = new ServerSocket(port);
                // get a socket connection
                Socket clientSocket = serverSocket.accept();
                // store output stream for the socket connection
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                // input stream for socket connection
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String input;

            while (true) {
                System.out.println("Waiting for input from client...");
                input = in.readLine();
                System.out.println("Recieved data from client: " + input);

                if (input.equals("quit")) {
                    out.println("Goodbye!\n");
                    break;
                }


                out.println("Echo: " + input);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
