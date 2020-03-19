package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Bean.Email.SmtpService;
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
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.util.concurrent.ExecutorService;
import com.github.tncrazvan.arcano.Bean.WebSocket.WebSocketNotFound;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;
import com.github.tncrazvan.arcano.Bean.WebSocket.WebSocketService;
import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;
import com.github.tncrazvan.arcano.Tool.Http.Status;
import static com.github.tncrazvan.arcano.Tool.Strings.normalizePathSlashes;
import com.github.tncrazvan.arcano.Bean.Http.HttpServiceNotFound;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEventManager;
import java.util.Arrays;
import java.util.LinkedList;

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
    public static final String NO_COMPRESSION="",DEFLATE="deflate",GZIP="gzip";
    //SYSTEM RUNTIME & PROCESS BUILDERS
    public static final Runtime RUNTIME = Runtime.getRuntime();
    
    public final HashMap<String,WebSocketEventManager> webSocketEventManager = new HashMap<>();
    public final LinkedList<WebSocketEventManager> oldWebSocketEventManager = new LinkedList();
    
    public static final ProcessBuilder PROCESS_BUILDER = new ProcessBuilder();
    //ROUTING
    //public static final HashMap<String, WebObject> ROUTES = new HashMap<>();
    
    //HTTP ROUTES
    public final HashMap<String, HashMap<String,WebObject>> HTTP_ROUTES = new HashMap<String, HashMap<String,WebObject>>(){{
        put("GET", new HashMap<String,WebObject>(){{}});
        put("HEAD", new HashMap<String,WebObject>(){{}});
        put("POST", new HashMap<String,WebObject>(){{}});
        put("PUT", new HashMap<String,WebObject>(){{}});
        put("DELETE", new HashMap<String,WebObject>(){{}});
        put("CONNECT", new HashMap<String,WebObject>(){{}});
        put("CONNECT", new HashMap<String,WebObject>(){{}});
        put("PATCH", new HashMap<String,WebObject>(){{}});
    }};
    
    //HTTP SPECIAL ROUTES
    public final HashMap<String, WebObject> HTTP_SPECIAL_ROUTES_404 = new HashMap<String, WebObject>(){{
        put("GET", null);
        put("HEAD", null);
        put("POST", null);
        put("PUT", null);
        put("DELETE", null);
        put("CONNECT", null);
        put("CONNECT", null);
        put("PATCH", null);
    }};
    
    public WebObject SMTP_ROUTE = null;
    
    public final HashMap<String, WebObject> WEB_SOCKET_ROUTES = new HashMap<String,WebObject>(){{}};
    public WebObject WEB_SOCKET_ROUTES_NOT_FOUND = null;
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
    public static final HttpResponse RESPONSE_EMPTY = new HttpResponse("").resolve();
    public static final HttpResponse RESPONSE_NOT_FOUND = new HttpResponse(new HttpHeaders(){{
        set("@Status", Status.STATUS_NOT_FOUND);
        set("Content-Length", "0");
    }},"").resolve();
    //DEFAULT PROJECT NAMES
    public static final String NAME_SESSION_ID = "JavaSessionID";
    public final void expose(String type,CompleteAction<Object,HttpEvent>  action){
        expose(type, null, action);
    }
    public final void expose404(String type,CompleteAction<Object,HttpEvent>  action){
        if(HTTP_SPECIAL_ROUTES_404.containsKey(type))
            HTTP_SPECIAL_ROUTES_404.put(type, new WebObject(
                action,
                null,
                null
            ));
    }
    public final void expose(String type, String path,CompleteAction<Object,HttpEvent>  action){
        if(HTTP_ROUTES.containsKey(type))
            HTTP_ROUTES
                .get(type)
                    .put(normalizePathSlashes(path), new WebObject(
                        action,
                        null,
                        null
                    ));
    }
    public final void expose(Class<?>... classes) {
        for (Class<?> cls : classes) {
            try {
                if (HttpController.class.isAssignableFrom(cls)) {
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                    	HttpService httpService = method.getAnnotation(HttpService.class);
                    	HttpService classService = cls.getAnnotation(HttpService.class);
                        if (httpService != null) {
                            String[] types = httpService.method();
                            if(types.length == 1 && types[0].equals("")){
                                if(classService != null){
                                    types = classService.method();
                                    if(types.length == 1 && types[0].equals(""))
                                        types[0] = "GET";
                                }else{
                                    types[0] = "GET";
                                }
                            }
                            WebObject wo = new WebObject(null,cls.getName(),method.getName());
                            String 
                                    path = (classService != null && !classService.path().equals("/")?classService.path().toLowerCase():"");
                                    path += httpService.path().toLowerCase().startsWith("/")?httpService.path().toLowerCase():"/"+httpService.path().toLowerCase();
                            if(Arrays.asList(types).contains("*")){
                                for (Map.Entry<String, HashMap<String, WebObject>> t : HTTP_ROUTES.entrySet()) {
                                    t
                                        .getValue()
                                            .put(path, wo);
                                }
                            } else for(int i = 0;i < types.length;i++){
                                if(HTTP_ROUTES.containsKey(types[i]))
                                    HTTP_ROUTES
                                        .get(types[i])
                                            .put(path, wo);
                            }
                        }
                        HttpServiceNotFound httpServiceNotFound = method.getAnnotation(HttpServiceNotFound.class);
                        if (httpServiceNotFound != null) {
                            String[] types = httpServiceNotFound.method();
                            if(types.length == 1 && types[0].equals(""))
                                types[0] = "GET";
                            
                            WebObject wo = new WebObject(null,cls.getName(),method.getName());
                            
                            if(Arrays.asList(types).contains("*")){
                                for (Map.Entry<String, WebObject> route404 : HTTP_SPECIAL_ROUTES_404.entrySet()) {
                                    route404.setValue(wo);
                                }
                            }else for(int i = 0;i < types.length;i++){
                                if(HTTP_SPECIAL_ROUTES_404.containsKey(types[i]))
                                    HTTP_SPECIAL_ROUTES_404
                                        .put(types[i], wo);
                            }
                        }
                    }
                } else if (WebSocketController.class.isAssignableFrom(cls)) {
                    WebSocketService webSocketService = (WebSocketService) cls.getAnnotation(WebSocketService.class);
                    WebObject wo = new WebObject(null, cls.getName(), null);
                    if (webSocketService != null){
                        String path = (webSocketService != null?webSocketService.path().toLowerCase():"");
                        WEB_SOCKET_ROUTES.put(path, wo);
                    }
                    if (cls.getAnnotation(WebSocketNotFound.class) != null)
                        WEB_SOCKET_ROUTES_NOT_FOUND = wo;
                } else if (SmtpController.class.isAssignableFrom(cls)) {
                    SmtpService smtpService = (SmtpService) cls.getAnnotation(SmtpService.class);
                    if(smtpService != null)
                        SMTP_ROUTE = new WebObject(null,cls.getName(), null);
                }
            } catch (SecurityException | IllegalArgumentException  ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}