package JavaChatServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps track of all the client handler threads and broadcasts new messages to them
 * Created by david on 4/1/17.
 */
public class MessageBroadcaster implements Runnable {

    private final int timeout = 100; // time to wait between attempted broadcasts, in ms
    private final int ns_to_ms = 1000;

    private final MessageQueue queue;

    final List<ClientHandler> handlerList; // list of client handlers


    public MessageBroadcaster(MessageQueue q) {
        this.queue = q;
        handlerList = Collections.synchronizedList(new ArrayList<ClientHandler>());
    }

    public void addHandlder(ClientHandler h) {
        handlerList.add(h);
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
                        for (ClientHandler h : handlerList) {
                            h.receive(message);
                        }
                    }
                }
            }
        }).start();

    }
}
