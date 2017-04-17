package JavaChatServer.Model;

import JavaChatServer.Controller.ClientHandler;
import JavaChatServer.Controller.MessageBroadcaster;

import java.util.List;

/**
 * Created by david on 4/11/17.
 */
public class Server {

    private MessageBroadcaster broadcaster;
    private MessageQueue messageQueue;
    private String serverName = "JavaChatServer";

    public Server() {
        messageQueue = new MessageQueue();
        broadcaster = new MessageBroadcaster(messageQueue);
    }

    public MessageBroadcaster getBroadcaster() {
        return broadcaster;
    }

    public void addHandlder(ClientHandler clientHandler) {
        broadcaster.addHandlder(clientHandler);
    }

    public void broadcast(Message message) {
        broadcaster.broadcast(message);
    }

    public void welcome(ClientHandler client) {

        client.receive(new ServerMessage("001 " + client.getNick() + " :" + this.getWelcomeMessage(), this));

    }

    public void joinChannel(ClientHandler handler, String channel) {

        // add the user to the channel
        broadcaster.joinChannel(handler, channel);

        User user = handler.getUser();

        // alert all members of the channel that the user has joined (by sending a message to the channel)
        Message joinMessage = new ClientMessage("JOIN " + channel, handler.getClientID(), user, channel);
        handler.receive(joinMessage);           // echo back
        broadcaster.broadcast(joinMessage);

        // display topic
        handler.receive(new ServerMessage("332 " + user.getNick() + " " + channel + " :Topic not yet implemented", this));

        // display creator of topic
        handler.receive(new ServerMessage("333 " + user.getNick() + " " + channel + " :Topic not yet implemented", this));

        // display list of users
        String usersMessage = "353 " + user.getNick() + " @ " + channel + " :";
        String userList = String.join(" ", getMembers(channel));

        handler.receive(new ServerMessage(usersMessage + userList, this));
        handler.receive(new ServerMessage("366 " + user.getNick() + " " + channel + " :End of /NAMES list.", this));

    }

    public void privateMessage(ClientHandler handler, String recipient, String message) {

        Message m = new ClientMessage("PRIVMSG " + recipient + " :" + message, handler.getClientID(), handler.getUser(), recipient);
        broadcast(m);
    }

    public void removeHandler(ClientHandler clientHandler) {
        broadcaster.removeHandler(clientHandler);
    }

    public List<String> getMembers(String channel) {
        return broadcaster.getMembers(channel);
    }

    public String getServerName() {
        return serverName;
    }

    public String getWelcomeMessage() {
        return "Welcome to " + serverName;
    }
}
