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
package com.github.tncrazvan.catpaw.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import com.github.tncrazvan.catpaw.Http.HttpHeader;
import com.github.tncrazvan.catpaw.Common;
import com.github.tncrazvan.catpaw.Http.HttpSession;
import com.github.tncrazvan.catpaw.WebObject;


/**
 *
 * @author Razvan
 */
public class WebSocketEvent extends WebSocketManager{
    
    private Method onCloseMethod;
    private Method onOpenMethod;
    private Method onMessageMethod;
    private String[] args;
    private Class<?> cls;
    private Object controller;
    private WebObject wo;
    private int classId;
    public HttpSession session;
    
    private void serveController(String[] location) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException{
        args = new String[0];
        try{
            classId = getClassnameIndex(wsControllerPackageName,location);
            wo = resolveClassName(classId,location);
            cls = Class.forName(wo.getClassname());
            controller = cls.getDeclaredConstructor().newInstance();
        }catch(ClassNotFoundException ex){
            try{
                cls = Class.forName(wsControllerPackageName+"."+wsNotFoundName);
            }catch(ClassNotFoundException eex){
                cls = Class.forName(wsControllerPackageNameOriginal+"."+wsNotFoundNameOriginal);
            }
            controller = cls.getDeclaredConstructor().newInstance();
        }
        
        ((WebSocketController)controller).setEvent(this);
        ((WebSocketController)controller).setArgs(args);
        
        onOpenMethod = controller.getClass().getMethod("onOpen");
        onMessageMethod = controller.getClass().getMethod("onMessage",byte[].class);
        onCloseMethod = controller.getClass().getMethod("onClose");
    }
    
    public WebSocketEvent(BufferedReader reader,Socket client,HttpHeader clientHeader) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException {
        super(reader,client,clientHeader);
        serveController(location.toString().split("/"));
    }


    @Override
    protected void onOpen() {        
        try {
            
            
            if(Common.WS_EVENTS.get(cls.getCanonicalName()) == null){
                ArrayList<WebSocketEvent> tmp = new ArrayList<>();
                tmp.add(this);
                Common.WS_EVENTS.put(cls.getCanonicalName(), tmp);
            }else{
                Common.WS_EVENTS.get(cls.getCanonicalName()).add(this);
            }
            onOpenMethod.invoke(controller);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE,null,ex);
        }
    }

    @Override
    protected void onMessage(byte[] data) {
        try {
            onMessageMethod.invoke(controller,data);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE,null,ex);
        }

    }
    
    @Override
    protected void onClose() {
        
        try {
            Common.WS_EVENTS.get(cls.getCanonicalName()).remove(this);
            onCloseMethod.invoke(controller);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE,null,ex);
        }
    }
    
}
