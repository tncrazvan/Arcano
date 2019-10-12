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
package com.github.tncrazvan.catpaw.Http;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.github.tncrazvan.catpaw.WebObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;


/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager{
    private Method method;
    private String[] args;
    private Class<?> cls;
    private Object controller;
    private WebObject wo;
    private int classId;
    
    public HttpEvent(DataOutputStream output, HttpHeader clientHeader, Socket client, StringBuilder content) throws UnsupportedEncodingException {
        super(output,clientHeader,client,content);
    }
    
    private void invoke(Object controller,Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InvocationTargetException{
        Class<?> type = method.getReturnType();
        if(type == HttpResponse.class){
            HttpResponse response = (HttpResponse) method.invoke(controller);
            HashMap<String,String> headers = response.getHeaders();
            if(headers != null){
                Iterator it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    setHeaderField((String) pair.getKey(), (String) pair.getValue());
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            send(response.getContent());
        }else if(type == String.class || type == int.class || type == float.class
                || type == double.class || type == boolean.class || type == float.class
                || type == byte.class || type == char.class || type == short.class
                || type == long.class){
            send(String.valueOf(method.invoke(controller)));
        }else{
            method.invoke(controller);
        }
        
    }
    
    private void serveController(String[] location) 
    throws InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException{
        args = new String[0];
        if(location.length == 0 || location.length == 1 && location[0].equals("")){
            location = new String[]{""};
        }
        try{
            classId = getClassnameIndex(location,getMethod());
            wo = resolveClassName(classId,location);
            cls = Class.forName(wo.getClassname());
            controller = cls.getDeclaredConstructor().newInstance();
            
            String methodname = location.length>classId?wo.getMethodname():"main";
            args = resolveMethodArgs(classId+1, location);
            try{
                method = controller.getClass().getDeclaredMethod(methodname);
            }catch(NoSuchMethodException ex){
                args = resolveMethodArgs(classId, location);
                method = controller.getClass().getDeclaredMethod("main");
            }
            
            ((HttpController)controller).setEvent(this);
            ((HttpController)controller).setArgs(args);
            ((HttpController)controller).setContent(content);
            
            invoke(controller,method);
        }catch(ClassNotFoundException ex){
            try{
                cls = Class.forName(httpControllerPackageName+"."+httpNotFoundName);
            }catch(ClassNotFoundException eex){
                cls = Class.forName(httpControllerPackageNameOriginal+"."+httpNotFoundNameOriginal);
            }
            controller = cls.getDeclaredConstructor().newInstance();
            Method main = controller.getClass().getDeclaredMethod("main");
            //Method onClose = controller.getClass().getDeclaredMethod("onClose");
            
            ((HttpController)controller).setEvent(this);
            ((HttpController)controller).setArgs(args);
            ((HttpController)controller).setContent(content);
            
            invoke(controller, main);
        }
    }
    
    @Override
    void onControllerRequest(StringBuilder url) {
        try {
            serveController(url.toString().split("/"));
            close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            logger.log(Level.WARNING, null, ex);
        } catch (IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    } 
}
