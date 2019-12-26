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
    
    public int getBodyFramesCounter(){
        return frames.size();
    }
    
    public boolean addRecipient(String recipient){
        return this.recipents.add(recipient);
    }
    
    public boolean removeRcipient(String recipient){
        return this.recipents.remove(recipient);
    }
    
    /**
     * Get the contents of the Email's body.<br />
     * This is the same as calling: <i>toString(getBodyFramesCounter())</i>
     * @return the whole body of the Email.
     */
    @Override
    public String toString(){
        return toString(frames.size());
    }
    
    /**
     * Get the contents of the Email's body.
     * @param length this indicates how many frames to append to the resulting String. This value is usually equal to the length of the frames ArrayList.<br />
     * <b>For example: </b>
     * <ul>
     *  <li>
     *      Given an Email having 2 body frames: <br/>
     *      <ol>
     *          <li>"<b>hello</b>"</li>
     *          <li>"<b>&lt;div class='user-signature'&gt;-- my signature&lt;/div&gt;</b>"</li>
     *      </ol>
     * </li>
     *  <li>
     *      Calling <i>toString(<b>1</b>)</i> will return a String composed of only the first frame of the Email body: "<b>hello</b>".
     * </li>
     *  <li>
     *      Calling <i>toString(<b>2</b>)</i> will return the whole body: "<b>hello&lt;div class='user-signature'&gt;-- my signature&lt;/div&gt;</b>".
     *  </li>
     * <li>
     *      Calling <i>toString(<b>-1</b>)</i> will return a String composed of only the last frame of the Email body: "<b>&lt;div class='user-signature'&gt;-- my signature&lt;/div&gt;</b>".
     *  </li>
     * <ul>
     * @return the body of the Email up until the specified frame.
     */
    public String toString(int length){
        final StringBuilder message = new StringBuilder();
        int tmp = length;
        if(length < 0)
            length *= -1;
        for(int i = tmp < 0?frames.size()+tmp:0;i<=length;i++){
            try{
                message.append(frames.get(i));
            }catch(IndexOutOfBoundsException e){}
        }
        return message.toString();
    }
    
    /**
     * Get all the frames of the body of this Email.
     * @return the ArrayList containing the whole body of the email.
     */
    public ArrayList<EmailFrame> getAllBodyFrames(){
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
