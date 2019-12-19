/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

/**
 *
 * @author RazvanTanase
 */
public class ClusterServer {
    private final String secret;
    private int weight;

    public ClusterServer(String secret, int weight) {
        this.secret = secret;
        this.weight = weight;
    }
    
    public int getWeight(){
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getSecret() {
        return secret;
    }
    
    
}
