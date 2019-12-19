package com.github.tncrazvan.arcano.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import com.github.tncrazvan.arcano.EventManager;
import static com.github.tncrazvan.arcano.SharedObject.DEFLATE;
import static com.github.tncrazvan.arcano.SharedObject.GZIP;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Deflate;
import com.github.tncrazvan.arcano.Tool.Gzip;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import static com.github.tncrazvan.arcano.Tool.MultipartFormData.generateMultipartBoundary;
import com.github.tncrazvan.arcano.Tool.Status;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_PARTIAL_CONTENT;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Razvan
 */
public abstract class HttpEventManager extends EventManager{
    private DataOutputStream output;
    //private HttpHeaders headers;
    private boolean defaultHeaders=true;
    private boolean alive=true;
    protected boolean isDir = false;
    private String acceptEncoding;
    private String encodingLabel;
    
    public void setDataOutputStream(DataOutputStream output){
        this.output = output;
    }
    
    public void initHttpEventManager(){
        if(this.request.headers.isDefined("Accept-Encoding")){
            acceptEncoding = this.request.headers.get("Accept-Encoding");
            encodingLabel = "Content-Encoding";
        }else if(this.request.headers.isDefined("Transfer-Encoding")){
            acceptEncoding = this.request.headers.get("Transfer-Encoding");
            encodingLabel = "Transfer-Encoding";
        }else{
            acceptEncoding = "";
            encodingLabel = "";
        }
    }
    
    /**
     * Note that this method WILL NOT invoke interaface method onClose
     */
    public void close(){
        try {
            client.close();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
    }
    
    /**
     * Set a header for your HttpResponse.
     * @param name name of your header.
     * @param value value of your header.
     */
    public void setResponseHeaderField(String name,String value){
        headers.set(name, value);
    }
    
    /**
     * Get the value a header from your HttpResponse.
     * @param name name of the header.
     * @return value of the header as a String.
     */
    public String getResponseHeaderField(String name){
        return headers.get(name);
    }
    
    /**
     * Check if your HttpResponse contains a specific header.
     * @param name name of the header.
     * @return true if the header exists, false otherwise.
     */
    public boolean issetResponseHeaderField(String name){
        return headers.isDefined(name);
    }
    
    /**
     * Set the status of your HttpResponse.
     * @param status an Http valid status String.
     * You can get all the available Http status strings inside the com.github.tncrazvan.arcano.Tool.Status class.
     */
    public void setResponseStatus(String status){
        setResponseHeaderField("@Status", status);
    }
    
    /**
     * Get the HttpHEaders object of your response.
     * @return headers of the response.
     */
    public HttpHeaders getResponseHttpHeaders(){
        return headers;
    }
    
    /**
     * Check if the HttpEvent is alive.
     * when called from within an HttpController, this should always return true.
     * @return true if the event is alive, false otherwise.
     */
    private boolean isAlive(){
        return alive;
    }
    
    private boolean firstMessage = true;
    
    public void sendHeaders(){
        firstMessage = false;
        try {
            output.write((headers.toString()+"\r\n").getBytes(so.charset));
            output.flush();
            alive = true;
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            alive=false;
            close();
        }
    }
    /**
     * Send data to the client.
     * The first time this method is called within an HttpEvent, it will also call the sendHEaders() method, to make sure the headers.
     * This means that whatever http headers are being set after the first time this method is called are completely ignored.
     * Calling this method is the same as returning a Object from your HttpController method.
     * There is really no good reason to call this method from within your HttpController.
     * @param data data to be sent.
     */
    public void send(byte[] data) {
        if(alive){
            try {
                for(String cmpr : so.compression){
                    switch(cmpr){
                        case DEFLATE:
                            if(acceptEncoding.matches(".+"+cmpr+".*")){
                                data = Deflate.deflate(data);
                                this.headers.set(encodingLabel, cmpr);
                                break;
                            }
                            break;
                        case GZIP:
                            if(acceptEncoding.matches(".+"+cmpr+".*")){
                                data = Gzip.compress(data);
                                this.headers.set(encodingLabel, cmpr);
                                break;
                            }
                            break;
                    }
                }
                if(firstMessage && defaultHeaders){
                    sendHeaders();
                }
                output.write(data);
                output.flush();
                alive = true;
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
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
    
    /**
     * Send data to the client.
     * The first time this method is called within an HttpEvent, it will also call the sendHEaders() method, to make sure the headers.
     * This means that whatever http headers are being set after the first time this method is called are completely ignored.
     * Calling this method is the same as returning a Object from your HttpController method.
     * There is really no good reason to call this method from within your HttpController.
     * @param data data to be sent.
     */
    public void send(String data){
        try {
            if(data == null)
                data = "";
            
            HttpEventManager.this.send(data.getBytes(so.charset));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }
    
    /**
     * Send data to the client.
     * The first time this method is called within an HttpEvent, it will also call the sendHEaders() method, to make sure the headers.
     * This means that whatever http headers are being set after the first time this method is called are completely ignored.
     * Calling this method is the same as returning a Object from your HttpController method.
     * There is really no good reason to call this method from within your HttpController.
     * @param data data to be sent.
     */
    public void send(int data){
        HttpEventManager.this.send(""+data);
    }
    
    /**
     * Set the Content-Type of your HttpResponse.
     * @param type Content-Type string.
     */
    public void setResponseContentType(String type){
        headers.set("Content-Type", type);
    }
    
    /**
     * Get the Content-Type of your HttpResponse.
     * @return Content-Type of the response.
     */
    public String getResponseContentType(){
        return headers.get("Content-Type");
    }
    
    /**
     * Usually the server sets a few headers to your HttpResponse, such as the 
     * "@Status" of the response as "200 OK", "Cache-Control" to "no-store", 
     * "Date" to the current Greenwich date.
     */
    public void disableDefaultResponseHeaders(){
        defaultHeaders = false;
    }
    
    /**
     * Usually the server sets a few headers to your HttpResponse, such as the 
     * "@Status" of the response as "200 OK", "Cache-Control" to "no-store", 
     * "Date" to the current Greenwich date.
     */
    public void enableDefaultResponseHeaders(){
        defaultHeaders = true;
    }
    
    /**
     * Send data to the client.
     * The first time this method is called within an HttpEvent, it will also call the sendHEaders() method, to make sure the headers.
     * This means that whatever http headers are being set after the first time this method is called are completely ignored.
     * Calling this method is the same as returning a Object from your HttpController method.
     * There is really no good reason to call this method from within your HttpController.
     * @param data data to be sent.
     */
    public void send(File data){
        try {
            if(!data.exists() || data.isDirectory()){
                setResponseStatus(Status.STATUS_NOT_FOUND);
                send("");
                return;
            }
            byte[] buffer;
            try (RandomAccessFile raf = new RandomAccessFile(data, "r"); 
                    DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {
                
                int fileLength = (int) raf.length();
                
                if(this.request.headers.isDefined("Range")){
                    setResponseStatus(STATUS_PARTIAL_CONTENT);
                    String[] ranges = this.request.headers.get("Range").split("=")[1].split(",");
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
                    String ctype = resolveContentType(data.getName());
                    int start,end;
                    if(rangeStart.length > 1){
                        String body = "";
                        String boundary = generateMultipartBoundary();
                        if(firstMessage && defaultHeaders){
                            firstMessage = false;
                            //header.set("Content-Length", ""+clength);
                            headers.set("Content-Type", "multipart/byteranges; boundary="+boundary);
                            dos.writeUTF(headers.toString());
                        }
                        
                        for (int i = 0; i < rangeStart.length; i++) {
                            start = rangeStart[i];
                            end = rangeEnd[i];
                            dos.writeUTF("--"+boundary+"\r\n");
                            dos.writeUTF("Content-Type: "+ctype+"\r\n");
                            dos.writeUTF("Content-Range: bytes "+start+"-"+end+"/"+fileLength+"\r\n\r\n");
                            if(end-start+1 > so.httpMtu){
                                int remainingBytes = end-start+1;
                                buffer = new byte[so.httpMtu];
                                raf.seek(start);
                                while(remainingBytes > 0){
                                    raf.read(buffer);
                                    dos.write(buffer);
                                    remainingBytes -= so.httpMtu;
                                    if(remainingBytes < 0){
                                        buffer = new byte[remainingBytes+so.httpMtu];
                                        dos.write(buffer);
                                        remainingBytes = 0;
                                    }else{
                                        buffer = new byte[so.httpMtu];
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
                            headers.set("Content-Range", "bytes "+start+"-"+end+"/"+fileLength);
                            headers.set("Content-Length", ""+len);
                            dos.write((headers.toString()+"\r\n").getBytes());
                        }
                        buffer = new byte[end-start+1];
                        raf.seek(start);
                        raf.read(buffer);
                        dos.write(buffer);
                    }
                }else{
                    if(firstMessage && defaultHeaders){
                        firstMessage = false;
                        headers.set("Content-Length", ""+fileLength);
                        dos.write((headers.toString()+"\r\n").getBytes());
                    }
                    buffer = new byte[fileLength];
                    raf.seek(0);
                    raf.read(buffer);
                    dos.write(buffer);
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.INFO,null,ex);
        } catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println("Client "+client.getInetAddress().toString()+" disconnected before receiving the whole file ("+data.getName()+")");
        }
        
        close();
    }
    
}
