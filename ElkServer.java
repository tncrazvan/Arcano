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

import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import elkserver.Http.HttpEventListener;
import java.io.File;
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
public abstract class ElkServer {
    private static final String message = "\n\n"+
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
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     */
    public static void main (String[] args) throws IOException, NoSuchAlgorithmException{
        class ConsoleServlet extends ElkServer{
            @Override
            public void init() {
                System.out.println(message);
            }
        }
        
        ConsoleServlet sc = new ConsoleServlet();
        sc.listen(args);
        
    }
    
    public abstract void init();
    
    public void listen(String[] args) throws IOException, NoSuchAlgorithmException {
        String 
                logLineSeparator = "\n=================================",
                bindAddress = "127.0.0.1",
                settings = "",
                web_root,
                tmp =args[0].substring(args[0].length()-1, args[0].length());
        if(args.length > 0){
            if(!tmp.equals("/") || !tmp.equals("\\")){
                settings = args[0]+"/";
            }else{
                settings = args[0];
            }
        }
        Settings.parse(settings);
        System.out.println(logLineSeparator+"\n###Reading port");
        ELK.PORT = Settings.getInt("port");
        
        System.out.println(">>>port:"+ELK.PORT+" [OK]");
        System.out.println(logLineSeparator+"\n###Reading bind_address");
        bindAddress = Settings.getString("bind_address");
        System.out.println(">>>bind_address:"+bindAddress+" [OK]");
        System.out.println(logLineSeparator+"\n###Reading web_root");
        web_root = Settings.getString("web_root");
        System.out.println(">>>web_root:"+web_root+" [OK]");
        ELK.PUBLIC_WWW = new File(settings).getParent()+"/"+web_root;
        System.out.println(logLineSeparator+"\n###Reading charset");
        ELK.CHARSET = Settings.getString("charset");
        System.out.println(">>>charset:"+ELK.CHARSET+" [OK]");
        System.out.println(logLineSeparator+"\n###Reading controllers");
        JsonObject controllers = Settings.get("controllers").getAsJsonObject();
        System.out.println(">>>controllers:[object] [OK]");
        System.out.println(logLineSeparator+"\n###Reading controllers.http");
        ELK.HTTP_CONTROLLER_PACKAGE_NAME = controllers.get("http").getAsString();
        System.out.println(">>>controllers.http:"+ELK.HTTP_CONTROLLER_PACKAGE_NAME+" [OK]");
        System.out.println(logLineSeparator+"\n###Reading controllers.websocket");
        ELK.WS_CONTROLLER_PACKAGE_NAME = controllers.get("websocket").getAsString();
        System.out.println(">>>controllers.websocket:"+ELK.WS_CONTROLLER_PACKAGE_NAME+" [OK]");
        if(ELK.PORT == 443){
            System.out.println(logLineSeparator+"\n###Reading tls");
            JsonObject tls = Settings.get("tls").getAsJsonObject();
            System.out.println(">>>tls:[object] [OK]");
            System.out.println(logLineSeparator+"\n###Reading tls.certificate");
            String tls_certificate = tls.get("certificate").getAsString();
            System.out.println(">>>tls.certificate:"+tls_certificate+" [OK]");
            System.out.println(logLineSeparator+"\n###Reading tls.certificate_type");
            String certificate_type = tls.get("certificate_type").getAsString();
            System.out.println(">>>tls.certificate_type:"+certificate_type+" [OK]");
            System.out.println(logLineSeparator+"\n###Reading tls.password");
            String password = tls.get("password").getAsString();
            System.out.println(">>>tls.password:***[OK]");
            SSLContext sslContext = createSSLContext(ELK.PUBLIC_WWW+"../"+tls_certificate,certificate_type,password);
            
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket ssl = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            ssl.bind(new InetSocketAddress(bindAddress, ELK.PORT));
            init();
            System.out.println("===== SERVER LISTENING =====");
            while(true){
                new HttpEventListener(ssl.accept()).start();
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(bindAddress, ELK.PORT));
            System.out.println("===== SERVER LISTENING =====");
            init();
            while(true){
                new HttpEventListener(ss.accept()).start();
            }
        }
        
    }
    
    private SSLContext createSSLContext(String tlsCertificate, String certificateType, String tlsPassword){
        System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        try{
            KeyStore keyStore = KeyStore.getInstance(certificateType);
            keyStore.load(new FileInputStream(tlsCertificate),tlsPassword.toCharArray());
             
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
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
