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
    private MessageBroadcaster broadcaster;

    private String nick = null;

    private PrintWriter out;
    private BufferedReader in;

    ClientHandler(Socket socket, int n, MessageBroadcaster b) {
        this.clientSocket = socket;
        id = n;
        broadcaster = b;
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

        // initialize
        try {
            // initiate by waiting for the user to indicate a nickname
            nick = null;

            out.println("Welcome to the chat server. Please choose a nickname by typing \"NICK <your nickname>\"");


            while (nick == null) {

                System.out.println("Waiting for client " + id + " to indicate nickname");

                String input = in.readLine();

                if (input.toLowerCase().contains("nick")) {
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
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize client");
            return;
        }


        // add this client to the broadcaster once we have initiaized

        broadcaster.addHandlder(this);

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

                                broadcaster.broadcast(new Message(message, id, nick, recipient));
                            }
                        } else if (command.equalsIgnoreCase("join")) {

                            if (input.split(" ").length == 2) {
                                String channel = input.split(" ")[1];
                                if (channel.startsWith(("#"))) {
                                    broadcaster.joinChannel(ClientHandler.this, channel);

                                    // :dsfsdfsf!~dasdasd@174.141.133.200 JOIN #test
                                    out.println(":" + nick + "!~" + "david" + "@127.0.0.1" + " JOIN " + channel);

                                    // :card.freenode.net 332 dsfsdfsf #test :Welcome to freenode's test channel
                                    out.println(":JavaChatServer 332 " + nick + " " + channel + " :test");

                                    // :card.freenode.net 333 dsfsdfsf #test sysdef!sysdef@debiancenter/founder.developer/pdpc.professional.sysdef 1348399912
                                    out.println(":JavaChatServer 333 " + nick + " " + channel + " random stuff here");

                                    // :niven.freenode.net 353 sdfsdfsadfawefd * ##linux :sdfsdfsadfawefd fenix_br tanuki abirchall Kundry_Wag piggah_ MRiddickW waco enterprisey bertman skweek l__ dayid Vyrus001 internauto Mateon1_ hagridaaron gigetoo KindOne dirtyroshi rulezzz desti Tipping_Fedora p71 bigbadjudas janof vonsyd0w Anonaly dishaze fassl____ MystaMax gregor2_ phatm1ike_ zchrykng uks insker xdexter Muyfret slack_ obZen s3n energizer Ninetou dj_pi markasoftware badpixel fstd WinterBluFox i8igmac CapsAdmin minimalism Flynnn
                                    out.print(":JavaChatServer 353 " + nick + " @ " + channel + " :" + nick);
                                    for (String member : broadcaster.getMembers(channel)) {
                                        out.print(" " + member);
                                    }
                                    out.println();
                                    // :card.freenode.net 366 dsfsdfsf #test :End of /NAMES list.
                                    out.println(":JavaChatServer 366 " + nick + " " + channel + " :End of /NAMES list.");

                                }
                            }

                        } else {
                            // echo data back to client
                            //out.println("Echo: " + input);

                            // broadcast message
                            System.out.println("Broadcasting message from client " + id);
                            Message m = new Message(input, id, nick, null);
                            broadcaster.broadcast(m);
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

                        String m = message.getClientNick() + ": " + message.getMessageText();

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
        return nick;
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
        NickRegistrar.getInstance().removeNick(nick);
        broadcaster.removeHandler(this);
    }

}
