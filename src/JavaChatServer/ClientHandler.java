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

    private int timeout = 100;

    private MessageQueue queue = new MessageQueue();

    private Server server;

    //private String nick = null;
    private User user = null;

    private PrintWriter out;
    private BufferedReader in;

    ClientHandler(Socket socket, int n, Server s) {
        this.clientSocket = socket;
        id = n;
        server = s;
        System.out.println("Starting new server thread to handel client number " + id + ".");

        try {
            // store output stream for the socket connection
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            // input stream for socket connection
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            out = null;
            in = null;
        }
    }

    @Override
    public void run() {

        // make sure we dont run if the constructor failed to get input and output streams for the socket
        if (out == null || in == null) return;

        // initialize the connection by getting NICK and USER information
        try {

            // initiate by waiting for the user to indicate a nickname
            String nick = null;

            out.println("Welcome to the chat server. Please choose a nickname by typing \"NICK <your nickname>\"");

            while (nick == null) {

                System.out.println("Waiting for client " + id + " to indicate nickname");

                String input = in.readLine();

                // TODO this yeilds a null pointer exception if the client closes the connection after readLine()
                // was called above. Fix this (Really it's fine... the clinet handler will crash but the connection was
                // closed already and this doesn't affect any other threads)
                if (input.toLowerCase().startsWith("nick")) {
                    String[] split = input.split(" ");

                    if (split.length == 2) {
                        String nicktemp = input.split(" ")[1];

                        if (NickRegistrar.getInstance().addNick(nicktemp)) {
                            nick = nicktemp;
                        } else {
                            out.println("The nickname " + nicktemp + " is already in use.");
                        }

                    } else if (split.length > 2) {
                        out.println("A nickname cannot have any spaces.");
                    } else {
                        out.println("A nickname cannot be empty.");
                    }
                }
            }

            // get the rest of the user information

            while (user == null) {

                System.out.println("Waiting for client " + id + " to indicate user information");

                String input = in.readLine();

                if (input.toLowerCase().startsWith("user")) {
                    String[] split = input.split(" ");

                    if (split.length == 5) {
                        // USER username hostname servername :realname
                        String userName = split[1];
                        String hostName = split[2];
                        String serverName = split[3];
                        String realName = split[4].substring(1);    // remove : before the real name

                        user = new User(nick, userName, hostName, serverName, realName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize client");
            return;
        }


        // add this client to the broadcaster once we have initiaized
        server.addHandlder(this);

        // keep listening for input asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        System.out.println("Waiting for input from client " + id + "...");
                        String input = in.readLine();

                        // scanner returns null if the socket it closed after we began readings
                        // treat this like any other IOException for now
                        if (input == null) throw new IOException();

                        System.out.println("Recieved data from client " + id + ": " + input);

                        String command = input.split(" ")[0];

                        if (command.equalsIgnoreCase("quit")) {
                            out.println("Goodbye!\n");
                            close();
                            break;
                        } else if (command.equalsIgnoreCase("privmsg")) {

                            if (input.split(" ").length >= 3) {
                                String recipient = input.split(" ")[1];
                                String message = input.substring(input.indexOf(recipient) + recipient.length());

                                server.broadcast(new ClientMessage(message, id, user, recipient));
                            }
                        } else if (command.equalsIgnoreCase("join")) {

                            if (input.split(" ").length == 2) {
                                String channel = input.split(" ")[1];
                                if (channel.startsWith(("#"))) {

                                    server.joinChannel(ClientHandler.this, channel);
                                }
                            }

                        } else {
                            // echo data back to client
                            //out.println("Echo: " + input);

                            // broadcast message
                            System.out.println("Broadcasting message from client " + id);
                            Message m = new ClientMessage(input, id, user, null);
                            server.broadcast(m);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // relay broadcasted message asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    Message message = queue.consume();

                    if (message != null) {
                        // a message was broadcast, print it

                        String m = message.getPrefix() + " " + message.getMessageText();

                        out.println(m);

                    } else {
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }

    // Receive message
    public void receive(Message m) {
        // just put it in the queue to deal with later
        this.queue.broadcast(m);
    }

    public int getClientID() {
        return id;
    }

    public String getNick() {
        return user.getNick();
    }

    public boolean closed() {
        return clientSocket.isClosed() || out.checkError();
    }

    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NickRegistrar.getInstance().removeNick(user.getNick());
        server.removeHandler(this);
    }

    public User getUser() {
        return user;
    }

}
