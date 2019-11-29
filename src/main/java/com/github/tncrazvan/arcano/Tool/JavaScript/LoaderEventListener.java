package com.github.tncrazvan.arcano.Tool.JavaScript;

import java.util.function.Function;

/**
 *
 * @author Administrator
 */
public interface LoaderEventListener {
    public class EventListener<T> implements Function<Function<T,Void>, Void>{
        public Function todo;
        
        @Override
        public Void apply(Function<T,Void> todo) {
            this.todo = todo;
            return null;
        }
    }
}
