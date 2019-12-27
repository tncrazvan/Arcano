package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.IOException;
import com.github.tncrazvan.arcano.Bean.Web.DefaultWebPath;
import com.github.tncrazvan.arcano.Bean.Web.WebPath;

/**
 *
 * @author Administrator
 */
@WebPath
public class App extends HttpController {
    @DefaultWebPath
    public HttpResponse main() throws IOException, ClassNotFoundException {        
        if(args.length == 0) args = new String[]{so.config.entryPoint};
        return new Delegate<Get>(){}.start().file();
    }
}
