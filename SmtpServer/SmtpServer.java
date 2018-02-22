/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.SmtpServer;

import elkserver.Elk;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author razvan
 */
public class SmtpServer extends Elk implements Runnable{

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
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
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
