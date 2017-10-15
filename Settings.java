/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Razvan
 */
public class Settings {
    private static JsonObject info;
    public static void parse() throws IOException{
        info = ELK.JSONPARSER.parse(new String(Files.readAllBytes(Paths.get(ELK.PUBLIC_WWW+"/../settings.json")))).getAsJsonObject();
    }
    public static JsonElement get(String membername){
        if(!info.has(membername)) return null;
        return info.get(membername);
    }
    public static String getString(String membername){
        if(get(membername) == null) return "";
        return get(membername).getAsString();
    }
    public static int getInt(String membername){
        return get(membername).getAsInt();
    }
}
