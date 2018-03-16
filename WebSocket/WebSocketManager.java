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
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import elkserver.Http.HttpHeader;
import elkserver.Elk;
import elkserver.EventManager;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.bind.DatatypeConverter;
import static sun.security.krb5.Confounder.bytes;

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
                    DataInputStream dis = new DataInputStream(read);
                    int bytes;
                    byte tmp;
                    while(connected){
                        unmask(dis.readByte());
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
    
    private final int FIRST_BYTE = 0, SECOND_BYTE = 1, LENGTH2 = 2, LENGTH8 = 3, MASK = 4, PAYLOAD = 5, DONE = 6;
    private int lengthKey = 0, reading = FIRST_BYTE, lengthIndex = 0, maskIndex = 0, payloadIndex = 0, payloadLength = 0;
    private boolean fin,rsv1,rsv2,rsv3;
    private byte opcode;
    private byte[] payload = null,mask = null,length = null;
    public void unmask(byte b) throws UnsupportedEncodingException{
        //System.out.println("=================================");
        switch (reading) {
            case FIRST_BYTE:
                fin = ((b & 0x80) != 0);
                rsv1 = ((b & 0x40) != 0);
                rsv2 = ((b & 0x20) != 0);
                rsv3 = ((b & 0x10) != 0);
                opcode = (byte)(b & 0x0F);
                mask = new byte[4];
                reading = SECOND_BYTE;
                break;
            case SECOND_BYTE:
                lengthKey = b & 127;
                if(lengthKey <= 125){
                    length = new byte[1];
                    length[0] = (byte) lengthKey;
                    reading = MASK;
                }else if(lengthKey == 126){
                    reading = LENGTH2;
                    length = new byte[2];
                }else if(lengthKey == 127){
                    reading = LENGTH8;
                    length = new byte[8];
                }   
                break;
            case LENGTH2:
                length[lengthIndex] = b;
                lengthIndex++;
                if(lengthIndex == 2){
                    payloadLength = ((length[0] & 0xff) << 8) | (length[1] & 0xff);
                    reading = MASK;
                }   
                break;
            case LENGTH8:
                length[lengthIndex] = b;
                lengthIndex++;
                if(lengthIndex == 8){
                    payloadLength = length[0] & 0xff;
                    for(int i = 1; i<length.length;i++){
                        payloadLength = ((payloadLength) << 8)  | (length[i] & 0xff);
                    }
                    reading = MASK;
                }   
                break;
            case MASK:
                mask[maskIndex] = b;
                maskIndex++;
                if(maskIndex == 4){
                    reading = PAYLOAD;
                    //int l = (int)ByteBuffer.wrap(length).getLong();
                    payload = new byte[payloadLength];
                }   
                break;
            case PAYLOAD:
                payload[payloadIndex] = (byte) (b ^ mask[payloadIndex%4]);
                payloadIndex++;
                if(payloadIndex == payload.length-1){
                    reading = DONE;
                }   
                break;
            case DONE:
                onMessage(client, payload);
                lengthKey = 0;
                reading = FIRST_BYTE;
                lengthIndex = 0;
                maskIndex = 0;
                payloadIndex = 0;
                payload = null;
                mask = null;
                length = null;
                break;
            default:
                break;
        }
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