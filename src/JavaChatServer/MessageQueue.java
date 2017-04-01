package JavaChatServer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implements a message queue.
 * <p>
 * Each sever thread can push messages to the queue, which can then be
 * consumed by other threads.
 * <p>
 * Created by david on 4/1/17.
 */
public class MessageQueue {

    // queue of messages
    private BlockingQueue<Message> queue;
    private final int capacity = 100; // max size of queue


    public MessageQueue() {
        queue = new ArrayBlockingQueue<Message>(capacity);
    }

    public void broadcast(Message message) {
        queue.add(message);
    }

    public Message consume() {
        return queue.poll();
    }

}
