package com.github.tncrazvan.arcano;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
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
import com.github.tncrazvan.arcano.http.HttpRequestReader;
import com.github.tncrazvan.arcano.smtp.SmtpServer;
import com.github.tncrazvan.arcano.websocket.WebSocketCommit;
import com.github.tncrazvan.arcano.websocket.WebSocketEventManager;

/**
 *
 * @author Razvan Tanase
 */
public class Arcano extends SharedObject {
    // private static SmtpServer smtpServer;
    public static void main(final String[] args)
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, URISyntaxException {
        if (args.length == 0) {
            System.out.println("No arguments provided. Server won't start.");
            return;
        }
        new Arcano().listen(args);
    }

    /**
     * Make a new Arcano Server
     * 
     * @param pckg The package that contains your services.<br />
     *             The package must be contained inside the current project's
     *             directory layout.
     */
    public Arcano() {
    }

    /**
     * Starts the server listening.
     * 
     * @param args   First argument must be the settings file. Check documentation
     *               to learn how to create a settings files.
     * @param action Action to be run before each connection.
     */
    public final void listen(String[] args) {
        System.out.println("ARGS: " + Arrays.toString(args));
        try {
            config.parse(args[0], this, args);
            configureThreadPoolPolicy();
            configureSmtpPolicy();
            pushWebSocketCommits();

            
            System.out.println("Caller Working Directory: " + config.callerDir);
            System.out.println("Config Working Directory: " + config.dir);

            if (!config.certificate.name.equals(""))
                try {
                    configureSecureServer();
                } catch (KeyStoreException | CertificateException | UnrecoverableKeyException
                        | KeyManagementException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(Arcano.class.getName()).log(Level.SEVERE, null, ex);
                }
            else {
                configureServer();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void configureServer() throws IOException {
        ServerSocket ss = new ServerSocket();
        // ss.setSoTimeout(this.config.timeout);
        ss.bind(new InetSocketAddress(config.bindAddress, config.port));
        HttpRequestReader reader;
        System.out.println("Server started.");
        while (config.listen) {
            try {
                reader = new HttpRequestReader(this, ss.accept());
                if (executor == null)
                    service.submit(reader);
                else
                    executor.submit(reader);
            } catch (SocketTimeoutException e) {
                LOGGER.log(Level.SEVERE, null, e);
                // System.out.println(String.format("Socket timed out after %s milliseconds.",
                // this.config.timeout));
            } catch (NoSuchAlgorithmException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        ss.close();
    }

    private void configureSecureServer() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, KeyManagementException, UnrecoverableKeyException {
        final char[] password = config.certificate.password.toCharArray();
        final KeyStore keyStore = KeyStore.getInstance(new File(config.dir + "/" + config.certificate.name), password);
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
                try {
                    reader = new HttpRequestReader(this, ss.accept());
                    if (executor == null)
                        service.submit(reader);
                    else
                        executor.submit(reader);
                } catch (SocketTimeoutException e) {
                    LOGGER.log(Level.SEVERE, null, e);
                    // System.out.println(String.format("Socket timed out after %s milliseconds.",
                    // this.config.timeout));
                }
            }
        }
    }

    private void configureSmtpPolicy() throws IOException {
        if (config.smtp.enabled)
            if (!config.smtp.hostname.equals("")) {
                SmtpServer smtpServer = new SmtpServer(new ServerSocket(), this, config.smtp.bindAddress,
                        config.smtp.port, config.smtp.hostname);
                new Thread(smtpServer).start();
            } else {
                System.err.println("\n[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
            }
    }

    private void configureThreadPoolPolicy() {
        switch (config.threads.policy) {
            case Threads.POLICY_FIX:
                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.threads.pool);
                break;
            case Threads.POLICY_CACHE:
                executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                break;
            case Threads.POLICY_STEAL:
            default:
                if (config.threads.pool == 0)
                    service = Executors.newWorkStealingPool();
                else
                    service = Executors.newWorkStealingPool(config.threads.pool);
                break;
        }
    }

    private void pushWebSocketCommits() {
        // push websocket commits
        Runnable webSocketPushRunnable = () -> {
            this.oldWebSocketEventManager.clear();
            int available = 0;
            Collection<WebSocketEventManager> collection;
            while (true) {
                collection = this.webSocketEventManager.values();
                for (WebSocketEventManager manager : collection) {
                    if (!manager.isConnected()) {
                        this.oldWebSocketEventManager.add(manager);
                        continue;
                    }
                    InputStream read = manager.getRead();
                    if (read != null) {
                        try {
                            if ((available = read.available()) > 0 && manager.isConnected()) {
                                manager.unmask(read.readNBytes(
                                        available < config.webSocket.mtu ? available : config.webSocket.mtu));
                            }
                            if (manager.getCommits().size() > 0) {
                                for (WebSocketCommit commit : manager.getCommits()) {
                                    manager.push(commit);
                                }
                            }
                        } catch (IOException ex) {
                            this.oldWebSocketEventManager.add(manager);
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                }

                // remove old events
                for (WebSocketEventManager element : this.oldWebSocketEventManager) {
                    this.webSocketEventManager.remove(element.getUuid());
                }
                this.oldWebSocketEventManager.clear();
                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };
        new Thread(webSocketPushRunnable).start();
    }
}
