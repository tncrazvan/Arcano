package com.github.tncrazvan.arcano.Http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import com.github.tncrazvan.arcano.Common;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Razvan
 */
public abstract class HttpRequestReader extends Common implements Runnable{
    protected Socket client=null;
    protected SSLSocket secureClient=null;
    protected BufferedReader reader=null;
    protected BufferedWriter writer=null;
    protected final DataOutputStream output;
    protected final DataInputStream input;
    private StringBuilder outputString = new StringBuilder();
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
                    outputString.append((char)chain[0]);
                    if((char)chain[3] == '\r' && (char)chain[2] == '\n' && (char)chain[1] == '\r' && (char)chain[0] == '\n'){
                        keepReading = false;
                    }
                }catch(EOFException ex){
                    keepReading = false;
                    EOFException = true;
                    //ex.printStackTrace();
                }
            }
            if(outputString.length() == 0){
                client.close();
            }else{
                HttpHeader clientHeader = HttpHeader.fromString(outputString.toString().trim());
                //outputString = new StringBuilder();
                ArrayList<byte[]> inputList = new ArrayList<>();
                int length = 0;
                if(!EOFException){
                    int chunkSize = 0;
                    if(clientHeader.isDefined("Content-Length")){
                        chunkSize = Integer.parseInt(clientHeader.get("Content-Length"));
                    }

                    if(chunkSize > 0){
                        chain = new byte[chunkSize];
                        input.readFully(chain);
                        inputList.add(chain);
                        length += chain.length;
                        //outputString.append(new String(chain,charset));
                    }else{
                        int offset = 0;
                        chain = new byte[httpMtu];
                        try{
                            if(input.available() > 0)
                            while(input.read(chain)>0){
                                if(offset < httpMtu){
                                    offset++;
                                }else{
                                    //outputString.append(new String(chain,charset));
                                    inputList.add(chain);
                                    length += chain.length;
                                    offset = 0;
                                    chain = new byte[httpMtu];
                                }
                            }
                        }catch(SocketTimeoutException | EOFException e){
                            //outputString.append(new String(chain,charset));
                            length += chain.length;
                            inputList.add(chain);
                        }
                    }
                }
                byte[] inputBytes = new byte[length];
                int pos = 0;
                for(byte[] bytes : inputList){
                    for(int i=0;i<bytes.length;i++,pos++){
                        inputBytes[pos] = bytes[i];
                    }
                }
                this.onRequest(clientHeader,inputBytes);
            }
            
        } catch (IOException ex) {
            try {
                client.close();
            } catch (IOException ex1) {
                logger.log(Level.SEVERE,null,ex);
            }
        }
    }
    
    
    public abstract void onRequest(HttpHeader clientHeader, byte[] input);
    
}