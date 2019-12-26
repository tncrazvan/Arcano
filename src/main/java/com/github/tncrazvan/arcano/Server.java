package com.github.tncrazvan.arcano;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.github.tncrazvan.arcano.Http.HttpEventListener;
import com.github.tncrazvan.arcano.Smtp.SmtpServer;
import com.github.tncrazvan.arcano.Tool.Cluster.ClusterServer;
import com.github.tncrazvan.arcano.Tool.Cluster.InvalidClusterEntryException;
import static com.github.tncrazvan.arcano.Tool.JsonTools.jsonParse;
import com.github.tncrazvan.arcano.Tool.Minifier;
import com.github.tncrazvan.arcano.Tool.Regex;
import com.github.tncrazvan.asciitable.AsciiTable;
import com.google.gson.JsonObject;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.net.ssl.SSLServerSocket;

/**
 *
 * @author Razvan
 */
public class Server extends SharedObject {
    private static SmtpServer smtpServer;
    public static void main (String[] args) throws NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException, IOException{
        Server server = new Server(args);
    }

    public Server(String[] args,Class<?>... classes) {
        try {
            expose(
                    com.github.tncrazvan.arcano.Controller.Http.App.class,
                    com.github.tncrazvan.arcano.Controller.Http.ControllerNotFound.class,
                    com.github.tncrazvan.arcano.Controller.Http.Get.class,
                    com.github.tncrazvan.arcano.Controller.Http.Isset.class,
                    com.github.tncrazvan.arcano.Controller.Http.Set.class,
                    com.github.tncrazvan.arcano.Controller.Http.Unset.class,
                    
                    com.github.tncrazvan.arcano.Controller.WebSocket.ControllerNotFound.class,
                    com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi.class
            );
            expose(classes);
            listen(args);
        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | URISyntaxException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    File root ;
    URLClassLoader classLoader;
    /**
     * Starts the server listening.
     * @param args First argument must be the settings file. Check documentation to learn how to create a settings files.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws java.lang.ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public void listen(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException {
        root = new File("/java"); // On Windows running on C:\, this is C:\java.
        classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
        char endchar;
        System.out.println("ARGS: "+Arrays.toString(args));

        configDir = new File(args[0]).getParent();

        config.parse(args[0]);
        
        if(config.isset("compress")){
            compression = jsonParse(config.get("compress").getAsJsonArray(), String[].class);
        }else{
            compression = new String[]{};
        }
        
        if(config.isset("cluster")){
            if(config.isset("cluster")){
                JsonObject clusterJson = config.get("cluster").getAsJsonObject();
                clusterJson.keySet().forEach((hostname) -> {
                    JsonObject serverJson = clusterJson.get(hostname).getAsJsonObject();
                    try{
                        if(!serverJson.has("arcanoSecret") || !serverJson.has("weight")){
                            throw new InvalidClusterEntryException("\nCluster entry "+hostname+" is invalid.\nA cluster enrty should contain the following configuration: \n{\n"
                                    + "\t\"arcanoSecret\":\"<your secret key>\",\n"
                                    + "\t\"weight\":<your server weight>\n"
                                    + "}");
                        }
                        ClusterServer server = new ClusterServer(
                                hostname,
                                serverJson.get("arcanoSecret").getAsString(), 
                                serverJson.get("weight").getAsInt()
                        );
                        cluster.setServer(hostname, server);
                    }catch(InvalidClusterEntryException ex){
                        LOGGER.log(Level.SEVERE,null,ex);                                                                                                         
                    }
                });
            }
        }

        if(config.isset("classOrder")){
            classOrder = jsonParse(config.get("classOrder").getAsJsonArray(), String[].class);
        }else{
            classOrder = new String[]{};
        }

        if(config.isset("responseWrapper"))
            responseWrapper = config.get("responseWrapper").getAsBoolean();
        
        if(config.isset("secret"))
            arcanoSecret = config.get("secret").getAsString();

        if(config.isset("sendExceptions"))
            sendExceptions = config.get("sendExceptions").getAsBoolean();

        if(config.isset("minify"))
            minify = config.getInt("minify");

        if(config.isset("threadPoolSize"))
            threadPoolSize = config.getInt("threadPoolSize");

        if(threadPoolSize <= 0){
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }else{
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        }

        if(config.isset("locale")){
            String[] localeTmpString = config.getString("locale").split("-");
            locale = new Locale(localeTmpString[0],localeTmpString[1]);
        }

        if(config.isset("timezone"))
            timezone = ZoneId.of(config.getString("timezone"));

        if(config.isset("port"))
            port = config.getInt("port");

        if(config.isset("bindAddress"))
            bindAddress = config.getString("bindAddress");
        else if(config.isset("bindingAddress"))
            bindAddress = config.getString("bindingAddress");

        if(config.isset("serverRoot"))
            serverRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+config.getString("serverRoot");
        else
            serverRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+serverRoot;
        endchar = serverRoot.charAt(serverRoot.length()-1);

        if(endchar != '/'){
            serverRoot +="/";
        }
        
        if(config.isset("webRoot"))
            webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+config.getString("webRoot");
        else
            webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+webRoot;
        
        endchar = webRoot.charAt(webRoot.length()-1);

        if(endchar != '/'){
            webRoot +="/";
        }
        
        
        
        if(config.isset("assets"))
            assets = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+config.getString("assets");
        else
            assets = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+assets;

        endchar = assets.charAt(assets.length()-1);

        if(endchar != '/'){
            assets +="/";
        }
        File assetsFile = new File(assets);
        if(assetsFile.exists())
            minifier = new Minifier(assetsFile,webRoot,"minified");

        if(config.isset("charset"))
            charset = config.getString("charset");

        if(config.isset("timeout"))
            timeout = config.getInt("timeout");

        if(config.isset("sessionTtl"))
            sessionTtl = config.getInt("sessionTtl");

        if(config.isset("webSocketMtu"))
            webSocketMtu = config.getInt("webSocketMtu");

        if(config.isset("httpMtu"))
            httpMtu = config.getInt("httpMtu");

        if(config.isset("entryPoint"))
            entryPoint = config.getString("entryPoint");

        AsciiTable st = new AsciiTable();
        st.add("Key","Value");
        st.add("locale",""+locale.toString());
        st.add("timezone",""+timezone.toString());
        st.add("port",""+port);
        st.add("bindAddress",bindAddress);
        st.add("serverRoot",serverRoot);
        st.add("webRoot",webRoot);
        st.add("charset",charset);
        st.add("timeout",""+timeout+" milliseconds");
        st.add("sessionTtl",""+sessionTtl+" seconds");
        st.add("webSocketMtu",""+webSocketMtu+" bytes");
        st.add("httpMtu",""+httpMtu+" bytes");
        st.add("entryPoint",""+entryPoint);
        st.add("minify",minify+" milliseconds");
        st.add("threadPoolSize",threadPoolSize+" Threads");
        st.add("sendExceptions",sendExceptions?"True":"False");
        st.add("responseWrapper",responseWrapper?"True":"False");

        //checking for SMTP server
        if(config.isset("smtp")){
            AsciiTable smtpt = new AsciiTable();
            smtpt.add("Attribute","Value");
            JsonObject smtp = config.get("smtp").getAsJsonObject();
            if(smtp.has("allow")){
                smtpAllowed = smtp.get("allow").getAsBoolean();
                smtpt.add("allow",smtp.get("allow").getAsString());
                if(smtpAllowed){
                    String smtpBindAddress = bindAddress;
                    if(smtp.has("bindAddress")){
                        smtpBindAddress = smtp.get("bindAddress").getAsString();
                    }
                    int smtpPort = 25;
                    if(smtp.has("smtpPort")){
                        smtpPort = smtp.get("smtpPort").getAsInt();
                    }
                    smtpt.add("bindAddress",smtpBindAddress);
                    smtpt.add("port",""+smtpPort);
                    if(smtp.has("hostname")){
                        String smtpHostname = smtp.get("hostname").getAsString();
                        smtpt.add("hostname",smtpHostname);
                        smtpServer = new SmtpServer(new ServerSocket(),smtpBindAddress,smtpPort,smtpHostname);
                        
                        new Thread(smtpServer).start();
                    }else{
                        System.err.println("\n[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
                    }

                }
            }
            st.add("smtp",smtpt.toString());
        }

        if(config.isset("groups")){
            JsonObject groups = (JsonObject) config.get("groups");
            groupsAllowed = groups.get("allow").getAsBoolean();
        }
        AsciiTable gt = new AsciiTable();
        gt.add("Attribute","Value");
        gt.add("allow",groupsAllowed?"True":"False");
        st.add("groups",gt.toString());

        AsciiTable pathsTable = new AsciiTable();
        pathsTable.add("Type","Name");
        ROUTES.entrySet().forEach((entry) -> {
            String type = Regex.extract(entry.getKey(), "^(.*?(?=\\/)|.*)", Pattern.CASE_INSENSITIVE);
            String name = entry.getKey().substring(type.length());
            pathsTable.add(type,name);
        });
        st.add("Paths",pathsTable.toString());
        
        if(config.isset("certificate")){
            JsonObject certificate_obj = config.get("certificate").getAsJsonObject();


            String certificate_name = certificate_obj.get("name").getAsString();

            String certificate_type = certificate_obj.get("type").getAsString();

            String certificate_password = certificate_obj.get("password").getAsString();

            AsciiTable certt = new AsciiTable();
            certt.add("Attribute","Value");
            certt.add("name",certificate_name);
            certt.add("type",certificate_type);
            certt.add("password","***");

            st.add("certificate",certt.toString());
            System.out.println("\nServer started.");

            minify();
            
            System.out.println(st.toString());
            
            
            final char[] password = certificate_password.toCharArray();
            final KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(new File(configDir+"/"+certificate_name), password);
                final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
                keyManagerFactory.init(keyStore, password);
                final SSLContext context = SSLContext.getInstance("TLSv1.2");//"SSL" "TLS"
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
                
                final SSLServerSocketFactory factory = context.getServerSocketFactory();
                try (SSLServerSocket ss = ((SSLServerSocket) factory.createServerSocket())) {
                    ss.bind(new InetSocketAddress(bindAddress, port));
                    HttpEventListener listener;
                    while(listen){
                        listener = new HttpEventListener(this,ss.accept());
                        executor.submit(listener);
                    }
                }
            } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(bindAddress, port));
            System.out.println("\nServer started.");

            minify();

            System.out.println(st.toString());

            HttpEventListener listener;
            while(listen){
                listener = new HttpEventListener(this,ss.accept());
                executor.submit(listener);
            }
        }

    }

    private void minify() throws IOException{
        if(minify > 0 && minifier != null) {
            minifier.minify();
            System.out.println("Files minified.");
        }else if(minify < 0 && minifier != null){
            minifier.minify(false);
            System.out.println("Files glued but not minified.");
        }
        if((minify > 0 || minify < 0) && minifier != null) {
            System.out.println("Server will minify files in background once every "+minify+"ms.");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            if(minify < 0){
                                Thread.sleep(-minify);
                                minifier.minify(false);
                            }else{
                                Thread.sleep(minify);
                                minifier.minify();
                            }
                        } catch (InterruptedException | IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }).start();
        }
    }
    
    /**
     * Creates an SSLContext which can be used to generate Secure Sockets.
     * @param tlsCertificate your tls certificate file location.
     * @param certificateType your tls certificate type.
     * @param tlsPassword your tls certificate password
     * @return an SSLContext generated from your certificate.
     */
    private static SSLContext createSSLContext(String tlsCertificate, String certificateType, String tlsPassword){
        System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        try{
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance(certificateType);

            try (InputStream is = new FileInputStream(tlsCertificate)) {
                keyStore.load(is,tlsPassword.toCharArray());
            }

            keyManagerFactory.init(keyStore, tlsPassword.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

            sslContext.init(km,  tm, null);

            return sslContext;
        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex){
            ex.printStackTrace(System.out);
        }

        return null;
    }
}
