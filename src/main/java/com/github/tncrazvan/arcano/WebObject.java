package com.github.tncrazvan.arcano;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private String classname=null;
    private String methodname=null;
    private String type="GET";
    
    public WebObject(String classname, String methodname, String httpMethod) {
        this.classname = classname;
        this.methodname = methodname;
        this.type = httpMethod;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setMethodname(String methodname) {
        this.methodname = methodname;
    }

    public void setHttpMethod(String httpMethod) {
        this.type = httpMethod;
    }
    
    public String getClassname(){
        return classname;
    }
    
    public String getMethodname(){
        return methodname;
    }
    
    public String getType(){
        return type;
    }
}
