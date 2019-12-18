/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.WebSocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public final class WebSocketMessage {

    /**
     * Contains the payload of the message.
     * You can send this back to the current client or a different web socket client.
     */
    public byte[] data;

    /**
     * Get the payload of the message as a String. The String is encoded to UTF-8 by default.
     * @return the payload of the message as a String.
     */
    @Override
    public String toString(){
        try {
            return new String(data,"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    /**
     * Get the payload of the message as a String.
     * @param charset The charset to use when encoding the String.
     * @return the payload of the message as a String.
     * @throws java.io.UnsupportedEncodingException
     */
    public String toString(String charset) throws UnsupportedEncodingException{
        return new String(data,charset);
    }

    /**
     * Identifies a WebSocketMessage.
     * This object contains the payload of the message.
     */
    public WebSocketMessage() {}
    
    /**
     * Set the payload of the message. The server will read and send this data to the client.
     * @param data bytes to set
     */
    public void setBytes(byte[] data){
        this.data=data;
    }
    
    /**
     * Get the first byte of the payload.
     * @return the first byte of the payload.
     */
    public byte toByte(){
        return data[0];
    }
    /**
     * Set the payload of the message.
     * @param data payload to set.
     */
    public void setByte(byte data){
        this.data=new byte[]{data};
    }
    /**
     * Identifies a WebSocketMessage. 
     * @param data the payload of the message.
     */
    public WebSocketMessage(byte[] data) {
        this.data = data;
    }
    /**
     * Set the payload of the message.
     * @param data the payload of the message.
     * @param charset the charset used to encode the payload.
     * @throws java.io.UnsupportedEncodingException
     */
    public void setString(String data, String charset) throws UnsupportedEncodingException{
        this.data = data.getBytes(charset);
    }
    /**
     * Identifies a WebSocketMessage. 
     * @param data the payload of the message.
     * @param charset the charset used to encode the payload.
     * @throws java.io.UnsupportedEncodingException
     */
    public WebSocketMessage(String data,String charset) throws UnsupportedEncodingException {
        this.data = data.getBytes(charset);
    }
    /**
     * Set the payload of the message.
     * @param data the payload of the message. The payload will be encoded to charset UTF-8 by default.
     */
    public void setString(String data) {
        try {
            this.data=data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Identifies a WebSocketMessage. 
     * @param data the payload of the message. The payload will be encoded to charset UTF-8 by default.
     */
    public WebSocketMessage(String data) {
        this.data = data.getBytes();
    }
}
