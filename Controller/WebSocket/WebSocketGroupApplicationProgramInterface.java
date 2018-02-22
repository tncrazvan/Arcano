/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Controller.WebSocket;

import com.google.gson.JsonObject;
import elkserver.Settings;
import elkserver.SmtpServer.Email;
import elkserver.SmtpServer.EmailComposer;
import elkserver.SmtpServer.EmailFrame;
import elkserver.WebSocket.WebSocketController;
import elkserver.WebSocket.WebSocketEvent;
import elkserver.WebSocket.WebSocketGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author razvan
 */
public class WebSocketGroupApplicationProgramInterface extends WebSocketController{
    private String groupName;
    private WebSocketGroup group;
    @Override
    public void onOpen(WebSocketEvent e, ArrayList<String> get_data) {
        //if the settings.json file contains "ALLOW_WS_GROUPS"..
        if(Settings.isset("groups")){
            JsonObject groups = Settings.get("groups").getAsJsonObject();
            //if the "GROUPS" contains "ALLOW"
            if(groups.has("allow")){
                //if "ALLOW" is true
                if(groups.get("allow").getAsBoolean()){ //ws groups are allowed
                    //if query "?join" is present in the request URL
                    if(e.issetUrlQuery("join")){
                        //use that query value as the group's name
                        groupName = e.getUrlQuery("join");
                        //if the group exists in this controller
                        if(GROUP_MANAGER.groupExists(groupName)){
                            //NOTE: GROUP_MANAGER is relative to the controller,
                            //in this case relative to: "WebSocketGroupApplicationProgramInterface",
                            //so any other controller will have a different GROUP_MANAGER
                            //with different groups. EVEN THOUGH THE GROUPS INSIDE THE GROUP_MANAGER
                            //COULD HAVE THE SAME NAMES (very low chance)
                            //THAT DOESN'T MEAN THEY ARE THE SAME GROUPS


                            //save the pointer in a local variable
                            group = GROUP_MANAGER.getGroup(groupName);
                            //and add this client to the group
                            group.addClient(e);
                        }
                    }
                }else{
                    //"ALLOW" is false => ws groups are not allowed
                    e.close();
                }
            }else{
                //"GROUPS" does not contain "ALLOW"
                e.close();
            }
        }else{
            //WS groups policy not specified
            e.close();
        }
    }

    @Override
    public void onMessage(WebSocketEvent e, byte[] data, ArrayList<String> get_data) {
        //send data to everyone inside the group except for this client (obviously)
        e.send(data, group);
        /**
         * NOTE: the other clients will receive the data as raw bytes.
         * in the case of JavaScript, you should read this data using a 
         * FileReader object and read the data as Text, Blob or ArrayBuffer.
         **/
        
    }

    @Override
    public void onClose(WebSocketEvent e, ArrayList<String> get_data) {
        //if the client exists in the group...
        if(group.clientExists(e)){
            //remove the client from group
            group.removeClient(e);
            //if this was the last client in the group...
            if(GROUP_MANAGER.getGroup(groupName).getMap().size() <= 0){
                //remove the group from the public list
                GROUP_MANAGER.removeGroup(group);
                //and mark the group for garbage collection to free memory
                //by setting it to null
                group = null;
            }
        }
    }
}
