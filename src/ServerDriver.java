import JavaChatServer.Server;

/**
 * Created by david on 3/29/17.
 */
public class ServerDriver {

    public static void main(String[] args) {

        Server myServer = new Server();

        new Thread(myServer).start();

    }

}
