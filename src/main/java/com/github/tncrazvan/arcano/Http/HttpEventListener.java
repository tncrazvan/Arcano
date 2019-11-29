package com.github.tncrazvan.arcano.Http;

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
            upgradePattern = Pattern.compile("Upgrade"),
            websocketPattern = Pattern.compile("websocket"),
            http2Pattern = Pattern.compile("h2c");
    public HttpEventListener(final Socket client) throws IOException, NoSuchAlgorithmException{
        super(client);
    }

    @Override
    public void onRequest(final HttpHeader clientHeader, final byte[] input) {
        if(clientHeader != null && clientHeader.get("Connection")!=null){
            matcher = upgradePattern.matcher(clientHeader.get("Connection"));
            if(matcher.find()){
                matcher = websocketPattern.matcher(clientHeader.get("Upgrade"));
                //WebSocket connection
                if(matcher.find()){
                    try {
                        new WebSocketEvent(reader, client, clientHeader).execute();
                    }catch(final IOException e){
                        try {
                            client.close();
                        } catch (final IOException ex) {
                            logger.log(Level.SEVERE,null,ex);
                        }
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                        logger.log(Level.SEVERE,null,ex);
                    } catch (ClassNotFoundException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    matcher = http2Pattern.matcher(clientHeader.get("Upgrade"));
                    // Http 2.x connection
                    if(matcher.find()){
                        System.out.println("Http 2.0 connection detected. Not yet implemented.");
                    }
                }
            }else{
                try {
                    client.setSoTimeout(timeout);
                    //default connection, assuming it's Http 1.x
                    new HttpEvent(output,clientHeader,client,input).execute();
                } catch (final IOException ex) {
                    logger.log(Level.SEVERE,null,ex);
                }
            }
        }
    }
}
