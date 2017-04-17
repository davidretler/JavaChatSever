package JavaChatServer.Controller;

/**
 * A thread which can be gracefully stopped
 * <p>
 * Created by david on 4/17/17.
 */
public class StoppableThread extends Thread {

    public volatile boolean finished = false;

    public StoppableThread() {

    }

    public void stopThread() {
        finished = true;
    }

    public StoppableThread(Runnable task) {
        super(task);
        start();
    }

}
