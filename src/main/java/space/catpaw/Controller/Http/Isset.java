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
package space.catpaw.Controller.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import space.catpaw.Http.HttpController;
import space.catpaw.Bean.Web;

/**
 *
 * @author Razvan
 */

@Web(path="/isset")
public class Isset extends HttpController{
    @Web(path="/file")
    public void file() throws FileNotFoundException, IOException{
        String url = String.join("/", args);
        File f = new File(webRoot,url);
        if(f.exists()){
            e.setStatus(STATUS_FOUND);
        }else{
            e.setStatus(STATUS_NOT_FOUND);
        }
        e.flush();
    }
    
    @Web(path="/cookie")
    public void cookie(){
            String name = String.join("/",args);
            if(e.issetCookie(name)){
                e.setStatus(STATUS_FOUND);
            }else{
                e.setStatus(STATUS_NOT_FOUND);
            }
           e.flush();
    }
}
