/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver;

import javahttpserver.Controller.WebSocket.TextEditor;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javahttpserver.WebSocket.WebSocketEvent;

/**
 *
 * @author Razvan
 */
public class EditorActionHandler {
    private LogicFile file = null;

    public EditorActionHandler(String filename) throws IOException {
        this.file = new LogicFile(filename);
    }
    
    
    
    
    
    
    
    public void logicalSave(JsonElement content){
        //System.out.println("\n---------------------------------------");
        JsonObject o = content.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = o.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entry: entries) {
            if(entry
                .getValue()
                .getAsJsonObject()
                .has("content")){
                    file.setRowContent(
                        Integer
                                .parseInt(entry
                                            .getKey()), 
                        entry
                                .getValue()
                                .getAsJsonObject()
                                .get("content")
                                .getAsString(),
                        false
                    );
            }
            
            if(entry
                .getValue()
                .getAsJsonObject()
                .has("delete")){
                    file.setRowContent(
                            Integer
                                .parseInt(entry
                                            .getKey()), 
                            "", 
                            true
                    );
            }
            
        }
        //System.out.println("\n");
        //System.out.println(content);
    }
    
    public void physicalSave(){
        file.commitRows();
    }
    
    public void requestFile(WebSocketEvent e){
        String response = "{"
                + "\"action\":"+TextEditor.ACTION_REQUEST_FILE+","
                + "\"content\":"+JHS.JSON_PARSER.toJson(file.getRows())
                + "}";
        e.send(response);
    }
}
