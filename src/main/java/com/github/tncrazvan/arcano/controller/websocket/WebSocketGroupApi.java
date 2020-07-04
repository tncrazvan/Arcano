package com.github.tncrazvan.arcano.controller.websocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.tool.action.WebSocketEventAction;
import com.github.tncrazvan.arcano.websocket.WebSocketCommit;
import com.github.tncrazvan.arcano.websocket.WebSocketEvent;

/**
 *
 * @author razvan
 */
public class WebSocketGroupApi implements WebSocketEventAction{

    private String groupName;
    private com.github.tncrazvan.arcano.websocket.WebSocketGroup group;


    @Override
    public void onOpen(WebSocketEvent e) {
        //if "ALLOW" is true
        if(e.so.config.webSocket.groups.enabled){ //ws groups are enabled
            //if query "?join" is present in the request URL
            if(e.request.queryStrings.containsKey("join")){
                //use that query value as the group's name
                groupName = e.request.queryStrings.get("join");
                //if the group exists in this controller
                if(e.GROUP_MANAGER.groupExists(groupName)){
                    //NOTE: GROUP_MANAGER is relative to the controller,
                    //in this case relative to: "WebSocketGroupApi",
                    //so any other controller will have a different GROUP_MANAGER
                    //with different groups. EVEN THOUGH THE GROUPS INSIDE THE GROUP_MANAGER
                    //COULD HAVE THE SAME NAMES (very low chance)
                    //THAT DOESN'T MEAN THEY ARE THE SAME GROUPS


                    //save the pointer in a local variable
                    group = e.GROUP_MANAGER.getGroup(groupName);
                    //if the group is public
                    if(group.getVisibility() == com.github.tncrazvan.arcano.websocket.WebSocketGroup.PUBLIC){
                        try {
                            //add this client to the group
                            group.addClient(e);
                        } catch (final UnsupportedEncodingException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    } else {
                        // if the group is not public, close the connection
                        e.close();
                    }

                }
            }
        } else {
            // "ALLOW" is false => ws groups are not enabled
            e.close();
        }
    }

    @Override
    public void onMessage(WebSocketEvent e, WebSocketCommit commit) {
        //send data to everyone inside the group except for this client (obviously)
        e.push(commit);
        /**
         * NOTE: the other clients will receive the data as raw bytes.
         * in the case of JavaScript, you should read this data using a 
         * FileReader object and read the data as Text, Blob or ArrayBuffer.
         **/
    }

    @Override
    public void onClose(WebSocketEvent e) {
        if(group == null) return;
        //if the client exists in the group...
        if(group.clientExists(e)){
            //remove the client from group
            group.removeClient(e);
        }
        //if groups has no clients, remove it from memory
        if(e.GROUP_MANAGER.getGroup(groupName).getMap().size() <= 0){
            //remove the group from the public list
            e.GROUP_MANAGER.removeGroup(group);
            //and mark the group for garbage collection to free memory
            //by setting it to null
            group = null;
            System.out.println("removing group from memory");
        }
    }
}
