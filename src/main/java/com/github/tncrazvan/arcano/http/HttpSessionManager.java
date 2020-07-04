package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.SharedObject.NAME_SESSION_ID;

import java.util.HashMap;
import java.util.Map;

import com.github.tncrazvan.arcano.EventManager;

/**
 *
 * @author Razvan Tanase
 */
public class HttpSessionManager {
    public final Map<String,HttpSession> LIST = new HashMap<>();
    
    public HttpSessionManager() {}
    
    public final HttpSession startSession(final EventManager e, final long sessionTtl) {
        if (e.request.issetCookie(NAME_SESSION_ID)) {// if session id is set
            final String sessionID = e.request.getCookie(NAME_SESSION_ID);
            if (LIST.containsKey(sessionID)) {// if session exists
                final HttpSession session = LIST.get(sessionID);
                if (e.so.config.session.keepAlive)
                    session.setTime(System.currentTimeMillis());
                return session;
            }
        }

        final HttpSession session = new HttpSession(e);
        setSession(session);
        return session;
    }

    public final HttpSession getSession(final String sessionID) {
        return LIST.get(sessionID);
    }

    public final void setSession(final HttpSession session) {
        LIST.put(session.id(), session);
    }

    public final boolean issetSession(final String sessionID) {
        return LIST.containsKey(sessionID);
    }

    public final void stopSession(final HttpSession session) {
        LIST.remove(session.id());
    }
}
