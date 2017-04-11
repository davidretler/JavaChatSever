package JavaChatServer.Model;

/**
 * Message class
 * Created by david on 4/1/17.
 */
public abstract class Message {

    private int clientID;       // id of client who sent the message
    private String message;     // message text
    private String recipient;   // the recipient of the message (null if broadcast to all users).
    // may be a channel or user (if channel it will start with #)
    private boolean echo;       // should this message be echo'd back to the sender?


    public Message(String message, int clientID, String recipient) {
        this(message, clientID, recipient, false);
    }

    public Message(String message, int clientID, String recipient, boolean echo) {
        this.clientID = clientID;
        this.message = message;
        this.recipient = recipient;
        this.echo = echo;
    }

    public int getClientID() {
        return clientID;
    }

    public String getMessageText() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isEcho() {
        return echo;
    }

    public abstract String getPrefix();
}
