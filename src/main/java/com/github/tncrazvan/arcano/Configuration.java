package com.github.tncrazvan.arcano;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.SharedObject.ROUTES;
import com.github.tncrazvan.arcano.Tool.Cluster.Cluster;
import com.github.tncrazvan.arcano.Tool.Cluster.ClusterServer;
import com.github.tncrazvan.arcano.Tool.Cluster.InvalidClusterEntryException;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonObject;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonParse;
import com.github.tncrazvan.arcano.Tool.Http.Status;
import com.github.tncrazvan.asciitable.AsciiTable;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

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
    public int minify = 0;
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
    public String dir = "./http.json";
    public String arcanoSecret = "HF75HFGY4764TH4TJ4T4TY";
    public String jwtSecret = "eswtrweqtr3w25trwes4tyw456t";
    public String assets = "/www/assets.json";
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
    
    public void parse(final String settings, final SharedObject so, final String[] args) throws IOException {
        this.parse(new File(settings),so,args);
    }

    /**
     * Parse configuration from the input filename.
     * @param settings json configuration filename.
     * @param so
     * @param args
     * @throws IOException 
     */
    public void parse(final File settings, final SharedObject so, final String[] args) throws IOException{
        this.dir = settings.getParent();
        
        final byte[] configBytes;
        try (FileInputStream fis = new FileInputStream(settings)) {
            configBytes = fis.readAllBytes();
        }
        config = jsonObject(new String(configBytes));
        char endchar;
        JsonObject tmp;
        if(config.has("compress")){
            this.compression = jsonParse(config.get("compress").getAsJsonArray(), String[].class);
        }else{
            this.compression = new String[]{};
        }
        
        if(config.has("cluster")){
            if(config.has("cluster")){
                final JsonObject clusterJson = config.get("cluster").getAsJsonObject();
                clusterJson.keySet().forEach((hostname) -> {
                    final JsonObject serverJson = clusterJson.get(hostname).getAsJsonObject();
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
                });
            }
        }

        if (config.has("responseWrapper"))
            this.responseWrapper = config.get("responseWrapper").getAsBoolean();

        if (config.has("secret"))
            this.arcanoSecret = config.get("secret").getAsString();

        if (config.has("sendExceptions"))
            this.sendExceptions = config.get("sendExceptions").getAsBoolean();

        if (config.has("minify"))
            this.minify = config.get("minify").getAsInt();

        if (config.has("threads")){
            tmp = config.get("threads").getAsJsonObject();
            if(tmp.has("pool"))
                this.threads.pool = tmp.get("pool").getAsInt();
            if(this.threads.pool <= 0)
                this.threads.pool = 1;
            if(tmp.has("policy"))
                this.threads.policy = tmp.get("policy").getAsString();
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
            this.serverRoot = new File(args[0]).getParent().replaceAll("\\\\", "/") + "/"
                    + config.get("serverRoot").getAsString();
        else
            this.serverRoot = new File(args[0]).getParent().replaceAll("\\\\", "/") + "/" + this.serverRoot;
        endchar = this.serverRoot.charAt(this.serverRoot.length() - 1);

        if (endchar != '/') {
            this.serverRoot += "/";
        }

        if (config.has("webRoot"))
            this.webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/") + "/"
                    + config.get("webRoot").getAsString();
        else
            this.webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/") + "/" + this.webRoot;

        endchar = this.webRoot.charAt(this.webRoot.length() - 1);

        if (endchar != '/') {
            this.webRoot += "/";
        }

        if (config.has("assets"))
            this.assets = new File(args[0]).getParent().replaceAll("\\\\", "/") + "/"
                    + config.get("assets").getAsString();
        else
            this.assets = new File(args[0]).getParent().replaceAll("\\\\", "/") + "/" + this.assets;

        endchar = this.assets.charAt(this.assets.length() - 1);

        if (endchar != '/') {
            this.assets += "/";
        }

        if (config.has("charset"))
            this.charset = config.get("charset").getAsString();

        if (config.has("timeout"))
            this.timeout = config.get("timeout").getAsInt();

        if (config.has("session")) {
            tmp = config.get("session").getAsJsonObject();
            if (tmp.has("ttl"))
                this.session.ttl = tmp.get("ttl").getAsInt();
            if (tmp.has("keepAlive"))
                this.session.keepAlive = tmp.get("keepAlive").getAsBoolean();
        }
        session.table.add("ttl", "" + this.session.ttl + " seconds");
        session.table.add("keepAlive", this.session.keepAlive ? "True" : "False");

        if (config.has("cookie")) {
            tmp = config.get("cookie").getAsJsonObject();
            if (tmp.has("ttl"))
                this.cookie.ttl = tmp.get("ttl").getAsInt();
        }
        cookie.table.add("ttl", "" + this.cookie.ttl + " seconds");

        if (config.has("webSocket")) {
            tmp = config.get("webSocket").getAsJsonObject();
            if (tmp.has("mtu"))
                this.webSocket.mtu = tmp.get("mtu").getAsInt();
            if (tmp.has("groups")) {
                tmp = tmp.get("groups").getAsJsonObject();
                if (tmp.has("enabled"))
                    this.webSocket.groups.enabled = tmp.get("enabled").getAsBoolean();
                if (tmp.has("connections")) {
                    tmp = tmp.get("connections").getAsJsonObject();
                    this.webSocket.groups.connections.max = tmp.get("max").getAsInt();
                }
            }
        }
        this.webSocket.groups.connections.table.add("max", this.webSocket.groups.connections.max + " connections (This value is ofcourse dependent on the thread pool size)");
        this.webSocket.groups.table.add("connections", this.webSocket.groups.connections.table.toString());
        this.webSocket.groups.table.add("enabled", this.webSocket.groups.enabled ? "True" : "False");
        this.webSocket.table.add("groups", this.webSocket.groups.table.toString());
        this.webSocket.table.add("mtu", this.webSocket.mtu + " bytes");

        if (config.has("http")) {
            tmp = config.get("http").getAsJsonObject();
            if (tmp.has("mtu"))
                this.http.mtu = tmp.get("mtu").getAsInt();
        }
        this.http.table.add("mtu", this.http.mtu + " bytes");

        if (config.has("entryPoint"))
            this.entryPoint = config.get("entryPoint").getAsString();

        final AsciiTable configurationTable = new AsciiTable();
        configurationTable.add("KEY", "VALUE");
        configurationTable.add("locale", locale.toString());
        configurationTable.add("timezone", timezone.toString()+" (Http cookies by default use GMT aka UTC±00:00)");
        configurationTable.add("port", "" + this.port);
        configurationTable.add("bindAddress", this.bindAddress);
        configurationTable.add("serverRoot", this.serverRoot+" (Relative to the JSON configuration file)");
        configurationTable.add("webRoot", this.webRoot+" (Relative to the JSON configuration file)");
        configurationTable.add("entryPoint", this.entryPoint+" (Relative to the webRoot)");
        configurationTable.add("charset", this.charset);
        configurationTable.add("timeout", "After " + this.timeout + " milliseconds");
        configurationTable.add("session", this.session.table.toString());
        configurationTable.add("cookie", this.cookie.table.toString());
        configurationTable.add("webSocket", this.webSocket.table.toString());
        configurationTable.add("http", "" + this.http.table.toString());
        configurationTable.add("minify", this.minify == 0?"Once when the server starts": "Once every "+this.minify+" milliseconds");
        configurationTable.add("threads", this.threads.table.toString());
        configurationTable.add("sendExceptions", this.sendExceptions ? "True" : "False");
        configurationTable.add("responseWrapper", this.responseWrapper ? "True" : "False");

        // checking for SMTP server
        if (config.has("smtp")) {
            final JsonObject smtpObject = config.get("smtp").getAsJsonObject();
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
            configurationTable.add("smtp", this.smtp.table.toString());
        }

        final AsciiTable controllersTable = new AsciiTable();
        controllersTable.add("TYPE", "PATH", "CLASS");
        ROUTES.entrySet().forEach((entry) -> {
            final WebObject wo = entry.getValue();
            final String type = wo.getType();
            final String name = entry.getKey().substring(type.length());
            controllersTable.add(type, name, wo.getClassname());
        });
        configurationTable.add("Controllers", controllersTable.toString());

        if (config.has("certificate")) {
            final JsonObject certificateObject = config.get("certificate").getAsJsonObject();


            this.certificate.name = certificateObject.get("NAME").getAsString();

            this.certificate.type = certificateObject.get("TYPE").getAsString();

            this.certificate.password = certificateObject.get("password").getAsString();

            
            this.certificate.table.add("Attribute","VALUE");
            this.certificate.table.add("NAME",this.certificate.name);
            this.certificate.table.add("TYPE",this.certificate.type);
            this.certificate.table.add("password","***");

            configurationTable.add("certificate",this.certificate.table.toString());
        }
        System.out.println(configurationTable.toString());
    }
    
}
