/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.WebSocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

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
    public final String toString(){
        try {
            return new String(data,"UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return "";
        }
    }

    /**
     * Get the payload of the message as a String.
     * 
     * @param charset The charset to use when encoding the String.
     * @return the payload of the message as a String.
     * @throws java.io.UnsupportedEncodingException
     */
    public final String toString(final String charset) throws UnsupportedEncodingException {
        return new String(data, charset);
    }

    /**
     * Identifies a WebSocketMessage. This object contains the payload of the
     * message.
     */
    public WebSocketMessage() {
    }

    /**
     * Set the payload of the message. The server will read and send this data to
     * the client.
     * 
     * @param data bytes to set
     */
    public final void setBytes(final byte[] data) {
        this.data = data;
    }

    /**
     * Get the first byte of the payload.
     * 
     * @return the first byte of the payload.
     */
    public final byte toByte() {
        return data[0];
    }

    /**
     * Set the payload of the message.
     * 
     * @param data payload to set.
     */
    public final void setByte(final byte data) {
        this.data = new byte[] { data };
    }

    /**
     * Identifies a WebSocketMessage.
     * 
     * @param data the payload of the message.
     */
    public WebSocketMessage(final byte[] data) {
        this.data = data;
    }

    /**
     * Set the payload of the message.
     * 
     * @param data    the payload of the message.
     * @param charset the charset used to encode the payload.
     * @throws java.io.UnsupportedEncodingException
     */
    public final void setString(final String data, final String charset) throws UnsupportedEncodingException {
        this.data = data.getBytes(charset);
    }

    /**
     * Identifies a WebSocketMessage.
     * 
     * @param data    the payload of the message.
     * @param charset the charset used to encode the payload.
     * @throws java.io.UnsupportedEncodingException
     */
    public WebSocketMessage(final String data, final String charset) throws UnsupportedEncodingException {
        this.data = data.getBytes(charset);
    }

    /**
     * Set the payload of the message.
     * 
     * @param data the payload of the message. The payload will be encoded to
     *             charset UTF-8 by default.
     */
    public final void setString(final String data) {
        try {
            this.data = data.getBytes("UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Identifies a WebSocketMessage.
     * 
     * @param data the payload of the message. The payload will be encoded to
     *             charset UTF-8 by default.
     */
    public WebSocketMessage(final String data) {
        this.data = data.getBytes();
    }
}
