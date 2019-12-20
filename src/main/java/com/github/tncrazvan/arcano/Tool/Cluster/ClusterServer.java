/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Tool.Http.Fetch;
import com.github.tncrazvan.arcano.Tool.Http.FetchResult;
import java.util.HashMap;

public class ClusterServer {
    private final String hostname;
    private final String arcanoSecret;
    private int weight;

    public ClusterServer(String hostname, String secret, int weight) {
        this.hostname = hostname;
        this.arcanoSecret = secret;
        this.weight = weight;
    }
    
    public int getWeight(){
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getArcanoSecret() {
        return arcanoSecret;
    }
    
    
    public void getData(String arcanoSecret){
        HttpHeaders headers = new HttpHeaders(false);
        headers.setCookie("ArcanoSecret", arcanoSecret, "UTF-8");
        FetchResult result = Fetch.get("http://"+hostname+"/@get/memory", new HashMap<String, String>(){{
            put("Cookie","ArcanoSecret="+arcanoSecret);
        }});
        System.out.println("result: "+new String(result.getBytes()));
    }
}
