package com.github.tncrazvan.arcano.Http;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.Tool.Reflect.ConstructorFinder;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;

/**
 *
 * @author razvan
 */
public class HttpController extends HttpEvent implements HttpControllerFeatures{
    
    public final HttpController init(final HttpRequestReader reader, final String[] args) {
        try {
            this.reader = reader;
            this.setResponseHttpHeaders(new HttpHeaders());
            this.setSharedObject(reader.so);
            this.setDataOutputStream(reader.output);
            this.setSocket(reader.client);
            this.setHttpRequest(reader.request);
            this.initEventManager();
            this.initHttpEventManager();
            this.findRequestLanguages();
            this.args = args;
            return this;
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public final HttpController init(final HttpRequestReader reader) {
        return this.init(reader, new String[] {});
    }

    protected HttpController init(final HttpController controller) {
        return this.init(controller.reader, controller.args);
    }

    HttpController self = this;

    public class Delegate<T extends HttpController> {
        @SuppressWarnings("unchecked")
        public final Class<T> getGenericClass() throws ClassNotFoundException {
            final Type mySuperclass = getClass().getGenericSuperclass();
            final Type tType = ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
            final String className = tType.getTypeName();
            return (Class<T>) Class.forName(className);
        }

        public final T init() {
            return start();
        }

        public final T start() {
            T controller = null;
            try {
                final Class<?> cls = getGenericClass();
                final Constructor constructor = ConstructorFinder.getNoParametersConstructor(cls);
                controller = (T) constructor.newInstance();
                controller.init(self);
            } catch (IllegalArgumentException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } 
            return controller;
        }
    }
}
