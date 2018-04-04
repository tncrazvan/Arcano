/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.razshare.elkserver.SmtpServer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author razvan
 */
public class EmailReader extends SmtpMessageManager{
    String 
                clientHostName = "", 
                currentFrameContent = "", 
                type = "", 
                boundaryId="", 
                //charset="", 
                subject="", 
                sender="",
                line;
    
    String[]    contentType = null;
    
    boolean 
                readingFrame = false, 
                readingBody = false;
    
    int 
                currentFrame = 0, 
                lastFrame = 0;
    
    ArrayList<EmailFrame> frames = new ArrayList<>();
    
    boolean
                //conversation checks
                checkHELO = false,
                checkEHLO = false,
                checkMAIL_FROM = false,
                checkDATA = false,
                //checkFROM = false,
                //checkCC = false,
                checkSUBJECT = false,
                checkQUIT = false;
    
    ArrayList<String> checkRCPT_TO = new ArrayList<>();
    ArrayList<SmtpListener> listeners;
    
    public EmailReader(SmtpServer server,Socket client,ArrayList<SmtpListener> listeners) throws IOException {
        super(server,client);
        this.listeners = listeners;
    }
    
    public void parse() throws IOException{
        sayReady();
        line = read();
        while(line != null && !checkQUIT){
            if(checkDATA){
                readData();
            }else if(isHelo(line) && !checkHELO){ //client says HELO
                sayOk(hostname+", I'm glad to meet you");
                checkHELO = true;
            }else if(isEhlo(line) && !checkEHLO){ //client says EHLO
                checkEHLO = true;
                clientHostName = jumpOnceAndGetRemaining(line);
                sayOkExtended(hostname+" Hello "+clientHostName);
                sayOkExtended("SIZE 14680064");
                sayOkExtended("PIPELINING");
                sayOk("HELP");
            }else if(isMailFrom(line) && !checkMAIL_FROM){ //this message is sent by...
                checkMAIL_FROM = true;
                sender = getMailAddress(line);
                sayOk("Ok");
            }else if(isRecipient(line)){ //this message is meant to be received by...
                sayOk("Ok");
                checkRCPT_TO.add(getMailAddress(line));
            }else if(isData(line) && !checkDATA){ //content of the message
                checkDATA = true;
                sayEndDataWith();
            }else if(isQuit(line) && !checkQUIT){ //client wants to quit
                checkQUIT = true;
                sayBye();
                client.close();
                listeners.forEach((listener) -> {
                    listener.onEmailReceived(new Email(subject, frames, sender, checkRCPT_TO));
                });

            }
            if(!checkQUIT){
                line = read();
            }else{
                if(client.isConnected()){
                    client.close();
                }
            }
        }
        
    }//end read()
    
    
    private void readData() throws IOException{
        /*
        Since a message body can contain a line with just a period
        as part of the text, the client sends two periods every time a
        line starts with a period; correspondingly, the server replaces
        every sequence of two periods at the beginning of a line
        with a single one. Such escaping method is called dot-stuffing.
        */
        if(line.length()>1){
            if(line.substring(0, 2).equals("..")){
                line = line.substring(1);
            }
        }

        if(isNewBoundary(line, boundaryId)){ //new frame detected
            saveFrame();
        }else if(isLastBoundary(line, boundaryId)){ //end of message
            saveLastFrame();
        }else if(isEndOfData(line)){
            closeAndNotifyListeners();
        }else if(readingBody){
            continueReadingBody();
        }else{
            if(currentFrame > lastFrame || readingFrame){ //new frame to be read
                readNewFrame();
            }else{
                readCurrentFrameHeaders();
            }
        }
    }//end readData()
    
    private void readCurrentFrameHeaders() throws IOException{
        if(isSubject(line) && !checkSUBJECT){
            checkSUBJECT = true;
            subject = getSubject(line);
        }else if(isFrom(line)){
            sender = getMailAddress(line);
        }else if(isContentType(line)){
            contentType = new String[]{
                getContentType(line),
                getBoundary(line)
            };

            if(!type.trim().equals("multipart/alternative")){
                sayByeAndClose();
            }
        }
    }
    
    private void readNewFrame(){
        //reading a frame right now...
        readingFrame = true;
        if(isContentType(line)){
            contentType = new String[]{
                getContentType(line),
                getCharset(line)
            };

            //type of the content, this must be: multipart/alternative
            type=contentType[0].trim();
        }
    }
    
    private void continueReadingBody(){
        //reading the actual body of the message right now
        if(currentFrame > 0) {
            currentFrameContent += "\r\n";
        }
        if(isContentType(line)){
            contentType = new String[]{
                getContentType(line),
                getCharset(line)
            };
        }else{
            currentFrameContent += line;
        }
    }
    
    private void closeAndNotifyListeners() throws IOException{
        //"." means end of DATA
        //(normaly this should be sent right before the last frame "QUIT"),
        //however, it seems gmail closes the socket by force
        //as soon as it sends the end of data frame, containing the character "."

        checkDATA = false;
        sayOkAndQueue(12345);
        checkQUIT = true;
        sayByeAndClose();

        listeners.forEach((listener) -> {
            listener.onEmailReceived(new Email(subject, frames, sender, checkRCPT_TO));
        });
    }
    
    private void saveLastFrame(){
        //I'm saving the last frame
        frames.add(new EmailFrame(currentFrameContent, contentType[0], contentType[1]));
        readingFrame = false;
        readingBody = false;
    }
    
    private void saveFrame(){
        readingBody = true;

        //I'm saving the current frame before starting to read the next one
        if(currentFrame > 0){
            frames.add(new EmailFrame(currentFrameContent, contentType[0], contentType[1]));
        }
        currentFrame++;
    }
}
