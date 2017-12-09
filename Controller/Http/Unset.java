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
public class Unset implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> get_data,JsonObject post_data) {}
    
    @Override
    public void onClose() {}
    
    public void cookie(HttpEvent e, ArrayList<String> get_data,JsonObject post_data){
        if(e.getMethod().equals("POST")){
            if(post_data.has("name") && post_data.has("domain") && post_data.has("path")){

                String name = post_data.get("name").getAsString();
                if(e.cookieIsset(name)){
                    e.unsetCookie(name, post_data.get("path").getAsString(), post_data.get("domain").getAsString());
                    e.send(0);
                }else{
                    e.send(0);
                }
            }else{
                e.setStatus(HttpEvent.STATUS_NOT_FOUND);
                e.flushHeaders();
            }
        }else{
            String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
    
}
