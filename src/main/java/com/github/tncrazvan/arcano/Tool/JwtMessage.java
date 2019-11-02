/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import com.github.tncrazvan.arcano.Common;
import com.google.gson.JsonObject;

/**
 *
 * @author Administrator
 */
public class JwtMessage extends Common implements JsonTools{
    private static final String SECRET = "";
    private final JsonObject header = new JsonObject();
    private final JsonObject body;
    private String contents = "";
    public JwtMessage(JsonObject body){
        header.addProperty("alg", "HS512");
        header.addProperty("typ", "JWT");
        this.body = body;
        String header64 = btoa(this.header.toString());
        String body64 = btoa(this.body.toString());
        this.contents = header64+"."+body64;
        String token = btoa(getSha512String(this.contents, SECRET));
        
        this.contents = this.contents+"."+token;
    }
    
    @Override
    public String toString(){return contents;}
    public JsonObject getHeader(){return header;}
    public JsonObject getBody(){return body;}
    
    public static boolean verify(String message){
        String[] pieces = message.split("\\.");
        if(pieces.length < 3) return false;
        return btoa(getSha512String(pieces[0]+"."+pieces[1], SECRET)).equals(pieces[2]);
    }
}
