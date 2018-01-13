/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
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
    private static JsonObject info = null;
    public static void parse() throws IOException{
        info = ELK.JSONPARSER.parse(new String(Files.readAllBytes(Paths.get(ELK.PUBLIC_WWW+"/../settings.json")))).getAsJsonObject();
    }
    public static JsonElement get(String membername){
        if(!info.has(membername)) return null;
        return info.get(membername);
    }
    public static String getString(String membername){
        if(get(membername) == null) return null;
        return get(membername).getAsString();
    }
    public static int getInt(String membername){
        return get(membername).getAsInt();
    }
    public static boolean isset(String key){
        return info.has(key);
    }
}
