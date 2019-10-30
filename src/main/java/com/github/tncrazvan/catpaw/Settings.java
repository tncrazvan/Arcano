/**
 * CatPaw is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.tncrazvan.catpaw;

import com.github.tncrazvan.catpaw.Tools.JsonTools;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Containst the settings file objects.
 * @author Razvan
 */
public class Settings implements JsonTools{
    private JsonObject info = null;
    
    /**
     * Creates an JsonObject from the given String.
     * @param settings json file.
     * @throws IOException 
     */
    public void parse(String settings) throws IOException{
        info = toJsonObject(new String(Files.readAllBytes(Paths.get(settings))));
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
