package com.github.tncrazvan.arcano;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private final boolean LOCKED;
    private String classname=null;
    private String methodname=null;
    private String type="GET";
    
    public WebObject(final String classname, final String methodname, final String type, final boolean LOCKED) {
        this.classname = classname;
        this.methodname = methodname;
        this.type = type;
        this.LOCKED = LOCKED;
    }

    public boolean isLocked() {
        return LOCKED;
    }

    public void setClassname(final String classname) {
        this.classname = classname;
    }

    public void setMethodname(final String methodname) {
        this.methodname = methodname;
    }

    public void setType(final String httpMethod) {
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
