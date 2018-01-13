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
 * Copyright (C) 2016  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package elkserver.Controller.Http;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import elkserver.Http.Cookie;
import elkserver.Http.HttpEvent;
import elkserver.Http.HttpInterface;
import elkserver.ELK;

/**
 *
 * @author Razvan
 */
public class Isset implements HttpInterface{
    @Override
    public void main(HttpEvent e, ArrayList<String> get_data,JsonObject post_data){}
    
    @Override
    public void onClose() {}
    
    public void file(HttpEvent e, ArrayList<String> get_data,JsonObject post_data) throws FileNotFoundException, IOException{
        if(get_data.size() >= 0){
            File f = new File(ELK.PUBLIC_WWW+"/"+get_data.get(0));
            if(f.exists()){
                e.send(0);
            }else{
                e.send(-2);
            }
        }else{
            e.send(-1);
        }
    }
    
    public void cookie(HttpEvent e, ArrayList<String> get_data,JsonObject post_data){
        if(e.getClientHeader().get("Method").equals("POST")){
           if(post_data.has("name")){
                String name = post_data.get("name").getAsString();
                if(e.cookieIsset(name)){
                    e.send(0);
                }else{
                    e.send(-2);
                }
            }else{
                e.send(-1);
            } 
        }else{
            String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
}
