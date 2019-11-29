package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.Bean.Web;
import com.google.gson.JsonObject;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author razvan
 */
@Web(path = "/WebSocketGroupApplicationProgramInterface")
public class WebSocketGroupApplicationProgramInterface extends WebSocketController{
    private String groupName;
    private WebSocketGroup group;
    
    @Override
    public void onOpen() {
        //if the settings.json file contains "ALLOW_WS_GROUPS"..
        if(settings.isset("groups")){
            JsonObject groups = settings.get("groups").getAsJsonObject();
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
                            //if the group is public
                            if(group.getVisibility() == WebSocketGroup.PUBLIC){
                                try {
                                    //add this client to the group
                                    group.addClient(e);
                                } catch (UnsupportedEncodingException ex) {
                                    Logger.getLogger(WebSocketGroupApplicationProgramInterface.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }else{
                                //if the group is not public, close the connection
                                e.close();
                            }
                            
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
    public void onMessage(byte[] data) {
        //send data to everyone inside the group except for this client (obviously)
        e.send(data, group, false);
        /**
         * NOTE: the other clients will receive the data as raw bytes.
         * in the case of JavaScript, you should read this data using a 
         * FileReader object and read the data as Text, Blob or ArrayBuffer.
         **/
        
    }

    @Override
    public void onClose() {
        if(group == null) return;
        //if the client exists in the group...
        if(group.clientExists(e)){
            //remove the client from group
            group.removeClient(e);
        }
        //if groups has no clients, remove it from memory
        if(GROUP_MANAGER.getGroup(groupName).getMap().size() <= 0){
            //remove the group from the public list
            GROUP_MANAGER.removeGroup(group);
            //and mark the group for garbage collection to free memory
            //by setting it to null
            group = null;
            System.out.println("removing group from memory");
        }
    }
}
