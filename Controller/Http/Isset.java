/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import com.google.gson.JsonObject;
import java.io.File;
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
public class Isset implements HttpInterface{
    @Override
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post){
        e.send("No resource specified");
    }
    public void file(HttpEvent e, ArrayList<String> args,JsonObject post) throws FileNotFoundException, IOException{
        if(args.size() >= 0){
            File f = new File(JHS.PUBLIC_WWW+"/"+args.get(0));
            if(f.exists()){
                e.send(0);
            }else{
                e.send(-2);
            }
        }else{
            e.send(-1);
        }
    }
    
    public void cookie(HttpEvent e, ArrayList<String> args,JsonObject post){
        if(args.size() >= 0){
            if(e.cookieIsset(args.get(0))){
                e.send(0);
            }else{
                e.send(-2);
            }
        }else{
            e.send(-1);
        }
    }
}
