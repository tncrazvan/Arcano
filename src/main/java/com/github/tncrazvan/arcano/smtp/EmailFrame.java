package com.github.tncrazvan.arcano.smtp;

/**
 *
 * @author Razvan Tanase
 */
public class EmailFrame {
    private final String message;
    private final String[] contentType;
    public EmailFrame(final String message, final String contentType, final String charset) {
        this.message = message;
        this.contentType=new String[]{contentType,charset};
    }
    
    public final String getContentTye(){
        return contentType[0];
    }
    public final String getCharset(){
        return contentType[1];
    }
    @Override
    public final String toString(){
        return message;
    }
}
