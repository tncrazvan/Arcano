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
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.github.tncrazvan.arcano.Http.HttpEventListener;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.SmtpServer.SmtpServer;
import com.github.tncrazvan.arcano.Tool.Action;
import com.github.tncrazvan.arcano.Tool.FileSystem;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import com.github.tncrazvan.arcano.Tool.Minifier;
import com.github.tncrazvan.arcano.Tool.Regex;
import com.github.tncrazvan.asciitable.AsciiTable;
import com.google.gson.JsonObject;
import java.util.regex.Pattern;
import com.github.tncrazvan.arcano.Tool.ServerFile;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import static com.github.tncrazvan.arcano.Tool.Regex.match;

/**
 *
 * @author Razvan
 */
public class Server extends SharedObject implements JsonTools{
    private static SmtpServer smtpServer;
    public static void main (String[] args) throws NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException, IOException{
        Server server = new Server();
        server.listen(args);
    }

    public Server(Class<?>... classes) {
        expose(
            com.github.tncrazvan.arcano.Controller.Http.App.class,
            com.github.tncrazvan.arcano.Controller.Http.ControllerNotFound.class,
            com.github.tncrazvan.arcano.Controller.Http.Get.class,
            com.github.tncrazvan.arcano.Controller.Http.Isset.class,
            com.github.tncrazvan.arcano.Controller.Http.Set.class,
            com.github.tncrazvan.arcano.Controller.Http.Unset.class,

            com.github.tncrazvan.arcano.Controller.WebSocket.ControllerNotFound.class,
            com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApplicationProgramInterface.class
        );
        expose(classes);
    }


    /**
     * Starts the server listening.
     * @param args First argument must be the settings file. Check documentation to learn how to create a settings files.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws java.lang.ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public void listen(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException {

        //System.out.println(Arrays.toString(args));

        configDir = new File(args[0]).getParent();

        config.parse(args[0]);

        if(config.isset("scripts")){
            scripts = config.getString("scripts");
            ArrayList<Class<?>> list = new ArrayList<>();
            ServerFile scriptsDir = new ServerFile(configDir,scripts);
            FileSystem.explore(scriptsDir,true,new Action<File>() {
                @Override
                public boolean callback(File f) {
                    if(f.isDirectory()) return true;
                    ServerFile file = new ServerFile(f);
                    if(match(file.getName(), "\\.java", Pattern.CASE_INSENSITIVE)){
                        try{
                            String relativePath = this.base.toURI().relativize(f.toURI()).getPath();
                            String className = Regex.replace(Regex.extract(relativePath,".*(?=\\.java(?=$))",Pattern.CASE_INSENSITIVE), "\\/", ".");
                            // Save source in .java file.
                            File root = new File("/java"); // On Windows running on C:\, this is C:\java.
                            File sourceFile = new File(root, relativePath);
                            sourceFile.getParentFile().mkdirs();
                            Files.write(sourceFile.toPath(), file.read());

                            // Compile source file.
                            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                            compiler.run(System.in, System.out, System.err, sourceFile.getPath());

                            // Load and instantiate compiled class.
                            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
                            Class<?> cls = Class.forName(className, true, classLoader);
                            list.add(cls);
                        } catch (SecurityException | IllegalArgumentException | ClassNotFoundException | IOException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                    return f.isDirectory();
                }
            });
            
            list.forEach((cls) -> {
                this.expose(true,cls);
            });
        }
        
        if(config.isset("compress")){
            compression = jsonParse(config.get("compress").getAsJsonArray(), String[].class);
        }else{
            compression = new String[]{};
        }


        if(config.isset("responseWrapper"))
            responseWrapper = config.get("responseWrapper").getAsBoolean();

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

        if(config.isset("webRoot"))
            webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+config.getString("webRoot");
        else
            webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+webRoot;

        char endchar = webRoot.charAt(webRoot.length()-1);

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
                    smtpt.add("bind address",smtpBindAddress);
                    if(smtp.has("hostname")){
                        String smtpHostname = smtp.get("hostname").getAsString();
                        smtpt.add("hostname",smtpHostname);
                        smtpServer = new SmtpServer(new ServerSocket(),smtpBindAddress,25,smtpHostname);
                        executor.submit(smtpServer);
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

        if(config.isset("certificate")){
            JsonObject certificate_obj = config.get("certificate").getAsJsonObject();


            String certificate_name = certificate_obj.get("name").getAsString();

            String certificate_type = certificate_obj.get("type").getAsString();

            String certificate_password = certificate_obj.get("password").getAsString();


            SSLContext sslContext = createSSLContext(configDir+"/"+certificate_name,certificate_type,certificate_password);

            AsciiTable certt = new AsciiTable();
            certt.add("Attribute","Value");
            certt.add("name",certificate_name);
            certt.add("type",certificate_type);
            certt.add("password","***");

            st.add("certificate",certt.toString());

            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket ssl = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            ssl.bind(new InetSocketAddress(bindAddress, port));
            System.out.println("\nServer started.");

            minify();

            System.err.println(st.toString());
            while(listen){
                executor.submit(new HttpEventListener(this,ssl.accept()));
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(bindAddress, port));
            System.out.println("\nServer started.");

            minify();

            AsciiTable routesTable = new AsciiTable();
            routesTable.add("Path");
            ROUTES.entrySet().forEach((entry) -> {
                routesTable.add(entry.getKey());
            });
            st.add("Routes",routesTable.toString());

            System.out.println(st.toString());

            while(listen){
                executor.submit(new HttpEventListener(this,ss.accept()));
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
