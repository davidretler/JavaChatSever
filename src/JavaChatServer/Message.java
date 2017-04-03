package JavaChatServer;

/**
 * Message class
 * Created by david on 4/1/17.
 */
public class Message {

    private int clientID;       // id of client who sent the message
    private String clientNick;  // nickname of the client
    private String message;     // message text

    public Message(String message, int clientID, String nick) {
        this.clientID = clientID;
        this.message = message;
        this.clientNick = nick;
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
}
