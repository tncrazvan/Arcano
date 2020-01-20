package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import java.io.IOException;
import com.github.tncrazvan.arcano.Bean.Http.HttpDefault;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class App extends HttpController {
    @HttpDefault
    public File main() throws IOException, ClassNotFoundException {
        if(args.length == 0) 
            args = new String[]{so.config.entryPoint};
        else if(args.length == 1 && args[0].equals("")) 
            args[0] = so.config.entryPoint;
        return new Delegate<Get>(){}.start().file();
    }
}
