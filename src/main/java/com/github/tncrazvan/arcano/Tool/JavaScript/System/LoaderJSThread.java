/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.JavaScript.System;

import static com.github.tncrazvan.arcano.Common.executor;
import java.util.function.Function;

/**
 *
 * @author Administrator
 */
public interface LoaderJSThread {
    public class JSThread<T> implements Function<Function<Thread,Void>, Void>{
        @Override
        public Void apply(Function<Thread,Void> todo) {
            executor.submit(()->{
                todo.apply(Thread.currentThread());
            });
            return null;
        }
    }
}
