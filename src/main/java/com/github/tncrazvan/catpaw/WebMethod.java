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
    
    public WebMethod(String classname, String methodname) {
        this.classname = classname;
        this.methodname = methodname;
    }
    public String getClassname(){
        return classname;
    }
    
    public String getMethodname(){
        return methodname;
    }
}
