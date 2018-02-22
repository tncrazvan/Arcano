/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package elkserver.Http;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import elkserver.Elk;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Razvan
 */
public abstract class HttpRequestReader extends Elk implements Runnable{
    protected Socket client=null;
    protected SSLSocket secureClient=null;
    protected BufferedReader reader=null;
    protected BufferedWriter writer=null;
    protected final DataOutputStream output;
    protected final DataInputStream input;
    private String outputString = "";
    private final JsonObject post = new JsonObject();
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
        output = new DataOutputStream(client.getOutputStream());
        input = new DataInputStream(client.getInputStream());
        
    }
    
    @Override
    public void run(){
        try {
            byte[] chain = new byte[]{0,0,0,0};
            boolean keepReading = true, EOFException = false;
            while (keepReading) {
                try{
                    chain[3] = chain[2];
                    chain[2] = chain[1];
                    chain[1] = chain[0];
                    chain[0] = input.readByte();
                    outputString += (char)chain[0];
                    if((char)chain[3] == '\r' && (char)chain[2] == '\n' && (char)chain[1] == '\r' && (char)chain[0] == '\n'){
                        keepReading = false;
                    }
                }catch(EOFException ex){
                    keepReading = false;
                    EOFException = true;
                    //ex.printStackTrace();
                }
            }
            HttpHeader clientHeader = HttpHeader.fromString(outputString);
            outputString = "";
            
            if((clientHeader.get("Method").equals("POST") || port == 25) && !EOFException){
                
                try {
                    int chunkSize = 2048, offset = 0;
                    byte[] tmp = new byte[chunkSize];
                    boolean arrayIsEmpty = false;

                    keepReading = true;
                    while (keepReading && !arrayIsEmpty) {
                        keepReading = (input.read(tmp, offset, chunkSize)== -1);
                        arrayIsEmpty = byteArrayIsEmpty(tmp);
                        outputString += new String(tmp);
                        offset += chunkSize;
                    }
                    String[] lines = outputString.split("\r\n");
                    String currentLabel = null,
                            currentValue = "";
                    Pattern pattern1 = Pattern.compile("^Content-Disposition");
                    Pattern pattern2 = Pattern.compile("(?<=name\\=\\\").*?(?=\\\")");
                    Matcher matcher;
                    boolean next = false, skippedBlank = false;
                    for(int i = 0; i<lines.length; i++){
                        matcher = pattern1.matcher(lines[i]);
                        if(matcher.find()){
                            matcher = pattern2.matcher(lines[i]);
                            if(matcher.find() && currentLabel == null){
                                currentLabel = matcher.group();
                                i +=2;
                                currentValue = atob(lines[i]);
                                post.addProperty(currentLabel, currentValue);
                                currentLabel = null;
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