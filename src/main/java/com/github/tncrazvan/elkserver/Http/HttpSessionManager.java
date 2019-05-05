/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.elkserver.Http;

import com.github.tncrazvan.elkserver.EventManager;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Razvan
 */
public class HttpSessionManager extends HttpSession{
    
    private HttpSessionManager(EventManager e) throws UnsupportedEncodingException {
        super(e);
    }
    
    public static HttpSession startSession(EventManager e) throws UnsupportedEncodingException, UnsupportedEncodingException{
        if(e.issetCookie("sessionId")){//if session_id is set
            final String sessionId = e.getCookie("sessionId");
            if(LIST.containsKey(sessionId)){//if session exists
                HttpSession session = LIST.get(sessionId);
                //if session is expired
                if(session.getTime() + sessionTtl*1000 < System.currentTimeMillis()){
                    stopSession(session);
                }else{//if session is alive
                    session.setTime(System.currentTimeMillis());
                    return session;
                }
            }
        }
        final HttpSession session = new HttpSession(e);
        setSession(session);
        return session;
    }
    
    public static HttpSession getSession(String sessionId){
        return LIST.get(sessionId);
    }
    
    public static void setSession(HttpSession session){
        LIST.put(session.id(), session);
    }
    
    public static boolean issetSession(String sessionId){
        return LIST.containsKey(sessionId);
    }
    
    public static void stopSession(HttpSession session){
        LIST.remove(session.id());
    }
}
