/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;

/**
 * An object which contains data of an http request.
 * @author Razvan Tanase
 */

public class HttpRequest {
    /**
     * Headers of the current request.
     */
    public HttpHeaders headers;
    /**
     * Payload of the current request.
     */
    public final byte[] content;

    private static final byte[] SEPARATOR = "\r\n\r\n".getBytes();

    public HttpRequest(final HashMap<String, String> headers, final String content)
            throws UnsupportedEncodingException {
        this(HttpHeaders.request(headers), content.getBytes("UTF-8"));
    }
    public HttpRequest(final HashMap<String, String> headers, final String content, final String charset)
            throws UnsupportedEncodingException {
        this(HttpHeaders.request(headers), content.getBytes(charset));
    }
    public HttpRequest(final HashMap<String, String> headers, final byte[] content) {
        this(HttpHeaders.request(headers), content);
    }

    public HttpRequest(final HttpHeaders headers, final String content)
            throws UnsupportedEncodingException {
        this.headers = headers;
        this.content = content.getBytes("UTF-8");
    }
    public HttpRequest(final HttpHeaders headers, final String content, final String charset)
            throws UnsupportedEncodingException {
        this.headers = headers;
        this.content = content.getBytes(charset);
    }
    public HttpRequest(final HttpHeaders headers, final byte[] content) {
        this.headers = headers;
        this.content = content;
    }

    /**
     * Send this HttpRequest to a specific host.
     * @param hostname name of the host you want to send this request to.
     * @param port port number of the host you want to send this request to.
     * @param timeout number of milliseconds the socket will wait before closing the connection.
     * @param charset charset to use when decoding your headers.
     * NOTE: the body of the request is being sent as raw binary data, thus this parameter does NOT affect the encoding of the body of your request, 
     * because there is no encoding going on, it's up to you what this byte array contains.
     * @throws IOException
     */
    public void send(String hostname,int port, int timeout,String charset) throws IOException {
        Socket host = new Socket(hostname, port);
        host.setSoTimeout(timeout);
        OutputStream output = host.getOutputStream();
        output.write(headers.toString().getBytes(charset));
        output.write(SEPARATOR);
        output.write(content);
        output.flush();
        output.close();
        host.close();
    }
}
