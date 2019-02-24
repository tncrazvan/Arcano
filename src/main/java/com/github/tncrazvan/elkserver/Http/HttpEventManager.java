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
package com.github.tncrazvan.elkserver.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import com.github.tncrazvan.elkserver.Elk;
import com.github.tncrazvan.elkserver.EventManager;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Razvan
 */
public abstract class HttpEventManager extends EventManager{
    private final DataOutputStream output;
    //private HttpHeader header;
    private boolean defaultHeaders=true;
    private boolean alive=true;
    protected final Socket client;
    protected final String content;
    public HttpEventManager(DataOutputStream output, HttpHeader clientHeader,Socket client,String content) throws UnsupportedEncodingException {
        super(clientHeader);
        this.client=client;
        this.output = output;
        this.content=content;
    }
    
    /**
     * Note that this method WILL NOT invoke interaface method onClose
     */
    public void close(){
        try {
            client.close();
        } catch (IOException ex) {
            logger.log(Level.WARNING,null,ex);
        }
    }
    public Socket getClient(){
        return client;
    }
    
    
    public void setHeaderField(String fieldName,String fieldContent){
        header.set(fieldName, fieldContent);
    }
    
    public static final String 
            //INFORMATINOAL RESPONSES
            STATUS_CONTINUE = "100 Continue",
            STATUS_SWITCHING_PROTOCOLS = "101 Switching Protocols",
            STATUS_PROCESSING = "102 Processing",
            
            //SUCCESS
            STATUS_SUCCESS = "200 OK",
            STATUS_CREATED = "201 CREATED",
            STATUS_ACCEPTED = "202 ACCEPTED",
            STATUS_NON_AUTHORITATIVE_INFORMATION = "203 Non-Authoritative Information",
            STATUS_NO_CONTENT = "204 No Content",
            STATUS_RESET_CONTENT = "205 Reset Content",
            STATUS_PARTIAL_CONTENT = "206 Partial Content",
            STATUS_MULTI_STATUS = "207 Multi-Status",
            STATUS_ALREADY_REPORTED = "208 Already Reported",
            STATUS_IM_USED = "226 IM Used",
            
            //REDIRECTIONS
            STATUS_MULTIPLE_CHOICES = "300 Multiple Choices",
            STATUS_MOVED_PERMANENTLY = "301 Moved Permanently",
            STATUS_FOUND = "302 Found",
            STATUS_SEE_OTHER = "303 See Other",
            STATUS_NOT_MODIFIED = "304 Not Modified",
            STATUS_USE_PROXY = "305 Use Proxy",
            STATUS_SWITCH_PROXY = "306 Switch Proxy",
            STATUS_TEMPORARY_REDIRECT = "307 Temporary Redirect",
            STATUS_PERMANENT_REDIRECT = "308 Permanent Redirect",
            
            //CLIENT ERRORS
            STATUS_BAD_REQUEST = "400 Bad Request",
            STATUS_UNAUTHORIZED = "401 Unauthorized",
            STATUS_PAYMENT_REQUIRED = "402 Payment Required",
            STATUS_FORBIDDEN = "403 Forbidden",
            STATUS_NOT_FOUND = "404 Not Found",
            STATUS_METHOD_NOT_ALLOWED = "405 Method Not Allowed",
            STATUS_NOT_ACCEPTABLE = "406 Not Acceptable",
            STATUS_PROXY_AUTHENTICATION_REQUIRED = "407 Proxy Authentication Required",
            STATUS_REQUEST_TIMEOUT = "408 Request Timeout",
            STATUS_CONFLICT = "409 Conflict",
            STATUS_GONE = "410 Gone",
            STATUS_LENGTH_REQUIRED = "411 Length Required",
            STATUS_PRECONDITION_FAILED = "412 Precondition Failed",
            STATUS_PAYLOAD_TOO_LARGE = "413 Payload Too Large",
            STATUS_URI_TOO_LONG = "414 URI Too Long",
            STATUS_UNSUPPORTED_MEDIA_TYPE = "415 Unsupported Media Type",
            STATUS_RANGE_NOT_SATISFIABLE = "416 Range Not Satisfiable",
            STATUS_EXPECTATION_FAILED = "417 Expectation Failed",
            STATUS_IM_A_TEAPOT = "418 I'm a teapot",
            STATUS_MISDIRECTED_REQUEST = "421 Misdirected Request",
            STATUS_UNPROCESSABLE_ENTITY = "422 Unprocessable Entity",
            STATUS_LOCKED = "423 Locked",
            STATUS_FAILED_DEPENDENCY = "426 Failed Dependency",
            STATUS_UPGRADE_REQUIRED = "428 Upgrade Required",
            STATUS_PRECONDITION_REQUIRED = "429 Precondition Required",
            STATUS_TOO_MANY_REQUESTS = "429 Too Many Requests",
            STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE = "431 Request Header Fields Too Large",
            STATUS_UNAVAILABLE_FOR_LEGAL_REASONS = "451 Unavailable For Legal Reasons",
            
            //SERVER ERRORS
            STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error",
            STATUS_NOT_IMPLEMENTED = "501 Not Implemented",
            STATUS_BAD_GATEWAY = "502 Bad Gateway",
            STATUS_SERVICE_UNAVAILABLE = "503 Service Unavailable",
            STATUS_GATEWAY_TIMEOUT = "504 Gateway Timeout",
            STATUS_HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version Not Supported",
            STATUS_VARIANT_ALSO_NEGOTATIES = "506 Variant Also Negotiates",
            STATUS_INSUFFICIENT_STORAGE = "507 Insufficient Storage",
            STATUS_LOOP_DETECTED = "508 Loop Detected",
            STATUS_NOT_EXTENDED = "510 Not Extended",
            STATUS_NETWORK_AUTHENTICATION_REQUIRED = "511 Network Authentication Required";
    
    
    public void setStatus(String status){
        setHeaderField("Status", "HTTP/1.1 "+status);
    }
    
    public String getHeaderField(String fieldName){
        return header.get(fieldName);
    }
    public HttpHeader getHeader(){
        return header;
    }
    public HttpHeader getClientHeader(){
        return clientHeader;
    }
    
    public String getMethod(){
        return clientHeader.get("Method");
    }
    
    public boolean isAlive(){
        return alive;
    }
    
    /*public boolean issetUrlQuery(String key){
        return queryString.containsKey(key);
    }
    
    public String getUrlQuery(String key){
        return queryString.get(key);
    }*/
    
    public boolean execute() throws IOException{
        findUserLanguages();
        File f = new File(Elk.webRoot+location);
        header.set("Content-Type", Elk.processContentType(location));
        if(f.exists() /*&& !location.equals(ELK.INDEX_FILE)*/){
            if(!f.isDirectory()){
                header.set("Last-Modified",httpDateFormat.format(f.lastModified()));
                header.set("Last-Modified-Timestamp",f.lastModified()+"");
                sendFileContents(f);
            }else{
                header.set("Content-Type", "text/plain");
                onControllerRequest(location);
            }
        }else{
            if(location.substring(1,2).equals("@")){
                if(header.get("Content-Type").equals("")){
                    header.set("Content-Type", "text/html");
                }
                onControllerRequest(location);
            }else{
                header.set("Content-Type", "text/html");
                try{
                    Class.forName(httpControllerPackageName+"."+location.substring(1).split("[#?&/\\\\]")[0]);
                    header.set("Last-Modified",httpDateFormat.format(f.lastModified()));
                    header.set("Last-Modified-Timestamp",f.lastModified()+"");
                    sendFileContents(indexFile);
                }catch(ClassNotFoundException ex){
                    onControllerRequest("/@"+Elk.httpNotFoundName);
                }
            }
        }
       close();
        return true;
    }
    
    
    
    abstract void onControllerRequest(String location);
    
    public Map<String,String> getUserLanguages(){
        return userLanguages;
    }
    
    public String getUserDefaultLanguage(){
        return userLanguages.get("DEFAULT-LANGUAGE");
    }
    
    public String getUserAgent(){
        return clientHeader.get("User-Agent");
    }
    
    public void setUserObject(String name, Object o) throws IOException{
        send("<script>window."+name+"="+Elk.JSON_PARSER.toJson(o)+";</script>\n");
    }
    
    public void setUserObject(String name, JsonObject o){
        send("<script>window."+name+"="+o.toString()+";</script>\n");
    }
    
    public void setUserArray(String name, JsonArray a){
        send("<script>window."+name+"="+a.toString()+";</script>\n");
    }
    
    private boolean firstMessage = true;
    
    public void sendHeaders(){
        firstMessage = false;
        try {
            output.write((header.toString()+"\r\n").getBytes(Elk.charset));
            output.flush();
            alive = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            alive=false;
            close();
        }
    }
    
    public void send(byte[] data) {
        if(alive){
            if(firstMessage && defaultHeaders){
                sendHeaders();
            }
            try {
                output.write(data);
                output.flush();
                alive = true;
            } catch (IOException ex) {
                ex.printStackTrace();
                alive=false;
                close();
            }
        }
    }
    
    public void flushHeaders(){
        flush();
    }
    public void flush(){
        sendHeaders();
    }
    
    public void send(String data){
        try {
            send(data.getBytes(Elk.charset));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE,null,ex);
        }
    }
    
    public void send(int data){
        send(""+data);
    }
    public void setContentType(String type){
        header.set("Content-Type", type);
    }
    
    public void sendFileContents(String filename) throws IOException{
        sendFileContents(new File(webRoot,filename));
    }
    
    public void disableDefaultHeaders(){
        defaultHeaders = false;
    }
    
    public void enableDefaultHeaders(){
        defaultHeaders = true;
    }
    
    private void sendFileContents(File f){
        try {
            byte[] buffer;
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            
            int fileLength = (int) raf.length();
            
            if(clientHeader.isDefined("Range")){
                setStatus(HttpEvent.STATUS_PARTIAL_CONTENT);
                String[] ranges = clientHeader.get("Range").split("=")[1].split(",");
                int[] rangeStart = new int[ranges.length];
                int[] rangeEnd = new int[ranges.length];
                int lastIndex;
                for (int i = 0; i < ranges.length; i++) {
                    lastIndex = ranges[i].length() - 1;
                    String[] tmp = ranges[i].split("-");
                    if(!ranges[i].substring(0, 1).equals("-")){
                        rangeStart[i] = Integer.parseInt(tmp[0]);
                    }else{
                        rangeStart[i] = 0;
                    }
                    if(!ranges[i].substring(lastIndex, lastIndex+1).equals("-")){
                        rangeEnd[i] = Integer.parseInt(tmp[1]);
                    }else{
                        rangeEnd[i] = fileLength-1;
                    }
                }
                String ctype = Elk.processContentType(f.getName());
                int start,end;
                if(rangeStart.length > 1){
                    String body = "";
                    String boundary = generateMultipartBoundary();
                    if(firstMessage && defaultHeaders){
                        firstMessage = false;
                        //header.set("Content-Length", ""+clength);
                        header.set("Content-Type", "multipart/byteranges; boundary="+boundary);
                        dos.writeUTF(header.toString());
                    }
                    
                    for (int i = 0; i < rangeStart.length; i++) {
                        start = rangeStart[i];
                        end = rangeEnd[i];
                        dos.writeUTF("--"+boundary+"\r\n");
                        dos.writeUTF("Content-Type: "+ctype+"\r\n");
                        dos.writeUTF("Content-Range: bytes "+start+"-"+end+"/"+fileLength+"\r\n\r\n");
                        if(end-start+1 > httpMtu){
                            int remainingBytes = end-start+1;
                            buffer = new byte[httpMtu];
                            raf.seek(start);
                            while(remainingBytes > 0){
                                raf.read(buffer);
                                dos.write(buffer);
                                remainingBytes -= httpMtu;
                                if(remainingBytes < 0){
                                    buffer = new byte[remainingBytes+httpMtu];
                                    dos.write(buffer);
                                    remainingBytes = 0;
                                }else{
                                    buffer = new byte[httpMtu];
                                }
                            }

                        }else{
                            buffer = new byte[end-start+1];
                            raf.seek(start);
                            raf.read(buffer);
                            dos.write(buffer);
                        }
                        if(i < rangeStart.length-1){
                            dos.writeUTF("\r\n");
                        }
                    }
                    if(rangeStart.length > 1){
                        dos.writeUTF("\r\n--"+boundary+"--");
                    }
                }else{
                    start = rangeStart[0];
                    end = rangeEnd[0];
                    int len = end-start+1;
                    if(firstMessage && defaultHeaders){
                        firstMessage = false;
                        header.set("Content-Range", "bytes "+start+"-"+end+"/"+fileLength);
                        header.set("Content-Length", ""+len);
                        dos.write((header.toString()+"\r\n").getBytes());
                    }
                    buffer = new byte[end-start+1];
                    raf.seek(start);
                    raf.read(buffer);
                    dos.write(buffer);
                }
            }else{
                if(firstMessage && defaultHeaders){
                    firstMessage = false;
                    header.set("Content-Length", ""+fileLength);
                    dos.write((header.toString()+"\r\n").getBytes());
                }
                buffer = new byte[fileLength];
                raf.seek(0);
                raf.read(buffer);
                dos.write(buffer);
            }
            dos.close();
            raf.close();
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO,null,ex);
        } catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println("Client "+client.getInetAddress().toString()+" disconnected before receiving the whole file ("+f.getName()+")");
        }
        
        close();
    }
    
}
