package com.github.tncrazvan.arcano.WebSocket;

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

import javax.xml.bind.DatatypeConverter;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.EventManager;
import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Http.HttpRequest;
import static com.github.tncrazvan.arcano.Tool.Hashing.getSha1Bytes;
import static com.github.tncrazvan.arcano.Tool.Hashing.getSha1String;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_SWITCHING_PROTOCOLS;

/**
 *
 * @author Razvan
 */
public abstract class WebSocketManager extends EventManager{
    protected final BufferedReader reader;
    protected final String requestId;
    protected final OutputStream outputStream;
    private final Map<String,String> userLanguages = new HashMap<>();
    private boolean connected = true;
    //private final HttpHeaders headers;
    public WebSocketManager(SharedObject so,BufferedReader reader, Socket client, HttpRequest request) throws IOException {
        this.setSharedObject(so);
        this.setSocket(client);
        this.setHttpRequest(request);
        this.request=request;
        this.client=client;
        this.reader=reader;
        this.requestId = getSha1String(System.identityHashCode(client)+"::"+System.currentTimeMillis(),so.charset);
        this.outputStream = client.getOutputStream();
        //header = new HttpHeaders();
    }

    public HttpHeaders getClientHeader(){
        return request.headers;
    }

    @Override
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
        return this.request.headers.get("User-Agent");
    }

    public void execute(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String acceptKey = DatatypeConverter.printBase64Binary(getSha1Bytes(request.headers.get("Sec-WebSocket-Key") + SharedObject.WEBSOCKET_ACCEPT_KEY,so.charset));
                    
                    headers.set("@Status", STATUS_SWITCHING_PROTOCOLS);
                    headers.set("Connection","Upgrade");
                    headers.set("Upgrade","websocket");
                    headers.set("Sec-WebSocket-Accept",acceptKey);
                    outputStream.write((headers.toString()+"\r\n").getBytes());
                    outputStream.flush();
                    onOpen();
                    InputStream read = client.getInputStream();
                    while(connected){
                        unmask((byte) read.read());
                    }
                } catch (IOException ex) {
                    close();
                } catch (NoSuchAlgorithmException ex) {
                    LOGGER.log(Level.SEVERE,null,ex);
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
    //private boolean fin,rsv1,rsv2,rsv3;
    private byte opcode;
    private byte[] payload = null,mask = null,length = null;
    //private final String base = "";
    public void unmask(byte b) throws UnsupportedEncodingException, IOException{
        //System.out.println("=================================");
        switch (reading) {
            case FIRST_BYTE:
                //fin = ((b & 0x80) != 0);
                //rsv1 = ((b & 0x40) != 0);
                //rsv2 = ((b & 0x20) != 0);
                //rsv3 = ((b & 0x10) != 0);
                opcode = (byte)(b & 0x0F);
                if(opcode == 0x8){ //fin
                    close();
                }
                mask = new byte[4];
                reading = SECOND_BYTE;
                break;
            case SECOND_BYTE:
                lengthKey = b & 127;
                if(lengthKey <= 125){
                    length = new byte[1];
                    length[0] = (byte) lengthKey;
                    payloadLength = lengthKey & 0xff;
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
                if(payload.length == 0){
                    onMessage(payload);
                    break;
                }
                try{
                    payload[payloadIndex] = (byte) (b ^ mask[payloadIndex%4]);
                }catch(Exception e){
                    e.printStackTrace(System.out);
                }
                payloadIndex++;
                if(payloadIndex == payload.length){
                    reading = DONE;

                    onMessage(payload);
                    lengthKey = 0;
                    reading = FIRST_BYTE;
                    lengthIndex = 0;
                    maskIndex = 0;
                    payloadIndex = 0;
                    payload = null;
                    mask = null;
                    length = null;
                }
                break;
        }

    }

    public void close(){
        try {
            connected = false;
            client.close();
            onClose();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }
    public void send(String data){
        try {
            send(data.getBytes(so.charset), false);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }
    public void send(byte[] data){
        send(data, true);
    }
    public void send(byte[] data,boolean binary){
        int offset = 0, maxLength = so.webSocketMtu-1;
        if(data.length > maxLength){
            while(offset < data.length){
                if(offset+maxLength > data.length){
                    encodeAndSendBytes(Arrays.copyOfRange(data, offset, data.length),binary);
                    offset = data.length;
                }else{
                    encodeAndSendBytes(Arrays.copyOfRange(data, offset, offset+maxLength),binary);
                    offset += maxLength;
                }
            }
        }else{
            encodeAndSendBytes(data,binary);
        }
    }

    private void encodeAndSendBytes(byte[] messageBytes, boolean binary){
        try {
            outputStream.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
        try {
            //We need to set only FIN and Opcode.
            outputStream.write(binary?0x82:0x81);

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
                LOGGER.log(Level.SEVERE,null,ex);
            }
        } catch (IOException ex) {
            close();
        }

    }

    public void send(int data){
        send(""+data);
    }


    public void broadcast(String msg,Object o){
        try {
            broadcast(msg.getBytes(so.charset),o,false);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }
    public void broadcast(byte[] data,Object o){
        broadcast(data, o, true);
    }
    public void broadcast(byte[] data,Object o,boolean binary){
        for (WebSocketEvent e : so.WEB_SOCKET_EVENTS.get(o.getClass().getCanonicalName())) {
            if(e!=this){
                e.send(data,binary);
            }
        }
    }

    public void send(byte[] data,WebSocketGroup group){
        send(data, group, true);
    }
    public void send(byte[] data, WebSocketGroup group,boolean binary){
        group.getMap().keySet().forEach((key) -> {
            final WebSocketEvent client = group.getMap().get(key);
            if((WebSocketManager)client != this){
                client.send(data,binary);
            }
        });
    }

    public void send(String data, WebSocketGroup group){
        try {
            send(data.getBytes(so.charset),group,false);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }

    }




    protected abstract void onOpen();
    protected abstract void onMessage(byte[] data);
    protected abstract void onClose();
}
