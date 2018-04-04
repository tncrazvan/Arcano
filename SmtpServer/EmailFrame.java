/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.razshare.elkserver.SmtpServer;

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
    public String toString(){
        return message;
    }
}
