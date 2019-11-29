/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.JavaScript.System;

import static com.github.tncrazvan.arcano.Common.logger;
import java.util.function.Function;
import java.util.logging.Level;

/**
 *
 * @author Administrator
 */
public interface LoaderJSSleep {
    public class JSSleep implements Function<Long, Void>{
        @Override
        public Void apply(Long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
