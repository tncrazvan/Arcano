package com.github.tncrazvan.arcano.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.github.tncrazvan.arcano.Common;
import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.Tool.JavaScript.LoaderEventListener;
import com.github.tncrazvan.arcano.Tool.JavaScript.LoaderJSLog;
import com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem.LoaderJSFile;
import com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem.LoaderJSFileMaker;
import com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem.LoaderJSZip;
import com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem.LoaderJSZipMaker;
import com.github.tncrazvan.arcano.Tool.JavaScript.Http.LoaderJSHttp;
import com.github.tncrazvan.arcano.Tool.JavaScript.Http.LoaderJSHttpResult;
import com.github.tncrazvan.arcano.Tool.JavaScript.MySQL.LoaderJSMySQLConnection;
import com.github.tncrazvan.arcano.Tool.JavaScript.MySQL.LoaderJSMySQLConnector;
import com.github.tncrazvan.arcano.Tool.JavaScript.System.LoaderJSSleep;
import com.github.tncrazvan.arcano.Tool.JavaScript.System.LoaderJSThread;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;

/**
 *
 * @author Administrator
 */
public class JavaScriptExecutor extends Common 
        implements LoaderJSFile,LoaderJSFileMaker,LoaderJSLog,LoaderJSHttpResult,LoaderJSHttp,LoaderJSZip,LoaderJSZipMaker,
        LoaderJSSleep,LoaderJSThread,LoaderJSMySQLConnection,LoaderJSMySQLConnector,LoaderEventListener{
    private final static String NASHORN_ARGS = "nashorn.args";
    private final static String ES_6 = "--language=es6 --no-deprecation-warning";
    
    //Http
    public void execute(final HttpEvent e, final ScriptContext context, final String filename, final String[] args,
            final byte[] input) throws ScriptException, IOException {
        final ScriptEngineManager mgr = new ScriptEngineManager();
        if (js == null) {
            System.setProperty(NASHORN_ARGS, ES_6);
            js = mgr.getEngineByName("nashorn");
        }
        eval(e, context, filename, args, input);
    }

    private static String tail(final String dirname) {
        return "\nfunction header(key,value){server.setHeaderField(key,value);}function status(value){header(\"@Status\",value);}function send(data){try{data=JSON.parse(data);}catch(e){}server.send(data);}\nfunction require(filename){load('"
                + dirname.replace("\\", "/") + "/'+filename);}function echo(value){return send(value);}\nmain();";
    }

    // Http
    private void eval(final HttpEvent e, final ScriptContext context, final String filename, final String[] args,
            final byte[] input) throws ScriptException, IOException {
        final String dirname = Path.of(filename).getParent().toString();
        e.setStatus(STATUS_SUCCESS);
        final String script = Files.readString(Path.of(filename));
        if (context != null)
            js.setContext(context);

        final Common c = this;

        js.eval("function main(){" + script + "}" + tail(dirname), new SimpleBindings(new HashMap<String, Object>() {
            {
                put("args", args);
                put("log", new JSLog());
                put("method", e.getMethod());
                put("input", new JSHttpResult(input));
                put("server", e);
                put("mysql", new JSMySQLConnector());
                put("file", new JSFileMaker(dirname));
                put("thread", new JSThread());
                put("sleep", new JSSleep());
                put("zip", new JSZipMaker(dirname));
                put("http", new JSHttp());

                // INFORMATINOAL RESPONSES
                put("STATUS_CONTINUE", STATUS_CONTINUE);
                put("STATUS_SWITCHING_PROTOCOLS", STATUS_SWITCHING_PROTOCOLS);
                put("STATUS_PROCESSING", STATUS_PROCESSING);

                // SUCCESS
                put("STATUS_SUCCESS", STATUS_SUCCESS);
                put("STATUS_CREATED", STATUS_CREATED);
                put("STATUS_ACCEPTED", STATUS_ACCEPTED);
                put("STATUS_NON_AUTHORITATIVE_INFORMATION", STATUS_NON_AUTHORITATIVE_INFORMATION);
                put("STATUS_NO_CONTENT", STATUS_NO_CONTENT);
                put("STATUS_RESET_CONTENT", STATUS_RESET_CONTENT);
                put("STATUS_PARTIAL_CONTENT", STATUS_PARTIAL_CONTENT);
                put("STATUS_MULTI_STATUS", STATUS_MULTI_STATUS);
                put("STATUS_ALREADY_REPORTED", STATUS_ALREADY_REPORTED);
                put("STATUS_IM_USED", STATUS_IM_USED);

                // REDIRECTIONS
                put("STATUS_MULTIPLE_CHOICES", STATUS_MULTIPLE_CHOICES);
                put("STATUS_MOVED_PERMANENTLY", STATUS_MOVED_PERMANENTLY);
                put("STATUS_FOUND", STATUS_FOUND);
                put("STATUS_SEE_OTHER", STATUS_SEE_OTHER);
                put("STATUS_NOT_MODIFIED", STATUS_NOT_MODIFIED);
                put("STATUS_USE_PROXY", STATUS_USE_PROXY);
                put("STATUS_SWITCH_PROXY", STATUS_SWITCH_PROXY);
                put("STATUS_TEMPORARY_REDIRECT", STATUS_TEMPORARY_REDIRECT);
                put("STATUS_PERMANENT_REDIRECT", STATUS_PERMANENT_REDIRECT);

                // CLIENT ERRORS
                put("STATUS_BAD_REQUEST", STATUS_BAD_REQUEST);
                put("STATUS_UNAUTHORIZED", STATUS_UNAUTHORIZED);
                put("STATUS_PAYMENT_REQUIRED", STATUS_PAYMENT_REQUIRED);
                put("STATUS_FORBIDDEN", STATUS_FORBIDDEN);
                put("STATUS_NOT_FOUND", STATUS_NOT_FOUND);
                put("STATUS_METHOD_NOT_ALLOWED", STATUS_METHOD_NOT_ALLOWED);
                put("STATUS_NOT_ACCEPTABLE", STATUS_NOT_ACCEPTABLE);
                put("STATUS_PROXY_AUTHENTICATION_REQUIRED", STATUS_PROXY_AUTHENTICATION_REQUIRED);
                put("STATUS_REQUEST_TIMEOUT", STATUS_REQUEST_TIMEOUT);
                put("STATUS_CONFLICT", STATUS_CONFLICT);
                put("STATUS_GONE", STATUS_GONE);
                put("STATUS_LENGTH_REQUIRED", STATUS_LENGTH_REQUIRED);
                put("STATUS_PRECONDITION_FAILED", STATUS_PRECONDITION_FAILED);
                put("STATUS_PAYLOAD_TOO_LARGE", STATUS_PAYLOAD_TOO_LARGE);
                put("STATUS_URI_TOO_LONG", STATUS_URI_TOO_LONG);
                put("STATUS_UNSUPPORTED_MEDIA_TYPE", STATUS_UNSUPPORTED_MEDIA_TYPE);
                put("STATUS_RANGE_NOT_SATISFIABLE", STATUS_RANGE_NOT_SATISFIABLE);
                put("STATUS_EXPECTATION_FAILED", STATUS_EXPECTATION_FAILED);
                put("STATUS_IM_A_TEAPOT", STATUS_IM_A_TEAPOT);
                put("STATUS_MISDIRECTED_REQUEST", STATUS_MISDIRECTED_REQUEST);
                put("STATUS_UNPROCESSABLE_ENTITY", STATUS_UNPROCESSABLE_ENTITY);
                put("STATUS_LOCKED", STATUS_LOCKED);
                put("STATUS_FAILED_DEPENDENCY", STATUS_FAILED_DEPENDENCY);
                put("STATUS_UPGRADE_REQUIRED", STATUS_UPGRADE_REQUIRED);
                put("STATUS_PRECONDITION_REQUIRED", STATUS_PRECONDITION_REQUIRED);
                put("STATUS_TOO_MANY_REQUESTS", STATUS_TOO_MANY_REQUESTS);
                put("STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE", STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE);
                put("STATUS_UNAVAILABLE_FOR_LEGAL_REASONS", STATUS_UNAVAILABLE_FOR_LEGAL_REASONS);

                // SERVER ERRORS
                put("STATUS_INTERNAL_SERVER_ERROR", STATUS_INTERNAL_SERVER_ERROR);
                put("STATUS_NOT_IMPLEMENTED", STATUS_NOT_IMPLEMENTED);
                put("STATUS_BAD_GATEWAY", STATUS_BAD_GATEWAY);
                put("STATUS_SERVICE_UNAVAILABLE", STATUS_SERVICE_UNAVAILABLE);
                put("STATUS_GATEWAY_TIMEOUT", STATUS_GATEWAY_TIMEOUT);
                put("STATUS_HTTP_VERSION_NOT_SUPPORTED", STATUS_HTTP_VERSION_NOT_SUPPORTED);
                put("STATUS_VARIANT_ALSO_NEGOTATIES", STATUS_VARIANT_ALSO_NEGOTATIES);
                put("STATUS_INSUFFICIENT_STORAGE", STATUS_INSUFFICIENT_STORAGE);
                put("STATUS_LOOP_DETECTED", STATUS_LOOP_DETECTED);
                put("STATUS_NOT_EXTENDED", STATUS_NOT_EXTENDED);
                put("STATUS_NETWORK_AUTHENTICATION_REQUIRED", STATUS_NETWORK_AUTHENTICATION_REQUIRED);
            }
        }));
        e.send("");
    }

    // WebSockets
    public void execute(final WebSocketEvent e, final ScriptContext context, final String filename, final String[] args)
            throws ScriptException, IOException {
        final ScriptEngineManager mgr = new ScriptEngineManager();
        if (js == null) {
            System.setProperty(NASHORN_ARGS, ES_6);
            js = mgr.getEngineByName("nashorn");
        }
        eval(e, context, filename, args);
    }

    public EventListener<Void> onOpen;
    public EventListener<String> onMessage;
    public EventListener<Void> onClose;

    // WebSockets
    private void eval(final WebSocketEvent e, final ScriptContext context, final String filename, final String[] args)
            throws ScriptException, IOException {
        final String dirname = Path.of(filename).getParent().toString();
        final String script = Files.readString(Path.of(filename));
        if (context != null)
            js.setContext(context);

        onOpen = new EventListener<>();
        onMessage = new EventListener<>();
        onClose = new EventListener<>();
        
        js.eval("function main(){"+script+"}"+tail(dirname),new SimpleBindings(
            new HashMap<String,Object>(){{
                put("args",args);
                put("log",new JSLog());
                put("server",e);
                put("file",new JSFileMaker(dirname));
                put("mysql",new JSMySQLConnector());
                put("onOpen",onOpen);
                put("onMessage",onMessage);
                put("onClose",onClose);
                put("thread",new JSThread());
                put("sleep",new JSSleep());
                put("zip",new JSZipMaker(dirname));
                put("http",new JSHttp());
                put("fromBytesToString",new JSHttp());
                
                //INFORMATINOAL RESPONSES
                put("STATUS_CONTINUE",STATUS_CONTINUE);
                put("STATUS_SWITCHING_PROTOCOLS",STATUS_SWITCHING_PROTOCOLS);
                put("STATUS_PROCESSING",STATUS_PROCESSING);

                //SUCCESS
                put("STATUS_SUCCESS",STATUS_SUCCESS);
                put("STATUS_CREATED",STATUS_CREATED);
                put("STATUS_ACCEPTED",STATUS_ACCEPTED);
                put("STATUS_NON_AUTHORITATIVE_INFORMATION",STATUS_NON_AUTHORITATIVE_INFORMATION);
                put("STATUS_NO_CONTENT",STATUS_NO_CONTENT);
                put("STATUS_RESET_CONTENT",STATUS_RESET_CONTENT);
                put("STATUS_PARTIAL_CONTENT",STATUS_PARTIAL_CONTENT);
                put("STATUS_MULTI_STATUS",STATUS_MULTI_STATUS);
                put("STATUS_ALREADY_REPORTED",STATUS_ALREADY_REPORTED);
                put("STATUS_IM_USED",STATUS_IM_USED);

                //REDIRECTIONS
                put("STATUS_MULTIPLE_CHOICES",STATUS_MULTIPLE_CHOICES);
                put("STATUS_MOVED_PERMANENTLY",STATUS_MOVED_PERMANENTLY);
                put("STATUS_FOUND",STATUS_FOUND);
                put("STATUS_SEE_OTHER",STATUS_SEE_OTHER);
                put("STATUS_NOT_MODIFIED",STATUS_NOT_MODIFIED);
                put("STATUS_USE_PROXY",STATUS_USE_PROXY);
                put("STATUS_SWITCH_PROXY",STATUS_SWITCH_PROXY);
                put("STATUS_TEMPORARY_REDIRECT",STATUS_TEMPORARY_REDIRECT);
                put("STATUS_PERMANENT_REDIRECT",STATUS_PERMANENT_REDIRECT);

                //CLIENT ERRORS
                put("STATUS_BAD_REQUEST",STATUS_BAD_REQUEST);
                put("STATUS_UNAUTHORIZED",STATUS_UNAUTHORIZED);
                put("STATUS_PAYMENT_REQUIRED",STATUS_PAYMENT_REQUIRED);
                put("STATUS_FORBIDDEN",STATUS_FORBIDDEN);
                put("STATUS_NOT_FOUND",STATUS_NOT_FOUND);
                put("STATUS_METHOD_NOT_ALLOWED",STATUS_METHOD_NOT_ALLOWED);
                put("STATUS_NOT_ACCEPTABLE",STATUS_NOT_ACCEPTABLE);
                put("STATUS_PROXY_AUTHENTICATION_REQUIRED",STATUS_PROXY_AUTHENTICATION_REQUIRED);
                put("STATUS_REQUEST_TIMEOUT",STATUS_REQUEST_TIMEOUT);
                put("STATUS_CONFLICT",STATUS_CONFLICT);
                put("STATUS_GONE",STATUS_GONE);
                put("STATUS_LENGTH_REQUIRED",STATUS_LENGTH_REQUIRED);
                put("STATUS_PRECONDITION_FAILED",STATUS_PRECONDITION_FAILED);
                put("STATUS_PAYLOAD_TOO_LARGE",STATUS_PAYLOAD_TOO_LARGE);
                put("STATUS_URI_TOO_LONG",STATUS_URI_TOO_LONG);
                put("STATUS_UNSUPPORTED_MEDIA_TYPE",STATUS_UNSUPPORTED_MEDIA_TYPE);
                put("STATUS_RANGE_NOT_SATISFIABLE",STATUS_RANGE_NOT_SATISFIABLE);
                put("STATUS_EXPECTATION_FAILED",STATUS_EXPECTATION_FAILED);
                put("STATUS_IM_A_TEAPOT",STATUS_IM_A_TEAPOT);
                put("STATUS_MISDIRECTED_REQUEST",STATUS_MISDIRECTED_REQUEST);
                put("STATUS_UNPROCESSABLE_ENTITY",STATUS_UNPROCESSABLE_ENTITY);
                put("STATUS_LOCKED",STATUS_LOCKED);
                put("STATUS_FAILED_DEPENDENCY",STATUS_FAILED_DEPENDENCY);
                put("STATUS_UPGRADE_REQUIRED",STATUS_UPGRADE_REQUIRED);
                put("STATUS_PRECONDITION_REQUIRED",STATUS_PRECONDITION_REQUIRED);
                put("STATUS_TOO_MANY_REQUESTS",STATUS_TOO_MANY_REQUESTS);
                put("STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE",STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE);
                put("STATUS_UNAVAILABLE_FOR_LEGAL_REASONS",STATUS_UNAVAILABLE_FOR_LEGAL_REASONS);

                //SERVER ERRORS
                put("STATUS_INTERNAL_SERVER_ERROR",STATUS_INTERNAL_SERVER_ERROR);
                put("STATUS_NOT_IMPLEMENTED",STATUS_NOT_IMPLEMENTED);
                put("STATUS_BAD_GATEWAY",STATUS_BAD_GATEWAY);
                put("STATUS_SERVICE_UNAVAILABLE",STATUS_SERVICE_UNAVAILABLE);
                put("STATUS_GATEWAY_TIMEOUT",STATUS_GATEWAY_TIMEOUT);
                put("STATUS_HTTP_VERSION_NOT_SUPPORTED",STATUS_HTTP_VERSION_NOT_SUPPORTED);
                put("STATUS_VARIANT_ALSO_NEGOTATIES",STATUS_VARIANT_ALSO_NEGOTATIES);
                put("STATUS_INSUFFICIENT_STORAGE",STATUS_INSUFFICIENT_STORAGE);
                put("STATUS_LOOP_DETECTED",STATUS_LOOP_DETECTED);
                put("STATUS_NOT_EXTENDED",STATUS_NOT_EXTENDED);
                put("STATUS_NETWORK_AUTHENTICATION_REQUIRED",STATUS_NETWORK_AUTHENTICATION_REQUIRED);
            }}
        ));
        e.send("");
    }
}
