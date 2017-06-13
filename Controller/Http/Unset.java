/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import java.util.ArrayList;
import javahttpserver.Http.HttpEvent;
import javahttpserver.Http.HttpInterface;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class Unset implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void cookie(HttpEvent e, ArrayList<String> args){
        try{
            e.unsetCookie(args.get(0), args.get(1),"/"+args.get(2));
        }catch(Exception e2){
            try{
                e.unsetCookie(args.get(0), args.get(1));
            }catch(Exception e3){
                e.unsetCookie(args.get(0));
            }
        }
            
        e.send("");
    }
    
}
