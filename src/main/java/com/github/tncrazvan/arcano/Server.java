package com.github.tncrazvan.arcano;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.github.tncrazvan.arcano.Http.HttpEventListener;
import com.github.tncrazvan.arcano.Smtp.SmtpServer;
import com.github.tncrazvan.arcano.Tool.Minifier;
import java.util.Arrays;
import javax.net.ssl.SSLServerSocket;

/**
 *
 * @author Razvan
 */
public class Server extends SharedObject {
    private static SmtpServer smtpServer;
    public static void main (String[] args) throws NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException, IOException{
        new Server(args);
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Starts the server listening.
     * @param args First argument must be the settings file. Check documentation to learn how to create a settings files.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws java.lang.ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public final void listen(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException {
        System.out.println("ARGS: "+Arrays.toString(args));

        config.parse(args[0],this, args);
        
        if(config.threadPoolSize <= 0){
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }else{
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.threadPoolSize);
        }

        File assetsFile = new File(config.assets);
        if(assetsFile.exists())
            minifier = new Minifier(assetsFile,config.webRoot,"minified");
        
        if(!config.smtp.hostname.equals("")){
            smtpServer = new SmtpServer(new ServerSocket(),config.smtp.bindAddress,config.smtp.port,config.smtp.hostname);

            new Thread(smtpServer).start();
        }else{
            System.err.println("\n[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
        }

        
        if(!config.certificate.name.equals("")){
            minify(executor);
            final char[] password = config.certificate.password.toCharArray();
            final KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(new File(config.dir+"/"+config.certificate.name), password);
                final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
                keyManagerFactory.init(keyStore, password);
                final SSLContext context = SSLContext.getInstance("TLSv1.2");//"SSL" "TLS"
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
                
                final SSLServerSocketFactory factory = context.getServerSocketFactory();
                try (SSLServerSocket ss = ((SSLServerSocket) factory.createServerSocket())) {
                    ss.bind(new InetSocketAddress(config.bindAddress, config.port));
                    HttpEventListener listener;
                    System.out.println("Server started (using TLSv1.2).");
                    while(config.listen){
                        listener = new HttpEventListener(this,ss.accept());
                        executor.submit(listener);
                    }
                }
            } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }else{
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(config.bindAddress, config.port));

            minify(executor);
            HttpEventListener listener;
            System.out.println("Server started.");
            while(config.listen){
                listener = new HttpEventListener(this,ss.accept());
                executor.submit(listener);
            }
        }

    }

    private final void minify(ThreadPoolExecutor executor) throws IOException{
        if(config.minify > 0 && minifier != null) {
            minifier.minify();
            System.out.println("Files minified.");
        }else if(config.minify < 0 && minifier != null){
            minifier.minify(false);
            System.out.println("Files glued but not minified.");
        }
        if((config.minify > 0 || config.minify < 0) && minifier != null) {
            System.out.println("Server will minify files in background once every "+config.minify+"ms.");
            executor.submit(() -> {
                while(true){
                    try {
                        if(config.minify < 0){
                            Thread.sleep(-config.minify);
                            minifier.minify(false);
                        }else{
                            Thread.sleep(config.minify);
                            minifier.minify();
                        }
                    } catch (InterruptedException | IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }
}
