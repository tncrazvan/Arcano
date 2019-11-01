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

import space.catpaw.Http.HttpController;
import space.catpaw.Http.HttpEvent;

/**
 *
 * @author razvan
 */
public class ControllerNotFound extends HttpController{
    public void main() {
        e.setStatus(HttpEvent.STATUS_NOT_FOUND);
        try{
            e.send("Page not found");
        }catch(Exception ex){
            System.err.println("\n\nException on location: "+e.getClientHeader().get("@Resource"));
            ex.printStackTrace(System.out);
        }
    }
}
