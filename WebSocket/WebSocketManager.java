/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javahttpserver.Http.HttpHeader;
import javahttpserver.JHS;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Razvan
 */
public abstract class WebSocketManager{
    protected final Socket client;
    protected final HttpHeader clientHeader;
    protected final BufferedReader reader;
    protected final String requesteId;
    protected final OutputStream outputStream;
    private Map<String,String> userLanguages = new HashMap<String,String>();
    protected byte[] oldMask;
    protected int oldOpCode;
    protected int oldLength;
    private boolean connected = true;
    private boolean isMoz = false;
    
    private long prev_hit;
    public WebSocketManager(BufferedReader reader, Socket client, HttpHeader clientHeader,String requestId) throws IOException {
        this.client=client;
        this.clientHeader=clientHeader;
        this.reader=reader;
        this.requesteId=requestId;
        this.outputStream = client.getOutputStream();
        isMoz = clientHeader.get("Connection").equals("keep-alive, Upgrade");
    }
    
    public HttpHeader getClientHeader(){
        return clientHeader;
    }
    
    private void findUserLanguages(){
        String[] tmp = new String[2];
        String[] languages = clientHeader.get("Accept-Language").split(",");
        userLanguages.put("DEFAULT-LANGUAGE", languages[0]);
        for(int i=1;i<languages.length;i++){
            tmp=languages[i].split(";");
            userLanguages.put(tmp[0], tmp[1]);
        }
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
    
    public String getCookie(String name){
        return clientHeader.getCookie(name);
    }
    
    public boolean cookieIsset(String key){
        return clientHeader.cookieIsset(key);
    }

    public void execute(){
        new Thread(()->{
            try {
                String acceptKey = DatatypeConverter.printBase64Binary(JHS.getSha1Bytes(clientHeader.get("Sec-WebSocket-Key") + JHS.WS_ACCEPT_KEY));
                
                HttpHeader header = new HttpHeader();
                header.set("Status", "HTTP/1.1 101 Switching Protocols");
                
                if(clientHeader.get("Connection").equals("Upgrade")){
                    //webkit (and others) response
                    header.set("Connection","Upgrade");
                }else if(clientHeader.get("Connection").equals("keep-alive, Upgrade")){
                    //mozilla response
                    header.set("Connection","keep-alive, Upgrade");
                }
                
                header.set("Upgrade","websocket");
                header.set("Sec-WebSocket-Accept",acceptKey);
                outputStream.write((header.toString()+"\r\n").getBytes());
                outputStream.flush();
                onOpen(client);
                Charset UTF8 = Charset.forName("UTF-8");
                byte[] data = new byte[JHS.WS_MTU];
                //char[] data = new char[128];
                InputStream read = client.getInputStream();
                int bytes = 0;
                String currentMessage = null;
                while(connected){
                    bytes = read.read(data);
                    currentMessage = new String(isMoz?unmaskMoz(data, bytes):unmask(data, bytes));

                    if(this.oldOpCode==8){
                        connected = false;
                        client.close();
                        onClose(client);
                    }else{
                        onMessage(client, currentMessage);
                    }
                }
            } catch (IOException ex) {
                try {
                    connected = false;
                    client.close();
                    onClose(client);
                } catch (IOException ex1) {
                    Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public byte[] unmaskMoz(byte[] payload,int bytes){
        ByteBuffer buf = ByteBuffer.wrap(payload);
        int fin =  payload[0] & 0x77;
        this.oldOpCode = (byte)(payload[0] & 0x0F);
        byte[] result;
        if(fin != 1){
            result = new byte[bytes];
            for(int i = 0;i<result.length;i++){
                result[i] = (byte) (payload[i] ^ this.oldMask[i%this.oldMask.length]);
            }
        }else{
            byte[] masks = new byte[4];
            int length = (int)payload[1] & 127;
            this.oldLength = length;
            if(length == 126){
                for(int i=0;i<4;i++) buf.get();
            }else if(length == 127){
                for(int i=0;i<10;i++) buf.get();
            }else{
                for(int i=0;i<2;i++) buf.get();
            }
            
            buf.get(masks, 0, masks.length);
            int resultOffset=buf.position();
            result = new byte[bytes-resultOffset];
            
            for(int i = 0;i<result.length;i++){
                result[i] = (byte) (payload[resultOffset+i] ^ masks[i%masks.length]);
            }
            this.oldMask = masks;
        }
        
        return result;
    }
    
    public byte[] unmask(byte[] payload,int bytes){
        ByteBuffer buf = ByteBuffer.wrap(payload);
        int fin =  payload[0] & 0x77;
        this.oldOpCode = (byte)(payload[0] & 0x0F);
        if(fin != 1){
            byte[] result;
            if(this.oldLength == 126){
                for(int i=0;i<8;i++) buf.get();
            }else if(this.oldLength == 127){
                for(int i=0;i<14;i++) buf.get();
            }else{
                for(int i=0;i<6;i++) buf.get();
            }
            
            int resultOffset=buf.position();
            result = new byte[bytes-resultOffset];
            
            byte currentByte;
            if(!Arrays.toString(this.oldMask).equals("null"))
            for(int i = 0;i<result.length;i++){
                currentByte = (byte) (payload[resultOffset+i] ^ this.oldMask[i%this.oldMask.length]);
                result[i] = currentByte;

            }
            return result;
        }else{
            byte[] result;
            byte[] masks = new byte[4];
            int length = (int)payload[1] & 127;
            this.oldLength = length;
            if(length == 126){
                for(int i=0;i<4;i++) buf.get();
            }else if(length == 127){
                for(int i=0;i<10;i++) buf.get();
            }else{
                for(int i=0;i<2;i++) buf.get();
            }
            
            buf.get(masks, 0, masks.length);
            int resultOffset=buf.position();
            result = new byte[bytes-resultOffset];
            
            byte currentByte;
            for(int i = 0;i<result.length;i++){
                currentByte = (byte) (payload[resultOffset+i] ^ masks[i%masks.length]);
                result[i] = currentByte;
            }
            this.oldMask = masks;
            return result;
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
        send(new String(data));
    }
    
    public void send(int data){
        send(""+data);
    }
    
    public void send(String message) {
            
        try {
            Thread.sleep(0, 1);
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
            try {
                connected = false;
                client.close();
                onClose(client);
            } catch (IOException ex1) {
                Logger.getLogger(WebSocketManager.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
    
    public void broadcast(String msg){
        Iterator i = JHS.EVENT_WS.iterator();
        
        while(i.hasNext()){
            WebSocketEvent e = (WebSocketEvent) i.next();
            if(e!=this){
                e.send(msg);
            }
        }
    }
    
    protected abstract void onOpen(Socket client);
    protected abstract void onMessage(Socket client, String msg);
    protected abstract void onClose(Socket client);
}
