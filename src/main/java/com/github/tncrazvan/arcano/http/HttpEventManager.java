package com.github.tncrazvan.arcano.http;

import static com.github.tncrazvan.arcano.SharedObject.DEFLATE;
import static com.github.tncrazvan.arcano.SharedObject.GZIP;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.tool.http.ContentType.resolveContentType;
import static com.github.tncrazvan.arcano.tool.http.MultipartFormData.generateMultipartBoundary;
import static com.github.tncrazvan.arcano.tool.http.Status.STATUS_PARTIAL_CONTENT;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.EventManager;
import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.tool.Strings;
import com.github.tncrazvan.arcano.tool.compression.Deflate;
import com.github.tncrazvan.arcano.tool.compression.Gzip;
import com.github.tncrazvan.arcano.tool.http.Status;
import com.github.tncrazvan.arcano.tool.security.JwtMessage;
import com.google.gson.JsonObject;

/**
 *
 * @author Razvan Tanase
 */
public abstract class HttpEventManager extends EventManager implements HttpEventFeatures{
    //private HttpHeaders responseHeaders;
    private boolean defaultHeaders=true;
    private boolean alive=true;
    protected boolean isDir = false;
    private String acceptEncoding;
    private String encodingLabel;


    public HttpEventManager(HttpRequestReader reader, SharedObject so) throws UnsupportedEncodingException {
        super(reader, so);
        if (this.request.reader.content.headers.isDefined("Accept-Encoding")) {
            acceptEncoding = this.request.reader.content.headers.get("Accept-Encoding");
            encodingLabel = "Content-Encoding";
        } else if (this.request.reader.content.headers.isDefined("Transfer-Encoding")) {
            acceptEncoding = this.request.reader.content.headers.get("Transfer-Encoding");
            encodingLabel = "Transfer-Encoding";
        } else {
            acceptEncoding = "";
            encodingLabel = "";
        }
    }

    /**
     * Note that this method WILL NOT invoke interaface method onClose
     */
    public final void close() {
        try {
            request.reader.client.close();
            onClose();
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private boolean firstMessage = true;

    public final void sendHeaders() {
        firstMessage = false;
        try {
            String headers = response.headers.toString();
            request.reader.output.write((headers + "\r\n").getBytes(so.config.charset));
            request.reader.output.flush();
            alive = true;
        } catch (final IOException ex) {
            ex.printStackTrace(System.out);
            alive = false;
            close();
        }
    }
    private final LinkedList<HttpResponse> commits = new LinkedList<>();
    
    public LinkedList<HttpResponse> getCommits(){
        return commits;
    }
    
    public void commit(HttpResponse commit){
        commits.add(commit);
    }
    
    public void commit(Exception e){
        final String message = e.getMessage();
        final HttpResponse response = new HttpResponse(message == null ? "" : message);
        commit(response);
    }
    
    public void commit(InvocationTargetException e){
        final String message = e.getTargetException().getMessage();
        final HttpResponse response = new HttpResponse(message == null ? "" : message);
        commit(response);
    }
    
    
    /**
     * Send data to the client.The first time this method is called within an
     * HttpEvent, it will also call the sendHeaders() method, to make sure the
     * headers are always sent before the body.<br />
     * If you called sendHeaders() manually before calling push(...), the headers won't be sent for a second time.<br />
     * This means that whatever http headers are being set after the first
     * time this method is called are completely ignored. Calling this method is the
     * same as returning a Object from your HttpController method. <br />
     * There is really no good reason to call this method from within your HttpController.
     * 
     * @param data data to be sent.
     */
    public final void push(byte[] data) {
        this.push(data,true);
    }
    /**
     * Send data to the client.The first time this method is called within an
     * HttpEvent, it will also call the sendHeaders() method, to make sure the
     * headers are always sent before the body.<br />
     * If you called sendHeaders() manually before calling push(...), the headers won't be sent for a second time.<br />
     * This means that whatever http headers are being set after the first
     * time this method is called are completely ignored. Calling this method is the
     * same as returning a Object from your HttpController method. <br />
     * There is really no good reason to call this method from within your HttpController.
     * 
     * @param data data to be sent.
     * @param includeHeaders specifies wether or not the method should flush the HttpHeaders.<br/>
     * If this value is set to false, responseHeaders must be set manually.
     */
    public final void push(byte[] data, boolean includeHeaders) {
        if (alive) {
            try {
                for (final String cmpr : so.config.compression) {
                    switch (cmpr) {
                    case DEFLATE:
                        if (acceptEncoding.matches(".+" + cmpr + ".*")) {
                            data = Deflate.deflate(data);
                            this.response.headers.set(encodingLabel, cmpr);
                            break;
                        }
                        break;
                    case GZIP:
                        if (acceptEncoding.matches(".+" + cmpr + ".*")) {
                            data = Gzip.compress(data);
                            this.response.headers.set(encodingLabel, cmpr);
                            break;
                        }
                        break;
                    }
                }
                if (includeHeaders && firstMessage && defaultHeaders)
                    sendHeaders();
                
                request.reader.output.write(data);
                request.reader.output.flush();
                alive = true;
            } catch (final IOException ex) {
                ex.printStackTrace(System.out);
                alive = false;
                close();
            }
        }
    }

    public final void flushHeaders() {
        flush();
    }

    public final void flush() {
        sendHeaders();
    }
    /**
     * Send data to the client.The first time this method is called within an
     * HttpEvent, it will also call the sendHeaders() method, to make sure the
     * headers are always sent before the body.<br />
     * If you called sendHeaders() manually before calling push(...), the headers won't be sent for a second time.<br />
     * This means that whatever http headers are being set after the first
     * time this method is called are completely ignored. Calling this method is the
     * same as returning a Object from your HttpController method. <br />
     * There is really no good reason to call this method from within your HttpController.
     * 
     * @param data data to be sent.
     */
    public final void push(String data) {
        this.push(data,true);
    }
    /**
     * Send data to the client.The first time this method is called within an
     * HttpEvent, it will also call the sendHeaders() method, to make sure the
     * headers are always sent before the body.<br />
     * If you called sendHeaders() manually before calling push(...), the headers won't be sent for a second time.<br />
     * This means that whatever http headers are being set after the first
     * time this method is called are completely ignored. Calling this method is the
     * same as returning a Object from your HttpController method. <br />
     * There is really no good reason to call this method from within your HttpController.
     * 
     * @param data data to be sent.
     * @param includeHeaders specifies wether or not the method should flush the HttpHeaders.<br/>
     * If this value is set to false, responseHeaders must be set manually.
     */
    public final void push(String data, boolean  includeHeaders) {
        try {
            if (data == null)
                data = "";

            this.push(data.getBytes(so.config.charset),includeHeaders);
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Grant a JavaArcanoKey to the request by setting a cookie named "JavaArcanoKey".<br />
     * This cookie will unlock HttpServices that specify the "lock" parameter as true.<br />
     * 
     */
    public final void grantKey(){
        final JsonObject obj = new JsonObject();
        obj.addProperty("token", Strings.uuid());
        final JwtMessage message = new JwtMessage(obj,so.config.key,so.config.charset);
        response.setCookie("JavaArcanoKey", message.toString());
    }
    
    /**
     * Send data to the client.The first time this method is called within an
     * HttpEvent, it will also call the sendHeaders() method, to make sure the
     * headers are always sent before the body.<br />
     * If you called sendHeaders() manually before calling push(...), the headers won't be sent for a second time.<br />
     * This means that whatever http headers are being set after the first
     * time this method is called are completely ignored. Calling this method is the
     * same as returning a Object from your HttpController method. <br />
     * There is really no good reason to call this method from within your HttpController.
     * 
     * @param data data to be sent.
     */
    public final void push(final int data) {
        this.push("" + data);
    }

    /**
     * Set the Content-Type of your HttpResponse.
     * 
     * @param type Content-Type string.
     */
    public final void setResponseContentType(final String type) {
        response.headers.set("Content-Type", type);
    }

    /**
     * Get the Content-Type of your HttpResponse.
     * 
     * @return Content-Type of the response.
     */
    public final String getResponseContentType() {
        return response.headers.get("Content-Type");
    }

    /**
     * Usually the server sets a few responseHeaders to your HttpResponse, such as the
     * "@Status" of the response as "200 OK", "Cache-Control" to "no-store", "Date"
     * to the current Greenwich date.
     */
    public final void disableDefaultResponseHeaders() {
        defaultHeaders = false;
    }

    /**
     * Usually the server sets a few responseHeaders to your HttpResponse, such as the
     * "@Status" of the response as "200 OK", "Cache-Control" to "no-store", "Date"
     * to the current Greenwich date.
     */
    public final void enableDefaultResponseHeaders() {
        defaultHeaders = true;
    }

    /**
     * Send data to the client.The first time this method is called within an
     * HttpEvent, it will also call the sendHeaders() method, to make sure the
     * headers are always sent before the body.<br />
     * If you called sendHeaders() manually before calling push(...), the headers won't be sent for a second time.<br />
     * This means that whatever http headers are being set after the first
     * time this method is called are completely ignored. Calling this method is the
     * same as returning a Object from your HttpController method. <br />
     * There is really no good reason to call this method from within your HttpController.
     * 
     * @param data data to be sent.
     */
    public final void push(final File data) {
        try {
            if (!data.exists() || data.isDirectory()) {
                response.headers.setStatus(Status.STATUS_NOT_FOUND);
                HttpEventManager.this.push("");
                return;
            }
            byte[] buffer;
            try (RandomAccessFile raf = new RandomAccessFile(data, "r");
                    DataOutputStream dos = new DataOutputStream(request.reader.client.getOutputStream())) {

                final int fileLength = (int) raf.length();

                if (this.request.reader.content.headers.isDefined("Range")) {
                    response.headers.setStatus(STATUS_PARTIAL_CONTENT);
                    final String[] ranges = this.request.reader.content.headers.get("Range").split("=")[1].split(",");
                    final int[] rangeStart = new int[ranges.length];
                    final int[] rangeEnd = new int[ranges.length];
                    int lastIndex;
                    for (int i = 0; i < ranges.length; i++) {
                        lastIndex = ranges[i].length() - 1;
                        final String[] tmp = ranges[i].split("-");
                        if (!ranges[i].substring(0, 1).equals("-")) {
                            rangeStart[i] = Integer.parseInt(tmp[0]);
                        } else {
                            rangeStart[i] = 0;
                        }
                        if (!ranges[i].substring(lastIndex, lastIndex + 1).equals("-")) {
                            rangeEnd[i] = Integer.parseInt(tmp[1]);
                        } else {
                            rangeEnd[i] = fileLength - 1;
                        }
                    }
                    final String ctype = resolveContentType(data.getName());
                    int start, end;
                    if (rangeStart.length > 1) {
                        final String boundary = generateMultipartBoundary();
                        if (firstMessage && defaultHeaders) {
                            firstMessage = false;
                            // header.set("Content-Length", ""+clength);
                            response.headers.set("Content-Type", "multipart/byteranges; boundary=" + boundary);
                            dos.writeUTF(response.headers.toString());
                        }

                        for (int i = 0; i < rangeStart.length; i++) {
                            start = rangeStart[i];
                            end = rangeEnd[i];
                            dos.writeUTF("--" + boundary + "\r\n");
                            dos.writeUTF("Content-Type: " + ctype + "\r\n");
                            dos.writeUTF("Content-Range: bytes " + start + "-" + end + "/" + fileLength + "\r\n\r\n");
                            if (end - start + 1 > so.config.http.mtu) {
                                int remainingBytes = end - start + 1;
                                buffer = new byte[so.config.http.mtu];
                                raf.seek(start);
                                while (remainingBytes > 0) {
                                    raf.read(buffer);
                                    dos.write(buffer);
                                    remainingBytes -= so.config.http.mtu;
                                    if (remainingBytes < 0) {
                                        buffer = new byte[remainingBytes + so.config.http.mtu];
                                        dos.write(buffer);
                                        remainingBytes = 0;
                                    } else {
                                        buffer = new byte[so.config.http.mtu];
                                    }
                                }

                            } else {
                                buffer = new byte[end - start + 1];
                                raf.seek(start);
                                raf.read(buffer);
                                dos.write(buffer);
                            }
                            if (i < rangeStart.length - 1) {
                                dos.writeUTF("\r\n");
                            }
                        }
                        if (rangeStart.length > 1) {
                            dos.writeUTF("\r\n--" + boundary + "--");
                        }
                    } else {
                        start = rangeStart[0];
                        end = rangeEnd[0];
                        final int len = end - start + 1;
                        if (firstMessage && defaultHeaders) {
                            firstMessage = false;
                            response.headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                            response.headers.set("Content-Length", "" + len);
                            dos.write((response.headers.toString() + "\r\n").getBytes());
                        }
                        buffer = new byte[end - start + 1];
                        raf.seek(start);
                        raf.read(buffer);
                        dos.write(buffer);
                    }
                } else {
                    if (firstMessage && defaultHeaders) {
                        firstMessage = false;
                        response.headers.set("Content-Length", "" + fileLength);
                        dos.write((response.headers.toString() + "\r\n").getBytes());
                    }
                    buffer = new byte[fileLength];
                    raf.seek(0);
                    raf.read(buffer);
                    dos.write(buffer);
                }
            }
        } catch (final FileNotFoundException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (final IOException ex) {
            //ex.printStackTrace();
            System.out.println("Client "+request.reader.client.getInetAddress().toString()+" disconnected before receiving the whole file ("+data.getName()+")");
        }
        
        close();
    }
    
}
