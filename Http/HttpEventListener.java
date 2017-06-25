/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Http;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javahttpserver.JHS;
import javahttpserver.WebSocket.WebSocketEvent;

/**
 *
 * @author Razvan
 */
public class HttpEventListener extends HttpRequestReader{
    private final String requestId;
    private JsonObject post = new JsonObject();
    public HttpEventListener(Socket client) throws IOException, NoSuchAlgorithmException{
        super(client);
        requestId = JHS.getSha1String(System.identityHashCode(client)+"::"+System.currentTimeMillis());
    }
    
    @Override
    public void onRequest(String result) {
        HttpHeader clientHeader = HttpHeader.fromString(result);
        if(clientHeader.get("Method").equals("POST")){
            try {
                String line = "";
                String currentObjectName = null;
                boolean canRead = true;
                String currentLabel = null,
                        currentValue = "";
                Pattern pattern1 = Pattern.compile("^Content-Disposition");
                Pattern pattern2 = Pattern.compile("(?<=name\\=\\\").*?(?=\\\")");
                Matcher matcher;
                
                while(canRead){
                    line = reader.readLine();
                    //System.out.println(line);
                    if(currentObjectName == null && line.matches("^\\-+.*$")){
                        currentObjectName = line;
                    }else if(currentObjectName != null && line.equals(currentObjectName)){
                        post.addProperty(currentLabel, currentValue);
                        currentLabel = null;
                        currentValue = "";
                    }else if(currentObjectName != null && line.equals(currentObjectName+"--")){
                        canRead = false;
                        post.addProperty(currentLabel, currentValue);
                    }else if(currentObjectName != null){
                        
                        matcher = pattern1.matcher(line);
                        if(matcher.find()){
                            matcher = pattern2.matcher(line);
                            if(matcher.find() && currentLabel == null){
                                currentLabel = matcher.group();
                            }
                        }else if(currentLabel != null && !line.equals("")){
                            currentValue = JHS.atob(line);
                        }
                    }
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(post);
        }
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
