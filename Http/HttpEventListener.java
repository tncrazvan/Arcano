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
package elkserver.Http;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import elkserver.ELK;
import elkserver.WebSocket.WebSocketEvent;
/**
 *
 * @author Razvan
 */
public class HttpEventListener extends HttpRequestReader{
    private final String sessionId;
    public HttpEventListener(Socket client) throws IOException, NoSuchAlgorithmException{
        super(client);
        sessionId = ELK.getSha1String(System.identityHashCode(client)+"::"+System.currentTimeMillis());
    }
    
    @Override
    public void onRequest(HttpHeader clientHeader,JsonObject post) {
        if(clientHeader != null && clientHeader.get("Connection")!=null){
            if(clientHeader.get("Connection").equals("Upgrade") || clientHeader.get("Connection").equals("keep-alive, Upgrade")){
                if(clientHeader.get("Upgrade").equals("websocket")){
                    try {
                        new WebSocketEvent(reader, client, clientHeader, sessionId).execute();
                    }catch(IOException e){
                        try {
                            client.close();
                        } catch (IOException ex) {
                            Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else{
                try {
                    new HttpEvent(output,clientHeader,client,post).execute();
                } catch (IOException ex) {
                    try {
                        client.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                
            } 
        }
    }
    
}
