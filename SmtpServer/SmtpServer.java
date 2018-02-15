/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.SmtpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author razvan
 */
public class SmtpServer implements Runnable{

    /**
     * @param args the command line arguments
     */
    
    private final ArrayList<SmtpListener> listeners = new ArrayList<>();
    final String 
            //client commands
            HELO = "HELO",
            EHLO = "EHLO",
            MAIL_FROM = "MAIL FROM:", 
            RCPT_TO = "RCPT TO:", 
            DATA = "DATA", 
            FROM = "From:", 
            TO="To:", 
            CC="Cc:", 
            DATE="Date:", 
            SUBJECT="Subject:",
            QUIT = "QUIT",
            CONTENT_TYPE = "Content-Type:",

            //server responses
            WELCOME = "220 razshare.zapto.org ESMTP Postfix",
            OK = "250 Ok",
            OK_EXTENDED = "250-razshare.zapto.org Hello ",
            OK_SIZE = "250-SIZE 14680064",
            OK_PIPELINING = "250-PIPILINING",
            OK_HELP = "250 HELP",
            FEED_ME_DATA = "354 End data with <CR><LF>.<CR><LF>",
            BYE = "221 Bye";
    private final ServerSocket ss;
    
    public SmtpServer(ServerSocket ss,String bindAddress,int port,SmtpListener... listeners) throws IOException{
        this.ss=ss;
        ss.bind(new InetSocketAddress(bindAddress, port));
        
    }
    
    @Override
    public void run() {
        while(true){
            try {
                listen(ss.accept());
            } catch (IOException ex) {
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addEventListener(SmtpListener listener){
        listeners.add(listener);
    }
    
    public void removeEventListener(SmtpListener listener){
        listeners.remove(listener);
    }
    
    protected void listen(Socket client){
        InputStream is = null;
        try {
            String 
                    clientHostName = "", 
                    currentFrameContent = "", 
                    type = "", 
                    boundary="", 
                    //charset="", 
                    subject="", 
                    sender="", 
                    tmpContentType = "", 
                    line;
            
            String[] 
                    contentType = null;
            
            boolean 
                    readingFrame = false, 
                    readingMessage = false;
            
            int 
                    currentFrame = 0, 
                    lastFrame = 0;
            
            ArrayList<EmailFrame> 
                    frames = new ArrayList<>();
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
            
            ArrayList<String> 
                    checkRCPT_TO = new ArrayList<>();
            
            is = client.getInputStream();
            
            BufferedReader 
                    sockin = new BufferedReader(new InputStreamReader(is));
            
            OutputStream 
                    os = client.getOutputStream();
            
            PrintWriter 
                    sockout = new PrintWriter(os, true);

            Pattern
                    boundaryPattern = Pattern.compile("(?<=boundary\\=\\\")[A-z0-9]+(?=\\\")"),
                    emailPattern = Pattern.compile("(?<=\\<)[A-z0-9!#$%&'*+\\-\\/=?^_`{|}~.]+@[A-z0-9\\-.]+(?=\\>)"),
                    contentTypePattern = Pattern.compile("(?<=Content-Type:\\s)[A-z0-9]+\\/[A-z0-9]+"),
                    charsetPattern = Pattern.compile("(?<=charset=\\\")[A-z0-9\\-\\s]+(?=\\\")");
            
            Matcher 
                    matcher;
            
            sockout.println(WELCOME);
            line = sockin.readLine();
            
            while(line != null && !checkQUIT){
                if(checkDATA){
                    
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
                    
                    if(line.equals("--"+boundary)){ //new frame detected
                        readingMessage = true;
                        
                        //I'm saving the current frame before starting to read the next one
                        if(currentFrame > 0){
                            frames.add(new EmailFrame(currentFrameContent, contentType));
                        }
                        currentFrame++;
                    }else if(line.equals("--"+boundary+"--")){ //end of message
                        
                        //I'm saving the last frame
                        frames.add(new EmailFrame(currentFrameContent, contentType));
                        readingFrame = false;
                        readingMessage = false;
                    }else if(line.equals(".")){
                        //"." means end of DATA
                        //(normaly this should be sent right before the last frame "QUIT"),
                        //however, it seems gmail closes the socket by force
                        //as soon as it sends the end of data frame, containing the character "."
                        
                        checkDATA = false;
                        sockout.println(OK+": ququed as 12345");
                        //System.out.println("S:"+OK+": queued as 12345");
                        checkQUIT = true;
                        froceClose(client,sockout,frames);
                        
                        for(SmtpListener listener:listeners){
                            listener.onEmailReceived(new Email(subject, sender, frames, checkRCPT_TO));
                        }
                        
                    }else if(readingMessage){
                        //reading the actual message right now
                        if(currentFrame > 0) {
                            currentFrameContent += "\r\n";
                        }
                        if(clientCmd(line, CONTENT_TYPE)){
                            matcher = contentTypePattern.matcher(line);
                            if(matcher.find()){
                                tmpContentType = matcher.group(0);
                                matcher = charsetPattern.matcher(line);
                                if(matcher.find()){
                                    contentType = new String[]{
                                        tmpContentType.trim(), //content type
                                        matcher.group(0).trim() //charset
                                    };
                                }else{
                                    System.err.println("No match found for charset in: "+line);
                                }
                            }else{
                                System.err.println("No match found for Content-Type in: "+line);
                            }
                            
                        }else{
                            currentFrameContent += line;
                        }
                    }else{
                        if(currentFrame > lastFrame || readingFrame){ //new frame to be read
                            //reading a frame right now...
                            readingFrame = true;
                            if(clientCmd(line, CONTENT_TYPE)){
                                line = line.substring(CONTENT_TYPE.length());
                                contentType = line.split(";");
                                //type of the content, this must be: multipart/alternative
                                type=contentType[0].trim();
                                //charset=contentType[1].trim();
                                //if this frame has html content, treat all message as html
                                
                            }
                            
                        }else{
                            if(clientCmd(line, SUBJECT) && !checkSUBJECT){
                                checkSUBJECT = true;
                                subject = line.substring(SUBJECT.length());
                            }else if(clientCmd(line, CONTENT_TYPE)){
                                line = line.substring(CONTENT_TYPE.length());
                                contentType = line.split(";");
                                
                                //type of the content, this must be: multipart/alternative
                                type=contentType[0].trim();
                                if(!type.trim().equals("multipart/alternative")){
                                    sockout.println(BYE);
                                    client.close();
                                }else{
                                    //boundary, aka the UNIQUE token which identifies the beginning of each frame
                                    boundary=contentType[1].trim();
                                    matcher = boundaryPattern.matcher(boundary);
                                    if(matcher.find()){
                                        boundary = matcher.group(0);
                                    }else{
                                        sockout.println(BYE);
                                        client.close();
                                    }
                                }
                            }
                        }
                    }
                }else if(clientCmd(line, HELO) && !checkHELO){ //client says HELO
                    sockout.println(OK);
                    checkHELO = true;
                    //System.out.println("S:"+OK);
                }else if(clientCmd(line, EHLO) && !checkEHLO){ //client says EHLO
                    checkEHLO = true;
                    clientHostName = line.split("\\s")[1];
                    sockout.println(OK_EXTENDED+clientHostName);
                    sockout.println(OK_SIZE);
                    sockout.println(OK_PIPELINING);
                    sockout.println(OK_HELP);
                    //System.out.println("S:"+OK_EXTENDED+clientHostName);
                    //System.out.println("S:"+OK_SIZE);
                    //System.out.println("S:"+OK_PIPELINING);
                    //System.out.println("S:"+OK_HELP);
                }else if(clientCmd(line, MAIL_FROM) && !checkMAIL_FROM){ //this message is sent by...
                    matcher = emailPattern.matcher(line);
                    if(matcher.find()){
                        checkMAIL_FROM = true;
                        sockout.println(OK);
                        sender = matcher.group(0);
                        //System.out.println("S:"+OK);
                    }else{
                        sockout.println(BYE);
                        client.close();
                        System.err.println("No match found for sender email address in: "+line);
                    }
                    
                }else if(clientCmd(line, RCPT_TO)){ //this message is meant to be received by...
                    sockout.println(OK);
                    matcher = emailPattern.matcher(line);
                    if(matcher.find()){
                        checkRCPT_TO.add(matcher.group(0));
                        //System.out.println("S:"+OK);
                        //System.out.println("Receiver: "+receiver+" added to list.");
                    }else{
                        //System.err.println("No match found for recipient email address in: "+line);
                    }
                    
                }else if(clientCmd(line, DATA) && !checkDATA){ //content of the message
                    checkDATA = true;
                    sockout.println(FEED_ME_DATA);  //ask client to send data
                    //System.out.println("S:"+FEED_ME_DATA);
                }else if(clientCmd(line, QUIT) && !checkQUIT){ //client wants to quit
                    checkQUIT = true;
                    froceClose(client,sockout,frames);
                    for(SmtpListener listener:listeners){
                        listener.onEmailReceived(new Email(subject, sender, frames, checkRCPT_TO));
                    }
                    
                }
                if(!checkQUIT){
                    line = sockin.readLine();
                    //System.out.println("C:"+line);
                }else{
                    //System.out.println("Connection closed.");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void froceClose(Socket client, PrintWriter sockout,ArrayList<EmailFrame> frames) throws IOException{
        sockout.println(BYE); //server says "bye"
        //System.out.println("S:"+BYE);
        client.close(); //server closes connection
        
    }
    
    private boolean clientCmd(String line, String phrase){
        if(phrase.length() <= line.length()){
            String token = line.substring(0, phrase.length());
            if(token.equals(phrase)){
                return true;
            }
        }
        return false;
    }
}
