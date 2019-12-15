package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
/**
 *
 * @author Razvan
 */
public class HttpEventListener extends HttpRequestReader{
    private Matcher matcher;
    private static final Pattern
            UPGRADE_PATTERN = Pattern.compile("Upgrade"),
            WEB_SOCKET_PATTERN = Pattern.compile("websocket"),
            HTTP2_PATTERN = Pattern.compile("h2c");
    private final SharedObject so;
    public HttpEventListener(SharedObject so, final Socket client) throws IOException, NoSuchAlgorithmException{
        super(client);
        this.so=so;
    }

    @Override
    public void onRequest(final HttpHeader clientHeader, final byte[] input) {
        if(clientHeader != null && clientHeader.get("Connection")!=null){
            matcher = UPGRADE_PATTERN.matcher(clientHeader.get("Connection"));
            if(matcher.find()){
                matcher = WEB_SOCKET_PATTERN.matcher(clientHeader.get("Upgrade"));
                //WebSocket connection
                if(matcher.find()){
                    try {
                        new WebSocketEvent(so, reader, client, clientHeader).execute();
                    }catch(final IOException e){
                        try {
                            client.close();
                        } catch (final IOException ex) {
                            LOGGER.log(Level.SEVERE,null,ex);
                        }
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                        LOGGER.log(Level.SEVERE,null,ex);
                    } catch (ClassNotFoundException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    matcher = HTTP2_PATTERN.matcher(clientHeader.get("Upgrade"));
                    // Http 2.x connection
                    if(matcher.find()){
                        System.out.println("Http 2.0 connection detected. Not yet implemented.");
                    }
                }
            }else{
                try {
                    client.setSoTimeout(timeout);
                    //default connection, assuming it's Http 1.x
                    new HttpEvent(so,output,clientHeader,client,input).execute();
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE,null,ex);
                }
            }
        }
    }
}
