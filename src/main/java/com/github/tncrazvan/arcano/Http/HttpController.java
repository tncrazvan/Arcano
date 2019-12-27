package com.github.tncrazvan.arcano.Http;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Reflect.ConstructorFinder;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import com.github.tncrazvan.arcano.Tool.Actions.CompleteAction;

/**
 *
 * @author razvan
 */
public class HttpController extends HttpEvent{
    protected String[] args;
    protected HttpRequestReader reader = null;
    
    public final HttpController init(HttpRequestReader reader, String[] args){
        try {
            this.reader = reader;
            this.setHttpHeaders(new HttpHeaders());
            this.setSharedObject(reader.so);
            this.setDataOutputStream(reader.output);
            this.setSocket(reader.client);
            this.setHttpRequest(reader.request);
            this.initEventManager();
            this.initHttpEventManager();
            this.findRequestLanguages();
            this.args=args;
            return this;
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public final HttpController init(HttpRequestReader reader){
        return this.init(reader, new String[]{});
    }
    
    protected HttpController init(HttpController controller){
        return this.init(controller.reader,controller.args);
    }
    
    HttpController self = this;
    public class Delegate <T extends HttpController>{
        @SuppressWarnings("unchecked")
        public final Class<T> getGenericClass() throws ClassNotFoundException {
            Type mySuperclass = getClass().getGenericSuperclass();
            Type tType = ((ParameterizedType)mySuperclass).getActualTypeArguments()[0];
            String className = tType.getTypeName();
            return (Class<T>) Class.forName(className);
        }
        
        public final T init(){
            return start();
        }
        
        public final T start(){
            T controller = null;
            try {
                Class<?> cls = getGenericClass();
                Constructor constructor = ConstructorFinder.getNoParametersConstructor(cls);
                controller = (T) constructor.newInstance();
                controller.init(self);
            } catch (IllegalArgumentException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } 
            return controller;
        }

    }
}
