/**
 * Arcano is a Java library that makes it easier
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
package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Tool.JavaScriptExecutor;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.script.ScriptException;

/**
 *
 * @author razvan
 */
@NotFound
public class ControllerNotFound extends WebSocketController{
    private JavaScriptExecutor executor = null;
    @Override
    public void onOpen() {
        
        if(Files.exists(Path.of(configDir+"/"+scripts+"/WebSocket"))){
            String filename = configDir+"/"+scripts+"/WebSocket";
            try{
                for(short i=0;i<args.length;i++){
                    filename +="/"+args[i];
                    if(Files.exists(Path.of(filename+".js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".js", ar);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".JS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".JS", ar);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".jS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".jS", ar);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".Js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".Js", ar);
                        break;
                    }
                }
            }catch(ScriptException | IOException e){
                
            }
            if(executor.onOpen.todo != null)
                executor.onOpen.todo.apply(null);
        }else
            e.close();
    }

    @Override
    public void onMessage(byte[] data) {
        if(executor != null && executor.onMessage.todo != null)
            executor.onMessage.todo.apply(new String(data));
    }

    @Override
    public void onClose() {
        if(executor != null && executor.onClose.todo != null)
            executor.onClose.todo.apply(null);
    }

}