/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Razvan
 */
public abstract class HttpRequestReader extends Thread{
    protected Socket client=null;
    protected BufferedReader reader=null;
    protected BufferedWriter writer=null;
    private String output = "";
    public HttpRequestReader(Socket client) {
        try {
            this.client=client;
            reader = new BufferedReader(
                    new InputStreamReader(
                            client
                                    .getInputStream()));
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            client
                                    .getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(HttpRequestReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        try {
            String line = reader.readLine();
            while(line != null && line.length() > 0){
                output +=line+"\r\n";
                line = reader.readLine();
            }
            this.onRequest(output);
        } catch (IOException ex) {
            try {
                client.close();
            } catch (IOException ex1) {
                Logger.getLogger(HttpRequestReader.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    public abstract void onRequest(String result);
    
}
