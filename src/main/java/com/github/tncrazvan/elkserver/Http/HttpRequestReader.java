/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
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
package com.github.tncrazvan.elkserver.Http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import com.github.tncrazvan.elkserver.Elk;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.SocketTimeoutException;
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
    public HttpRequestReader(Socket client) throws NoSuchAlgorithmException, IOException {
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
            if(outputString.trim().length() == 0){
                client.close();
            }else{
                HttpHeader clientHeader = HttpHeader.fromString(outputString);
                outputString = "";
                if((clientHeader.get("Method").equals("POST") || port == 25) && !EOFException){
                    int chunkSize = 0;
                    if(clientHeader.isDefined("Content-Length")){
                        chunkSize = Integer.parseInt(clientHeader.get("Content-Length"));
                    }

                    if(chunkSize > 0){
                        chain = new byte[chunkSize];
                        input.readFully(chain);
                        outputString = new String(chain,charset);
                    }else{
                        int offset = 0;
                        chain = new byte[httpMtu];
                        try{
                            while(input.read(chain)>0){
                                if(offset < httpMtu){
                                    offset++;
                                }else{
                                    outputString = new String(chain,charset);
                                    offset = 0;
                                    chain = new byte[httpMtu];
                                }
                            }
                        }catch(SocketTimeoutException e){
                            outputString = new String(chain,charset);
                        }
                    }
                }
                this.onRequest(clientHeader,outputString);
            }
            
        } catch (IOException ex) {
            try {
                client.close();
            } catch (IOException ex1) {
                logger.log(Level.SEVERE,null,ex);
            }
        }
    }
    
    
    public abstract void onRequest(HttpHeader clientHeader, String content);
    
}