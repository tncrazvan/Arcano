/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

import java.util.HashMap;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Tool.Http.Fetch;
import com.github.tncrazvan.arcano.Tool.Http.FetchResult;

/**
 *
 * @author Razvan Tanase
 */
public class ClusterServer {
    private final String hostname;
    private final String arcanoSecret;
    private int weight;

    public ClusterServer(final String hostname, final String secret, final int weight) {
        this.hostname = hostname;
        this.arcanoSecret = secret;
        this.weight = weight;
    }

    public final int getWeight() {
        return weight;
    }

    public final void setWeight(final int weight) {
        this.weight = weight;
    }

    public final String getArcanoSecret() {
        return arcanoSecret;
    }

    public final void getData(final String arcanoSecret) {
        final HttpHeaders headers = HttpHeaders.response();
        headers.setCookie("ArcanoSecret", arcanoSecret, "UTF-8");
        final FetchResult result = Fetch.get("http://" + hostname + "/@get/memory", new HashMap<String, String>() {
            private static final long serialVersionUID = -7816453807505595526L;
            {
                put("Cookie","ArcanoSecret="+arcanoSecret);
            }
        });
        System.out.println("result: "+new String(result.getBytes()));
    }
}
