package com.github.tncrazvan.arcano.Tool.JavaScript;

import java.util.function.Function;

/**
 *
 * @author Administrator
 */
public interface LoaderJSLog {
    public class JSLog implements Function<Object, Void>{
        @Override
        public Void apply(Object message) {
            return null;
        }
    }
}
