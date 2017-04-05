package JavaChatServer;

/**
 * Message class
 * Created by david on 4/1/17.
 */
public class Message {

    private int clientID;       // id of client who sent the message
    private String clientNick;  // nickname of the client
    private String message;     // message text
    private String recipient;   // the recipient of the message (null if broadcast to all users).
    // may be a channel or user (if channel it will start with #)


    public Message(String message, int clientID, String nick, String recipient) {
        this.clientID = clientID;
        this.message = message;
        this.clientNick = nick;
        this.recipient = recipient;
    }

    public int getClientID() {
        return clientID;
    }

    public String getMessageText() {
        return message;
    }

    public String getClientNick() {
        return clientNick;
    }

    public String getRecipient() {
        return recipient;
    }
}
