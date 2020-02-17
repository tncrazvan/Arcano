package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Configuration.Threads;
import com.github.tncrazvan.arcano.Http.HttpController;
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
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.github.tncrazvan.arcano.Http.HttpEventListener;
import com.github.tncrazvan.arcano.Smtp.SmtpServer;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;
import com.github.tncrazvan.arcano.Tool.Actions.TypedAction;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.net.ssl.SSLServerSocket;

/**
 *
 * @author Razvan
 */
public class Arcano extends SharedObject {
    private static SmtpServer smtpServer;
    public static void main (final String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, URISyntaxException{
        new Arcano(Arcano.class.getPackage()).listen(args,(so) -> {
            so.config.pack("imports.json");
            return 1000L;
        });
    }

    
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     * Source: https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection#answer-520344
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
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
   private static Class[] getClasses(Package pckg) throws ClassNotFoundException, IOException {
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
       ArrayList<Class> classes = new ArrayList<>();
       for (File directory : dirs) {
           classes.addAll(findClasses(directory, packageName));
       }
       return classes.toArray(new Class[classes.size()]);
   }
    
    
    /**
     * Make a new Arcano Server
     * 
     * @param pckg The package that contains your services.
     */
    public Arcano(Package pckg) {
        try {
            expose(com.github.tncrazvan.arcano.Controller.Http.FileService.class,
                    com.github.tncrazvan.arcano.Controller.Http.ControllerNotFound.class,
                    com.github.tncrazvan.arcano.Controller.Http.Get.class,
                    com.github.tncrazvan.arcano.Controller.Http.Isset.class,
                    com.github.tncrazvan.arcano.Controller.Http.Set.class,
                    com.github.tncrazvan.arcano.Controller.Http.Unset.class,
                    
                    com.github.tncrazvan.arcano.Controller.WebSocket.ControllerNotFound.class,
                    com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi.class);
            
            Class[] clss = getClasses(pckg);
            for(Class cls : clss){
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

    /**
     * Starts the server listening.
     * 
     * @param args First argument must be the settings file. Check documentation to
     *             learn how to create a settings files.
     * @param action Action to be run before each connection.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws java.lang.ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public final void listen(String[] args, CompleteAction<Long,SharedObject> action)
            throws IOException, NoSuchAlgorithmException, ClassNotFoundException, URISyntaxException {
        System.out.println("ARGS: " + Arrays.toString(args));

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

        
        System.out.println("Absolute Workling Directory: "+FileSystems.getDefault().getPath(".").toAbsolutePath());
        
        if(config.smtp.enabled)
            if (!config.smtp.hostname.equals("")) {
                smtpServer = new SmtpServer(new ServerSocket(), config.smtp.bindAddress, config.smtp.port,
                        config.smtp.hostname);
                new Thread(smtpServer).start();
            } else {
                System.err.println("\n[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
            }
        
        if(action != null)
        new Thread(() -> {
            System.out.println("Public imports will be packed in background at @webRoot/pack.");
            for(;;){
                try {
                    action.callback(this);
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        
        if (!config.certificate.name.equals("")) try {
            final char[] password = config.certificate.password.toCharArray();
            final KeyStore keyStore = KeyStore.getInstance(new File(config.dir + "/" + config.certificate.name),
                    password);
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
            keyManagerFactory.init(keyStore, password);
            final SSLContext context = SSLContext.getInstance("TLSv1.2");// "SSL" "TLS"
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            final SSLServerSocketFactory factory = context.getServerSocketFactory();
            try (SSLServerSocket ss = ((SSLServerSocket) factory.createServerSocket())) {
                ss.bind(new InetSocketAddress(config.bindAddress, config.port));
                HttpEventListener listener;
                System.out.println("Server started (using TLSv1.2).");
                while (config.listen) {
                    
                    listener = new HttpEventListener(this, ss.accept());
                    if(executor == null)
                        service.submit(listener);
                    else
                        executor.submit(listener);
                }
            }
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        else try (ServerSocket ss = new ServerSocket()) {
            ss.bind(new InetSocketAddress(config.bindAddress, config.port));
            
            HttpEventListener listener;
            System.out.println("Server started.");
            while (config.listen) {
                listener = new HttpEventListener(this, ss.accept());
                if(executor == null)
                    service.submit(listener);
                else
                    executor.submit(listener);
            }
            ss.close();
        }
    }
}
