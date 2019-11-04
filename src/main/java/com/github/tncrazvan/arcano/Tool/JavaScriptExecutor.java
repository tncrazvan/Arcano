/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import static com.github.tncrazvan.arcano.Common.STATUS_SUCCESS;
import static com.github.tncrazvan.arcano.Common.js;
import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.Tool.Database.Query;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
import com.google.gson.JsonObject;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class JavaScriptExecutor {
    private final static String NASHORN_ARGS = "nashorn.args";
    private final static String ES_6 = "--language=es6 --no-deprecation-warning";
    
    private abstract class JavaScriptCurrentContext{
        final String dirname;

        public JavaScriptCurrentContext(String dirname) {
            this.dirname = dirname;
        }
        
    }
    
    private class JSFile extends JavaScriptCurrentContext implements Function<String, File> {
        public JSFile(String dirname) {
            super(dirname);
        }
        
        @Override
        public File apply(String filename) {
            return new File(dirname+"/"+filename);
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
        public Function todo;
        
        @Override
        public Void apply(String message) {
            System.out.println(message);
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
    
    //Http
    private void eval(HttpEvent e,ScriptContext context,String filename,String[] args,StringBuilder content) throws ScriptException, IOException{
        String dirname = Path.of(filename).getParent().toString();
        e.setStatus(STATUS_SUCCESS);
        String script = Files.readString(Path.of(filename));
        if(context != null)
            js.setContext(context);
        
        
        js.eval("function send(data){try{data=JSON.parse(data);}catch(e){}server.send(data);}function require(filename){load('"+dirname.replace("\\", "/")+"/'+filename);}"+script+"\nmain();",new SimpleBindings(
            new HashMap<String,Object>(){{
                put("args",args);
                put("log",new JSLog());
                put("method",e.getMethod());
                put("content",content.toString());
                put("server",e);
                put("mysql",new JSMySQLConnector());
                put("File",new JSFile(dirname));
            }}
        ));
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
        
        js.eval("function send(data){try{data=JSON.parse(data);}catch(e){}server.send(data);}\nfunction require(filename){load('"+dirname.replace("\\", "/")+"/'+filename);}\n"+script+"\nmain();",new SimpleBindings(
            new HashMap<String,Object>(){{
                put("args",args);
                put("log",new JSLog());
                put("server",e);
                put("File",new JSFile(dirname));
                put("mysql",new JSMySQLConnector());
                put("onOpen",onOpen);
                put("onMessage",onMessage);
                put("onClose",onClose);
            }}
        ));
        
    }
}
