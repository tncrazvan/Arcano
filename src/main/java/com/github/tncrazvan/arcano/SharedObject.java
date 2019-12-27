package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Bean.Email.EmailPath;
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
import com.github.tncrazvan.arcano.Bean.Web.WebMethod;
import com.github.tncrazvan.arcano.Bean.Web.WebPath;
import com.github.tncrazvan.arcano.Http.HttpSessionManager;
import com.github.tncrazvan.arcano.Smtp.SmtpController;
import com.github.tncrazvan.arcano.Tool.Strings;
import static com.github.tncrazvan.arcano.Tool.Strings.normalizePathSlashes;
import com.github.tncrazvan.arcano.Bean.Security.ArcanoSecret;
import com.github.tncrazvan.arcano.Bean.Web.WebPathNotFound;
import com.github.tncrazvan.arcano.Bean.Web.DefaultWebPath;

/**
 * 
 * @author Razvan
 */
public class SharedObject implements Strings{
    //SESSIONS
    public final HttpSessionManager sessions = new HttpSessionManager();
    //THREADS
    public ThreadPoolExecutor executor = null;
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
    
    public final void expose(Class<?>... classes) {
        for(Class<?> cls : classes){
            try {
                if(HttpController.class.isAssignableFrom(cls)){
                    WebPath classRoute = (WebPath) cls.getAnnotation(WebPath.class);
                    ArcanoSecret classWebLocked = (ArcanoSecret) cls.getAnnotation(ArcanoSecret.class);
                    WebMethod classWebFilter = (WebMethod) cls.getAnnotation(WebMethod.class);
                    Method[] methods = cls.getDeclaredMethods();
                    for(Method method : methods){
                        WebPath methodRoute = method.getAnnotation(WebPath.class);
                        ArcanoSecret methodWebLocked = (ArcanoSecret) method.getAnnotation(ArcanoSecret.class);
                        if(methodRoute != null){
                            String classPath = normalizePathSlashes(classRoute.name().trim());
                            String methodPath = normalizePathSlashes(methodRoute.name().trim());
                            WebMethod methodWebFilter = (WebMethod) method.getAnnotation(WebMethod.class);
                            if(methodWebFilter == null)
                                methodWebFilter = classWebFilter;
                            String path = (classPath.toLowerCase()+methodPath.toLowerCase()).replaceAll("/+", "/");
                            String type;
                            if(methodWebFilter != null){
                                type = methodWebFilter.name();
                            }else{
                                type = "GET";
                            }
                            path = normalizePathSlashes(path);
                            WebObject wo = new WebObject(cls.getName(), method.getName(), type, classWebLocked != null || methodWebLocked != null);
                            ROUTES.put(type+path, wo);
                        }else if(method.getAnnotation(WebPathNotFound.class) != null){
                            String type = "HTTP[404]";
                            WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                            ROUTES.put(type, wo);
                            this.config.http.controllerNotFound = wo;
                        }else if(method.getAnnotation(DefaultWebPath.class) != null){
                            String type = "HTTP[???]";
                            WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                            ROUTES.put(type, wo);
                            this.config.http.controllerDefault = wo;
                        }
                    }
                }else if(WebSocketController.class.isAssignableFrom(cls)){
                    WebPath route = (WebPath) cls.getAnnotation(WebPath.class);
                    ArcanoSecret classWebLocked = (ArcanoSecret) cls.getAnnotation(ArcanoSecret.class);
                    if(route != null){
                        String path = normalizePathSlashes(route.name().toLowerCase());
                        String type = "WS";
                        
                        WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                        ROUTES.put(type+path, wo);
                    }else{
                        WebPathNotFound nf = (WebPathNotFound) cls.getAnnotation(WebPathNotFound.class);
                        if(nf != null){
                            String type = "WS[404]";
                            WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                            ROUTES.put(type, wo);
                            this.config.webSocket.controllerNotFound = wo;
                        }
                    }
                }else if(SmtpController.class.isAssignableFrom(cls)){
                    EmailPath route = (EmailPath) cls.getAnnotation(EmailPath.class);
                    ArcanoSecret classWebLocked = (ArcanoSecret) cls.getAnnotation(ArcanoSecret.class);
                    if(route != null){
                        String type = "SMTP";
                        WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                        ROUTES.put(type, wo);
                    }
                }
            } catch (SecurityException | IllegalArgumentException  ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}