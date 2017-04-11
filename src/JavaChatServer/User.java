package JavaChatServer;

/**
 * Created by david on 4/11/17.
 */
public class User {

    private String nick;
    private String userName;
    private String hostName;
    private String serverName;
    private String realName;

    public User(String nick, String userName, String hostName, String serverName, String realName) {
        this.nick = nick;
        this.userName = userName;
        this.hostName = hostName;
        this.serverName = serverName;
        this.realName = realName;
    }

    public String getNick() {
        return nick;
    }

    public String getUserName() {
        return userName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRealName() {
        return realName;
    }

}
