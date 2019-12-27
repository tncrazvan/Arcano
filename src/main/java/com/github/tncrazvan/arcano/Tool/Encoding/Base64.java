/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Encoding;

import static com.github.tncrazvan.arcano.SharedObject.BASE64_DECODER;
import static com.github.tncrazvan.arcano.SharedObject.BASE64_ENCODER;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

/**
 *
 * @author Administrator
 */
public interface Base64 {
    
    /**
     * Decodes base64 String.
     * @param value base64 String.
     * @param charset character set to use
     * @return decoded String.
     */
    public static String atob(String value, String charset){
        try {
            return new String(BASE64_DECODER.decode(value.getBytes(charset)),charset);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Decodes base64 String to byte array.
     * @param value encoded String.
     * @param charset character set to use
     * @return decoded byte array.
     */
    public static  byte[] atobByte(String value, String charset){
        try {
            return BASE64_DECODER.decode(value.getBytes(charset));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Decodes base64 byte array.
     * @param value encoded byte array.
     * @return decoded byte array.
     */
    public static byte[] atobByte(byte[] value){
        return BASE64_DECODER.decode(value);
    }
    
    
    
    /**
    * Encodes String to base64.
    * @param value input String.
    * @param charset character set to use
    * @return encoded String.
    */ 
    public static String btoa(byte[] value,String charset){
        try {
            return new String(BASE64_ENCODER.encode(value),charset);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
   /**
    * Encodes String to base64.
    * @param value input String.
    * @param charset character set to use
    * @return encoded String.
    */ 
    public static String btoa(String value, String charset){
        try {
            return new String(BASE64_ENCODER.encode(value.getBytes(charset)),charset);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Encodes String to base64 byte array.
     * @param value input String.
     * @param charset character set to use
     * @return encoded byte array.
     */
    public static byte[] btoaGetBytes(String value, String charset){
        try {
            return BASE64_ENCODER.encode(value.getBytes(charset));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Encodes byte array to base64.
     * @param value input byte array.
     * @return encoded byte array.
     */
    public static byte[] btoaGetBytes(byte[] value){
        return BASE64_ENCODER.encode(value);
    }
}
