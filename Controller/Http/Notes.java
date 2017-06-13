/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import java.util.ArrayList;
import javahttpserver.Http.HttpEvent;
import javahttpserver.Http.HttpInterface;

/**
 *
 * @author Razvan
 */
public class Notes implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args) {
        e.send("Notes -> main");
    }
    
    public void java_oop(HttpEvent e, ArrayList<String> args){
        e.send("Notes -> java_oop");
    }
    public void php_oop(HttpEvent e, ArrayList<String> args){
        e.send("Notes -> php_oop");
    }
    public void similarities(HttpEvent e, ArrayList<String> args){
        e.send("Notes -> similarities");
    }
    public void gaps(HttpEvent e, ArrayList<String> args){
        e.send("Notes -> gaps");
    }
    
}
