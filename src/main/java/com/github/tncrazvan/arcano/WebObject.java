package com.github.tncrazvan.arcano;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private final boolean LOCKED;
    private final String SHELL_SCRIPT;
    private String classname=null;
    private String methodname=null;
    private String type="GET";

    public String getShellScript() {
        return SHELL_SCRIPT;
    }
    
    public WebObject(final String CLASSNAME, final String METRHOD_NAME, final String TYPE, final boolean LOCKED, final String SHELL_SCRIPT) {
        this.classname = CLASSNAME;
        this.SHELL_SCRIPT = SHELL_SCRIPT;
        this.methodname = METRHOD_NAME;
        this.type = TYPE;
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
