package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.IOException;
import com.github.tncrazvan.arcano.Bean.Web.HttpPath;
import com.github.tncrazvan.arcano.Bean.Web.HttpDefault;
import com.github.tncrazvan.arcano.Bean.ShellScript;

/**
 *
 * @author Administrator
 */
@HttpPath
public class App extends HttpController {
    @HttpDefault
    @ShellScript(execute = "ls")
    public HttpResponse main() throws IOException, ClassNotFoundException {
        if(args.length == 0) 
            args = new String[]{so.config.entryPoint};
        else if(args.length == 1 && args[0].equals("")) 
            args[0] = so.config.entryPoint;
        return new Delegate<Get>(){}.start().file();
    }
}
