package com.github.tncrazvan.arcano;

import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;

/**
 *
 * @author Administrator
 */
public class WebObject {
    private final CompleteAction<Object,HttpEvent> action;
    private final String classname;
    private final String methodname;
    private final String type;
    
    public WebObject(final CompleteAction<Object,HttpEvent> action, final String CLASSNAME, final String METRHOD_NAME, final String TYPE) {
        this.action = action;
        this.classname = CLASSNAME;
        this.methodname = METRHOD_NAME;
        this.type = TYPE;
    }
    
    public final CompleteAction<Object,HttpEvent> getAction(){
        return this.action;
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
