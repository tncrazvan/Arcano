/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Http;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javahttpserver.JHS;
import javahttpserver.WebSocket.WebSocketEvent;

/**
 *
 * @author Razvan
 */
public class HttpEventListener extends HttpRequestReader{
    private final String requestId;
    public HttpEventListener(Socket client) throws IOException, NoSuchAlgorithmException{
        super(client);
        requestId = new String(JHS.stringToSha1(System.identityHashCode(client)+"::"+System.currentTimeMillis()));
    }
    
    @Override
    public void onRequest(String result) {
        HttpHeader clientHeader = HttpHeader.fromString(result);
        if(clientHeader != null && clientHeader.get("Connection")!=null){
            if(clientHeader.get("Connection").equals("keep-alive")){
                try {
                    new HttpEvent(writer,clientHeader,client).execute();
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
                        new WebSocketEvent(reader, client, clientHeader, requestId).execute();
                    }catch(IOException e){
                        try {
                            client.close();
                        } catch (IOException ex) {
                            Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (InstantiationException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NoSuchMethodException ex) {
                        Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
}
