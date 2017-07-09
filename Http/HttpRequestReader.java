/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Http;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javahttpserver.JHS;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Razvan
 */
public abstract class HttpRequestReader extends Thread{
    protected Socket client=null;
    protected SSLSocket secureClient=null;
    protected BufferedReader reader=null;
    protected BufferedWriter writer=null;
    private String output = "";
    private Map<String,String> form = new HashMap<>();
    private JsonObject post = new JsonObject();
    public HttpRequestReader(Socket client) throws NoSuchAlgorithmException, IOException {
        /*if(JHS.PORT == 443){
            secureClient = (SSLSocket) client;
            secureClient.setEnabledCipherSuites(secureClient.getSupportedCipherSuites());

            
            secureClient.startHandshake();
            SSLSession sslSession = secureClient.getSession();

        }*/
        
        
        
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
    
    @Override
    public void run(){
        try {
            String line = "";
            boolean canRead = true;
            while (canRead) { //check null reference
                line = reader.readLine();
                if(line == null || line.length() == 0){
                    canRead = false;
                }
                output +=line+"\r\n";
            }
            
            HttpHeader clientHeader = HttpHeader.fromString(output);
            if(clientHeader.get("Method").equals("POST")){
                try {
                    line = "";
                    String currentObjectName = null;
                    canRead = true;
                    String currentLabel = null,
                            currentValue = "";
                    Pattern pattern1 = Pattern.compile("^Content-Disposition");
                    Pattern pattern2 = Pattern.compile("(?<=name\\=\\\").*?(?=\\\")");
                    Matcher matcher;

                    while(canRead){
                        line = reader.readLine();
                        //System.out.println(line);
                        if(currentObjectName == null && line.matches("^\\-+.*$")){
                            currentObjectName = line;
                            if(line.substring(line.length()-2,line.length()).equals("--")){
                                canRead = false;
                            }
                        }else if(currentObjectName != null && line.equals(currentObjectName)){
                            post.addProperty(currentLabel, currentValue);
                            currentLabel = null;
                            currentValue = "";
                        }else if(currentObjectName != null && line.equals(currentObjectName+"--")){
                            canRead = false;
                            post.addProperty(currentLabel, currentValue);
                        }else if(currentObjectName != null){

                            matcher = pattern1.matcher(line);
                            if(matcher.find()){
                                matcher = pattern2.matcher(line);
                                if(matcher.find() && currentLabel == null){
                                    currentLabel = matcher.group();
                                }
                            }else if(currentLabel != null && !line.equals("")){
                                currentValue = JHS.atob(line);
                            }
                        }

                    }
                } catch (IOException ex) {
                    Logger.getLogger(HttpEventListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            this.onRequest(clientHeader,post);
        } catch (Exception ex) {
            try {
                client.close();
            } catch (IOException ex1) {
                Logger.getLogger(HttpRequestReader.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    
    public abstract void onRequest(HttpHeader clientHeader, JsonObject post);
    
}
