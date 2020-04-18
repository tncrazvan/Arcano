package com.github.tncrazvan.arcano.http;

/**
 *
 * @author Razvan Tanase
 */
public class Cookie {
    String 
            type,
            value;

    public Cookie(final String type, final String value) {
        this.type=type;
        this.value=value;
    }
}
