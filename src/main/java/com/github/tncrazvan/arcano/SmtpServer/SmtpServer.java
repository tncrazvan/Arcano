package com.github.tncrazvan.arcano.SmtpServer;

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
    
    public SmtpServer(ServerSocket ss,String bindAddress,int port,String hostname) throws IOException{
        this.ss=ss;
        ss.bind(new InetSocketAddress(bindAddress, port));
        this.hostname=hostname;
    }
    
    public String getHostname(){
        return hostname;
    }
    
    @Override
    public void run() {
        while(listen){
            try {
                EmailReader emailReader = new EmailReader(this,ss.accept(),listeners);
                emailReader.parse();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,null,ex);
            }
        }
    }
    
    public void addEventListener(SmtpListener listener){
        listeners.add(listener);
    }
    
    public void removeEventListener(SmtpListener listener){
        listeners.remove(listener);
    }
}
