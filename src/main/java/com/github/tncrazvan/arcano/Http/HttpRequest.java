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
    public final HttpHeaders headers;
    public final byte[] content;

    public HttpRequest(final HashMap<String,String> headers, final byte[] content) {
        this(new HttpHeaders(headers), content);
    }
    
    public HttpRequest(final HttpHeaders headers, final byte[] content) {
        this.headers = headers;
        this.content = content;
    }
}
