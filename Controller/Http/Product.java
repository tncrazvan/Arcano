/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http;

import java.io.File;
import java.io.FileInputStream;
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
public class Product implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> args) {
        e.send("Product -> main");
    }
    public void how_to(HttpEvent e, ArrayList<String> args) {
        if(args.size()>0)
        switch(args.get(0).toLowerCase()){
            default:
                e.send("Demo -> how_to -> "+args.get(0).toLowerCase());
                break;
        }
        else e.send("Demo -> how_to");
    }
    
    public void demo(HttpEvent e, ArrayList<String> args) throws FileNotFoundException, IOException, InterruptedException{
        byte[] b;
        File f;
        FileInputStream fis;
        if(args.size()>0)
        switch(args.get(0).toLowerCase()){
            case "chat":
                e.sendFileContents("/view/Product/Demo/chat/chat.html");
                break;
            case "upload_file":
                e.sendFileContents("/view/Product/Demo/upload_file/upload_file.html");
                break;
            case "server_sent":
                e.setContentType("text/event-stream");
                e.setHeaderField("Cache-Control","no-cache");
                while(e.isAlive()){
                    e.send(Long.toString(JHS.DATE.getTime()));
                    Thread.sleep(1);
                }
                System.out.println("Server sent event has ended.");
                break;
            case "text_editor":
                e.sendFileContents("/view/Product/Demo/text_editor/text_editor.html");
                break;
        }
    }
    
}
