package com.github.tncrazvan.arcano;

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
import com.github.tncrazvan.arcano.Bean.Web.HttpMethod;
import com.github.tncrazvan.arcano.Bean.Web.HttpNotFound;
import com.github.tncrazvan.arcano.Bean.Web.HttpPath;
import com.github.tncrazvan.arcano.Bean.Web.HttpDefault;
import com.github.tncrazvan.arcano.Bean.Security.HttpLock;

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
    
    public final void expose(final Class<?>... classes) {
        for (final Class<?> cls : classes) {
            try {
                if (HttpController.class.isAssignableFrom(cls)) {
                    final HttpPath classRoute = (HttpPath) cls.getAnnotation(HttpPath.class);
                    final HttpLock classHttpLocked = (HttpLock) cls.getAnnotation(HttpLock.class);
                    final HttpMethod classWebFilter = (HttpMethod) cls.getAnnotation(HttpMethod.class);
                    final Method[] methods = cls.getDeclaredMethods();
                    for (final Method method : methods) {
                        final HttpPath methodRoute = method.getAnnotation(HttpPath.class);
                        final HttpLock methodHttpLocked = (HttpLock) method.getAnnotation(HttpLock.class);
                        if (methodRoute != null) {
                            final String classPath = normalizePathSlashes(classRoute.name().trim());
                            final String methodPath = normalizePathSlashes(methodRoute.name().trim());
                            HttpMethod methodWebFilter = (HttpMethod) method.getAnnotation(HttpMethod.class);
                            if (methodWebFilter == null)
                                methodWebFilter = classWebFilter;
                            String path = (classPath.toLowerCase() + methodPath.toLowerCase()).replaceAll("/+", "/");
                            String type;
                            if (methodWebFilter != null) {
                                type = methodWebFilter.name();
                            } else {
                                type = "GET";
                            }
                            path = normalizePathSlashes(path);
                            final WebObject wo = new WebObject(
                                cls.getName(), 
                                method.getName(), 
                                type, 
                                classHttpLocked != null || methodHttpLocked != null
                            );
                            ROUTES.put(type + path, wo);
                        }
                        if (method.getAnnotation(HttpNotFound.class) != null) {
                            final String type = "HTTP 404";
                            final WebObject wo = new WebObject(
                                cls.getName(), 
                                method.getName(), 
                                type, 
                                classHttpLocked != null || methodHttpLocked != null
                            );
                            ROUTES.put(type, wo);
                            this.config.http.controllerNotFound = wo;
                        }
                        if (method.getAnnotation(HttpDefault.class) != null) {
                            final String type = "HTTP DEFAULT";
                            final WebObject wo = new WebObject(
                                cls.getName(), 
                                method.getName(), 
                                type, 
                                classHttpLocked != null || methodHttpLocked != null
                            );
                            ROUTES.put(type, wo);
                            this.config.http.controllerDefault = wo;
                        }
                    }
                } else if (WebSocketController.class.isAssignableFrom(cls)) {
                    final HttpPath route = (HttpPath) cls.getAnnotation(HttpPath.class);
                    final HttpLock classWebLocked = (HttpLock) cls.getAnnotation(HttpLock.class);
                    if (route != null) {
                        final String path = normalizePathSlashes(route.name().toLowerCase());
                        final String type = "WS";

                        final WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                        ROUTES.put(type + path, wo);
                    } else {
                        final HttpNotFound nf = (HttpNotFound) cls.getAnnotation(HttpNotFound.class);
                        if (nf != null) {
                            final String type = "WS 404";
                            final WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                            ROUTES.put(type, wo);
                            this.config.webSocket.controllerNotFound = wo;
                        }
                    }
                } else if (SmtpController.class.isAssignableFrom(cls)) {
                    final HttpPath route = (HttpPath) cls.getAnnotation(HttpPath.class);
                    final HttpLock classWebLocked = (HttpLock) cls.getAnnotation(HttpLock.class);
                    if (route != null) {
                        final String type = "SMTP";
                        final WebObject wo = new WebObject(cls.getName(), null, type, classWebLocked != null);
                        ROUTES.put(type, wo);
                    }
                }
            } catch (SecurityException | IllegalArgumentException  ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}