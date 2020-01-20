package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Bean.Email.SmtpService;
import com.github.tncrazvan.arcano.Tool.Minifier;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.util.concurrent.ThreadPoolExecutor;
import com.github.tncrazvan.arcano.Http.HttpSessionManager;
import com.github.tncrazvan.arcano.Smtp.SmtpController;
import com.github.tncrazvan.arcano.Tool.Strings;
import static com.github.tncrazvan.arcano.Tool.Strings.normalizePathSlashes;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.util.concurrent.ExecutorService;
import com.github.tncrazvan.arcano.Bean.Http.HttpNotFound;
import com.github.tncrazvan.arcano.Bean.Http.HttpDefault;
import com.github.tncrazvan.arcano.Bean.WebSocket.WebSocketNotFound;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;
import com.github.tncrazvan.arcano.Bean.WebSocket.WebSocketService;

/**
 * 
 * @author Razvan
 */
public class SharedObject implements Strings{
    //SESSIONS
    public final HttpSessionManager sessions = new HttpSessionManager();
    //THREADS
    public ThreadPoolExecutor executor = null;
    public ExecutorService service = null;
    //CONFIGURATION OBJECTS
    public final Configuration config = new Configuration();
    public Minifier minifier = null;
    public static final String NO_COMPRESSION="",DEFLATE="deflate",GZIP="gzip";
    //SYSTEM RUNTIME
    public static final Runtime RUNTIME = Runtime.getRuntime();
    //ROUTING
    public static final HashMap<String, WebObject> ROUTES = new HashMap<>();
    //LOCALE & DATES
    public static final Calendar CALENDAR = Calendar.getInstance();
    public static final Date DATE = new Date();
    public static ZoneId londonTimezone = ZoneId.of("Europe/London");
    //LOGGING
    public static final Logger LOGGER = Logger.getLogger(SharedObject.class.getName());
    //WEBSOCKETS OBJECTS
    public final Map<String,ArrayList<WebSocketEvent>> WEB_SOCKET_EVENTS = new HashMap<>();
    public final String WEBSOCKET_ACCEPT_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    //SERVER STATUS
    public boolean isRunning = false;
    //ENCODING & DECODING
    public static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    public static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    //DEFAULT RESPONSES
    public static final HttpResponse EMPTY_RESPONSE = new HttpResponse("").resolve();
    //DEFAULT PROJECT NAMES
    public static final String NAME_SESSION_ID = "JavaSessionID";
    private static final String HTTP_SERVICE_METHOD_404 = "HTTP 404";
    private static final String HTTP_SERVICE_METHOD_DEFAULT = "HTTP DEFAULT";
    public final void expose(Class<?>... classes) {
        for (Class<?> cls : classes) {
            try {
                if (HttpController.class.isAssignableFrom(cls)) {
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                        HttpService service = method.getAnnotation(HttpService.class);
                        if (service != null) {
                            final WebObject wo = new WebObject(
                                cls.getName(), 
                                method.getName(), 
                                service.method(), 
                                service.locked()
                            );
                            ROUTES.put(service.method() + normalizePathSlashes(service.path()), wo);
                        }
                        if (method.getAnnotation(HttpNotFound.class) != null) {
                            WebObject wo = new WebObject(
                                cls.getName(), 
                                method.getName(), 
                                HTTP_SERVICE_METHOD_404, 
                                service != null && service.locked()
                            );
                            ROUTES.put(HTTP_SERVICE_METHOD_404, wo);
                            this.config.http.controllerNotFound = wo;
                        }
                        if (method.getAnnotation(HttpDefault.class) != null) {
                            WebObject wo = new WebObject(
                                cls.getName(), 
                                method.getName(), 
                                HTTP_SERVICE_METHOD_DEFAULT, 
                                service != null && service.locked()
                            );
                            ROUTES.put(HTTP_SERVICE_METHOD_DEFAULT, wo);
                            this.config.http.controllerDefault = wo;
                        }
                    }
                } else if (WebSocketController.class.isAssignableFrom(cls)) {
                    WebSocketService service = (WebSocketService) cls.getAnnotation(WebSocketService.class);
                    if (service != null) {
                        String type = "WS";
                        WebObject wo = new WebObject(cls.getName(), null, type, service.locked());
                        ROUTES.put(type + normalizePathSlashes(service.path()), wo);
                    }
                    if (cls.getAnnotation(WebSocketNotFound.class) != null){
                        String type = "WS 404";
                        WebObject wo = new WebObject(cls.getName(), null, type, service != null && service.locked());
                        ROUTES.put(type, wo);
                        this.config.webSocket.controllerNotFound = wo;
                    }
                } else if (SmtpController.class.isAssignableFrom(cls)) {
                    SmtpService service = (SmtpService) cls.getAnnotation(SmtpService.class);
                    String type = "SMTP";
                    WebObject wo = new WebObject(cls.getName(), null, type, service != null && service.locked());
                    ROUTES.put(type, wo);
                }
            } catch (SecurityException | IllegalArgumentException  ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}