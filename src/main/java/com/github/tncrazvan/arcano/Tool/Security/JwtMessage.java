package com.github.tncrazvan.arcano.Tool.Security;

import com.github.tncrazvan.arcano.Tool.Encoding.JsonTools;
import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.Tool.Encoding.Base64.btoa;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getSha512String;
import com.google.gson.JsonObject;

/**
 *
 * @author Administrator
 */
public class JwtMessage extends SharedObject implements JsonTools{
    private static final String SECRET = "";
    private final JsonObject header = new JsonObject();
    private final JsonObject body;
    private String contents = "";
    public JwtMessage(JsonObject body){
        header.addProperty("alg", "HS512");
        header.addProperty("typ", "JWT");
        this.body = body;
        String header64 = btoa(this.header.toString(),config.charset);
        String body64 = btoa(this.body.toString(),config.charset);
        this.contents = header64+"."+body64;
        String token = btoa(getSha512String(this.contents, SECRET, config.charset),config.charset);
        
        this.contents = this.contents+"."+token;
    }
    
    @Override
    public String toString(){return contents;}
    public JsonObject getHeader(){return header;}
    public JsonObject getBody(){return body;}
    
    public static boolean verify(String message,String charset){
        String[] pieces = message.split("\\.");
        if(pieces.length < 3) return false;
        return btoa(getSha512String(pieces[0]+"."+pieces[1], SECRET,charset),charset).equals(pieces[2]);
    }
}
