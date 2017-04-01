package JavaChatServer;

/**
 * Message class
 * Created by david on 4/1/17.
 */
public class Message {

    private int clientID;       // id of client who sent the message
    private String message;     // message text

    public Message(String message, int clientID) {
        this.clientID = clientID;
        this.message = message;
    }

    public int getClientID() {
        return clientID;
    }

    public String getMessageText() {
        return message;
    }

}
