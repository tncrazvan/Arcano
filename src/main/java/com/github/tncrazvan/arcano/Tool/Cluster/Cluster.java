/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

import com.github.tncrazvan.arcano.Tool.Regex;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author RazvanTanase
 */
public class Cluster {
    private HashMap<String, ClusterServer> map;

    public Cluster(HashMap<String, ClusterServer> list) {
        this.map = list;
    }
    
    public String[] getServerHostnames(){
        return map.keySet().toArray(new String[0]);
    }
    
    public boolean issetServer(String hostname){
        return map.containsKey(hostname);
    }
    
    public ClusterServer validateArcanoKey(Socket client, String key){
        ClusterServer server;
        for (Map.Entry<String, ClusterServer> entry : map.entrySet()) {
            server = entry.getValue();
            if(Regex.match(client.getInetAddress()+":"+client.getPort(), entry.getKey(), Pattern.CASE_INSENSITIVE) && server.getSecret().equals(key))
                return server;
        }
        return null;
    }
    
    public ClusterServer getServer(String hostname){
        return map.get(hostname);
    }
    
    public void setServer(String hostname,ClusterServer server){
        map.put(hostname, server);
    }
}
