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
        final String settingsPath = new File(args[0]).getParent().toString();
        
        Settings.parse(args[0]);
        if(Settings.isset("port"))
            port = Settings.getInt("port");
        
        if(Settings.isset("bindAddress"))
            bindAddress = Settings.getString("bindAddress");
        
        if(Settings.isset("webRoot"))
            webRoot = new File(args[0]).getParent()+"/"+Settings.getString("webRoot");
        else
            webRoot = new File(args[0]).getParent()+"/"+webRoot;
        
        if(Settings.isset("charset"))
            charset = Settings.getString("charset");
        
        if(Settings.isset("timeout"))
            timeout = Settings.getInt("timeout");
        
        AsciiTable st = new AsciiTable("Socket");
        st.addColumn(15);
        st.addColumn(10);
        st.addRow("Attribute","Value");
        st.addRow("port",""+port);
        st.addRow("bind address",bindAddress);
        st.addRow("web root",webRoot);
        st.addRow("charset",charset);
        st.addRow("timeout (ms)",""+timeout);
        st.draw();
        System.out.println("\n");
        if(Settings.isset("controllers")){
            JsonObject controllers = Settings.get("controllers").getAsJsonObject();

            httpControllerPackageName = controllers.get("http").getAsString();

            wsControllerPackageName = (
                    controllers
                    .has("websocket")?
                        controllers
                                .get("websocket")
                                .getAsString():
                        controllers
                                .get("ws")
                                .getAsString());
            
            
        }
        
        AsciiTable ct = new AsciiTable("Controllers");
        ct.addColumn(15);
        ct.addColumn(10);
        ct.addRow("type","Package Name");
        ct.addRow("http",""+httpControllerPackageName);
        ct.addRow("websocket",""+wsControllerPackageName);
        ct.draw();
        
        //checking for SMTP server
        if(Settings.isset("smtp")){
            AsciiTable smtpt = new AsciiTable("SMTP");
            smtpt.addRow("Attribute","Value");
            JsonObject smtp = Settings.get("smtp").getAsJsonObject();
            if(smtp.has("allow")){
                smtpAllowed = smtp.get("allow").getAsBoolean();
                smtpt.addRow("allow",smtp.get("allow").getAsString());
                if(smtpAllowed){
                    String smtpBindAddress = bindAddress;
                    if(smtp.has("bindAddress")){
                        smtpBindAddress = smtp.get("bindAddress").getAsString();
                    }
                    smtpt.addRow("bind address",smtpBindAddress);
                    if(smtp.has("hostname")){
                        String smtpHostname = smtp.get("hostname").getAsString();
                        smtpt.addRow("hostname",smtpHostname);
                        smtpServer = new SmtpServer(new ServerSocket(),smtpBindAddress,25,smtpHostname);
                        new Thread(smtpServer).start();
                    }else{
                        System.err.println("\n[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
                    }
                    
                }
            }
            
            System.out.println("\n");
            smtpt.draw();
        }
        
        if(Settings.isset("groups")){
            JsonObject groups = (JsonObject) Settings.get("groups");
            AsciiTable gt = new AsciiTable("Groups Settings");
            gt.addRow("Attribute","Value");
            gt.addRow("allow",groups.get("allow").getAsString());
            System.out.println("\n");
            gt.draw();
        }
        
        if(port == 443){
            AsciiTable certt = new AsciiTable("Certificate");
            
            certt.addRow("Attribute","Value");
            
            JsonObject certificate_obj = Settings.get("certificate").getAsJsonObject();
            
            String certificate_name = certificate_obj.get("name").getAsString();
            
            String certificate_type = certificate_obj.get("type").getAsString();
            
            String certificate_password = certificate_obj.get("password").getAsString();
            
            certt.addRow("name",certificate_name);
            certt.addRow("type",certificate_type);
            certt.addRow("password","[Password matches]");
            
            System.out.println("\n");
            certt.draw();
            
            SSLContext sslContext = createSSLContext(settingsPath+"/"+certificate_name,certificate_type,certificate_password);
            
            
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket ssl = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            ssl.bind(new InetSocketAddress(bindAddress, port));
            init();
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
