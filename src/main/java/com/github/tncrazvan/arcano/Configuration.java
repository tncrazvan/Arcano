package com.github.tncrazvan.arcano;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.SharedObject.ROUTES;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;
import com.github.tncrazvan.arcano.Tool.Cluster.Cluster;
import com.github.tncrazvan.arcano.Tool.Cluster.ClusterServer;
import com.github.tncrazvan.arcano.Tool.Cluster.InvalidClusterEntryException;
import com.github.tncrazvan.arcano.Tool.Encoding.JsonTools;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonObject;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonParse;
import com.github.tncrazvan.arcano.Tool.Http.Status;
import com.github.tncrazvan.arcano.Tool.Regex;
import com.github.tncrazvan.arcano.Tool.System.ServerFile;
import com.github.tncrazvan.asciitable.AsciiTable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import jdk.internal.joptsimple.internal.Strings;

/**
 * Containst the configuration file objects.
 * @author Razvan
 */
public class Configuration {
    public ZoneId timezone = ZoneId.systemDefault();
    public Locale locale = Locale.getDefault();
    public DateTimeFormatter formatHttpDefaultDate = DateTimeFormatter.ofPattern("EEE, d MMM y HH:mm:ss z", this.locale).withZone(this.timezone);
    public boolean responseWrapper = false;
    public boolean sendExceptions = true;
    public boolean listen = true;
    public static class Smtp{
        public boolean enabled = false;
        public int port = 25;
        public String bindAddress;
        public String hostname = "";
        public AsciiTable table = new AsciiTable();
        public Smtp() {
            table.add("KEY","VALUE");
        }
    }
    public Smtp smtp = new Smtp();
    public int port = 80;
    public int timeout = 30000;
    public static class Threads{
        public static final String POLICY_FIX = "fix";
        public static final String POLICY_CACHE = "cache";
        public static final String POLICY_STEAL = "steal";
        public String policy = POLICY_STEAL; 
        public int pool = 3;
        public AsciiTable table = new AsciiTable();
        public Threads() {
            table.add("KEY","VALUE");
        }
    }
    public Threads threads = new Threads();
    public static class WebSocket{
        public static class Groups{
            public static class Connections{
                public int max = 10;
                public AsciiTable table = new AsciiTable();
                public Connections() {
                    table.add("KEY","VALUE");
                }
            }
            public Connections connections = new Connections();
            public boolean enabled = false;
            public AsciiTable table = new AsciiTable();
            public Groups() {
                table.add("KEY","VALUE");
            }
        }
        public Groups groups = new Groups();
        public int mtu = 65536;
        public WebObject controllerNotFound = null;
        public AsciiTable table = new AsciiTable();
        public WebSocket() {
            table.add("KEY","VALUE");
        }
        
    }
    public WebSocket webSocket = new WebSocket();
    
    public static class Http{
        public int mtu = 65536;
        public AsciiTable table = new AsciiTable();
        public WebObject controllerNotFound = null;
        public WebObject controllerDefault = null;
        public Http() {
            table.add("KEY","VALUE");
        }
    }
    public Http http = new Http();
    public String[] compression = new String[0];
    public HashMap<String,String> headers = new HashMap<String,String>(){
		private static final long serialVersionUID = -8770720041851024009L;
        {
            put("@Status",Status.STATUS_SUCCESS);
        }
};
    public static class Session{
        public int ttl = 1440;
        public boolean keepAlive = false;
        public AsciiTable table = new AsciiTable();

        public Session() {
            table.add("KEY","VALUE");
        }
        
    }
    public Session session = new Session();
    public static class Cookie{
        public int ttl = 1440;
        public AsciiTable table = new AsciiTable();
        public Cookie() {
            table.add("KEY","VALUE");
        }
    }
    public Cookie cookie = new Cookie();
    public Cluster cluster = new Cluster(new HashMap<>());

    public String key;
    public String dir;
    public String callerDir;
    public String jwtSecret = "eswtrweqtr3w25trwes4tyw456t";
    public String webRoot = "www";
    public String serverRoot = "server";
    public String charset = "UTF-8";
    public String bindAddress = "::";
    public String entryPoint = "/index.html";
    public static class Certificate{
        String name = "";
        String type = "JKS";
        String password = "";
        public AsciiTable table = new AsciiTable();
        public Certificate() {
            table.add("KEY","VALUE");
        }
    }
    public Certificate certificate = new Certificate();
    private JsonObject config = null;

    public JsonObject getConfig() {
        return config;
    }
    
    public void parse(final String settings, final SharedObject so, final String[] args) throws IOException {
        this.parse(new File(settings),so,args);
    }

    /**
     * Parse configuration from the input filename.
     * @param json json configuration filename.
     * @param so
     * @param args
     * @throws IOException 
     */
    public final void parse(final File json, final SharedObject so, final String[] args) throws IOException{
        if(!json.exists()){
            System.out.println("Configuration file "+json.getAbsolutePath()+" does not seem to exist.");
            return;
        }
        this.dir = json.getAbsoluteFile().getParent();
        if(this.dir == null)
            this.dir = "";
        char endchar = this.dir.charAt(this.dir.length() - 1);
        if(endchar != '/')
            this.dir +="/";
        this.dir = Regex.replace(this.dir, "\\\\", "/");
        this.dir = Regex.replace(this.dir, "/+", "/");
        this.dir = Regex.replace(this.dir, "\\/\\.\\/", "/");
        this.dir = Regex.replace(this.dir, "\\/\\.$", "/");
        
        this.callerDir = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();
        if(this.callerDir == null)
            this.callerDir = "";
        endchar = this.callerDir.charAt(this.callerDir.length() - 1);
        if(endchar != '/')
            this.callerDir +="/";
        this.callerDir = Regex.replace(this.callerDir, "\\\\", "/");
        this.callerDir = Regex.replace(this.callerDir, "/+", "/");
        this.callerDir = Regex.replace(this.callerDir, "\\/\\.\\/", "/");
        this.callerDir = Regex.replace(this.callerDir, "\\/\\.$", "/");
        
        final byte[] configBytes;
        try (FileInputStream fis = new FileInputStream(json)) {
            configBytes = fis.readAllBytes();
        }
        config = jsonObject(new String(configBytes));
        JsonElement el;
        JsonObject obj;
        if(config.has("compress")){
            this.compression = jsonParse(config.get("compress").getAsJsonArray(), String[].class);
        }else{
            this.compression = new String[]{};
        }
        
        if(config.has("key"))
            key =config.get("key").getAsString();
        else
            key = "EW3RWSETR2W345TW34ETGWSETQ3E325TE47E45T324W5RTWESRTF3QW245RW3ERFEFRG435444444TRWSEFRGTSER324RW3ERFASERTFWSERTWSETRWESWTESTE";
        
        if(config.has("cluster")){
            if(config.has("cluster")){
                el = config.get("cluster");
                if(el.isJsonObject()){
                    final JsonObject clusterObject = el.getAsJsonObject();
                    clusterObject.keySet().forEach((hostname) -> {
                        final JsonElement clusterElement = clusterObject.get(hostname);
                        if(clusterElement.isJsonObject()){
                            final JsonObject serverJson = clusterElement.getAsJsonObject();
                            try {
                                if (!serverJson.has("arcanoSecret") || !serverJson.has("weight")) {
                                    throw new InvalidClusterEntryException("\nCluster entry " + hostname
                                            + " is invalid.\nA cluster enrty should contain the following configuration: \n{\n"
                                            + "\t\"arcanoSecret\":\"<your secret key>\",\n"
                                            + "\t\"weight\":<your server weight>\n" + "}");
                                }
                                final ClusterServer server = new ClusterServer(hostname,
                                        serverJson.get("arcanoSecret").getAsString(), serverJson.get("weight").getAsInt());
                                this.cluster.setServer(hostname, server);
                            } catch (final InvalidClusterEntryException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }else
                            System.out.println("cluster["+hostname+"] is not an object.");
                    });
                }else
                    System.out.println("cluster is not an object.");
            }
        }

        if (config.has("responseWrapper"))
            this.responseWrapper = config.get("responseWrapper").getAsBoolean();

        if (config.has("sendExceptions"))
            this.sendExceptions = config.get("sendExceptions").getAsBoolean();
        
        if (config.has("threads")){
            el = config.get("threads");
            if(el.isJsonObject()){
                obj = el.getAsJsonObject();
                if(obj.has("pool"))
                    this.threads.pool = obj.get("pool").getAsInt();
                if(this.threads.pool <= 0)
                    this.threads.pool = 1;
                if(obj.has("policy"))
                    this.threads.policy = obj.get("policy").getAsString();
            }else 
                System.out.println("threads is not an object.");
        }
        
        
        switch(this.threads.policy){
            case Threads.POLICY_CACHE:
                this.threads.table.add("policy", this.threads.policy+" (Creates new threads as needed and reuses them)");
            break;
            case Threads.POLICY_FIX:
                this.threads.table.add("policy", this.threads.policy+" (Uses a fixed number of threads)");
            break;
            case Threads.POLICY_STEAL:
            default:
                this.threads.table.add("policy", this.threads.policy+" (Uses Work-Stealing thread pool)");
        }
        
        switch(this.threads.policy){
            case Threads.POLICY_CACHE:
                this.threads.table.add("pool", "Cache policy ignores this field");
            break;
            case Threads.POLICY_FIX:
                this.threads.table.add("pool", this.threads.pool + " threads");
            break;
            case Threads.POLICY_STEAL:
            default:
                if(this.threads.pool == 0)
                    this.threads.table.add("pool", "As many threads as there are available processors.");
                else
                    this.threads.table.add("pool", this.threads.pool + " threads (The actual number of threads may grow or shrink dinamically.)");
            
        }

        if (config.has("timezone"))
            this.timezone = ZoneId.of(config.get("timezone").getAsString());

        if (config.has("locale")) {
            final String[] localeTmpString = config.get("locale").getAsString().split("_");
            this.locale = new Locale(localeTmpString[0], localeTmpString[1]);
            this.formatHttpDefaultDate = DateTimeFormatter.ofPattern("EEE, d MMM y HH:mm:ss z", this.locale)
                    .withZone(this.timezone);
        }

        if (config.has("port"))
            this.port = config.get("port").getAsInt();

        if (config.has("bindAddress"))
            this.bindAddress = config.get("bindAddress").getAsString();
        else if (config.has("bindingAddress"))
            this.bindAddress = config.get("bindingAddress").getAsString();

        if (config.has("serverRoot"))
            this.serverRoot = this.dir + "/" + config.get("serverRoot").getAsString();
        else
            this.serverRoot = this.dir + "/" + this.serverRoot;
        endchar = this.serverRoot.charAt(this.serverRoot.length() - 1);

        if (endchar != '/') {
            this.serverRoot += "/";
        }
        this.serverRoot = Regex.replace(this.serverRoot, "\\\\", "/");
        this.serverRoot = Regex.replace(this.serverRoot, "/+", "/");
        this.serverRoot = Regex.replace(this.serverRoot, "\\/\\.\\/", "/");
        this.serverRoot = Regex.replace(this.serverRoot, "\\/\\.$", "/");

        if (config.has("webRoot"))
            this.webRoot = this.dir + "/" + config.get("webRoot").getAsString();
        else
            this.webRoot = this.dir + "/" + this.webRoot;

        endchar = this.webRoot.charAt(this.webRoot.length() - 1);
        
        if (endchar != '/') {
            this.webRoot += "/";
        }
        this.webRoot = Regex.replace(this.webRoot,"\\\\", "/");
        this.webRoot = Regex.replace(this.webRoot,"/+", "/");
        this.webRoot = Regex.replace(this.webRoot, "\\/\\.\\/", "/");
        this.webRoot = Regex.replace(this.webRoot, "\\/\\.$", "/");
        if (config.has("charset"))
            this.charset = config.get("charset").getAsString();

        if (config.has("timeout"))
            this.timeout = config.get("timeout").getAsInt();

        if (config.has("session")) {
            el = config.get("session");
            if(el.isJsonObject()){
                obj = el.getAsJsonObject();
                if (obj.has("ttl"))
                    this.session.ttl = obj.get("ttl").getAsInt();
                if (obj.has("keepAlive"))
                    this.session.keepAlive = obj.get("keepAlive").getAsBoolean();
            }else
                System.out.println("session is not an object.");
        }
        session.table.add("ttl", "" + this.session.ttl + " seconds");
        session.table.add("keepAlive", this.session.keepAlive ? "True" : "False");

        if (config.has("cookie")) {
            el = config.get("cookie");
            if(el.isJsonObject()){
                obj = el.getAsJsonObject();
                if (obj.has("ttl"))
                    this.cookie.ttl = obj.get("ttl").getAsInt();
            }else
                System.out.println("cookie is not an object.");
        }
        cookie.table.add("ttl", "" + this.cookie.ttl + " seconds");

        if (config.has("webSocket")) {
            el = config.get("webSocket");
            if(el.isJsonObject()){
                obj = el.getAsJsonObject();
                if (obj.has("mtu"))
                    this.webSocket.mtu = obj.get("mtu").getAsInt();
                if (obj.has("groups")) {
                    el = obj.get("groups");
                    if(el.isJsonObject()){
                        obj = el.getAsJsonObject();
                        if (obj.has("enabled"))
                            this.webSocket.groups.enabled = obj.get("enabled").getAsBoolean();
                        if (obj.has("connections")) {
                            el = obj.get("connections");
                            if(el.isJsonObject()){
                                obj = el.getAsJsonObject();
                                if(obj.has("max"))
                                    this.webSocket.groups.connections.max = obj.get("max").getAsInt();
                            }else
                                System.out.println("webSocket.groups.connections is not an object.");
                        }
                    }else
                        System.out.println("webSocket.groups is not an object.");
                }
            }else
                System.out.println("webSocket is not an object.");
        }
        this.webSocket.groups.connections.table.add("max", this.webSocket.groups.connections.max + " connections (This value is ofcourse dependent on the thread pool size)");
        this.webSocket.groups.table.add("connections", this.webSocket.groups.connections.table.toString());
        this.webSocket.groups.table.add("enabled", this.webSocket.groups.enabled ? "True" : "False");
        this.webSocket.table.add("groups", this.webSocket.groups.table.toString());
        this.webSocket.table.add("mtu", this.webSocket.mtu + " bytes");

        if (config.has("http")) {
            el = config.get("http");
            if(el.isJsonObject()){
                obj = el.getAsJsonObject();
                if (obj.has("mtu"))
                    this.http.mtu = obj.get("mtu").getAsInt();
            }else
                System.out.println("http is not an object.");
        }
        this.http.table.add("mtu", this.http.mtu + " bytes");

        if (config.has("entryPoint"))
            this.entryPoint = "/"+config.get("entryPoint").getAsString();
        
        this.entryPoint = this.entryPoint.replaceAll("/+", "/");

        final AsciiTable configurationTable = new AsciiTable();
        configurationTable.add("KEY", "VALUE");
        configurationTable.add("locale", locale.toString());
        configurationTable.add("timezone", timezone.toString()+" (Http cookies by default use GMT aka UTC±00:00)");
        configurationTable.add("port", "" + this.port);
        configurationTable.add("bindAddress", this.bindAddress);
        configurationTable.add("serverRoot", this.serverRoot);
        configurationTable.add("webRoot", this.webRoot);
        configurationTable.add("entryPoint", this.entryPoint+" (Relative to the webRoot)");
        configurationTable.add("charset", this.charset);
        configurationTable.add("timeout", "After " + this.timeout + " milliseconds");
        configurationTable.add("session", this.session.table.toString());
        configurationTable.add("cookie", this.cookie.table.toString());
        configurationTable.add("webSocket", this.webSocket.table.toString());
        configurationTable.add("http", "" + this.http.table.toString());
        configurationTable.add("threads", this.threads.table.toString());
        configurationTable.add("sendExceptions", this.sendExceptions ? "True" : "False");
        configurationTable.add("responseWrapper", this.responseWrapper ? "True" : "False");

        // checking for SMTP server
        if (config.has("smtp")) {
            el = config.get("smtp");
            if(el.isJsonObject()){
                final JsonObject smtpObject = el.getAsJsonObject();
                if (smtpObject.has("enabled")) {
                    this.smtp.enabled = smtpObject.get("enabled").getAsBoolean();
                    this.smtp.table.add("enabled", smtpObject.get("enabled").getAsString());
                    if (this.smtp.enabled) {
                        this.smtp.bindAddress = this.bindAddress;
                        if (smtpObject.has("bindAddress")) {
                            this.smtp.bindAddress = smtpObject.get("bindAddress").getAsString();
                        }
                        if (smtpObject.has("port")) {
                            this.smtp.port = smtpObject.get("port").getAsInt();
                        }
                        if (smtpObject.has("hostname")) {
                            this.smtp.hostname = smtpObject.get("hostname").getAsString();
                        }
                        this.smtp.table.add("hostname", this.smtp.hostname);
                        this.smtp.table.add("bindAddress", this.smtp.bindAddress);
                        this.smtp.table.add("port", "" + this.smtp.port);
                    }
                }
            }else
                System.out.println("smtp is not an object.");
            
            configurationTable.add("smtp", this.smtp.table.toString());
        }

        final AsciiTable controllersTable = new AsciiTable();
        controllersTable.add("TYPE", "PATH", "CLASS");
        
        ArrayList<String> sortedKeys = new ArrayList<>(ROUTES.keySet());
        Collections.sort(sortedKeys, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
              return a.compareTo(b);
            }
        });
        
        
        for(String key : sortedKeys){
            final WebObject wo = ROUTES.get(key);
            final String type = wo.getType();
            final String name = key.substring(type.length());
            final String methodName = wo.getMethodName()==null?"{?}":wo.getMethodName();
            controllersTable.add(type, name, wo.getClassName()+"."+methodName);
        }
        /*ROUTES.entrySet().forEach((entry) -> {
            final WebObject wo = entry.getValue();
            final String type = wo.getType();
            final String name = entry.getKey().substring(type.length());
            final String methodName = wo.getMethodName()==null?"<?>":wo.getMethodName();
            controllersTable.add(type, name, wo.getClassName()+"."+methodName);
        });*/
        configurationTable.add("Controllers", controllersTable.toString());

        if (config.has("certificate")) {
            el = config.get("certificate");
            if(el.isJsonObject()){
                final JsonObject certificateObject = el.getAsJsonObject();

                this.certificate.name = certificateObject.get("NAME").getAsString();

                this.certificate.type = certificateObject.get("TYPE").getAsString();

                this.certificate.password = certificateObject.get("password").getAsString();


                this.certificate.table.add("Attribute","VALUE");
                this.certificate.table.add("NAME",this.certificate.name);
                this.certificate.table.add("TYPE",this.certificate.type);
                this.certificate.table.add("password","***");
            }else
                System.out.println("certificate is not an object.");
            
            configurationTable.add("certificate",this.certificate.table.toString());
        }
        System.out.println(configurationTable.toString());
    }
    
    /**
     * Read JavaScript and CSS filename entries from a json array inside a json file and pack them together.<br />
     * The output packed files are named "main.js" and "main.css" and they will be located
     * inside a "pack" directory that is relative to the location of the input <b>.json</b> file.<br /><br />
     * 
     * For example, if the input file is "<b>www/imports.json</b>" the output packed files will be saved as
     * "<b>www/pack/main.js</b>" and "<b>www/pack/main.css</b>".
     * @param directory The directory in which to look for the input json file.
     * @param imports The name of the input file (<b>www/imports.json</b>).<br />
     * This input file is relative to the json configuration file.<br />
     * The input file should also be a json file, although it should only contain a json array
     * and its entries must contain the filenames (CSS and JavaScript files) of the files you want to
     * pack together.<br />
     * These filenames are relative to the imports file (<b>www/imports.json</b>) itself.
     * @return true if the files have been minified correctly, false otherwise.
     */
    public boolean pack(String directory, String imports){
        return pack(directory,imports, null);
    }
    
    /**
     * Read JavaScript and CSS filename entries from a json array inside a json file and pack them together.<br />
     * The output packed files are named "main.js" and "main.css" and they will be located
     * inside a "pack" directory that is relative to the location of the input <b>.json</b> file.<br /><br />
     * 
     * For example, if the input file is "<b>www/imports.json</b>" the output packed files will be saved as
     * "<b>www/pack/main.js</b>" and "<b>www/pack/main.css</b>".
     * @param directory The directory in which to look for the input json file.
     * @param imports The name of the input file (<b>www/imports.json</b>).<br />
     * This input file is relative to the json configuration file.<br />
     * The input file should also be a json file, although it should only contain a json array
     * and its entries must contain the filenames (CSS and JavaScript files) of the files you want to
     * pack together.<br />
     * These filenames are relative to the imports file (<b>www/imports.json</b>) itself.
     * @param forEachFile an action to be run everytime an existing file is encountered.<br />
     * The action has a ServerFile as an input argument and should return a String as output.<br /><br />
     * 
     * The ServerFile class extends the File class and it simply offers a few methods to quickly read the file and write to it.<br/>
     * The returned String will be appended to the final packed file.<br />
     * It is recommended you add a new line to the returned String for JavaScript files as 
     * JavaScript instructions are allowed to ommit the ";" character, which could result 
     * in collisions between the scripts when they are packed together.
     * @return true if the files have been minified correctly, false otherwise.
     */
    public boolean pack(String directory, String imports, CompleteAction<String,ServerFile> forEachFile){
        try {
            ServerFile f = new ServerFile(directory,imports);
            if(!f.exists())
                return false;
            
            JsonArray arr = JsonTools.jsonArray(f.readString(charset));
            String item;
            String js = "";
            String css = "";
            for(JsonElement e : arr){
                item = e.getAsString();
                ServerFile current = new ServerFile(webRoot,item);
                if(!current.exists()) continue;
                if(forEachFile != null){
                    if(item.endsWith(".css")){
                        css += forEachFile.callback(current);
                    }else if(item.trim().endsWith(".js")){
                        js += forEachFile.callback(current);
                    }
                }else{
                    if(item.endsWith(".css")){
                        css += current.readString(charset)+"\n";
                    }else if(item.endsWith(".js")){
                        js += current.readString(charset)+"\n";
                    }
                }
            }
            ServerFile mainCSS = new ServerFile(webRoot,"pack/main.css");
            ServerFile mainJS = new ServerFile(webRoot,"pack/main.js");
            mainCSS.getParentFile().mkdirs();
            mainJS.getParentFile().mkdirs();
            if(!mainCSS.exists())
                mainCSS.createNewFile();
            if(!mainJS.exists())
                mainJS.createNewFile();
            mainCSS.write(css, charset);
            mainJS.write(js, charset);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
