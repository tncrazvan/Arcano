package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Tool.JsonTools;
import static com.github.tncrazvan.arcano.Tool.JsonTools.jsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Containst the configuration file objects.
 * @author Razvan
 */
public class Configuration {
    private JsonObject info = null;
    
    /**
     * Creates an JsonObject from the given String.
     * @param settings json file.
     * @throws IOException 
     */
    public void parse(String settings) throws IOException{
        info = jsonObject(new String(Files.readAllBytes(Paths.get(settings))));
    }
    
    /**
     * 
     * @param membername object name.
     * @return the settings object as a JsonElement.
     */
    public JsonElement get(String membername){
        return info.get(membername);
    }
    
    /**
     * 
     * @param membername array name.
     * @return the settings object as a JsonArray.
     */
    public JsonArray getJsonArray(String membername){
        return info.get(membername).getAsJsonArray();
    }
    
    /**
     * 
     * @param membername array name.
     * @return the settings object as a JsonObject.
     */
    public JsonObject getJsonObject(String membername){
        return info.get(membername).getAsJsonObject();
    }
    
    /**
     * 
     * @param membername object name.
     * @return the settings object as a String.
     */
    public String getString(String membername){
        return get(membername).getAsString();
    }
    
    /**
     * 
     * @param membername object name.
     * @return the settings object as an int.
     */
    public int getInt(String membername){
        return get(membername).getAsInt();
    }
    
    /**
     * 
     * @param membername object name.
     * @return the settings object as a boolean.
     */
    public boolean getBoolean(String membername){
        return get(membername).getAsBoolean();
    }
    
    /**
     * Checks if the object name is iset in the settings file.
     * @param key
     * @return 
     */
    public boolean isset(String key){
        return info.has(key);
    }
}
