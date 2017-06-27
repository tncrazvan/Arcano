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
                DataType,
                Value;

        public Cookie(String DataType,String Value) {
            this.DataType=DataType;
            this.Value=Value;
        }
        
    }
    public void cookie(HttpEvent e, ArrayList<String> args,JsonObject post){
        if(e.getClientHeader().get("Method").equals("POST")){
            e.setContentType("application/json");
            String value = post.get("Value").getAsString();
            try{
                e.setCookie(args.get(0), value,"/"+args.get(1),args.get(2),args.get(3));
            }catch(Exception e0){
                try{
                    e.setCookie(args.get(0), value,"/"+args.get(1),args.get(2));
                }catch(Exception e1){
                    try{
                        e.setCookie(args.get(0), value,"/"+args.get(1));
                    }catch(Exception e2){
                        e.setCookie(args.get(0), value);
                    }
                }
            }

            String jsonCookie = JHS.JSON_PARSER.toJson(new Cookie("Cookie", value));
            e.send(jsonCookie);
        }
    }
}
