/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Tools;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author Administrator
 */
public interface JsonTools {
    static final Gson JSON_PARSER = new Gson();
    
    //StringBuilder to [...]
    static JsonObject toJsonObject(StringBuilder str){
        return toJsonObject(str.toString());
    }
    //StringBuilder to [...]
    static JsonArray toJsonArray(StringBuilder str){
        return toJsonArray(str.toString());
    }
    
    //String to [...]
    static JsonObject toJsonObject(String str){
        return JSON_PARSER.fromJson(str, JsonObject.class);
    }
    static JsonArray toJsonArray(String str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    
    //Object to [...]
    static JsonObject toJsonObject(Object o){
        return JSON_PARSER.toJsonTree(o).getAsJsonObject();
    }
    
    
    static String jsonEncode(Object o){
        return toJsonObject(o).toString();
    }
    static Object jsonDecode(String o){
        return JSON_PARSER.fromJson(o, Object.class);
    }
}
