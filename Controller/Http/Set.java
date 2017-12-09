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
    public void main(HttpEvent e, ArrayList<String> get_data,JsonObject post_data) {}
    
    @Override
    public void onClose() {}
    
    public void cookie(HttpEvent e, ArrayList<String> get_data,JsonObject post_data){
        if(e.getMethod().equals("POST")){
            if(post_data.has("name") 
                && post_data.has("value") 
                && post_data.has("path") 
                && post_data.has("domain") 
                && post_data.has("expire")){
                e.setContentType("application/json");
                String 
                    name = post_data.get("name").getAsString(),
                    value = post_data.get("value").getAsString(),
                    path = post_data.get("path").getAsString(),
                    domain = post_data.get("domain").getAsString(),
                    expire = post_data.get("expire").getAsString();
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
