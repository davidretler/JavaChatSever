import Controller.ServerThread;
import JavaChatServer.Model.Server;

/**
 * Created by david on 3/29/17.
 */
public class ServerDriver {

    public static void main(String[] args) {

        Server myServer = new Server();
        ServerThread myServerThread = new ServerThread(myServer);

        new Thread(myServerThread).start();

    }

}
