/**
 * ElkServer is a Java library that makes it easier
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
package com.razshare.elkserver;

import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import com.razshare.elkserver.Http.HttpEventListener;
import com.razshare.elkserver.SmtpServer.SmtpServer;
import java.io.File;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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
public abstract class ElkServer extends Elk{
    private SmtpServer smtpServer;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     */
    private static ElkServer server;
    
    public static ElkServer getServer(){
        return server;
    }
    
    public static void main (String[] args) throws IOException, NoSuchAlgorithmException{
        class ConsoleWebServer extends ElkServer{
            @Override
            public void init() {
                
            }
        }
        
        
        server = new ConsoleWebServer();
        server.listen(args);
        
    }
    
    public abstract void init();
    
    public SmtpServer getSmtpServer(){
        return smtpServer;
    }
    
    
    /**
     * Starts the server listening.
     * @param args First argument must be the settings file. Check documentation to learn how to create a settings files.
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    public void listen(String[] args) throws IOException, NoSuchAlgorithmException {
        String settingsPath = new File(args[0]).getParent().toString();
        String logLineSeparator = "\n";

        Settings.parse(args[0]);
        //System.out.println(logLineSeparator+"\n[Reading port]");
        if(Settings.isset("port"))
            port = Settings.getInt("port");
        System.out.println("port => "+port);
        
        //System.out.println(logLineSeparator+"\n[Reading bindAddress]");
        if(Settings.isset("bindAddress"))
            bindAddress = Settings.getString("bindAddress");
        System.out.println("bindAddress => "+bindAddress);
        
        //System.out.println(logLineSeparator+"\n[Reading webRoot]");
        if(Settings.isset("webRoot"))
            webRoot = new File(args[0]).getParent()+"/"+Settings.getString("webRoot");
        else
            webRoot = new File(args[0]).getParent()+"/"+webRoot;
        
        System.out.println("webRoot => "+webRoot);
        
        //System.out.println(logLineSeparator+"\n[Reading charset]");
        if(Settings.isset("charset"))
            charset = Settings.getString("charset");
        System.out.println("charset => "+charset);
        
        //System.out.println(logLineSeparator+"[Reading timeout]");
        if(Settings.isset("timeout"))
            timeout = Settings.getInt("timeout");
        System.out.println("timeout => "+timeout);
        
        //System.out.println(logLineSeparator+"\n[Reading controllers]");
        if(Settings.isset("controllers")){
            JsonObject controllers = Settings.get("controllers").getAsJsonObject();
            System.out.println("controllers => [object]");

            System.out.println(logLineSeparator+"[Reading controllers.http]");
            httpControllerPackageName = controllers.get("http").getAsString();
            System.out.println("controllers.http => "+httpControllerPackageName);

            System.out.println(logLineSeparator+"[Reading controllers.websocket]");
            wsControllerPackageName = (
                    controllers
                    .has("websocket")?
                        controllers
                                .get("websocket")
                                .getAsString():
                        controllers
                                .get("ws")
                                .getAsString());
            System.out.println("controllers.websocket => "+wsControllerPackageName);
        }else{
            System.out.println("Using default controllers");
        }
        

        //checking for SMTP server
        if(Settings.isset("smtp")){
            //System.out.println(logLineSeparator+"\n[Reading smtp]");
            JsonObject smtp = Settings.get("smtp").getAsJsonObject();
            System.out.println("smtp => [object]");
            if(smtp.has("allow")){
                smtpAllowed = smtp.get("allow").getAsBoolean();
                System.out.println(logLineSeparator+"\t[Reading smtp.allow]");
                System.out.println("\tsmtp.allow => "+smtpAllowed);
                if(smtpAllowed){
                    String smtpBindAddress = bindAddress;
                    if(smtp.has("bindAddress")){
                        smtpBindAddress = smtp.get("bindAddress").getAsString();
                    }
                    if(smtp.has("hostname")){
                        smtpServer = new SmtpServer(new ServerSocket(),smtpBindAddress,25,smtp.get("hostname").getAsString());
                        new Thread(smtpServer).start();
                        System.out.println("\t[Smtp server started.]");
                    }else{
                        System.err.println("[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
                    }
                    
                }
            }
        }
        
        if(port == 443){
            System.out.println(logLineSeparator+"\n[Reading tls]");
            JsonObject tls = Settings.get("tls").getAsJsonObject();
            
            System.out.println(logLineSeparator+"\t[Reading tls.certificate]");
            String tls_certificate = tls.get("certificate").getAsString();
            System.out.println("\ttls.certificate => "+tls_certificate);
            
            System.out.println(logLineSeparator+"\t\n[Reading tls.certificateType]");
            String certificate_type = tls.get("certificateType").getAsString();
            System.out.println("\ttls.certificate_type:"+certificate_type);
            
            System.out.println(logLineSeparator+"\t\n[Reading tls.password]");
            String password = tls.get("password").getAsString();
            System.out.println("\ttls.password:***[OK]");
            
            SSLContext sslContext = createSSLContext(settingsPath+"/"+tls_certificate,certificate_type,password);
            
            
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket ssl = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            ssl.bind(new InetSocketAddress(bindAddress, port));
            init();
            System.out.println("\nServer listening...");
            while(listen){
                new Thread(new HttpEventListener(ssl.accept())).start();
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(bindAddress, port));
            System.out.println("\nServer listening...");
            init();
            while(listen){
                new Thread(new HttpEventListener(ss.accept())).start();
            }
        }
        
    }
    
    
    /**
     * Creates an SSLContext which can be used to generate Secure Sockets.
     * @param tlsCertificate your tls certificate file location.
     * @param certificateType your tls certificate type.
     * @param tlsPassword your tls certificate password
     * @return an SSLContext generated from your certificate.
     */
    private SSLContext createSSLContext(String tlsCertificate, String certificateType, String tlsPassword){
        System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        try{
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance(certificateType);
            
            InputStream is = new FileInputStream(tlsCertificate);
            keyStore.load(is,tlsPassword.toCharArray());
            is.close();
             
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
            ex.printStackTrace();
        }
         
        return null;
    }
}
