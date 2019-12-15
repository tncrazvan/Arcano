package com.github.tncrazvan.arcano.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import com.github.tncrazvan.arcano.EventManager;
import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.SharedObject.DEFLATE;
import static com.github.tncrazvan.arcano.SharedObject.GZIP;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Deflate;
import com.github.tncrazvan.arcano.Tool.Gzip;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
import static com.github.tncrazvan.arcano.Tool.MultipartFormData.generateMultipartBoundary;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_PARTIAL_CONTENT;
import static com.github.tncrazvan.arcano.Tool.Time.time;
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
    protected final byte[] input;
    protected boolean isDir = false;
    private final String acceptEncoding;
    private final String encodingLabel;
    public HttpEventManager(SharedObject so,DataOutputStream output, HttpHeader clientHeader,Socket client,byte[] input) throws UnsupportedEncodingException {
        super(so,client,clientHeader);
        this.output = output;
        this.input=input;
        if(this.clientHeader.isDefined("Accept-Encoding")){
            acceptEncoding = this.clientHeader.get("Accept-Encoding");
            encodingLabel = "Content-Encoding";
        }else if(this.clientHeader.isDefined("Transfer-Encoding")){
            acceptEncoding = this.clientHeader.get("Transfer-Encoding");
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
    
    public boolean isDirectory(){
        return isDir;
    }
    
    public void setHeaderField(String fieldName,String fieldContent){
        header.set(fieldName, fieldContent);
    }
    
    public void setStatus(String status){
        setHeaderField("@Status", status);
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
    
    public boolean execute() throws IOException{
        findUserLanguages();
        File f = new File(so.webRoot+location);
        if(f.exists()){
            if(!f.isDirectory()){
                header.set("Content-Type", resolveContentType(location.toString()));
                header.set("Last-Modified",so.formatHttpDefaultDate.format(time(f.lastModified())));
                header.set("Last-Modified-Timestamp",f.lastModified()+"");
                sendFileContents(f);
            }else{
                isDir = true;
                header.set("Content-Type", "text/html");
                onControllerRequest(location);
            }
        }else{
            header.set("Content-Type", "text/html");
            onControllerRequest(location);
        }
        close();
        return true;
    }
    
    
    
    abstract void onControllerRequest(StringBuilder location);
    
    public Map<String,String> getUserLanguages(){
        return userLanguages;
    }
    
    public String getUserDefaultLanguage(){
        return userLanguages.get("DEFAULT-LANGUAGE");
    }
    
    public String getUserAgent(){
        return clientHeader.get("User-Agent");
    }
    
    private boolean firstMessage = true;
    
    public void sendHeaders(){
        firstMessage = false;
        try {
            output.write((header.toString()+"\r\n").getBytes(so.charset));
            output.flush();
            alive = true;
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            alive=false;
            close();
        }
    }
    
    public void send(byte[] data) {
        if(alive){
            try {
                for(String cmpr : so.compression){
                    switch(cmpr){
                        case DEFLATE:
                            if(acceptEncoding.matches(".+"+cmpr+".*")){
                                data = Deflate.deflate(data);
                                this.header.set(encodingLabel, cmpr);
                                break;
                            }
                            break;
                        case GZIP:
                            if(acceptEncoding.matches(".+"+cmpr+".*")){
                                data = Gzip.compress(data);
                                this.header.set(encodingLabel, cmpr);
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
    
    public void send(String data){
        try {
            if(data == null)
                data = "";
            
            send(data.getBytes(so.charset));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE,null,ex);
        }
    }
    
    public void send(int data){
        send(""+data);
    }
    public void setContentType(String type){
        header.set("Content-Type", type);
    }
    
    public void sendFileContents(String filename) throws IOException{
        sendFileContents(new File(so.webRoot,filename));
    }
    
    public void disableDefaultHeaders(){
        defaultHeaders = false;
    }
    
    public void enableDefaultHeaders(){
        defaultHeaders = true;
    }
    
    public String getClientAddress(){
        return client.getInetAddress().toString();
    }
    
    public int getClientPort(){
        return client.getPort();
    }
    
    private void sendFileContents(File f){
        try {
            byte[] buffer;
            try (RandomAccessFile raf = new RandomAccessFile(f, "r"); 
                    DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {
                
                int fileLength = (int) raf.length();
                
                if(clientHeader.isDefined("Range")){
                    setStatus(STATUS_PARTIAL_CONTENT);
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
                    String ctype = resolveContentType(f.getName());
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
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.INFO,null,ex);
        } catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println("Client "+client.getInetAddress().toString()+" disconnected before receiving the whole file ("+f.getName()+")");
        }
        
        close();
    }
    
}
