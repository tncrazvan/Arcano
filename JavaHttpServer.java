/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import javahttpserver.Http.HttpEventListener;
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
public class JavaHttpServer {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){}
    public static void listen(String[] args) throws IOException, NoSuchAlgorithmException {
        
        if(args.length > 0){
            if(args[0].substring(args[0].length()-1, args[0].length()).equals("/")){
                JHS.PUBLIC_WWW = args[0].substring(0,args[0].length()-1);
            }else{
                JHS.PUBLIC_WWW = args[0];
            }
            if(args.length > 1) {
                JHS.PORT = Integer.parseInt(args[1]);
            }
        }
        
        Settings.parse();
        JHS.DOMAIN_NAME = Settings.getString("DOMAIN_NAME");
        JHS.BIND_ADDRESS = Settings.getString("BIND_ADDRESS");
        JHS.HTTP_CONTROLLER_PACKAGE_NAME = Settings.getString("HTTP_CONTROLLER_PACKAGE_NAME");
        JHS.WS_CONTROLLER_PACKAGE_NAME = Settings.getString("WS_CONTROLLER_PACKAGE_NAME");
        
        JHS.HTTPS_CERTIFICATE = Settings.getString("HTTPS_CERTIFICATE");
        JHS.HTTPS_CERTIFICATE_PASSWORD = Settings.getString("HTTPS_CERTIFICATE_PASSWORD");
        
        if(JHS.PORT == 443){
            JHS.HTTPS_CERTIFICATE = args[2];
            JHS.HTTPS_CERTIFICATE_PASSWORD = args[3];
            
            SSLContext sslContext = createSSLContext();
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket ssl = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            ssl.bind(new InetSocketAddress(JHS.BIND_ADDRESS, JHS.PORT));
            while(true){
                new HttpEventListener(ssl.accept()).start();
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(JHS.BIND_ADDRESS, JHS.PORT));
            while(true){
                new HttpEventListener(ss.accept()).start();
            }
        }
        
    }
    
    private static SSLContext createSSLContext(){
        System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(JHS.HTTPS_CERTIFICATE),JHS.HTTPS_CERTIFICATE_PASSWORD.toCharArray());
             
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, JHS.HTTPS_CERTIFICATE_PASSWORD.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km,  tm, null);
             
            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }
         
        return null;
    }
}
