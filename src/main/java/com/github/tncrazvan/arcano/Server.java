/**
 * Arcano is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.tncrazvan.arcano;

import static com.github.tncrazvan.arcano.Common.minifier;
import static com.github.tncrazvan.arcano.Common.minify;
import com.github.tncrazvan.asciitable.AsciiTable;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import com.github.tncrazvan.arcano.Http.HttpEventListener;
import com.github.tncrazvan.arcano.SmtpServer.SmtpServer;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import com.github.tncrazvan.arcano.Tool.Minifier;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
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

/**
 *
 * @author Razvan
 */
public class Server extends Common implements JsonTools{
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
        
        settings.parse(args[0]);
        
        if(settings.isset("scripts"))
            scripts = settings.get("scripts").getAsString();
        
        if(settings.isset("compress")){
            compression = JSON_PARSER.fromJson(settings.get("compress").getAsJsonArray(), String[].class);
        }else{
            compression = new String[]{};
        }
            
        
        if(settings.isset("responseWrapper"))
            responseWrapper = settings.get("responseWrapper").getAsBoolean();
        
        if(settings.isset("sendExceptions"))
            sendExceptions = settings.get("sendExceptions").getAsBoolean();
        
        if(settings.isset("minify"))
            minify = settings.getInt("minify");
        
        if(settings.isset("threadPoolSize"))
            threadPoolSize = settings.getInt("threadPoolSize");
        
        if(threadPoolSize <= 0){
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }else{
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        }
            
        if(settings.isset("locale")){
            String[] localeTmpString = settings.getString("locale").split("-");
            locale = new Locale(localeTmpString[0],localeTmpString[1]);
        }
        
        if(settings.isset("timezone"))
            timezone = ZoneId.of(settings.getString("timezone"));
        
        if(settings.isset("port"))
            port = settings.getInt("port");
        
        if(settings.isset("bindAddress"))
            bindAddress = settings.getString("bindAddress");
        else if(settings.isset("bindingAddress"))
            bindAddress = settings.getString("bindingAddress");
        
        if(settings.isset("webRoot"))
            webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+settings.getString("webRoot");
        else
            webRoot = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+webRoot;
        
        char endchar = webRoot.charAt(webRoot.length()-1);
        
        if(endchar != '/'){
            webRoot +="/";
        }
        
        if(settings.isset("assets"))
            assets = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+settings.getString("assets");
        else
            assets = new File(args[0]).getParent().replaceAll("\\\\", "/")+"/"+assets;
        
        endchar = assets.charAt(assets.length()-1);
        
        if(endchar != '/'){
            assets +="/";
        }
        File assetsFile = new File(assets);
        if(assetsFile.exists())
            minifier = new Minifier(assetsFile,webRoot,"minified");
            
        if(settings.isset("charset"))
            charset = settings.getString("charset");
        
        if(settings.isset("timeout"))
            timeout = settings.getInt("timeout");
        
        if(settings.isset("sessionTtl"))
            sessionTtl = settings.getInt("sessionTtl");
        
        if(settings.isset("wsMtu"))
            wsMtu = settings.getInt("wsMtu");
        
        if(settings.isset("httpMtu"))
            httpMtu = settings.getInt("httpMtu");
        
        if(settings.isset("entryPoint"))
            entryPoint = settings.getString("entryPoint");
        
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
        st.add("wsMtu",""+wsMtu+" bytes");
        st.add("httpMtu",""+httpMtu+" bytes");
        st.add("entryPoint",""+entryPoint);
        st.add("minify",minify+" milliseconds");
        st.add("threadPoolSize",threadPoolSize+" Threads");
        st.add("sendExceptions",sendExceptions?"True":"False");
        st.add("responseWrapper",responseWrapper?"True":"False");
        
        //checking for SMTP server
        if(settings.isset("smtp")){
            AsciiTable smtpt = new AsciiTable();
            smtpt.add("Attribute","Value");
            JsonObject smtp = settings.get("smtp").getAsJsonObject();
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
        
        if(settings.isset("groups")){
            JsonObject groups = (JsonObject) settings.get("groups");
            groupsAllowed = groups.get("allow").getAsBoolean();
        }
        AsciiTable gt = new AsciiTable();
        gt.add("Attribute","Value");
        gt.add("allow",groupsAllowed?"True":"False");
        st.add("groups",gt.toString());
        
        if(settings.isset("certificate")){
            JsonObject certificate_obj = settings.get("certificate").getAsJsonObject();
            
            
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
                executor.submit(new HttpEventListener(ssl.accept()));
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(bindAddress, port));
            System.out.println("\nServer started.");
            
            minify();
            
            AsciiTable routesTable = new AsciiTable();
            routesTable.add("Path");
            routes.entrySet().forEach((entry) -> {
                routesTable.add(entry.getKey());
            });
            st.add("Routes",routesTable.toString());
            
            System.out.println(st.toString());
            
            while(listen){
                executor.submit(new HttpEventListener(ss.accept()));
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
