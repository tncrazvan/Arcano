package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.util.logging.Logger;
import static com.github.tncrazvan.arcano.Tool.Time.now;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
/**
 *
 * @author Razvan
 */
public class HttpEventListener extends HttpRequestReader{
    private Matcher matcher;
    
    public static final DateTimeFormatter formatHttpDefaultDate = DateTimeFormatter.ofPattern("EEE, d MMM y HH:mm:ss z", Locale.US).withZone(londonTimezone);
    private static final Pattern
            UPGRADE_PATTERN = Pattern.compile("Upgrade"),
            WEB_SOCKET_PATTERN = Pattern.compile("websocket"),
            HTTP2_PATTERN = Pattern.compile("h2c");
    public HttpEventListener(SharedObject so, final Socket client) throws IOException, NoSuchAlgorithmException{
        super(so,client);
    }

    @Override
    public void onRequest() {
        if(request.headers != null && request.headers.get("Connection")!=null){
            matcher = UPGRADE_PATTERN.matcher(request.headers.get("Connection"));
            if(matcher.find()){
                matcher = WEB_SOCKET_PATTERN.matcher(request.headers.get("Upgrade"));
                //WebSocket connection
                if(matcher.find()){
                    try {
                        WebSocketController.serveController(this);
                        /*try {
                        new WebSocketController(so, bufferedReader, client, request).execute();
                        }catch(final IOException e){
                        try {
                        client.close();
                        } catch (final IOException ex) {
                        LOGGER.log(Level.SEVERE,null,ex);
                        }
                        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                        LOGGER.log(Level.SEVERE,null,ex);
                        } catch (ClassNotFoundException | IllegalArgumentException | InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                        }*/
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    matcher = HTTP2_PATTERN.matcher(request.headers.get("Upgrade"));
                    // Http 2.x connection
                    if(matcher.find()){
                        System.out.println("Http 2.0 connection detected. Not yet implemented.");
                    }
                }
            }else{
                try {
                    client.setSoTimeout(timeout);
                    //default connection, assuming it's Http 1.x
                    
                    //HttpHeaders headers = controller.getResponseHttpHeaders();
                    File f = new File(so.webRoot+location);
                    if(f.exists()){
                        if(!f.isDirectory()){
                            HttpController controller = new HttpController();
                            controller.setHttpHeaders(new HttpHeaders());
                            controller.setSharedObject(so);
                            controller.setDataOutputStream(output);
                            controller.setSocket(client);
                            controller.setHttpRequest(request);
                            controller.initEventManager();
                            controller.initHttpEventManager();
                            controller.findRequestLanguages();
                            
                            controller.setResponseHeaderField("Content-Type", resolveContentType(location.toString()));
                            controller.setResponseHeaderField("Last-Modified",formatHttpDefaultDate.format(now(londonTimezone,f.lastModified())));
                            controller.setResponseHeaderField("Last-Modified-Timestamp",f.lastModified()+"");
                            controller.send(f);
                        }else{
                            //String httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName
                            HttpController.onControllerRequest(this);
                        }
                    }else{
                        //controller.setResponseHeaderField("Content-Type", "text/html");
                        HttpController.onControllerRequest(this);
                    }
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE,null,ex);
                }
            }
        }
    }
}
