/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javahttpserver.Http.HttpEvent;
import javahttpserver.Http.HttpInterface;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class Introduction implements HttpInterface{
    
    @Override
    public void main(HttpEvent e, ArrayList<String> args){
        e.send("Home -> main");
    }
    
    public void about_me(HttpEvent e,ArrayList<String> args) throws IOException{
        e.sendFileContents("/view/Introduction/about_me/about_me.html");
    }
    public void study_field(HttpEvent e,ArrayList<String> args){
        e.send("Home -> study_field");
    }
    
    
}
