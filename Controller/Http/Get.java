/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javahttpserver.Http.HttpEvent;
import javahttpserver.Http.HttpInterface;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class Get implements HttpInterface{
    @Override
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post){
        e.send("No resource specified");
    }
    public void file(HttpEvent e, ArrayList<String> args,JsonObject post) throws FileNotFoundException, IOException{
        e.setContentType(JHS.processContentType(args.get(0)));
        e.sendFileContents("/"+(args.get(0).equals("")?args.get(1):args.get(0)));
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
        e.setContentType("application/json");
        if(e.cookieIsset(args.get(0))){
            String jsonCookie = JHS.JSON_PARSER.toJson(new Cookie("Cookie", e.getCookie(args.get(0))));
            e.send(jsonCookie);
        }else{
            e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
            e.send("");
        }
    }
}
