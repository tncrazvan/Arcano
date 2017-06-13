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
public abstract class HttpRequestReader{
    protected final Socket client;
    protected final BufferedReader reader;
    protected final BufferedWriter writer;
    private String output = "";
    public HttpRequestReader(Socket client) throws IOException {
        this.client=client;
        reader = new BufferedReader(
                    new InputStreamReader(
                            client
                                    .getInputStream()));
        writer = new BufferedWriter(
                    new OutputStreamWriter(
                            client
                                    .getOutputStream()));
    }
    
    public void execute(){
        try {
            String line = reader.readLine();
            while(line != null && line.length() > 0){
                output +=line+"\r\n";
                line = reader.readLine();
            }
            this.onRequest(output);
        } catch (IOException ex) {
            Logger.getLogger(HttpRequestReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public abstract void onRequest(String result);
    
}
