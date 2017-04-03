package JavaChatServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps track of nicknames
 * <p>
 * Created by david on 4/2/17.
 */
public class NickRegistrar {

    private static NickRegistrar registrar = null;

    protected NickRegistrar() {

    }

    private List<String> nickList = Collections.synchronizedList(new ArrayList<String>());


    public static NickRegistrar getInstance() {
        if (registrar == null) {
            registrar = new NickRegistrar();
        }
        return registrar;
    }

    /**
     * Add the nickname to the register. If the nick is unique, returns true. Otherwise
     * refuses to add and returns false.
     */
    public boolean addNick(String nick) {

        if (!exists(nick)) {
            nickList.add(nick);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeNick(String nick) {
        if (exists(nick)) {
            nickList.remove(nick);
            return true;
        } else {
            return false;
        }
    }

    public boolean exists(String nick) {
        for (String n : nickList) {
            if (n.equalsIgnoreCase(nick)) {
                return true;
            }
        }

        return false;
    }

}


