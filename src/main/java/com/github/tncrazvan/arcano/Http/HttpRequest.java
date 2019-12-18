/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Http;

import java.util.HashMap;

/**
 *
 * @author Administrator
 */

public class HttpRequest {
    /**
     * Headers of the current request.
     */
    public final HttpHeaders headers;
    /**
     * Payload of the current request.
     */
    public final byte[] content;

    public HttpRequest(final HashMap<String,String> headers, final byte[] content) {
        this(new HttpHeaders(headers), content);
    }
    
    public HttpRequest(final HttpHeaders headers, final byte[] content) {
        this.headers = headers;
        this.content = content;
    }
}
