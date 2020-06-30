package com.github.tncrazvan.arcano;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tncrazvan.arcano.tool.action.HttpEventAction;

/**
 *
 * @author Razvan Tanase
 */
public class WebObject {
    private final HttpEventAction<Object> action;
    private final String classname;
    private final String methodname;
    private String path;
    private Pattern pattern;
    public final ArrayList<String> paramNames = new ArrayList<>();
    public final HashMap<String, String> paramMap = new HashMap<>();
    
    public WebObject(final HttpEventAction<Object> action, final String CLASSNAME, final String METRHOD_NAME) {
        this.action = action;
        this.classname = CLASSNAME;
        this.methodname = METRHOD_NAME;
    }

    public final void setPath(String path){
        this.path = path;
        Matcher m = Pattern.compile("(?<=\\{)[A-z0-9]+(?=\\})").matcher(path);
        while(m.find()){
            int len = m.groupCount();
            for(int i = 0; i <= len; i++){
                String paramName = m.group(i);
                paramNames.add(paramName);
            }
        }
        this.pattern = Pattern.compile(
            "^"
            +path
            .replaceAll("\\s*\\{[A-z0-9]+\\}\\s*", "([^\\/]+)")
            .replaceAll("/","\\\\/")
            +"$"
        );
    }

    public final HttpEventAction<Object> getAction(){
        return this.action;
    }
    
    public final String getClassName(){
        return this.classname;
    }
    
    public final String getMethodName(){
        return this.methodname;
    }

    public final String getPath(){
        return path;
    }

    public final Pattern getPattern(){
        return pattern;
    }
}
