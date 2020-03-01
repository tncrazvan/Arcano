package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.SharedObject.londonTimezone;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import static com.github.tncrazvan.arcano.Tool.System.Time.toLocalDateTime;
import java.net.SocketException;
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
    public HttpEventListener(final SharedObject so, final Socket client) throws IOException, NoSuchAlgorithmException {
        super(so, client);
    }

    
    @Override
    public final void onRequest() {
        if (request.headers != null && request.headers.get("Connection") != null) {
            matcher = UPGRADE_PATTERN.matcher(request.headers.get("Connection"));
            if (matcher.find()) {
                matcher = WEB_SOCKET_PATTERN.matcher(request.headers.get("Upgrade"));
                // Upgrade connection
                upgrade();
            } else {
                http();
            }
        }
    }
    
    
    private void http(){
        try {
            client.setSoTimeout(so.config.timeout);
            // default connection, assuming it's Http 1.x
            
            // HttpHeaders headers = controller.getResponseHttpHeaders();
            final File f = new File(so.config.webRoot + locationBuilder);
            if (f.exists()) {
                if (!f.isDirectory()) {
                    final HttpController controller = new HttpController().install(this);
                    
                    controller.setResponseHeaderField("Content-Type", resolveContentType(locationBuilder.toString()));
                    controller.setResponseHeaderField("Last-Modified",formatHttpDefaultDate.format(toLocalDateTime(londonTimezone,f.lastModified())));
                    controller.setResponseHeaderField("Last-Modified-Timestamp",f.lastModified()+"");
                    controller.send(f);
                }else{
                    //String httpMethod,String httpNotFoundNameOriginal,String httpNotFoundName
                    HttpController.serve(this);
                }
            }else{
                //controller.setResponseHeaderField("Content-Type", "text/html");
                HttpController.serve(this);
            }
        } catch (SocketException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private void upgrade(){
        if (matcher.find()) {
                try{
                    WebSocketController.serve(this);
                }catch(Exception e){
                    LOGGER.log(Level.SEVERE, null, e);
                }
        } else {
            matcher = HTTP2_PATTERN.matcher(request.headers.get("Upgrade"));
            // Http 2.x connection
            if (matcher.find()) {
                System.out.println("Http 2.0 connection detected. Not yet implemented.");
            }
        }
    }
}
