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
package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Tool.JavaScriptExecutor;
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
public class ControllerNotFound extends HttpController{    
    private JavaScriptExecutor executor;
    public void main() throws ScriptException, IOException {
        String filename = configDir+"/"+scripts+"/Http";
        if(Files.exists(Path.of(filename))){
            if(args.length == 0){
                if(Files.exists(Path.of(filename+"/.js"))){
                    executor = new JavaScriptExecutor();
                    executor.execute(e, null, filename+"/.js", args, content);
                }else{
                    e.send("");
                }
            }else{
                for(short i=0;i<args.length;i++){
                    filename +="/"+args[i];
                    if(Files.exists(Path.of(filename+".js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".js", ar, content);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".JS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".JS", ar, content);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".jS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".jS", ar, content);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".Js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".Js", ar, content);
                        break;
                    }
                }
            }
        }else{
            e.send("");
        }
    }
}
