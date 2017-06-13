/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.WebSocket;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import javahttpserver.EditorActionHandler;
import javahttpserver.JHS;
import javahttpserver.WebSocket.WebSocketEvent;
import javahttpserver.WebSocket.WebSocketInterface;

/**
 *
 * @author Razvan
 */
public class TextEditor implements WebSocketInterface{
    public static final int 
        ACTION_LOGICAL_SAVE = 0,
        ACTION_PHYSICAL_SAVE = 1,
        ACTION_REQUEST_FILE = 2;
    private static EditorActionHandler eah = null;
    
    
    
    @Override
    public void onOpen(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Texteditor connection opened.");
    }

    @Override
    public void onMessage(WebSocketEvent e, String msg, ArrayList<String> args) {
        JsonObject o = JHS.JSON_PARSER.fromJson(msg, JsonObject.class); 
        
        switch(o.get("action").getAsInt()){
            case ACTION_LOGICAL_SAVE:
                e.broadcast(msg);
                eah.logicalSave(o.get("content"));
                break;
            case ACTION_PHYSICAL_SAVE:
                eah.physicalSave();
                break;
            case ACTION_REQUEST_FILE:
                try {
                    if(eah == null)
                        eah = new EditorActionHandler(JHS.PUBLIC_WWW+"/"+o.get("filename").getAsString());
                    eah.requestFile(e);
                } catch (IOException ex) {
                    String response = "{"
                    + "\"action\":"+TextEditor.ACTION_REQUEST_FILE+","
                    + "\"error\":404"
                    + "}";
                    e.send(response);
                }
                break;
        }
    }

    @Override
    public void onClose(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Texteditor connection closed.");
    }
    
}
