package com.github.tncrazvan.arcano;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.github.tncrazvan.arcano.Configuration.Threads;
import com.github.tncrazvan.arcano.http.HttpController;
import com.github.tncrazvan.arcano.http.HttpRequestReader;
import com.github.tncrazvan.arcano.smtp.SmtpServer;
import com.github.tncrazvan.arcano.websocket.WebSocketCommit;
import com.github.tncrazvan.arcano.websocket.WebSocketController;
import com.github.tncrazvan.arcano.websocket.WebSocketEventManager;

/**
 *
 * @author Razvan Tanase
 */
public class Arcano extends SharedObject {
    private static SmtpServer smtpServer;
    public static void main (final String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, URISyntaxException{
        if(args.length == 0) {
            System.out.println("No arguments provided. Server won't start.");
            return;
        }
        new Arcano(Arcano.class.getPackage()).listen(args);
    }

    
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     * Source: https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection#answer-520344
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    
    /**
    * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
    * Source: https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection#answer-520344
    * @param packageName The base package
    * @return The classes
    * @throws ClassNotFoundException
    * @throws IOException
    */
   private static Class<?>[] getClasses(Package pckg) throws ClassNotFoundException, IOException {
       String packageName = pckg.getName();
       ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
       assert classLoader != null;
       String path = packageName.replace('.', '/');
       Enumeration<URL> resources = classLoader.getResources(path);
       List<File> dirs = new ArrayList<>();
       while (resources.hasMoreElements()) {
           URL resource = resources.nextElement();
           dirs.add(new File(resource.getFile()));
       }
       ArrayList<Class<?>> classes = new ArrayList<>();
       for (File directory : dirs) {
           classes.addAll(findClasses(directory, packageName));
       }
       return classes.toArray(new Class<?>[classes.size()]);
   }
    
    
    /**
     * Make a new Arcano Server
     * 
     * @param pckg The package that contains your services.<br />
     * The package must be contained inside the current project's directory layout.
     */
    public Arcano(Package pckg) {
        expose(pckg);
    }
    public Arcano(){}
    
    public final void expose(Package pckg){
        try {
            Class<?>[] clss = getClasses(pckg);
            for(Class<?> cls : clss){
                try{
                    Object o = cls.getDeclaredConstructor().newInstance();
                    boolean a = o instanceof HttpController;
                    boolean b = o instanceof WebSocketController;
                     if(a || b) expose(cls);
                }catch(NoSuchMethodException e){
                    System.out.println("Class "+cls.getName()+" skipped while looking for services because it does not have a valid constructor.");
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException  ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public final void exposeDefaults(){
        expose(
            com.github.tncrazvan.arcano.controller.websocket.ControllerNotFound.class
        );
        if(config.webSocket.groups.enabled)
            expose(com.github.tncrazvan.arcano.controller.websocket.WebSocketGroupApi.class);
    }
    /**
     * Starts the server listening.
     * 
     * @param args First argument must be the settings file. Check documentation to
     *             learn how to create a settings files.
     * @param action Action to be run before each connection.
     */
    public final void listen(String[] args) {
        System.out.println("ARGS: " + Arrays.toString(args));

        try {
            config.parse(args[0], this, args);

            switch(config.threads.policy){
                case Threads.POLICY_FIX:
                    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.threads.pool);
                    break;
                case Threads.POLICY_CACHE:
                    executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                    break;
                case Threads.POLICY_STEAL:
                default:
                    if(config.threads.pool == 0)
                        service = Executors.newWorkStealingPool();
                    else
                        service = Executors.newWorkStealingPool(config.threads.pool);
                    break;
            }


            System.out.println("Caller Working Directory: "+config.callerDir);
            System.out.println("Config Working Directory: "+config.dir);

            if(config.smtp.enabled)
                if (!config.smtp.hostname.equals("")) {
                    smtpServer = new SmtpServer(new ServerSocket(), this,config.smtp.bindAddress, config.smtp.port,
                            config.smtp.hostname);
                    new Thread(smtpServer).start();
                } else {
                    System.err.println("\n[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
                }
            
            //push websocket commits
            Runnable webSocketPushRunnable = () -> {
                oldWebSocketEventManager.clear();
                int available = 0;
                while(true){
                    for (WebSocketEventManager manager : this.webSocketEventManager.values()) {
                        if(!manager.isConnected()){
                            oldWebSocketEventManager.add(manager);
                            continue;
                        }
                        InputStream read = manager.getRead();
                        if(read != null){
                            try {
                                if((available = read.available()) > 0 && manager.isConnected()){
                                    manager.unmask(read.readNBytes(available<config.webSocket.mtu?available:config.webSocket.mtu));
                                }
                                if(manager.getCommits().size() > 0){
                                    for (WebSocketCommit commit : manager.getCommits()) {
                                        manager.push(commit);
                                    }
                                }
                            } catch (IOException ex) {
                                oldWebSocketEventManager.add(manager);
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    
                    //remove old events
                    for (WebSocketEventManager element : oldWebSocketEventManager) {
                        this.webSocketEventManager.remove(element.getUuid());
                    }
                    oldWebSocketEventManager.clear();
                }
            };
            new Thread(webSocketPushRunnable).start();
            
            if (!config.certificate.name.equals("")) try {
                final char[] password = config.certificate.password.toCharArray();
                final KeyStore keyStore = KeyStore.getInstance(new File(config.dir + "/" + config.certificate.name),password);
                final TrustManagerFactory trustManagerFactory = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
                keyManagerFactory.init(keyStore, password);
                final SSLContext context = SSLContext.getInstance("TLSv1.2");// "SSL" "TLS"
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                final SSLServerSocketFactory factory = context.getServerSocketFactory();
                try (SSLServerSocket ss = ((SSLServerSocket) factory.createServerSocket())) {
                    ss.setSoTimeout(this.config.timeout);
                    ss.bind(new InetSocketAddress(config.bindAddress, config.port));
                    HttpRequestReader reader;
                    System.out.println("Server started (using TLSv1.2).");
                    while (config.listen) {
                        try{
                            reader = new HttpRequestReader(this, ss.accept());
                            if(executor == null)
                                service.submit(reader);
                            else
                                executor.submit(reader);
                        }catch(SocketTimeoutException e){
                            LOGGER.log(Level.SEVERE, null, e);
                            //System.out.println(String.format("Socket timed out after %s milliseconds.", this.config.timeout));
                        }
                    }
                }
            } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Arcano.class.getName()).log(Level.SEVERE, null, ex);
            } else {
                ServerSocket ss = new ServerSocket();
                //ss.setSoTimeout(this.config.timeout);
                ss.bind(new InetSocketAddress(config.bindAddress, config.port));
                HttpRequestReader reader;
                System.out.println("Server started.");
                while (config.listen) {
                    try {
                        reader = new HttpRequestReader(this, ss.accept());
                        if(executor == null)
                            service.submit(reader);
                        else
                            executor.submit(reader);
                    }catch(SocketTimeoutException e){
                        LOGGER.log(Level.SEVERE, null, e);
                        //System.out.println(String.format("Socket timed out after %s milliseconds.", this.config.timeout));
                    }catch (NoSuchAlgorithmException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
                ss.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
