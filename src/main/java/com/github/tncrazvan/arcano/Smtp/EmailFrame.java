package com.github.tncrazvan.arcano.Smtp;

/**
 *
 * @author razvan
 */
public class EmailFrame {
    private final String message;
    private final String[] contentType;
    public EmailFrame(final String message, final String contentType, final String charset) {
        this.message = message;
        this.contentType=new String[]{contentType,charset};
    }
    
    public String getContentTye(){
        return contentType[0];
    }
    public String getCharset(){
        return contentType[1];
    }
    @Override
    public String toString(){
        return message;
    }
}
