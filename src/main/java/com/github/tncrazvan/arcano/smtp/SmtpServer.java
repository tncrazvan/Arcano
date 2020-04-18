package com.github.tncrazvan.arcano.smtp;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.SharedObject;

/**
 *
 * @author Razvan Tanase
 */
public class SmtpServer implements Runnable{

    /**
     * @param args the command line arguments
     */
    
    private final ArrayList<SmtpListener> listeners = new ArrayList<>();
    private final ServerSocket ss;
    public final SharedObject so;
    private String hostname = "";
    
    public SmtpServer(final ServerSocket ss, SharedObject so, final String bindAddress, final int port, final String hostname)
            throws IOException {
        this.ss = ss;
        this.so = so;
        ss.bind(new InetSocketAddress(bindAddress, port));
        this.hostname = hostname;
    }

    public final String getHostname() {
        return hostname;
    }

    @Override
    public final void run() {
        while (this.so.config.listen) {
            try {
                final EmailReader emailReader = new EmailReader(this, ss.accept(), listeners);
                emailReader.parse();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public final void addEventListener(final SmtpListener listener) {
        listeners.add(listener);
    }

    public final void removeEventListener(final SmtpListener listener) {
        listeners.remove(listener);
    }
}
