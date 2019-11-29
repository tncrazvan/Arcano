package com.github.tncrazvan.arcano.Tool.JavaScript.Http;

import static com.github.tncrazvan.arcano.Common.charset;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Administrator
 */
public interface LoaderJSHttpResult {
    public class JSHttpResult{
        private final byte[] data;

        public JSHttpResult(byte[] data) {
            this.data = data;
        }
        
        public byte[] getBytes(){
            return data;
        }
        
        public String getString() throws UnsupportedEncodingException{
            return new String(data,charset);
        }
        
        public boolean isEmpty(){
            return data == null;
        }
        
        public boolean isNull(){
            return data == null;
        }
        
        public boolean isNullOrEmpty(){
            return data == null || data.length == 0;
        }
        
        public boolean isBlank(){
            return new String(data).trim().equals("");
        }
        
        public boolean isNullOrBLank(){
            return data == null || new String(data).trim().equals("");
        }
        
    }
}