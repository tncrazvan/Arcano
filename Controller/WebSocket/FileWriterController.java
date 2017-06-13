/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.WebSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javahttpserver.JHS;
import javahttpserver.WebSocket.WebSocketEvent;
import javahttpserver.WebSocket.WebSocketInterface;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Razvan
 */
public class FileWriterController implements WebSocketInterface{
    private String payload = "";
    private String[] file = new String[2];
    private FileOutputStream fs;
    
    
    @Override
    public void onOpen(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Connected.");
    }

    @Override
    public void onMessage(WebSocketEvent e, String msg, ArrayList<String> args) {
        try {
            writeFile(msg, e);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileWriterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onClose(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Connection closed.");
    }
    
    
    private void writeFile(String msg,WebSocketEvent e) throws InterruptedException{
        if(msg.equals(";")){
            file = payload.split(",");
            File f = new File(JHS.PUBLIC_WWW+"/files/"+file[0]);
            try {
                f.createNewFile();
                fs = new FileOutputStream(f);
                fs.write(DatatypeConverter.parseBase64Binary(file[1]));
                fs.close();
                System.out.println("File created.");
            } catch (IOException ex) {
                Logger.getLogger(WebSocketEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            payload +=msg;
            e.send("0");
        }
    }
}
