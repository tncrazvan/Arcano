/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Controller.Http;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import elkserver.Http.Cookie;
import elkserver.Http.HttpEvent;
import elkserver.Http.HttpInterface;
import elkserver.ELK;

/**
 *
 * @author Razvan
 */
public class Set implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post) {}
    
    @Override
    public void onClose() {}
    
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

                String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Cookie", value));
                e.send(jsonCookie);
                
                //System.out.println(e.getHeader().toString());
            }else{
                String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Error", "-1"));
                e.send(jsonCookie);
            }
        }else{
            String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Error", "-2"));
            e.send(jsonCookie);
        }
    }
}
