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
public class Development implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args) {
        e.send("Development -> main");
    }
    public void serving(HttpEvent e, ArrayList<String> args) {
        e.send("Development -> serving");
    }
    public void headers(HttpEvent e, ArrayList<String> args) {
        e.send("Development -> headers");
    }
    public void http_events(HttpEvent e, ArrayList<String> args) {
        e.send("Development -> http_events");
    }
    public void ws_events(HttpEvent e, ArrayList<String> args) {
        e.send("Development -> ws_events");
    }
    public void mvc_and_elk(HttpEvent e, ArrayList<String> args) {
        e.send("Development -> mvc_and_elk");
    }
    
}
