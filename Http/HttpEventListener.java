/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            if(clientHeader.get("Connection").toLowerCase().equals("keep-alive")){
                try {
                    new HttpEvent(writer,clientHeader,client,post).execute();
                } catch (IOException ex) {
                    try {
                        client.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                
            }else if(clientHeader.get("Connection").equals("Upgrade") || clientHeader.get("Connection").equals("keep-alive, Upgrade")){
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
            }
        }
    }
    
}
