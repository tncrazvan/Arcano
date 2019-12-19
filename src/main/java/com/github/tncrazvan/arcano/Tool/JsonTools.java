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
    static JsonObject jsonObject(String str){
        return JSON_PARSER.fromJson(str, JsonObject.class);
    }
    static JsonObject jsonObject(StringBuilder str){
        return jsonObject(str.toString());
    }
    static JsonObject jsonObject(Object o){
        return JSON_PARSER.toJsonTree(o).getAsJsonObject();
    }
    
    //JsonArray
    static JsonArray jsonArray(String str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    static JsonArray jsonArray(Reader str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    static JsonArray jsonArray(JsonReader str){
        return JSON_PARSER.fromJson(str, JsonArray.class);
    }
    static JsonArray jsonArray(StringBuilder str){
        return jsonArray(str.toString());
    }
    static JsonArray jsonArray(Object[] o){
        return JSON_PARSER.toJsonTree(o).getAsJsonArray();
    }
    
    //STRINGIFY
    static String jsonStringify(Object o){
        return jsonObject(o).toString();
    }
    static String jsonStringify(Object[] o){
        return jsonArray(o).toString();
    }
    
    //PARSE TO ?
    static <T> T jsonParse(String o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    static <T> T jsonParse(Reader o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    static <T> T jsonParse(JsonReader o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
    static <T> T jsonParse(StringBuilder o, Class<T> cls){
        return jsonParse(o.toString(),cls);
    }
    static <T> T jsonParse(JsonElement o, Class<T> cls){
        return (T) JSON_PARSER.fromJson(o, cls);
    }
}
