/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

import java.util.HashMap;

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
    
    public ClusterServer getServer(String hostname){
        return map.get(hostname);
    }
    
    public void setServer(String hostname,ClusterServer server){
        map.put(hostname, server);
    }
}
