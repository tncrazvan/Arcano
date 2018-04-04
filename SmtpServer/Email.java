/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.razshare.elkserver.SmtpServer;

import java.util.ArrayList;

/**
 *
 * @author razvan
 */
public class Email {
    private final String subject,sender;
    private final ArrayList<EmailFrame> frames;
    private final ArrayList<String> recipents;
    public Email(String subject, ArrayList<EmailFrame> frames, String sender, ArrayList<String> recipents) {
        this.subject=subject;
        this.sender=sender;
        this.frames=frames;
        this.recipents=recipents;
    }
    
    public boolean addRecipient(String recipient){
        return this.recipents.add(recipient);
    }
    
    public boolean removeRcipient(String recipient){
        return this.recipents.remove(recipient);
    }
    
    public ArrayList<EmailFrame> getFrames(){
        return frames;
    }
    
    public ArrayList<String> getRecipients(){
        return recipents;
    }
    
    public String getSubject(){
        return subject;
    }
    
    public String getSender(){
        return sender;
    }
}
