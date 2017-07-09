/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import javahttpserver.Http.Cookie;
import javahttpserver.Http.HttpEvent;
import javahttpserver.Http.HttpInterface;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class Unset implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void cookie(HttpEvent e, ArrayList<String> args,JsonObject post){
        if(e.getClientHeader().get("Method").equals("POST")){
            if(post.has("name") && post.has("domain") && post.has("path")){

                String name = post.get("name").getAsString();
                if(e.cookieIsset(name)){
                    e.unsetCookie(name, post.get("path").getAsString(), post.get("domain").getAsString());
                    e.send(0);
                }else{
                    e.send(0);
                }
            }else{
                e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                e.flushHeaders();
            }
        }else{
            String jsonCookie = JHS.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
    
}
