/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.tncrazvan.arcano.Tool.Regex;

/**
 *
 * @author Razvan Tanase
 */
public class Cluster {
    private final HashMap<String, ClusterServer> map;
    private int length = 0;
    private boolean lengthUpdated = false;

    public Cluster(final HashMap<String, ClusterServer> list) {
        this.map = list;
    }

    public final String[] getServerHostnames() {
        return map.keySet().toArray(new String[0]);
    }

    public final boolean issetServer(final String hostname) {
        return map.containsKey(hostname);
    }

    public final ClusterServer validateArcanoKey(final Socket client, final String key) {
        ClusterServer server;
        for (final Map.Entry<String, ClusterServer> entry : map.entrySet()) {
            server = entry.getValue();
            if (Regex.match(client.getInetAddress() + ":" + client.getPort(), entry.getKey(), Pattern.CASE_INSENSITIVE)
                    && server.getArcanoSecret().equals(key))
                return server;
        }
        return null;
    }

    public final ClusterServer getServer(final String hostname) {
        return map.get(hostname);
    }

    public final void setServer(final String hostname, final ClusterServer server) {
        lengthUpdated = false;
        map.put(hostname, server);
    }

    public final int getLength() {
        if(!lengthUpdated){
            length = map.size();
            lengthUpdated = true;
        }
        return length;
    }
}
