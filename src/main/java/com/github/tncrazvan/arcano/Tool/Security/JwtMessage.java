package com.github.tncrazvan.arcano.Tool.Security;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.Tool.Encoding.JsonTools;
import static com.github.tncrazvan.arcano.Tool.Encoding.Base64.btoa;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getSha512String;
import com.google.gson.JsonObject;

/**
 *
 * @author Administrator
 */
public class JwtMessage implements JsonTools{
    private final JsonObject header = new JsonObject();
    private final JsonObject body;
    private String contents = "";
    public JwtMessage(final JsonObject body, final String key, final String charset) {
        header.addProperty("alg", "HS512");
        header.addProperty("typ", "JWT");
        this.body = body;
        final String header64 = btoa(this.header.toString(), charset);
        final String body64 = btoa(this.body.toString(), charset);
        this.contents = header64 + "." + body64;
        final String token = btoa(getSha512String(this.contents, key, charset),charset);

        this.contents = this.contents + "." + token;
    }

    @Override
    public String toString() {
        return contents;
    }

    public JsonObject getHeader() {
        return header;
    }

    public JsonObject getBody() {
        return body;
    }

    public static boolean verify(final String message, final String key, final String charset) {
        final String[] pieces = message.split("\\.");
        if(pieces.length < 3) return false;
        return btoa(getSha512String(pieces[0]+"."+pieces[1], key, charset),charset).equals(pieces[2]);
    }
}
