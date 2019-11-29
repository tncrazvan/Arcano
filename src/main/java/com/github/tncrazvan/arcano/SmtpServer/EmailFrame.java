package com.github.tncrazvan.arcano.SmtpServer;

/**
 *
 * @author razvan
 */
public class EmailFrame {
    private final String message;
    private final String[] contentType;
    public EmailFrame(String message, String contentType, String charset) {
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
