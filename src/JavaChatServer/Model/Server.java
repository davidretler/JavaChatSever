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

        displayUsers(handler, channel);

    }

    public void displayUsers(ClientHandler handler, String channel) {
        // display list of users
        String usersMessage = "353 " + handler.getUser().getNick() + " @ " + channel + " :";
        String userList = String.join(" ", getMembers(channel));

        handler.receive(new ServerMessage(usersMessage + userList, this));
        handler.receive(new ServerMessage("366 " + handler.getUser().getNick() + " " + channel + " :End of /NAMES list.", this));
    }

    public void privateMessage(ClientHandler handler, String recipient, String message) throws IRCCommandException {

        if (recipient.startsWith("#")) {
            // recipient is a channel... make sure user is a member
            if (!isMember(handler, recipient)) {
                throw new IRCCommandException("Cannot send to channel");
            }
        }
        Message m = new ClientMessage("PRIVMSG " + recipient + " :" + message, handler.getClientID(), handler.getUser(), recipient);
        broadcast(m);
    }

    private boolean isMember(ClientHandler handler, String channel) {
        return broadcaster.isMember(handler, channel);
    }

    public void quit(ClientHandler client, String reason) {
        for (String channel : broadcaster.getChannels(client)) {
            Message m = new ClientMessage("QUIT :" + reason, client.getClientID(), client.getUser(), channel);
            broadcast(m);
        }
    }

    public void part(ClientHandler client, String channel, String reason) {
        // message notifying clients that this client has left the channel
        Message m = new ClientMessage("PART " + channel + " :" + reason, client.getClientID(), client.getUser(), channel, true);

        // echo message to client so they have confirmation this worked
        client.receive(m);
        // remove client from channel
        broadcaster.partChannel(client, channel);
        // notify all other members of the channel
        broadcaster.broadcast(m);
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
