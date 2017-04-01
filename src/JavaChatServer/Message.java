package JavaChatServer;

import java.time.Instant;

/**
 * Message class
 * Created by david on 4/1/17.
 */
public class Message {
    int clientID;       // id of client who sent the message
    String message;     // message text
    Instant timeStamp;  // time the message was sent
}
