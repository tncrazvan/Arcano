/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Controller.Http;

import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import elkserver.Http.HttpEvent;
import elkserver.Http.HttpInterface;
import elkserver.ELK;

/**
 *
 * @author Razvan
 */
public class Get implements HttpInterface{
    @Override
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post){}
    
    @Override
    public void onClose() {}
    
    public void file(HttpEvent e, ArrayList<String> args,JsonObject post) throws FileNotFoundException, IOException{
        e.setContentType(ELK.processContentType(args.get(0)));
        e.sendFileContents("/"+(args.get(0).equals("")?args.get(1):args.get(0)));
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
            if(post.has("name")){
                String name = post.get("name").getAsString();
                if(e.cookieIsset(name)){
                    e.setContentType("application/json");
                    String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Cookie", e.getCookie(name)));
                    e.send(jsonCookie);
                }else{
                    e.setContentType("text/plain");
                    e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                    e.send();
                }
            }else{
                e.setContentType("text/plain");
                e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                e.send();
            }
        }else{
            String jsonCookie = ELK.JSON_PARSER.toJson(new elkserver.Http.Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
}
