package com.github.tncrazvan.arcano.Smtp;

import com.github.tncrazvan.arcano.SharedObject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author razvan
 */
public class SmtpServer extends SharedObject implements Runnable{

    /**
     * @param args the command line arguments
     */
    
    private final ArrayList<SmtpListener> listeners = new ArrayList<>();
    private final ServerSocket ss;
    private String hostname = "";
    
    public SmtpServer(final ServerSocket ss, final String bindAddress, final int port, final String hostname)
            throws IOException {
        this.ss = ss;
        ss.bind(new InetSocketAddress(bindAddress, port));
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public void run() {
        while (config.listen) {
            try {
                final EmailReader emailReader = new EmailReader(this, ss.accept(), listeners);
                emailReader.parse();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addEventListener(final SmtpListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(final SmtpListener listener) {
        listeners.remove(listener);
    }
}
