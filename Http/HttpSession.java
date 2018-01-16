/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Http;

import elkserver.ELK;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author razvan
 */
public class HttpSession{
    private static final Map<String,Object> SESSION_STORAGE = new HashMap<>();
    private final String id;
    public HttpSession(HttpEvent e) {
        if(e.cookieIsset("sessionId")){
            id = e.getCookie("sessionId");
        }else{
            id = ELK.getSha1String(e.getClient().getInetAddress().toString()+","+e.getClient().getPort()+","+Math.random());
            e.setCookie("sessionId", id, "/");
            SESSION_STORAGE.put(id, null);
        }
    }
    
    public boolean sessionIsnull(){
        return getSessionObject() == null;
    }
    
    public String getSessionId(){
        return id;
    }
    public void setSessionObject(Object o){
        SESSION_STORAGE.put(id, o);
    }
    public void unsetSessionObject(){
        SESSION_STORAGE.remove(id);
    }
    
    public Object getSessionObject(){
        return SESSION_STORAGE.get(id);
    }
}
