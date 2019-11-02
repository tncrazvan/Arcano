/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private String classname=null;
    private String methodname=null;
    private String httpMethod="GET";
    
    public WebObject(String classname, String methodname, String httpMethod) {
        this.classname = classname;
        this.methodname = methodname;
        this.httpMethod = httpMethod;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setMethodname(String methodname) {
        this.methodname = methodname;
    }

    public void setHttpMethod(String httpMethod) {
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
