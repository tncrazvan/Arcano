/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.WebSocket;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 * @author Administrator
 */
public final class WebSocketMessage {
    public byte[] data;

    @Override
    public String toString(){
        return new String(data);
    }
    

    
    public WebSocketMessage() {}
    
    public void setBytes(byte[] data){
        this.data=data;
    }
    public byte toByte(){
        return data[0];
    }
    public void setByte(byte data){
        this.data=new byte[]{data};
    }
    public WebSocketMessage(byte[] data) {
        this.data = data;
    }
    public WebSocketMessage(byte data) {
        this.data = new byte[]{data};
    }
    
    public void setString(String data, String charset) throws UnsupportedEncodingException{
        this.data = data.getBytes(charset);
    }
    public WebSocketMessage(String data,String charset) throws UnsupportedEncodingException {
        this.data = data.getBytes(charset);
    }
    
    public void setString(String data){
        this.data=data.getBytes();
    }
    public WebSocketMessage(String data) {
        this.data = data.getBytes();
    }

    public char toChar(){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Character.BYTES);
        BUFFER.put(data[0]);
        BUFFER.flip();//need flip 
        return BUFFER.getChar();
    }
    public void setChar(char data){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Character.BYTES);
        BUFFER.putChar(data);
        this.data = BUFFER.array();
    }
    public WebSocketMessage(char data) {
        final ByteBuffer BUFFER = ByteBuffer.allocate(Character.BYTES);
        BUFFER.putChar(data);
        this.data = BUFFER.array();
    }
    
    public short toShort(){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Short.BYTES);
        BUFFER.put(data);
        BUFFER.flip();//need flip 
        return BUFFER.getShort();
    }
    public void setShort(short data){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Short.BYTES);
        BUFFER.putShort(data);
        this.data = BUFFER.array();
    }
    public WebSocketMessage(short data) {
        final ByteBuffer BUFFER = ByteBuffer.allocate(Short.BYTES);
        BUFFER.putShort(data);
        this.data = BUFFER.array();
    }
    
    public long toLong(){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Long.BYTES);
        BUFFER.put(data);
        BUFFER.flip();//need flip 
        return BUFFER.getLong();
    }
    public void setLong(long data){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Long.BYTES);
        BUFFER.putLong(data);
        this.data = BUFFER.array();
    }
    public WebSocketMessage(long data) {
        final ByteBuffer BUFFER = ByteBuffer.allocate(Long.BYTES);
        BUFFER.putLong(data);
        this.data = BUFFER.array();
    }
    
    public float toFloat(){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Float.BYTES);
        BUFFER.put(data);
        BUFFER.flip();//need flip 
        return BUFFER.getFloat();
    }
    public void setFloat(float data){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Float.BYTES);
        BUFFER.putFloat(data);
        this.data = BUFFER.array();
    }
    public WebSocketMessage(float data) {
        final ByteBuffer BUFFER = ByteBuffer.allocate(Float.BYTES);
        BUFFER.putFloat(data);
        this.data = BUFFER.array();
    }
    
    public double toDouble(){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Double.BYTES);
        BUFFER.put(data);
        BUFFER.flip();//need flip 
        return BUFFER.getDouble();
    }
    public void setDouble(double data){
        final ByteBuffer BUFFER = ByteBuffer.allocate(Double.BYTES);
        BUFFER.putDouble(data);
        this.data = BUFFER.array();
    }
    public WebSocketMessage(Double data) {
        final ByteBuffer BUFFER = ByteBuffer.allocate(Double.BYTES);
        BUFFER.putDouble(data);
        this.data = BUFFER.array();
    }
}
