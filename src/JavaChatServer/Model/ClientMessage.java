package JavaChatServer.Model;

/**
 * Created by david on 4/11/17.
 */
public class ClientMessage extends Message {

    private User user;

    public ClientMessage(String message, int clientID, User user, String recipient) {
        this(message, clientID, user, recipient, false);
    }

    public ClientMessage(String message, int clientID, User user, String recipient, boolean echo) {
        super(message, clientID, recipient, echo);
        this.user = user;
    }

    public String getPrefix() {
        return ":" + user.getNick() + "!" + user.getUserName() + "@" + user.getServerName();
    }
}
