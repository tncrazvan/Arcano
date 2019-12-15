package com.github.tncrazvan.arcano.Tool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import java.io.Reader;

/**
 *
 * @author Administrator
 */
public interface JsonTools {
    static final Gson JSON_PARSER = new Gson();
    
    //JsonObject
    default JsonObject jsonObject(String str){
        return JSON_PARSER.fromJson(str, JsonObject.class);
    }
    default JsonObject jsonObject(StringBuilder str){
        return jsonObject(str.toString());
    }
    default JsonObject jsonObject(Object o){
        return JSON_PARSER.toJsonTree(o).getAsJsonObject();
    }
    
    //JsonArray
    default JsonArray jsonArray(String str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    default JsonArray jsonArray(Reader str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    default JsonArray jsonArray(JsonReader str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    default JsonArray jsonArray(StringBuilder str){
        return jsonArray(str.toString());
    }
    default JsonArray jsonArray(Object[] o){
        return JSON_PARSER.toJsonTree(o).getAsJsonArray();
    }
    
    //STRINGIFY
    default String jsonStringify(Object o){
        return jsonObject(o).toString();
    }
    default String jsonStringify(Object[] o){
        return jsonArray(o).toString();
    }
    
    //PARSE TO ?
    default <T> T jsonParse(String o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    default <T> T jsonParse(Reader o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    default <T> T jsonParse(JsonReader o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    default <T> T jsonParse(StringBuilder o, Class<T> cls){
        return jsonParse(o.toString(),cls);
    }
    default <T> T jsonParse(JsonElement o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
}
