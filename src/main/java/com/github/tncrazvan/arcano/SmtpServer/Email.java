/**
 * Arcano is a Java library that makes it easier
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
package com.github.tncrazvan.arcano.SmtpServer;

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
