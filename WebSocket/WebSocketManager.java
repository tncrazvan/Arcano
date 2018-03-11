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
package elkserver.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import elkserver.Http.HttpHeader;
import elkserver.Elk;
import elkserver.EventManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Razvan
 */
public abstract class WebSocketManager extends EventManager{
    private static ArrayList<WebSocketEvent> subscriptions = new ArrayList<>();
    protected final Socket client;
    protected final HttpHeader clientHeader;
    protected final BufferedReader reader;
    protected final String requesteId;
    protected final OutputStream outputStream;
    private Map<String,String> userLanguages = new HashMap<>();
    private boolean connected = true;
    
    //private final HttpHeader header;
    public WebSocketManager(BufferedReader reader, Socket client, HttpHeader clientHeader,String requestId) throws IOException {
        super(clientHeader);
        this.client=client;
        this.clientHeader=clientHeader;
        this.reader=reader;
        this.requesteId=requestId;
        this.outputStream = client.getOutputStream();
        //header = new HttpHeader();
    }
    
    public HttpHeader getClientHeader(){
        return clientHeader;
    }
    
    public Socket getClient(){
        return client;
    }
    
    public Map<String,String> getUserLanguages(){
        return userLanguages;
    }
    
    public String getUserDefaultLanguage(){
        return userLanguages.get("DEFAULT-LANGUAGE");
    }
    
    public String getUserAgent(){
        return clientHeader.get("User-Agent");
    }
    
    public void execute(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String acceptKey = DatatypeConverter.printBase64Binary(Elk.getSha1Bytes(clientHeader.get("Sec-WebSocket-Key") + Elk.wsAcceptKey));
                    
                    header.set("Status", "HTTP/1.1 101 Switching Protocols");
                    header.set("Connection","Upgrade");
                    header.set("Upgrade","websocket");
                    header.set("Sec-WebSocket-Accept",acceptKey);
                    outputStream.write((header.toString()+"\r\n").getBytes());
                    outputStream.flush();
                    onOpen(client);
                    byte[] data = new byte[Elk.wsMtu];
                    //char[] data = new char[128];
                    InputStream read = client.getInputStream();
                    int bytes;
                    while(connected){
                        bytes = read.read(data);
                        if(unmask(data, bytes)){
                            onMessage(client, digest);
                            startNew = true;
                        }
                    }
                } catch (IOException ex) {
                    close();
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

        
    }
    
    
    /*
        WEBSOCKET FRAME:
        
        
              0                   1                   2                   3
              0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
             +-+-+-+-+-------+-+-------------+-------------------------------+
             |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
             |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
             |N|V|V|V|       |S|             |   (if payload len==126/127)   |
             | |1|2|3|       |K|             |                               |
             +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
             |     Extended payload length continued, if payload len == 127  |
             + - - - - - - - - - - - - - - - +-------------------------------+
             |                               |Masking-key, if MASK set to 1  |
             +-------------------------------+-------------------------------+
             | Masking-key (continued)       |          Payload Data         |
             +-------------------------------- - - - - - - - - - - - - - - - +
             :                     Payload Data continued ...                :
             + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
             |                     Payload Data continued ...                |
             +---------------------------------------------------------------+
        */
    
    protected byte[] mask;
    protected int 
            opCode,
            length,
            payloadOffset = 0,
            digestIndex = 0;
    private byte[] digest = new byte[8];
    private boolean startNew = true;
    public boolean unmask(byte[] payload,int bytes) throws UnsupportedEncodingException{
        //System.out.println("---------- NEW -----------");
        if(bytes == -1){
            close();
            return false;
        }
        
        int i = 0;
        boolean fin =  (int)(payload[0] & 0x77) != 1;
        opCode = (byte)(payload[0] & 0x0F);
        // 0x88 = 10001000 = -120
        // which means opCode = 8 and fin = 1
        // this is the standard way that all browsers use 
        // to indicate and end connection frame
        // I make sure there is no
        //System.out.println("payload[0]:"+payload[0]+",bytes:"+bytes);
        if(payload[0] == -120 && bytes <= 8){
            close();
            return false;
        }else if(bytes <= 6){
            return false;
        }
        
        if(startNew){
        mask = new byte[4];
        length = (int)payload[1] & 127;
        if(length == 126){
            length = ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
            mask[0] = payload[4];
            mask[1] = payload[5];
            mask[2] = payload[6];
            mask[3] = payload[7];
            payloadOffset = 8;

        }else if(length == 127){
            byte[] tmp = new byte[8];
            tmp[0] = payload[2];
            tmp[1] = payload[3];
            tmp[2] = payload[4];
            tmp[3] = payload[5];
            tmp[4] = payload[6];
            tmp[5] = payload[7];
            tmp[6] = payload[8];
            tmp[7] = payload[9];

            length = (int)ByteBuffer.wrap(tmp).getLong();
            mask[0] = payload[10];
            mask[1] = payload[11];
            mask[2] = payload[12];
            mask[3] = payload[13];
            payloadOffset = 14;
        }else{
            mask[0] = payload[2];
            mask[1] = payload[3];
            mask[2] = payload[4];
            mask[3] = payload[5];
            payloadOffset = 6;
        }

        startNew=false;
        digest = new byte[length];
        digestIndex = 0;
    }else{
        payloadOffset = 0;
    }
        
        
        
        if(fin){
            payloadOffset = 0;
        }
        
        byte currentByte;
        while(digestIndex < digest.length && (payloadOffset+i) < bytes){
            currentByte = (byte) (payload[(payloadOffset+i)] ^ mask[digestIndex%mask.length]);
            digest[digestIndex] = currentByte;
            digestIndex++;
            i++;
        }
        if(digestIndex == digest.length){
            return true;
        }
        return false;
        
    }
    
    public void close(){
        try {
            connected = false;
            client.close();
            onClose(client);
        } catch (IOException ex) {
            Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void send(byte[] data){
        int offset = 0, tmpLength = 60000;
        if(data.length > tmpLength){
            while(offset < data.length){
                encodeAndSendBytes(Arrays.copyOfRange(data, offset, offset+tmpLength));

                if(offset+tmpLength > data.length){
                    encodeAndSendBytes(Arrays.copyOfRange(data, offset, data.length));
                    offset = data.length;
                }else{
                    offset += tmpLength;
                }
            }
        }else{
            encodeAndSendBytes(data);
        }
        
    }
    
    private void encodeAndSendBytes(byte[] messageBytes){
        try {
            outputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            //We need to set only FIN and Opcode.
            outputStream.write(0x82);

            //Prepare the payload length.
            if(messageBytes.length <= 125) {
                outputStream.write(messageBytes.length);
            }else { //We assume it is 16 but length. Not more than that.
                outputStream.write(0x7E);
                int b1 =( messageBytes.length >> 8) &0xff;
                int b2 = messageBytes.length &0xff;
                outputStream.write(b1);
                outputStream.write(b2);
            }

            //Write the data.
            outputStream.write(messageBytes);
            try {
                outputStream.flush();
            } catch (IOException ex) {
                Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            close();
        }
        
    }
    
    public void send(int data){
        send(""+data);
    }
    
    public void send(String message) {
        try {
            byte messageBytes[] = message.getBytes();

            //We need to set only FIN and Opcode.
            outputStream.write(0x81);

            //Prepare the payload length.
            if(messageBytes.length <= 125) {
                outputStream.write(messageBytes.length);
            }

            else { //We assume it is 16 but length. Not more than that.
                outputStream.write(0x7E);
                int b1 =( messageBytes.length >> 8) &0xff;
                int b2 = messageBytes.length &0xff;
                outputStream.write(b1);
                outputStream.write(b2);
            }

            //Write the data.
            outputStream.write(messageBytes);
        } catch (IOException ex) {
            close();
        }

    }
    
    public void broadcast(String msg,Object o){
        try {
            broadcast(msg.getBytes(Elk.charset),o);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void broadcast(byte[] data,Object o){
        Iterator i = Elk.WS_EVENTS.get(o.getClass().getCanonicalName()).iterator();
        while(i.hasNext()){
            WebSocketEvent e = (WebSocketEvent) i.next();
            if(e!=this){
                e.send(data);
            }
        }
    }
    
    
    public void send(byte[] data, WebSocketGroup group){
        group.getMap().keySet().forEach((key) -> {
            WebSocketEvent client = group.getMap().get(key);
            if((WebSocketManager)client != this){
                client.send(data);
            }
        });
    }
    
    public void send(String data, WebSocketGroup group){
        try {
            send(data.getBytes(Elk.charset),group);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected abstract void onOpen(Socket client);
    protected abstract void onMessage(Socket client, byte[] data);
    protected abstract void onClose(Socket client);
}
