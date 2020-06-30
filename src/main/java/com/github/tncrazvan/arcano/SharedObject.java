package com.github.tncrazvan.arcano;

import static com.github.tncrazvan.arcano.tool.Strings.normalizePathSlashes;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import com.github.tncrazvan.arcano.bean.email.SmtpService;
import com.github.tncrazvan.arcano.bean.websocket.WebSocketControllerNotFound;
import com.github.tncrazvan.arcano.bean.websocket.WebSocketService;
import com.github.tncrazvan.arcano.http.HttpEvent;
import com.github.tncrazvan.arcano.http.HttpHeaders;
import com.github.tncrazvan.arcano.http.HttpResponse;
import com.github.tncrazvan.arcano.http.HttpSessionManager;
import com.github.tncrazvan.arcano.smtp.SmtpController;
import com.github.tncrazvan.arcano.tool.Strings;
import com.github.tncrazvan.arcano.tool.action.CompleteAction;
import com.github.tncrazvan.arcano.tool.action.HttpEventAction;
import com.github.tncrazvan.arcano.tool.http.Status;
import com.github.tncrazvan.arcano.websocket.WebSocketController;
import com.github.tncrazvan.arcano.websocket.WebSocketEvent;
import com.github.tncrazvan.arcano.websocket.WebSocketEventManager;

/**
 * 
 * @author Razvan Tanase
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
    public static final HttpResponse RESPONSE_NOT_FOUND = new HttpResponse(
            HttpHeaders
                    .response(Status.STATUS_NOT_FOUND)
                    .set("Content-Length", "0"),
            ""
    ).resolve();
    //DEFAULT PROJECT NAMES
    public static final String NAME_SESSION_ID = "JavaSessionID";
    
    
    public final SharedObject addHttpEventListener(String[] types,HttpEventAction<Object> action){
        for(String type : types){
            addHttpEventListener(type, action);
        }
        return this;
    }
    
    public final SharedObject addHttpEventListener(String[] types, String path,HttpEventAction<Object> action){
        for(String type : types){
            addHttpEventListener(type,path,action);
        }
        return this;
    }
    public final SharedObject addHttpEventListener(String type,HttpEventAction<Object> action){
        return addHttpEventListener(type, null, action);
    }
    public final SharedObject addHttpEventListener(String type, String path,HttpEventAction<Object> action){
        if(type.equals("*")){
            WebObject wo = new WebObject(action,null,null);
            wo.setPath(path);
            for (Map.Entry<String, HashMap<String, WebObject>> mtd : HTTP_ROUTES.entrySet()) {
                mtd.getValue().put(normalizePathSlashes(path), wo);
            }
        }else if(HTTP_ROUTES.containsKey(type)){
            WebObject wo = new WebObject(action,null,null);
            wo.setPath(path);
            HTTP_ROUTES
                .get(type)
                    .put(normalizePathSlashes(path), wo);
        }
        return this;
    }

    public final SharedObject addWebSocketEventListener(Class<WebSocketController> cls) {
        WebSocketService webSocketService = (WebSocketService) cls.getAnnotation(WebSocketService.class);
        WebObject wo = new WebObject(null, cls.getName(), null);
        if (webSocketService != null){
            String path = (webSocketService != null?webSocketService.path().toLowerCase():"");
            WEB_SOCKET_ROUTES.put(path, wo);
        }
        if (cls.getAnnotation(WebSocketControllerNotFound.class) != null)
            WEB_SOCKET_ROUTES_NOT_FOUND = wo;
        return this;
    }

    public final SharedObject addSmtpEventListener(Class<SmtpController> cls) {
        SmtpService smtpService = (SmtpService) cls.getAnnotation(SmtpService.class);
        if(smtpService != null)
            SMTP_ROUTE = new WebObject(null,cls.getName(), null);
        return this;
    }
}