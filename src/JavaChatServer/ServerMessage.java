package JavaChatServer;

/**
 * Created by david on 4/11/17.
 */
public class ServerMessage extends Message {


    private Server server;

    public ServerMessage(String message, int clientID, Server server, String recipient) {
        this(message, clientID, server, recipient, false);
    }

    public ServerMessage(String message, int clientID, Server server, String recipient, boolean echo) {
        super(message, clientID, recipient, echo);
        this.server = server;
    }

    public ServerMessage(String m, Server s) {
        super(m, 0, null, true);
        this.server = s;
    }

    public String getPrefix() {
        return ":" + server.getServerName();
    }


}
