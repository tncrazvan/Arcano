/**
 * CatPaw is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
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
package com.github.tncrazvan.catpaw.Controller.WebSocket;

import com.google.gson.JsonObject;
import com.github.tncrazvan.catpaw.WebSocket.WebSocketController;
import com.github.tncrazvan.catpaw.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
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
