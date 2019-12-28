package com.github.tncrazvan.arcano.WebSocket;

import com.github.tncrazvan.arcano.Http.HttpSession;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getBCryptString;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.validateBCryptString;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class WebSocketGroup {
    public static final int 
            PRIVATE = 0,
            PUBLIC = 1;
    private final String key;
    private final Map<String, WebSocketController> events = new HashMap<>();
    private WebSocketController master = null;
    private int visibility = PRIVATE;
    private String name;

    public WebSocketGroup(final HttpSession session) {
        this.key = getBCryptString(session.id());
    }

    public void setGroupName(final String name) {
        this.name = name;
    }

    public String getGroupName() {
        return name;
    }

    public void setVisibility(final int v) {
        visibility = v;
    }

    public int getVisibility() {
        return visibility;
    }

    public void addClient(final WebSocketController e) throws UnsupportedEncodingException {
        e.startSession();
        events.put(e.session.id(), e);
    }

    public WebSocketController removeClient(final WebSocketController e) {
        if (matchCreator(e)) {
            master = null;
        }
        return events.remove(e.session.id());
    }

    public boolean clientExists(final WebSocketController e) {
        return events.containsKey(e.session.id());
    }

    public Map<String, WebSocketController> getMap() {
        return events;
    }

    public String getKey() {
        return key;
    }

    public WebSocketController getGroupMaster() {
        return master;
    }

    public boolean groupMasterIsset() {
        return master != null;
    }

    public void setGroupMaster(final WebSocketController e) {
        master = e;
    }

    public void unsetGroupMaster() {
        master = null;
    }

    public boolean matchCreator(final WebSocketController e) {
        return validateBCryptString(e.session.id(), key);
    }
}
