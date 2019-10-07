/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Http;

import java.util.HashMap;

/**
 *
 * @author Administrator
 */
public class HttpResponse {
    private HashMap<String,String> headers;
    private String content;

    public HttpResponse(HashMap<String,String> headers, String content) {
        this.headers = headers;
        this.content = content;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }
}
