package com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem;

import java.util.function.Function;

/**
 *
 * @author Administrator
 */
public interface LoaderJSFileMaker {
    public class JSFileMaker extends com.github.tncrazvan.arcano.Tool.JavaScriptCurrentContext implements Function<String, LoaderJSFile.JSFile> {
        public JSFileMaker(String dirname) {
            super(dirname);
        }
        
        @Override
        public LoaderJSFile.JSFile apply(String filename) {
            return new LoaderJSFile.JSFile(dirname+"/"+filename);
        }
    }
}
