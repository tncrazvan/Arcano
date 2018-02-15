/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.SmtpServer;

import java.util.ArrayList;

/**
 *
 * @author razvan
 */
public class Email {
    private final String subject,sender;
    private final ArrayList<EmailFrame> frames;
    private final ArrayList<String> recipents;
    public Email(String subject, String sender, ArrayList<EmailFrame> frames, ArrayList<String> recipents) {
        this.subject=subject;
        this.sender=sender;
        this.frames=frames;
        this.recipents=recipents;
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
