package com.github.tncrazvan.arcano.Tool.JavaScript.FileSystem;

import static com.github.tncrazvan.arcano.Common.logger;
import com.github.tncrazvan.arcano.Tool.JavaScriptCurrentContext;
import java.io.FileNotFoundException;
import java.util.function.Function;
import java.util.logging.Level;

/**
 *
 * @author Administrator
 */
public interface LoaderJSZipMaker {
    public class JSZipMaker extends JavaScriptCurrentContext implements Function<String, LoaderJSZip.JSZip>{

        public JSZipMaker(String dirname) {
            super(dirname);
        }
        
        @Override
        public LoaderJSZip.JSZip apply(String filename) {
            try {
                return new LoaderJSZip.JSZip(dirname+"/"+filename);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
