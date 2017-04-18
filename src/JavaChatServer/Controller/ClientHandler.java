package JavaChatServer.Controller;

import JavaChatServer.Model.*;

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
    User user = null;

    private PrintWriter out;
    private BufferedReader in;

    StoppableThread listenThread, broadcastThread;

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

        String nick = null;

        // initialize the connection by getting NICK and USER information
        try {

            out.println("Welcome to the chat server. Please choose a nickname by typing \"NICK <your nickname>\"");

            while (nick == null) {

                System.out.println("Waiting for client " + id + " to indicate nickname");

                String input = in.readLine();

                if (input == null) throw new IOException("Client disconnected before choosing nick");

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
                if (in == null) throw new IOException("Client disconnected before providing user information");

                if (input.toLowerCase().startsWith("user")) {
                    String[] split = input.split(" ");

                    if (split.length == 5) {

                        System.out.println("Response from " + id + " " + input);

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
            if (user == null) {
                NickRegistrar.getInstance().removeNick(nick);
            }
            ClientHandler.this.disconnect("Failed to initialize.");
            return;
        }

        server.welcome(this);

        // add this client to the broadcaster once we have initiaized
        server.addHandlder(this);


        // keep listening for input asynchronously
        listenThread = new StoppableThread() {

            @Override
            public void run() {

                try {
                    while (!finished) {
                        System.out.println("Waiting for input from client " + id + "...");
                        String input = in.readLine();

                        // scanner returns null if the socket it closed after we began readings
                        // treat this like any other IOException for now
                        if (input == null) throw new IOException();

                        System.out.println("Recieved data from client " + id + ": " + input);


                        // TODO: refactor this so command is parsed in its own class
                        // probably should make a command class and have all specific commands be subclass of the command class
                        String command = input.split(" ")[0];

                        if (command.equalsIgnoreCase("quit")) {

                            String reason = null;
                            if (input.split(" ").length >= 2) {
                                reason = input.split(" ")[1];
                                if (reason.charAt(0) == ':') {
                                    reason = input.substring(input.indexOf(":") + 1);
                                } else {
                                    reason = null;
                                    System.err.println("Reason formatted incorrectly in message \"" + input + "\"");
                                }
                            }
                            disconnect(reason != null ? reason : "No Reason.");
                            break;
                        } else if (command.equalsIgnoreCase("privmsg")) {

                            if (input.split(" ").length >= 3) {

                                String[] split = input.split(" ");

                                String recipient = split[1];

                                if (split[2].startsWith(":")) {
                                    String message = input.substring(input.indexOf(":") + 1);
                                    try {
                                        server.privateMessage(ClientHandler.this, recipient, message);
                                    } catch (IRCCommandException e) {
                                        ClientHandler.this.receive(new ServerMessage("404 " + getNick() + " " + recipient + " :" + e.getMessage(), server));
                                    }
                                }
                            }
                        } else if (command.equalsIgnoreCase("join")) {

                            if (input.split(" ").length == 2) {
                                String channel = input.split(" ")[1];
                                if (channel.startsWith(("#"))) {
                                    server.joinChannel(ClientHandler.this, channel);
                                }
                            }
                        } else if (command.equalsIgnoreCase("who")) {

                            if (input.split(" ").length == 2) {
                                String channel = input.split(" ")[1];
                                if (channel.startsWith("#")) {
                                    server.displayUsers(ClientHandler.this, channel);
                                }
                            }
                        } else if (command.equalsIgnoreCase("part")) {
                            String[] split = input.split(" ");
                            String reason = null;
                            String channel = null;
                            if (split.length > 1) {
                                channel = split[1];
                                if (split.length > 2) {
                                    reason = input.substring(input.indexOf(":") + 1);
                                }
                                server.part(ClientHandler.this, channel, reason);
                            } else {
                                System.err.println("Error, needs to specify channel to part from in message \"" + input + "\"");
                            }
                        } else {
                            // echo data back to client
                            //out.println("Echo: " + input);

                            // broadcast message
                            //System.out.println("Broadcasting message from client " + id);
                            //Message m = new ClientMessage(input, id, user, null);
                            //server.broadcast(m);

                            System.err.println("Do not know how to handle message: " + input + " from " + id);
                        }
                    }

                    System.out.println("Listen thread for " + id + " finished.");

                } catch (IOException e) {
                    e.printStackTrace();
                    ClientHandler.this.disconnect("Socker Error.");
                }
            }
        };

        listenThread.start();

        // relay broadcasted message asynchronously
        broadcastThread = new StoppableThread() {

            @Override
            public void run() {

                while (!finished) {
                    Message message = queue.consume();

                    if (message != null) {
                        // a message was broadcast, print it

                        String m = message.getPrefix() + " " + message.getMessageText();

                        out.println(m);

                        System.out.println("Message to " + id + ": " + m);

                    } else {
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("Broadcast thread for " + id + " finished.");
            }
        };

        broadcastThread.start();
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

    private void disconnect(String reason) {

        if (user != null && getNick() != null) {
            // allow other users to use this nickname now
            listenThread.stopThread();
            broadcastThread.stopThread();
            server.quit(this, reason);
            NickRegistrar.getInstance().removeNick(getNick());
        }

        close();
    }

    private void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (user != null) {
            NickRegistrar.getInstance().removeNick(user.getNick());
        }
        server.removeHandler(this);
    }

    public User getUser() {
        return user;
    }

}
