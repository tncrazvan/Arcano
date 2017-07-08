/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import javahttpserver.Http.HttpEvent;
import javahttpserver.Http.HttpInterface;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class Set implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post) {
        
    }
    class Cookie{
        String 
                type,
                value;

        public Cookie(String type,String value) {
            this.type=type;
            this.value=value;
        }
        
    }
    public void cookie(HttpEvent e, ArrayList<String> args,JsonObject post){
        
        if(e.getClientHeader().get("Method").equals("POST")){
            if(post.has("name") 
                && post.has("value") 
                && post.has("path") 
                && post.has("domain") 
                && post.has("expire")){
                e.setContentType("application/json");
                String 
                    name = post.get("name").getAsString(),
                    value = post.get("value").getAsString(),
                    path = post.get("path").getAsString(),
                    domain = post.get("domain").getAsString(),
                    expire = post.get("expire").getAsString();

                e.setCookie(name, value, path, domain, expire);

                String jsonCookie = JHS.JSON_PARSER.toJson(new Cookie("Cookie", value));
                e.send(jsonCookie);
            }else{
                String jsonCookie = JHS.JSON_PARSER.toJson(new Cookie("Error", "-1"));
                e.send(jsonCookie);
            }
            
        }
    }
}
