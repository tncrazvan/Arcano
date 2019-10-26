/**
 * CatPaw is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
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
package com.github.tncrazvan.catpaw.SmtpServer;

import com.github.tncrazvan.catpaw.Common;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author razvan
 */
public class SmtpServer extends Common implements Runnable{

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
                logger.log(Level.SEVERE,null,ex);
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
