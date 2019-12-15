package com.github.tncrazvan.arcano.SmtpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static com.github.tncrazvan.arcano.Tool.Regex.match;
import static com.github.tncrazvan.arcano.Tool.Regex.match;
import static com.github.tncrazvan.arcano.Tool.Regex.match;
import static com.github.tncrazvan.arcano.Tool.Regex.match;
import static com.github.tncrazvan.arcano.Tool.Regex.group;
import static com.github.tncrazvan.arcano.Tool.Regex.group;
import static com.github.tncrazvan.arcano.Tool.Regex.group;
import static com.github.tncrazvan.arcano.Tool.Regex.group;
import static com.github.tncrazvan.arcano.Tool.Regex.extract;
import static com.github.tncrazvan.arcano.Tool.Regex.extract;
import static com.github.tncrazvan.arcano.Tool.Regex.extract;
import static com.github.tncrazvan.arcano.Tool.Regex.extract;

/**
 *
 * @author razvan
 */
public abstract class SmtpMessageManager {
    protected final PrintWriter sockout;
    protected final BufferedReader sockin;
    protected final String hostname;
    protected final Socket client;
    private String boundary;
    public SmtpMessageManager(SmtpServer server, Socket client) throws IOException {
        this.sockout=new PrintWriter(client.getOutputStream(),true);
        this.sockin=new BufferedReader(new InputStreamReader(client.getInputStream()));
        hostname = server.getHostname();
        this.client = client;
    }
    
    protected String read() throws IOException{
        String tmp = sockin.readLine();
        System.out.println("Remote:"+tmp);
        return tmp;
    }
    
    protected void setMultipartBoundaryId(String id){
        boundary = id;
    }
    
    protected boolean isNewBoundary(String line, String id){
        return match(line, "(?<=^--)"+id);
    }
    protected boolean isLastBoundary(String line, String id){
        return match(line, "(?<=^--)"+id+"(?=--$)");
    }
    
    protected boolean isReady(String line){
        return match(line, "^220");
    }
    
    protected boolean isOk(String line)  {
        return match(line, "^250");
    }
    
    protected boolean isOkExtended(String line)  {
        return match(line, "^250-.+");
    }
    
    protected boolean isOkSize(String line)  {
        return match(line, "^250-SIZE [0-9]+");
    }
    
    protected boolean isOkPipelining(String line)  {
        return match(line, "^250-PIPELINING");
    }
    
    protected boolean isOkHelp(String line)  {
        return match(line, "^250 HELP");
    }
    
    protected boolean isEndDataWith(String line)  {
        return match(line, "^354");
    }
    
    protected String getEndDataWithValue(String line){
        return group(line, "(?<=\\s)[A-z0-9\\<\\>\\.]+",-1);
    }
    
    protected boolean isHelo(String line){
        return match(line, "^HELO");
    }
    
    protected boolean isEhlo(String line){
        return match(line, "^EHLO");
    }
    
    protected boolean isMailFrom(String line){
        return match(line, "^MAIL FROM:");
    }
    
    protected String getMailAddress(String line){
        return extract(line, "(?<=\\z)[A-z0-9!#$%&'*+\\-\\/=?^_`{|}~.]+@[A-z0-9\\-.]+(?=\\>)");
    }
    
    protected boolean isRecipient(String line){
        return match(line, "^RCPT TO:");
    }
    
    
    protected boolean isData(String line){
        return match(line, "^DATA");
    }
    
    protected boolean isEndOfData(String line){
        return match(line, "^\\.$");
    }
    
    protected boolean isQuit(String line){
        return match(line, "^QUIT");
    }
    
    protected boolean isContentType(String line){
        return match(line, "^Content-Type");
    }
    protected String getContentType(String line){
        return extract(line, "(?<=^Content-Type:\\s)[A-z0-9]+\\/[A-z0-9]+");
    }
    
    protected String getCharset(String line){
        return extract(line, "(?<=charset=\\\")[A-z0-9\\-\\s]+(?=\\\")");
    }
    
    protected boolean isSubject(String line){
        return match(line, "^Subject");
    }
    
    protected String getSubject(String line){
        return extract(line, "(?<=^Subject:).*");
    }
    
    protected String getBoundary(String line){
        return extract(line, "(?<=boundary\\=\\\")[A-z0-9]+(?=\\\")");
    }
    
    protected boolean isFrom(String line){
        return match(line, "^From");
    }
    
    protected String getNickname(String line){
        return extract(line, "(?<=From:).+(?=\\<)");
    }
    
    protected String jumpOnceAndGetRemaining(String line){
        return extract(line, "(?<=\\s).+");
    }
    
    protected void say(int code, String extra){
        say(code+extra);
    }
    protected void say(int code){
        say(code,"");
    }
    
    protected void say(String value){
        System.out.println("Local:"+value);
        sockout.println(value);
    }
    
    protected void sayReady(){
        say(220," "+hostname+" ESMTP Postfix");
    }
    
    protected void sayHelo(){
        say("HELO "+hostname);
    }
    
    protected void sayEhlo(){
        say("EHLO "+hostname);
    }
    
    protected void sayMailFrom(String address){
        say("MAIL FROM:<"+address+">");
    }
    
    protected void sayRecipient(String address){
        say("RCPT TO:<"+address+">");
    }
    
    protected void sayData(){
        say("DATA");
    }
    
    protected void sayDataFrom(String address){
        say("From: "+address.split("@")[0]+" <"+address+">");
    }
    
    protected void sayDataSubject(String subject){
        say("Subject: "+subject);
    }
    
    DateFormat datePattern = new SimpleDateFormat("E, d M Y H:m:s Z");
    protected void sayDataDate(long unixTime){
        say("Date: "+datePattern.format(new Date(unixTime)));
    }
    
    protected void sayDataTo(String[] recipients){
        String tmp = "To: ";
        for(int i = 0; i<recipients.length;i++){
            tmp += recipients[i];
            //if it's no the last address, append ","
            if(i<recipients.length-1){
                tmp +=", ";
            }
        }
        say(tmp);
    }
    
    protected void sayDataContentType(){
        say("Content-Type: multipart/alternative; boundary=\""+boundary+"\"");
    }
    
    protected void sayDataFrames(ArrayList<EmailFrame> frames){
        frames.stream().map((frame) -> {
            say("Content-Type: "+frame.getContentTye()+"; charset=\""+frame.getCharset()+"\"");
            return frame;
        }).map((frame) -> {
            sayNothing();
            return frame;
        }).forEachOrdered((frame) -> {
            say(frame.toString());
        });
        sayEndBoundary();
    }
    
    protected void sayNothing(){
        say("");
    }
    
    protected void sayQuit(){
        say("QUIT");
    }
    
    protected void sayQuitAndClose() throws IOException{
        sayQuit();
        client.close();
    }
    
    protected void sayNewBoundary(){
        say("--"+boundary);
    }
    
    protected void sayEndBoundary(){
        say("--"+boundary+"--");
    }
    
    protected void sayOkExtended(String params){
        say(250,"-"+params);
    }
    
    protected void sayOk(String params){
        say(250," "+params);
    }
    
    protected void sayOk(){sayOk("");}
    
    protected void sayEndDataWith(){
        say(354," End data with <CR><LF>.<CR><LF>");
    }
    
    protected void sayOkAndQueue(int index){
        say(250," Ok: queued as "+index);
    }
    
    protected void sayBye(){
        say(221," Bye");
    }
    protected void sayByeAndClose() throws IOException{
        if(client.isConnected()){
            sayBye();
            client.close();
        }
    }
}
