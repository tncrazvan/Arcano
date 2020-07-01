package com.github.tncrazvan.arcano;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tncrazvan.arcano.tool.action.HttpEventAction;
import com.github.tncrazvan.arcano.tool.action.WebSocketEventAction;

/**
 *
 * @author Razvan Tanase
 */
public class WebObject {
    private final HttpEventAction<?> httpEventAction;
    private final WebSocketEventAction webSocketEventAction;
    private String path;
    private Pattern pattern;
    public final ArrayList<String> paramNames = new ArrayList<>();
    public final HashMap<String, String> paramMap = new HashMap<>();
    
    public WebObject(final HttpEventAction<?> action, final WebSocketEventAction webSocketEventAction) {
        this.httpEventAction = action;
        this.webSocketEventAction = webSocketEventAction;
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

    public final HttpEventAction<?> getHttpEventAction(){
        return this.httpEventAction;
    }
    public final WebSocketEventAction getWebSocketEventAction(){
        return this.webSocketEventAction;
    }

    public final String getPath(){
        return path;
    }

    public final Pattern getPattern(){
        return pattern;
    }
}
