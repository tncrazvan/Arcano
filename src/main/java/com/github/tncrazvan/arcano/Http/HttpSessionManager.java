package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.EventManager;
import static com.github.tncrazvan.arcano.SharedObject.NAME_SESSION_ID;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class HttpSessionManager {
    public final Map<String,HttpSession> LIST = new HashMap<>();
    
    public HttpSessionManager() {}
    
    public HttpSession startSession(final EventManager e, final long sessionTtl) {
        if (e.issetRequestCookie(NAME_SESSION_ID)) {// if session id is set
            final String sessionID = e.getRequestCookie(NAME_SESSION_ID);
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

    public HttpSession getSession(final String sessionID) {
        return LIST.get(sessionID);
    }

    public void setSession(final HttpSession session) {
        LIST.put(session.id(), session);
    }

    public boolean issetSession(final String sessionID) {
        return LIST.containsKey(sessionID);
    }

    public void stopSession(final HttpSession session) {
        LIST.remove(session.id());
    }
}
