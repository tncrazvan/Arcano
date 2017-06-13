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
public class Proposition implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args) {
        e.send("Proposition -> main");
    }
    public void http_requests(HttpEvent e, ArrayList<String> args) {
        e.send("Proposition -> http_requests");
    }
    public void websockets(HttpEvent e, ArrayList<String> args) {
        e.send("Proposition -> websockets");
    }
    public void pthreads(HttpEvent e, ArrayList<String> args) {
        e.send("Proposition -> pthreads");
    }
    
}
