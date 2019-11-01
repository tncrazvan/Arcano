/**
 * CatPaw is a Java library that makes it easier
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
package space.catpaw.SmtpServer;

import space.catpaw.Common;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author razvan
 */
public class EmailComposer extends SmtpMessageManager{
    private final Email email;
    public EmailComposer(Email email,SmtpServer server,String clientHostname,int port,boolean ssl) throws IOException {
        super(server, (ssl?SSLSocketFactory.getDefault().createSocket(InetAddress.getByName(clientHostname),port):new Socket(clientHostname,port)));
        this.email = email;
    }

    public EmailComposer(Email email,SmtpServer server,String clientHostname,int port) throws IOException {
        super(server, new Socket(clientHostname,port));
        this.email = email;
    }
    
    
    
    public boolean submit() throws IOException{
        String error, eodLine, eodSequence;
        if(!isReady(read())) return false;
        sayHelo();
        if(!isOk(read())) return false;
        sayMailFrom(email.getSender());
        if(!isOk(read())) return false;
        
        for(String recipient : email.getRecipients()){
            sayRecipient(recipient);
            
            if(!isOk((error = read()))){
                System.err.println("[WARNING] Server replied with: "+error);
            }
        }
        sayData();
        if(!isEndDataWith((eodLine = read()))) return false;
        eodSequence = getEndDataWithValue(eodLine);
        
        sayDataFrom(email.getSender());
        sayDataDate(Common.calendar.getTime().getTime());
        sayDataSubject(email.getSubject());
        sayDataTo((String[]) email.getRecipients().toArray());
        setMultipartBoundaryId(Common.generateMultipartBoundary());
        sayDataContentType(); // Content-Type:multipart/alternative
        sayNothing();
        sayDataFrames(email.getFrames());
        say(".");
        if(!isOk(read())) return false;
        sayQuitAndClose();
        return true;
    }
}
