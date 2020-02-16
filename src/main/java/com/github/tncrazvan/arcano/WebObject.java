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

    public final boolean isLocked() {
        return this.locked;
    }

    public final void setClassName(final String classname) {
        this.classname = classname;
    }

    public final void setMethodName(final String methodname) {
        this.methodname = methodname;
    }

    public final void setType(final String httpMethod) {
        this.type = httpMethod;
    }
    
    public final String getClassName(){
        return this.classname;
    }
    
    public final String getMethodName(){
        return this.methodname;
    }
    
    public final String getType(){
        return this.type;
    }
}
