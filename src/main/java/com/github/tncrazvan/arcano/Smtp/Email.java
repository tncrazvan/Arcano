package com.github.tncrazvan.arcano.Smtp;

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
