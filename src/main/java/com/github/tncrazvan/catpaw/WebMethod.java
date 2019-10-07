/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw;

/**
 *
 * @author Administrator
 */
public class WebMethod {
    private String classname;
    private String methodname;
    private String httpMethod;
    
    public WebMethod(String classname, String methodname, String httpMethod) {
        this.classname = classname;
        this.methodname = methodname;
        this.httpMethod = httpMethod;
    }
    public String getClassname(){
        return classname;
    }
    
    public String getMethodname(){
        return methodname;
    }
    
    public String getHttpMethod(){
        return httpMethod;
    }
}
