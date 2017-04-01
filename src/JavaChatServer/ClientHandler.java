package JavaChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A class which handles a single client
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private int id;

    private MessageQueue queue = new MessageQueue();

    ClientHandler(Socket socket, int n) {
        this.clientSocket = socket;
        id = n;

        System.out.println("Starting new server thread to handel client number " + id + ".");
    }

    @Override
    public void run() {

        // listen for input asynchronously

        new Thread(new Runnable() {
            @Override
            public void run() {

                String input;

                try (
                        // store output stream for the socket connection
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                        // input stream for socket connection
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ) {
                    while (true) {
                        System.out.println("Waiting for input from client " + id + "...");
                        input = in.readLine();

                        // scanner returns null if the socket it closed after we began readings
                        // treat this like any other IOException for now
                        if (input == null) throw new IOException();

                        System.out.println("Recieved data from client " + id + ": " + input);


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
        }).start();


    }


    // Receive message
    void receive(Message m) {
        // just put it in the queue to deal with later
        this.queue.broadcast(m);
    }
}
