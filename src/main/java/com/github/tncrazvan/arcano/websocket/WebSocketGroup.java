package com.github.tncrazvan.arcano.websocket;

import static com.github.tncrazvan.arcano.tool.encoding.Hashing.getBCryptString;
import static com.github.tncrazvan.arcano.tool.encoding.Hashing.validateBCryptString;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.github.tncrazvan.arcano.http.HttpSession;

/**
 *
 * @author Razvan Tanase
 */
public class WebSocketGroup {
    public static final int 
            PRIVATE = 0,
            PUBLIC = 1;
    private final String key;
    private final Map<String, WebSocketEvent> events = new HashMap<>();
    private WebSocketEvent master = null;
    private int visibility = PRIVATE;
    private String name;

    public WebSocketGroup(final HttpSession session) {
        this.key = getBCryptString(session.id());
    }

    public final void setGroupName(final String name) {
        this.name = name;
    }

    public final String getGroupName() {
        return name;
    }

    public final void setVisibility(final int v) {
        visibility = v;
    }

    public final int getVisibility() {
        return visibility;
    }

    public final void addClient(final WebSocketEvent e) throws UnsupportedEncodingException {
        e.startSession();
        events.put(e.session.id(), e);
    }

    public final WebSocketEvent removeClient(final WebSocketEvent e) {
        if (matchCreator(e)) {
            master = null;
        }
        return events.remove(e.session.id());
    }

    public final boolean clientExists(final WebSocketEvent e) {
        return events.containsKey(e.session.id());
    }

    public final Map<String, WebSocketEvent> getMap() {
        return events;
    }

    public final String getKey() {
        return key;
    }

    public final WebSocketEvent getGroupMaster() {
        return master;
    }

    public final boolean groupMasterIsset() {
        return master != null;
    }

    public final void setGroupMaster(final WebSocketEvent e) {
        master = e;
    }

    public final void unsetGroupMaster() {
        master = null;
    }

    public final boolean matchCreator(final WebSocketEvent e) {
        return validateBCryptString(e.session.id(), key);
    }
}
