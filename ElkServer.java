/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
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
package elkserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import elkserver.Http.HttpEventListener;
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
public abstract class ElkServer {
    private static final String message = 
"  ElkServer is a Java library that makes it easier\n" +
"  to program and manage a Java servlet by providing different tools\n" +
"  such as:\n" +
"  1) An MVC (Model-View-Controller) alike design pattern to manage \n" +
"     client requests without using any URL rewriting rules.\n" +
"  2) A WebSocket Manager, allowing the server to accept and manage \n" +
"     incoming WebSocket connections.\n" +
"  3) Direct access to every socket bound to every client application.\n" +
"  4) Direct access to the headers of the incomming and outgoing Http messages.\n" +
"  Copyright (C) 2016-2018  Tanase Razvan Catalin\n" +
"  \n" +
"  This program is free software: you can redistribute it and/or modify\n" +
"  it under the terms of the GNU Affero General Public License as\n" +
"  published by the Free Software Foundation, either version 3 of the\n" +
"  License, or (at your option) any later version.\n" +
"  \n" +
"  This program is distributed in the hope that it will be useful,\n" +
"  but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
"  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
"  GNU Affero General Public License for more details.\n" +
"  \n" +
"  You should have received a copy of the GNU Affero General Public License\n" +
"  along with this program.  If not, see <https://www.gnu.org/licenses/>.\n\n";
    /**
     * @param args the command line arguments
     */
    public static void main (String[] args) throws IOException, NoSuchAlgorithmException{
        class ConsoleServlet extends ElkServer{
            @Override
            public void init() {
                System.out.println(message);
            }
        }
        
        ConsoleServlet sc = new ConsoleServlet();
        sc.listen(new String[]{
            "C:\\Users\\razvan\\Documents\\NetBeansProjects\\ElkServer\\src\\elkserver\\docs",
            "80"
        });
        
    }
    
    public abstract void init();
    
    public void listen(String[] args) throws IOException, NoSuchAlgorithmException {
        if(args.length > 0){
            if(args[0].substring(args[0].length()-1, args[0].length()).equals("/")){
                ELK.PUBLIC_WWW = args[0].substring(0,args[0].length()-1);
            }else{
                ELK.PUBLIC_WWW = args[0];
            }
            if(args.length > 1) {
                ELK.PORT = Integer.parseInt(args[1]);
            }
        }
        Settings.parse();
        if(Settings.isset("CHARSET"))
            ELK.CHARSET = Settings.getString("CHARSET");
        if(Settings.isset("BIND_ADDRESS"))
            ELK.BIND_ADDRESS = Settings.getString("BIND_ADDRESS");
        if(Settings.isset("HTTP_CONTROLLER_PACKAGE_NAME"))
            ELK.HTTP_CONTROLLER_PACKAGE_NAME = Settings.getString("HTTP_CONTROLLER_PACKAGE_NAME");
        if(Settings.isset("WS_CONTROLLER_PACKAGE_NAME"))
            ELK.WS_CONTROLLER_PACKAGE_NAME = Settings.getString("WS_CONTROLLER_PACKAGE_NAME");
        if(Settings.isset("SSL_CERTIFICATE"))
            ELK.SSL_CERTIFICATE = Settings.getString("HTTPS_CERTIFICATE");
        if(Settings.isset("SSL_CERTIFICATE_PASSWORD"))
            ELK.SSL_CERTIFICATE_PASSWORD = Settings.getString("HTTPS_CERTIFICATE_PASSWORD");
        if(ELK.PORT == 443){
            if(!Settings.isset("SSL_CERTIFICATE") || !Settings.isset("SSL_CERTIFICATE_PASSWORD")){
                if(!Settings.isset("SSL_CERTIFICATE")){
                    System.err.println("SSL certificate missing");
                }
                if(!Settings.isset("SSL_CERTIFICATE_PASSWORD")){
                    System.err.println("SSL certificate password missing");
                }
            }else{
                ELK.SSL_CERTIFICATE = args[2];
                ELK.SSL_CERTIFICATE_PASSWORD = args[3];

                SSLContext sslContext = createSSLContext();
                // Create server socket factory
                SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

                // Create server socket
                SSLServerSocket ssl = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
                ssl.bind(new InetSocketAddress(ELK.BIND_ADDRESS, ELK.PORT));
                init();
                while(true){
                    new HttpEventListener(ssl.accept()).start();
                }
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(ELK.BIND_ADDRESS, ELK.PORT));
            init();
            while(true){
                new HttpEventListener(ss.accept()).start();
            }
        }
        
    }
    
    private SSLContext createSSLContext(){
        System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(ELK.SSL_CERTIFICATE),ELK.SSL_CERTIFICATE_PASSWORD.toCharArray());
             
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, ELK.SSL_CERTIFICATE_PASSWORD.toCharArray());
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
