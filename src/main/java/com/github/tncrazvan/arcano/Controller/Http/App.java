package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Bean.Default;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
@Default
public class App extends HttpController {
    public HttpResponse main() throws IOException {
        Get request = new Get();
        request.setArgs(so.entryPoint.split("/"));
        return request.file();
    }
}
