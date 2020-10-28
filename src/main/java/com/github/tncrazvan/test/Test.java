package com.github.tncrazvan.test;

import com.github.tncrazvan.arcano.Arcano;
import com.github.tncrazvan.arcano.tool.action.HttpEventAction;
import com.github.tncrazvan.arcano.tool.system.ServerFile;

public class Test extends Arcano {
    
    public static void main(String[] args) {
        Test server = new Test();
        server.addHttpEventListener("GET", "/test", (HttpEventAction<Object>) e -> {
            return "hello";
        });
        server.addHttpEventListener("GET", "@404", (HttpEventAction<Object>) e -> {
            ServerFile file = new ServerFile(e.so.config.webRoot,String.join("", e.request.reader.location));
            if(file.exists()) 
                return file;
            return 
                new ServerFile(e.so.config.webRoot,"index.html");
        });
        server.listen(new String[]{"./config.json"});
    }
}