/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.WebSocket;

import elkserver.ELK;

/**
 *
 * @author razvan
 */
public class WebSocketGroup {
    private final String name;
    private final int maxClients;

    public WebSocketGroup(String name) {
        this.name = name;
        this.maxClients = ELK.WS_GROUP_MAX_CLIENTS;
    }

    public WebSocketGroup(String name, int maxClients) {
        this.name = name;
        this.maxClients = maxClients;
    }
    
    
}
