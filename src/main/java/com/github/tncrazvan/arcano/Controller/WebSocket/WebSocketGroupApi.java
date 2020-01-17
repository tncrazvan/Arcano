package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.WebSocket.WebSocketMessage;
import com.github.tncrazvan.arcano.Bean.Web.WebSocketPath;

/**
 *
 * @author razvan
 */
@WebSocketPath(name = "/WebSocketGroupApi")
public class WebSocketGroupApi extends WebSocketController{
    private String groupName;
    private com.github.tncrazvan.arcano.WebSocket.WebSocketGroup group;
    
    @Override
    public void onOpen() {
        //if "ALLOW" is true
        if(so.config.webSocket.groups.enabled){ //ws groups are enabled
            //if query "?join" is present in the request URL
            if(issetRequestQueryString("join")){
                //use that query value as the group's name
                groupName = getRequestQueryString("join");
                //if the group exists in this controller
                if(GROUP_MANAGER.groupExists(groupName)){
                    //NOTE: GROUP_MANAGER is relative to the controller,
                    //in this case relative to: "WebSocketGroupApi",
                    //so any other controller will have a different GROUP_MANAGER
                    //with different groups. EVEN THOUGH THE GROUPS INSIDE THE GROUP_MANAGER
                    //COULD HAVE THE SAME NAMES (very low chance)
                    //THAT DOESN'T MEAN THEY ARE THE SAME GROUPS


                    //save the pointer in a local variable
                    group = GROUP_MANAGER.getGroup(groupName);
                    //if the group is public
                    if(group.getVisibility() == com.github.tncrazvan.arcano.WebSocket.WebSocketGroup.PUBLIC){
                        try {
                            //add this client to the group
                            group.addClient(this);
                        } catch (final UnsupportedEncodingException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    } else {
                        // if the group is not public, close the connection
                        close();
                    }

                }
            }
        } else {
            // "ALLOW" is false => ws groups are not enabled
            close();
        }
    }

    @Override
    public void onMessage(final WebSocketMessage message) {
        //send data to everyone inside the group except for this client (obviously)
        send(message, group, false);
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
        if(group.clientExists(this)){
            //remove the client from group
            group.removeClient(this);
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
