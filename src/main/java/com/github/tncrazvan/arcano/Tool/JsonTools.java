package com.github.tncrazvan.arcano.Tool;

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
    default JsonObject toJsonObject(StringBuilder str){
        return toJsonObject(str.toString());
    }
    default JsonArray toJsonArray(StringBuilder str){
        return toJsonArray(str.toString());
    }
    
    //String to [...]
    default JsonObject toJsonObject(String str){
        return JSON_PARSER.fromJson(str, JsonObject.class);
    }
    default JsonArray toJsonArray(String str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    
    //Object to [...]
    default JsonObject toJsonObject(Object o){
        return JSON_PARSER.toJsonTree(o).getAsJsonObject();
    }
    
    //Encode
    default String jsonEncode(Object o){
        return toJsonObject(o).toString();
    }
    
    //Decode
    default <T> T jsonDecode(String o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    default <T> T jsonDecode(StringBuilder o, Class<T> cls){
        return jsonDecode(o.toString(),cls);
    }
}
