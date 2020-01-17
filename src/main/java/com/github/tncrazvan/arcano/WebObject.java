package com.github.tncrazvan.arcano;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private boolean locked;
    private String classname=null;
    private String methodname=null;
    private String type="GET";
    
    public WebObject(final String CLASSNAME, final String METRHOD_NAME, final String TYPE, final boolean LOCKED) {
        this.classname = CLASSNAME;
        this.methodname = METRHOD_NAME;
        this.type = TYPE;
        this.locked = LOCKED;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setClassName(final String classname) {
        this.classname = classname;
    }

    public void setMethodName(final String methodname) {
        this.methodname = methodname;
    }

    public void setType(final String httpMethod) {
        this.type = httpMethod;
    }
    
    public String getClassName(){
        return this.classname;
    }
    
    public String getMethodName(){
        return this.methodname;
    }
    
    public String getType(){
        return this.type;
    }
}
