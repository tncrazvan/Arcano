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
public class ControllerNotFound implements HttpInterface{
    @Override
    public void main(HttpEvent e, ArrayList<String> args){
        e.send("Controller not found!");
    }
}
