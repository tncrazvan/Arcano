/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import com.github.tncrazvan.arcano.Common;
import static com.github.tncrazvan.arcano.Common.STATUS_ACCEPTED;
import static com.github.tncrazvan.arcano.Common.STATUS_ALREADY_REPORTED;
import static com.github.tncrazvan.arcano.Common.STATUS_BAD_GATEWAY;
import static com.github.tncrazvan.arcano.Common.STATUS_BAD_REQUEST;
import static com.github.tncrazvan.arcano.Common.STATUS_CONFLICT;
import static com.github.tncrazvan.arcano.Common.STATUS_CONTINUE;
import static com.github.tncrazvan.arcano.Common.STATUS_CREATED;
import static com.github.tncrazvan.arcano.Common.STATUS_EXPECTATION_FAILED;
import static com.github.tncrazvan.arcano.Common.STATUS_FAILED_DEPENDENCY;
import static com.github.tncrazvan.arcano.Common.STATUS_FORBIDDEN;
import static com.github.tncrazvan.arcano.Common.STATUS_FOUND;
import static com.github.tncrazvan.arcano.Common.STATUS_GATEWAY_TIMEOUT;
import static com.github.tncrazvan.arcano.Common.STATUS_GONE;
import static com.github.tncrazvan.arcano.Common.STATUS_HTTP_VERSION_NOT_SUPPORTED;
import static com.github.tncrazvan.arcano.Common.STATUS_IM_A_TEAPOT;
import static com.github.tncrazvan.arcano.Common.STATUS_IM_USED;
import static com.github.tncrazvan.arcano.Common.STATUS_INSUFFICIENT_STORAGE;
import static com.github.tncrazvan.arcano.Common.STATUS_INTERNAL_SERVER_ERROR;
import static com.github.tncrazvan.arcano.Common.STATUS_LENGTH_REQUIRED;
import static com.github.tncrazvan.arcano.Common.STATUS_LOCKED;
import static com.github.tncrazvan.arcano.Common.STATUS_LOOP_DETECTED;
import static com.github.tncrazvan.arcano.Common.STATUS_METHOD_NOT_ALLOWED;
import static com.github.tncrazvan.arcano.Common.STATUS_MISDIRECTED_REQUEST;
import static com.github.tncrazvan.arcano.Common.STATUS_MOVED_PERMANENTLY;
import static com.github.tncrazvan.arcano.Common.STATUS_MULTIPLE_CHOICES;
import static com.github.tncrazvan.arcano.Common.STATUS_MULTI_STATUS;
import static com.github.tncrazvan.arcano.Common.STATUS_NETWORK_AUTHENTICATION_REQUIRED;
import static com.github.tncrazvan.arcano.Common.STATUS_NON_AUTHORITATIVE_INFORMATION;
import static com.github.tncrazvan.arcano.Common.STATUS_NOT_ACCEPTABLE;
import static com.github.tncrazvan.arcano.Common.STATUS_NOT_EXTENDED;
import static com.github.tncrazvan.arcano.Common.STATUS_NOT_FOUND;
import static com.github.tncrazvan.arcano.Common.STATUS_NOT_IMPLEMENTED;
import static com.github.tncrazvan.arcano.Common.STATUS_NOT_MODIFIED;
import static com.github.tncrazvan.arcano.Common.STATUS_NO_CONTENT;
import static com.github.tncrazvan.arcano.Common.STATUS_PARTIAL_CONTENT;
import static com.github.tncrazvan.arcano.Common.STATUS_PAYLOAD_TOO_LARGE;
import static com.github.tncrazvan.arcano.Common.STATUS_PAYMENT_REQUIRED;
import static com.github.tncrazvan.arcano.Common.STATUS_PERMANENT_REDIRECT;
import static com.github.tncrazvan.arcano.Common.STATUS_PRECONDITION_FAILED;
import static com.github.tncrazvan.arcano.Common.STATUS_PRECONDITION_REQUIRED;
import static com.github.tncrazvan.arcano.Common.STATUS_PROCESSING;
import static com.github.tncrazvan.arcano.Common.STATUS_PROXY_AUTHENTICATION_REQUIRED;
import static com.github.tncrazvan.arcano.Common.STATUS_RANGE_NOT_SATISFIABLE;
import static com.github.tncrazvan.arcano.Common.STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE;
import static com.github.tncrazvan.arcano.Common.STATUS_REQUEST_TIMEOUT;
import static com.github.tncrazvan.arcano.Common.STATUS_RESET_CONTENT;
import static com.github.tncrazvan.arcano.Common.STATUS_SEE_OTHER;
import static com.github.tncrazvan.arcano.Common.STATUS_SERVICE_UNAVAILABLE;
import static com.github.tncrazvan.arcano.Common.STATUS_SUCCESS;
import static com.github.tncrazvan.arcano.Common.STATUS_SWITCHING_PROTOCOLS;
import static com.github.tncrazvan.arcano.Common.STATUS_SWITCH_PROXY;
import static com.github.tncrazvan.arcano.Common.STATUS_TEMPORARY_REDIRECT;
import static com.github.tncrazvan.arcano.Common.STATUS_TOO_MANY_REQUESTS;
import static com.github.tncrazvan.arcano.Common.STATUS_UNAUTHORIZED;
import static com.github.tncrazvan.arcano.Common.STATUS_UNAVAILABLE_FOR_LEGAL_REASONS;
import static com.github.tncrazvan.arcano.Common.STATUS_UNPROCESSABLE_ENTITY;
import static com.github.tncrazvan.arcano.Common.STATUS_UNSUPPORTED_MEDIA_TYPE;
import static com.github.tncrazvan.arcano.Common.STATUS_UPGRADE_REQUIRED;
import static com.github.tncrazvan.arcano.Common.STATUS_URI_TOO_LONG;
import static com.github.tncrazvan.arcano.Common.STATUS_USE_PROXY;
import static com.github.tncrazvan.arcano.Common.STATUS_VARIANT_ALSO_NEGOTATIES;
import static com.github.tncrazvan.arcano.Common.js;
import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.Tool.Database.Query;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
import com.google.gson.JsonObject;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import jdk.nashorn.api.scripting.JSObject;

/**
 *
 * @author Administrator
 */
public class JavaScriptExecutor extends Common{
    private final static String NASHORN_ARGS = "nashorn.args";
    private final static String ES_6 = "--language=es6 --no-deprecation-warning";
    
    private abstract class JavaScriptCurrentContext{
        final String dirname;

        public JavaScriptCurrentContext(String dirname) {
            this.dirname = dirname;
        }
        
    }
    
    public class JSFile extends File{

        public JSFile(String filename) {
            super(filename);
        }
        
        
        public String read() throws FileNotFoundException, IOException{
            FileInputStream fis = new FileInputStream(this);
            String result = new String(fis.readAllBytes(),charset);
            fis.close();
            return result;
            
        }
        
        public void write(String contents) throws FileNotFoundException, UnsupportedEncodingException, IOException{
            FileOutputStream fos = new FileOutputStream(this);
            fos.write(contents.getBytes(charset));
            fos.close();
        }
        
        public Map<String,Object> info() throws IOException{
            return info("*");
        }
        public Map<String,Object> info(String selection) throws IOException{
             return Files.readAttributes(this.toPath(), selection);
        }
    }
    
    private class JSFileMaker extends JavaScriptCurrentContext implements Function<String, JSFile> {
        public JSFileMaker(String dirname) {
            super(dirname);
        }
        
        @Override
        public JSFile apply(String filename) {
            return new JSFile(dirname+"/"+filename);
        }
    }
    
    
    //Http
    public void execute(HttpEvent e,ScriptContext context,String filename,String[] args,StringBuilder content) throws ScriptException, IOException{
        ScriptEngineManager mgr = new ScriptEngineManager();
        if(js == null){
            System.setProperty(NASHORN_ARGS, ES_6);
            js = mgr.getEngineByName("nashorn");
        }
        eval(e,context,filename,args,content);
    }
    
    public class JSLog implements Function<String, Void>{
        @Override
        public Void apply(String message) {
            System.out.println(message);
            return null;
        }
    }
    
    public class JSSleep implements Function<Long, Void>{
        @Override
        public Void apply(Long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(JavaScriptExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    
    public class JSThread<T> implements Function<Function<Thread,Void>, Void>{
        @Override
        public Void apply(Function<Thread,Void> todo) {
            executor.submit(()->{
                todo.apply(Thread.currentThread());
            });
            return null;
        }
    }
    
    public class EventListener<T> implements Function<Function<T,Void>, Void>{
        public Function todo;
        
        @Override
        public Void apply(Function<T,Void> todo) {
            this.todo = todo;
            return null;
        }
    }
    
    public class JSMySQLConnection implements JsonTools{
        private Connection connection;
        public void set(Connection connection){
            this.connection = connection;
        }
        public Connection get(){
            return connection;
        }
        
        public void close() throws SQLException{
            connection.close();
        }
        
        public void database(String dbname) throws SQLException{
            connection.setCatalog(dbname);
        }
        private Query query;
        public void prepare(String value) throws SQLException{
            query = new Query(connection, value);
        }
        public void bindString(String key, String value) throws SQLException{
            query.setString(key, value);
        }
        public JSObject execute() throws SQLException, ScriptException{
            ResultSet result = query.executeQuery();
            ResultSetMetaData metadata = result.getMetaData();
            ArrayList<Object> output = new ArrayList<>();
            while(result.next()){
                JsonObject o = new JsonObject();
                for(int i=1,length=metadata.getColumnCount();i<=length;i++){
                    String colname = metadata.getColumnName(i);
                    int type = metadata.getColumnType(i);
                    switch(type){
                        case java.sql.Types.BINARY:
                        case java.sql.Types.CLOB:
                        case java.sql.Types.BLOB:
                        case java.sql.Types.VARBINARY:
                        case java.sql.Types.NVARCHAR:
                        case java.sql.Types.NCHAR:
                        case java.sql.Types.DATE:
                        case java.sql.Types.CHAR:
                        case java.sql.Types.VARCHAR:
                        default:
                            o.addProperty(colname, result.getString(colname));
                            break;
                        case java.sql.Types.NUMERIC:
                        case java.sql.Types.SMALLINT:
                        case java.sql.Types.INTEGER:
                        case java.sql.Types.BIT:
                        case java.sql.Types.BIGINT:
                            o.addProperty(colname, result.getInt(colname));
                            break;
                        case java.sql.Types.FLOAT:
                            o.addProperty(colname, result.getFloat(colname));
                            break;
                        case java.sql.Types.DECIMAL:
                        case java.sql.Types.DOUBLE:
                            o.addProperty(colname, result.getDouble(colname));
                            break;
                        case java.sql.Types.BOOLEAN:
                            o.addProperty(colname, result.getBoolean(colname));
                            break;
                    }
                }
                output.add(o);
            }
            js.put("$tmp", output);
            JSObject obj = (JSObject)js.eval("JSON.parse($tmp)");
            return obj;
        }
    }
    
    public class JSMySQLConnector implements Function<Object, JSMySQLConnection>,JsonTools{
        public JSMySQLConnection mysql = null;
        @Override
        public JSMySQLConnection apply(Object arg) {
            JsonObject details = toJsonObject(arg);
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser(details.get("username").getAsString());
            dataSource.setPassword(details.get("password").getAsString());
            dataSource.setServerName(details.get("hostname").getAsString());
            try {
                if(mysql != null && !mysql.get().isClosed()){
                    mysql.get().close();
                }
                if(mysql == null)
                    mysql = new JSMySQLConnection();
                mysql.set(dataSource.getConnection());
                return mysql;
            } catch (SQLException ex) {
                Logger.getLogger(JavaScriptExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    
    private static String tail(String dirname){
        return "\nfunction header(key,value){server.setHeaderField(key,value);}function status(value){header(\"@Status\",value);}function send(data){try{data=JSON.parse(data);}catch(e){}server.send(data);}\nfunction require(filename){load('"+dirname.replace("\\", "/")+"/'+filename);}\nmain();";
    }
    
    //Http
    private void eval(HttpEvent e,ScriptContext context,String filename,String[] args,StringBuilder content) throws ScriptException, IOException{
        String dirname = Path.of(filename).getParent().toString();
        e.setStatus(STATUS_SUCCESS);
        String script = Files.readString(Path.of(filename));
        if(context != null)
            js.setContext(context);
        
        Common c = this;
        
        js.eval("function main(){"+script+"}"+tail(dirname),new SimpleBindings(
            new HashMap<String,Object>(){{
                put("args",args);
                put("log",new JSLog());
                put("method",e.getMethod());
                put("content",content.toString());
                put("server",e);
                put("mysql",new JSMySQLConnector());
                put("file",new JSFileMaker(dirname));
                put("thread",new JSThread());
                put("sleep",new JSSleep());
                
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
    
    
    
    //WebSockets
    public void execute(WebSocketEvent e,ScriptContext context,String filename,String[] args) throws ScriptException, IOException{
        ScriptEngineManager mgr = new ScriptEngineManager();
        if(js == null){
            System.setProperty(NASHORN_ARGS, ES_6);
            js = mgr.getEngineByName("nashorn");
        }
        eval(e,context,filename,args);
    }
    
    public EventListener<Void> onOpen;
    public EventListener<String> onMessage;
    public EventListener<Void> onClose;
    //WebSockets
    private void eval(WebSocketEvent e,ScriptContext context,String filename,String[] args) throws ScriptException, IOException{
        String dirname = Path.of(filename).getParent().toString();
        String script = Files.readString(Path.of(filename));
        if(context != null)
            js.setContext(context);
        
        onOpen = new EventListener<>();
        onMessage = new EventListener<>();
        onClose = new EventListener<>();
        
        Common c = this;
        
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
