package com.github.tncrazvan.arcano.Tool.Http;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Administrator
 */
public class FetchResult{
    private final byte[] data;

    public FetchResult(byte[] data) {
        this.data = data;
    }

    public byte[] getBytes(){
        return data;
    }

    public String getString(String charset) throws UnsupportedEncodingException{
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
