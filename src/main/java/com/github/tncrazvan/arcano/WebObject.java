package com.github.tncrazvan.arcano;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private String classname=null;
    private String methodname=null;
    private String httpMethod="GET";
    private boolean isRuntime;
    
    public WebObject(String classname, String methodname, String httpMethod, boolean isRuntime) {
        this.classname = classname;
        this.methodname = methodname;
        this.httpMethod = httpMethod;
        this.isRuntime = isRuntime;
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
    
    public boolean isRuntime(){
        return isRuntime;
    }
}
