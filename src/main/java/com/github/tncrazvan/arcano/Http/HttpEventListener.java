package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import static com.github.tncrazvan.arcano.Tool.Time.time;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
import java.io.File;
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
    public void onRequest(final HttpRequest request) {
        HttpHeaders clientHeader = request.getHttpHeaders();
        if(clientHeader != null && clientHeader.get("Connection")!=null){
            matcher = UPGRADE_PATTERN.matcher(clientHeader.get("Connection"));
            if(matcher.find()){
                matcher = WEB_SOCKET_PATTERN.matcher(clientHeader.get("Upgrade"));
                //WebSocket connection
                if(matcher.find()){
                    try {
                        new WebSocketEvent(so, reader, client, request).execute();
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
                    HttpEvent e = new HttpEvent(so,output,client,request);
                    StringBuilder location = e.getLocation();
                    HttpHeaders headers = e.getResponseHttpHeaders();
                    e.findRequestLanguages();
                    File f = new File(so.webRoot+location);
                    if(f.exists()){
                        if(!f.isDirectory()){
                            headers.set("Content-Type", resolveContentType(location.toString()));
                            headers.set("Last-Modified",so.formatHttpDefaultDate.format(time(f.lastModified())));
                            headers.set("Last-Modified-Timestamp",f.lastModified()+"");
                            e.sendFileContents(f);
                        }else{
                            e.isDir = true;
                            headers.set("Content-Type", "text/html");
                            e.onControllerRequest(location);
                        }
                    }else{
                        headers.set("Content-Type", "text/html");
                        e.onControllerRequest(location);
                    }
                    e.close();
                    
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE,null,ex);
                }
            }
        }
    }
}
