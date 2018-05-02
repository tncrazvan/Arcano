/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.razshare.elkserver.SmtpServer;

import com.razshare.elkserver.Elk;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        return Elk.matchRegex(line, "(?<=^--)"+id);
    }
    protected boolean isLastBoundary(String line, String id){
        return Elk.matchRegex(line, "(?<=^--)"+id+"(?=--$)");
    }
    
    protected boolean isReady(String line){
        return Elk.matchRegex(line, "^220");
    }
    
    protected boolean isOk(String line)  {
        return Elk.matchRegex(line, "^250");
    }
    
    protected boolean isOkExtended(String line)  {
        return Elk.matchRegex(line, "^250-.+");
    }
    
    protected boolean isOkSize(String line)  {
        return Elk.matchRegex(line, "^250-SIZE [0-9]+");
    }
    
    protected boolean isOkPipelining(String line)  {
        return Elk.matchRegex(line, "^250-PIPELINING");
    }
    
    protected boolean isOkHelp(String line)  {
        return Elk.matchRegex(line, "^250 HELP");
    }
    
    protected boolean isEndDataWith(String line)  {
        return Elk.matchRegex(line, "^354");
    }
    
    protected String getEndDataWithValue(String line){
        return Elk.extractRegexGroup(line, "(?<=\\s)[A-z0-9\\<\\>\\.]+",-1);
    }
    
    protected boolean isHelo(String line){
        return Elk.matchRegex(line, "^HELO");
    }
    
    protected boolean isEhlo(String line){
        return Elk.matchRegex(line, "^EHLO");
    }
    
    protected boolean isMailFrom(String line){
        return Elk.matchRegex(line, "^MAIL FROM:");
    }
    
    protected String getMailAddress(String line){
        return Elk.extractRegex(line, "(?<=\\z)[A-z0-9!#$%&'*+\\-\\/=?^_`{|}~.]+@[A-z0-9\\-.]+(?=\\>)");
    }
    
    protected boolean isRecipient(String line){
        return Elk.matchRegex(line, "^RCPT TO:");
    }
    
    
    protected boolean isData(String line){
        return Elk.matchRegex(line, "^DATA");
    }
    
    protected boolean isEndOfData(String line){
        return Elk.matchRegex(line, "^\\.$");
    }
    
    protected boolean isQuit(String line){
        return Elk.matchRegex(line, "^QUIT");
    }
    
    protected boolean isContentType(String line){
        return Elk.matchRegex(line, "^Content-Type");
    }
    protected String getContentType(String line){
        return Elk.extractRegex(line, "(?<=^Content-Type:\\s)[A-z0-9]+\\/[A-z0-9]+");
    }
    
    protected String getCharset(String line){
        return Elk.extractRegex(line, "(?<=charset=\\\")[A-z0-9\\-\\s]+(?=\\\")");
    }
    
    protected boolean isSubject(String line){
        return Elk.matchRegex(line, "^Subject");
    }
    
    protected String getSubject(String line){
        return Elk.extractRegex(line, "(?<=^Subject:).*");
    }
    
    protected String getBoundary(String line){
        return Elk.extractRegex(line, "(?<=boundary\\=\\\")[A-z0-9]+(?=\\\")");
    }
    
    protected boolean isFrom(String line){
        return Elk.matchRegex(line, "^From");
    }
    
    protected String getNickname(String line){
        return Elk.extractRegex(line, "(?<=From:).+(?=\\<)");
    }
    
    protected String jumpOnceAndGetRemaining(String line){
        return Elk.extractRegex(line, "(?<=\\s).+");
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
        for(EmailFrame frame : frames){
            say("Content-Type: "+frame.getContentTye()+"; charset=\""+frame.getCharset()+"\"");
            sayNothing();
            say(frame.toString());
        }
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
