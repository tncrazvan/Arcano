package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Bean.Default;
import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Bean.WebLocked;
import com.github.tncrazvan.arcano.Tool.Minifier;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import com.google.gson.JsonObject;
import java.util.concurrent.ThreadPoolExecutor;
import com.github.tncrazvan.arcano.Bean.WebMethod;
import com.github.tncrazvan.arcano.Bean.WebPath;
import com.github.tncrazvan.arcano.Http.HttpSessionManager;
import com.github.tncrazvan.arcano.Smtp.SmtpController;
import com.github.tncrazvan.arcano.Tool.Status;
import com.github.tncrazvan.arcano.Tool.Strings;
import static com.github.tncrazvan.arcano.Tool.Strings.normalizePathSlashes;

/**
 * 
 * @author Razvan
 */
public abstract class SharedObject implements Strings{
    //SESSIONS
    public final HttpSessionManager sessions = new HttpSessionManager();
    //THREADS
    public ThreadPoolExecutor executor = null;
    //CONFIGURATION OBJECTS
    public final Configuration config = new Configuration();
    public JsonObject mainSettings = null;
    public Minifier minifier = null;
    public static final String NO_COMPRESSION="",DEFLATE="deflate",GZIP="gzip";
    //CONFIGURATION VALUES
    public boolean responseWrapper = false;
    public boolean sendExceptions = true;
    public boolean listen = true;
    public boolean groupsAllowed = false;
    public boolean smtpAllowed = false;
    public long sessionTtl = 1440; //24 minutes
    public int port = 80;
    public int timeout = 30000;
    public int threadPoolSize = 2;
    public int minify = 0;
    public int webSocketGroupMaxConnections = 10;
    public int webSocketMtu = 65536;
    public int httpMtu = 65536;
    public String[] compression = new String[0];
    public String[] classOrder = new String[0];
    public HashMap<String,String> headers = new HashMap<String,String>(){{
        put("@Status",Status.STATUS_SUCCESS);
    }};
    public HashMap<String,String> cluster = new HashMap<String,String>(){{}};
    public String configDir = "./http.json";
    public String jwtSecret = "eswtrweqtr3w25trwes4tyw456t";
    public String assets = "../webapp/assets.json";
    public String webRoot = "www";
    public String serverRoot = "server";
    public String charset = "UTF-8";
    public String bindAddress = "::";
    public String httpDefaultNameOriginal = null;
    public String httpNotFoundNameOriginal = null;
    public String webSocketNotFoundNameOriginal = null;
    public String httpDefaultName = httpDefaultNameOriginal;
    public String httpNotFoundName = httpNotFoundNameOriginal;
    public String webSocketNotFoundName = webSocketNotFoundNameOriginal;
    public String entryPoint = "/index.html";
    //SYSTEM RUNTIME
    public static final Runtime RUNTIME = Runtime.getRuntime();
    //ROUTING
    public static final HashMap<String, WebObject> ROUTES = new HashMap<>();
    //LOCALE & DATES
    public static final Calendar CALENDAR = Calendar.getInstance();
    public static final Date DATE = new Date();
    public static Locale locale = Locale.getDefault();
    public static ZoneId timezone = ZoneId.systemDefault();
    public static ZoneId londonTimezone = ZoneId.of("Europe/London");
    public static DateTimeFormatter formatHttpDefaultDate = DateTimeFormatter.ofPattern("EEE, d MMM y HH:mm:ss z", locale).withZone(timezone);
    //LOGGING
    public static final Logger LOGGER = Logger.getLogger(SharedObject.class.getName());
    //WEBSOCKETS OBJECTS
    public final Map<String,ArrayList<WebSocketEvent>> WEB_SOCKET_EVENTS = new HashMap<>();
    public static final String WEBSOCKET_ACCEPT_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    //SERVER STATUS
    public boolean isRunning = false;
    //ENCODING & DECODING
    public static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    public static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    
    public void expose(Class<?>... classes) {
        for(Class<?> cls : classes){
            try {
                if(HttpController.class.isAssignableFrom(cls)){
                    WebPath classRoute = (WebPath) cls.getAnnotation(WebPath.class);
                    WebLocked webLocked = (WebLocked) cls.getAnnotation(WebLocked.class);
                    WebMethod classWebFilter = (WebMethod) cls.getAnnotation(WebMethod.class);
                    Method[] methods = cls.getDeclaredMethods();
                    for(Method method : methods){
                        WebPath methodRoute = method.getAnnotation(WebPath.class);
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
                            if(webLocked == null)
                                webLocked = (WebLocked) method.getAnnotation(WebLocked.class);
                            path = normalizePathSlashes(path);
                            WebObject wo = new WebObject(cls.getName(), method.getName(), type, webLocked != null);
                            ROUTES.put(type+path, wo);
                        }else if(cls.getAnnotation(NotFound.class) != null){
                            if(httpNotFoundNameOriginal == null)
                                httpNotFoundNameOriginal = cls.getName();
                            httpNotFoundName = cls.getName();
                        }else if(cls.getAnnotation(Default.class) != null){
                            if(httpDefaultNameOriginal == null)
                                httpDefaultNameOriginal = cls.getName();
                            httpDefaultName = cls.getName();
                        }
                    }
                }else if(WebSocketController.class.isAssignableFrom(cls)){
                    WebPath route = (WebPath) cls.getAnnotation(WebPath.class);
                    WebLocked webLocked = (WebLocked) cls.getAnnotation(WebLocked.class);
                    WebMethod webFilter = (WebMethod) cls.getAnnotation(WebMethod.class);
                    if(route != null){
                        String path = normalizePathSlashes(route.name().toLowerCase());
                        String type;
                        if(webFilter != null){
                            type = webFilter.name().toUpperCase();
                        }else{
                            type = "GET";
                        }
                        WebObject wo = new WebObject(cls.getName(), null, type, webLocked != null);
                        wo.setHttpMethod("WS");
                        ROUTES.put("WS"+path, wo);
                    }else{
                        NotFound nf = (NotFound) cls.getAnnotation(NotFound.class);
                        if(nf != null){
                            if(webSocketNotFoundNameOriginal == null)
                                webSocketNotFoundNameOriginal = cls.getName();
                            webSocketNotFoundName = cls.getName();
                        }
                    }
                }else if(SmtpController.class.isAssignableFrom(cls)){
                    WebPath classRoute = (WebPath) cls.getAnnotation(WebPath.class);
                    WebLocked webLocked = (WebLocked) cls.getAnnotation(WebLocked.class);
                    WebMethod classWebFilter = (WebMethod) cls.getAnnotation(WebMethod.class);
                    Method[] methods = cls.getDeclaredMethods();
                    for(Method method : methods){
                        WebPath methodRoute = method.getAnnotation(WebPath.class);
                        if(methodRoute != null){
                            String classPath = normalizePathSlashes(classRoute.name().trim());
                            String methodPath = normalizePathSlashes(methodRoute.name().trim());
                            WebMethod methodWebFilter = (WebMethod) method.getAnnotation(WebMethod.class);
                            if(methodWebFilter == null)
                                methodWebFilter = classWebFilter;
                            String path = (classPath.toLowerCase()+methodPath.toLowerCase()).replaceAll("/+", "/");
                            String type = "SMTP";
                            if(webLocked == null)
                                webLocked = (WebLocked) method.getAnnotation(WebLocked.class);
                            path = normalizePathSlashes(path);
                            WebObject wo = new WebObject(cls.getName(), method.getName(), type, webLocked != null);
                            ROUTES.put(type+path, wo);
                        }else if(cls.getAnnotation(NotFound.class) != null){
                            if(httpNotFoundNameOriginal == null)
                                httpNotFoundNameOriginal = cls.getName();
                            httpNotFoundName = cls.getName();
                        }else if(cls.getAnnotation(Default.class) != null){
                            if(httpDefaultNameOriginal == null)
                                httpDefaultNameOriginal = cls.getName();
                            httpDefaultName = cls.getName();
                        }
                    }
                }
            } catch (SecurityException | IllegalArgumentException  ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}