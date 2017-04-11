package JavaChatServer.Controller;

import JavaChatServer.Model.Message;
import JavaChatServer.Model.MessageQueue;
import JavaChatServer.Model.NickRegistrar;

import java.util.*;

/**
 * Keeps track of all the client handler threads and broadcasts new messages to them
 * Created by david on 4/1/17.
 */
public class MessageBroadcaster implements Runnable {

    private final int timeout = 100; // time to wait between attempted broadcasts, in ms
    private final int ns_to_ms = 1000;

    private final MessageQueue queue;

    private final List<ClientHandler> handlerList; // list of client handlers

    private final Map<String, List<ClientHandler>> channelMembers; // list of the channels subscribed to by each client


    public MessageBroadcaster(MessageQueue q) {
        this.queue = q;
        handlerList = Collections.synchronizedList(new ArrayList<ClientHandler>());
        channelMembers = Collections.synchronizedMap(new HashMap<String, List<ClientHandler>>());
    }

    public void addHandlder(ClientHandler h) {
        handlerList.add(h);
    }

    public void joinChannel(ClientHandler h, String channel) {

        System.out.println("Client wants to join " + channel);

        List<ClientHandler> memberList = channelMembers.get(channel);

        if (memberList == null) {
            System.out.println("First member for " + channel + " has joined!");
            channelMembers.put(channel, new ArrayList<>());
            memberList = channelMembers.get(channel);
        }

        memberList.add(h);
    }

    public void removeHandler(ClientHandler clientHandler) {
        System.out.println("Removing client " + clientHandler.getClientID() + " from broadcaster");

        // remove from all channels subscribed to
        for (List<ClientHandler> l : channelMembers.values()) {
            l.remove(clientHandler);
        }
        // remove from server wide handler list
        handlerList.remove(clientHandler);
    }

    public void broadcast(Message m) {
        queue.broadcast(m);
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Message message = queue.consume();

                    // if there are no new messages... wait
                    if (message == null) {
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {

                        // give each client handler a copy of the message

                        List<ClientHandler> closedClients = new ArrayList<>();
                        List<ClientHandler> recipients = new ArrayList();

                        if (message.getRecipient() == null) {

                            // broadcast to all users

                            synchronized (handlerList) { // make sure another thread does not change the list while we iterate over it
                                for (ClientHandler h : handlerList) {

                                    if (h.closed()) {
                                        // ignore closed connections and remove them from the queue
                                        System.out.println("Removed old client " + h.getClientID());
                                        NickRegistrar.getInstance().removeNick(h.getNick());
                                        closedClients.add(h);
                                        continue;
                                    }

                                    // don't broadcast back to sender
                                    if (h.getClientID() != message.getClientID() || message.isEcho()) {
                                        recipients.add(h);
                                    }
                                }
                            }


                        } else if (message.getRecipient().startsWith("#")) {
                            // send to all users in the channel

                            String channel = message.getRecipient();
                            List<ClientHandler> members = channelMembers.get(channel);

                            if (members == null) {
                                // there are no members...
                                System.err.println(channel + " has no memebers...");
                            } else {

                                synchronized (members) {
                                    for (ClientHandler h : members) {

                                        if (h.closed()) {
                                            // ignore closed connections and remove them from the queue
                                            NickRegistrar.getInstance().removeNick(h.getNick());
                                            closedClients.add(h);
                                            continue;
                                        }

                                        // don't broadcast back to sender
                                        if (h.getClientID() != message.getClientID() || message.isEcho()) {
                                            recipients.add(h);
                                        }
                                    }
                                }
                            }

                        } else {
                            // send to the particular user

                            // find the recipient message handler
                            synchronized (handlerList) {
                                for (ClientHandler h : handlerList) {
                                    if (h.getUser().getNick().equalsIgnoreCase(message.getRecipient())) {
                                        recipients.add(h);
                                    }
                                }
                            }
                        }

                        // remove the closed clients
                        for (ClientHandler h : closedClients) {
                            removeHandler(h);
                        }

                        for (ClientHandler h : recipients) {

                            System.out.println("Broadcasting message to " + h.getClientID());
                            h.receive(message);

                        }
                    }
                }
            }

        }).start();

    }


    public List<String> getMembers(String channel) {
        List<String> memebers = new ArrayList<>();
        List<ClientHandler> memberList = channelMembers.get(channel);
        synchronized (memberList) {
            if (memberList == null) {
                return new ArrayList<>();
            } else {
                for (ClientHandler h : memberList) {
                    memebers.add(h.getNick());
                }
            }
        }
        return memebers;
    }
}
