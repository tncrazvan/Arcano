package com.github.tncrazvan.arcano;

import static com.github.tncrazvan.arcano.SharedObject.NAME_SESSION_ID;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.http.HttpEventRequest;
import com.github.tncrazvan.arcano.http.HttpEventResponse;
import com.github.tncrazvan.arcano.http.HttpRequestReader;
import com.github.tncrazvan.arcano.http.HttpSession;

/**
 * Provides a layer of abstraction for both HttpEventManager and WebSocketEventManager.
 * Contains a few methods that are useful to both classes, 
 * such as Http Header managing methods, tools to set, unset, 
 * and read cookie, and more.
 * @author Razvan Tanase
 */
public abstract class EventManager{
    public HttpEventRequest request = null;
    public HttpEventResponse response = null;
    public HttpSession session = null;
    public final SharedObject so;


    public EventManager(HttpRequestReader reader, SharedObject so) throws UnsupportedEncodingException {
        this.so = so;
        this.request = new HttpEventRequest(this,reader);
        this.response = new HttpEventResponse(this);
        this.resolveRequestedUrl();
    }

    private final void resolveRequestedUrl() throws UnsupportedEncodingException {
        String uri = request.reader.content.headers.getResource();
        try {
            uri = URLDecoder.decode(uri, so.config.charset);
        } catch (final IllegalArgumentException ex) {
            SharedObject.LOGGER.log(Level.SEVERE, null, ex);
        }

        String[] tmp, object;
        final String[] uriParts = uri.split("\\?|\\&", 2);

        if (uriParts.length > 1) {
            tmp = uriParts[1].split("\\&");
            for (final String part : tmp) {
                object = part.split("=", 2);
                if (object.length > 1) {
                    request.queryStrings.put(object[0].trim(), object[1]);
                } else {
                    request.queryStrings.put(object[0].trim(), "");
                }
            }
        }
    }
    

    /**
     * Get the Socket connection to the client.
     * 
     * @return
     */
    public final Socket getClientSocket() {
        return request.reader.client;
    }

    // FOR HTTP
    /*protected static final WebObject getHttpWebObject(HttpRequestReader reader, final String[] location, final String httpMethod)
            throws ClassNotFoundException {
        for (int i = location.length; i > 0; i--) {
            String tmp = "/" + String.join("/", Arrays.copyOf(location, i)).toLowerCase();
            HashMap<String, WebObject> method = reader.so.HTTP_ROUTES.get(httpMethod);
            if(method != null){
                WebObject route = method.get(tmp);
                if(route != null){
                    return route;
                }
            }
        }
        throw new ClassNotFoundException();
    }*/

    // FOR WEBSOCKET
    /*protected static final WebObject getWebSocketWebObject(HttpRequestReader reader, final String[] location, final String httpMethod)
            throws ClassNotFoundException {
        for (int i = location.length; i > 0; i--) {
            //String path = "/" + String.join("/", Arrays.copyOf(location, i)).toLowerCase();
            WebObject route = reader.so.WEB_SOCKET_ROUTES.get(httpMethod);
            if(route != null){
                return route;
            }
        }
        throw new ClassNotFoundException();
    }*/

    public final boolean issetSession() {
        return (request.issetCookie(NAME_SESSION_ID) && so.sessions.issetSession(request.getCookie(NAME_SESSION_ID)));
    }

    /**
     * Start an HttpSession. This method will request the client to set a
     * SharedObject.NAME_SESSION_ID cookie which will identify this session. If the client already
     * has a VALID SharedObject.NAME_SESSION_ID, then that SharedObject.NAME_SESSION_ID is used instead, thus fetching an
     * existing session instead of creating a new one. This means that you can
     * safely call this method multiple times and can expect it to return the same
     * HttpSession (unless the session itself has expired meanwhile) object. The
     * session's Time To Live is set to the SharedObject.sessionTtl, which has its
     * value set directly from the configuration file. Here's an example of a
     * configuration file that sets the sessino's Time To Live to 60 minutes: {
     * "port": 80, "serverRoot":"server", "webRoot":"www", "charset":"UTF-8", ...
     * "sessionTtl": 3600, ... "threadPoolSize": 3, "sendExceptions": true,
     * "responseWrapper": false }
     * 
     * @return the HttpSession object. This is a new object if the client's
     *         SharedObject.NAME_SESSION_ID is invalid or non existent, or an already existing
     *         HttpSession object if the client provides a valid and existing
     *         SharedObject.NAME_SESSION_ID.
     */
    public final HttpSession startSession() {
        session = so.sessions.startSession(this, so.config.session.ttl);
        return session;
    }

    /**
     * Stops the current HttpSession of the client if it has one. This will also
     * delete the client's SharedObject.NAME_SESSION_ID cookie.
     */
    public final void stopSession() {
        if (session == null)
            session = startSession();
        if (issetSession())
            so.sessions.stopSession(session);
    }
}