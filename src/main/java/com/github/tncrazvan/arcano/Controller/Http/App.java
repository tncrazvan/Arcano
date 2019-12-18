package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.IOException;
import com.github.tncrazvan.arcano.Bean.IsDefaultController;

/**
 *
 * @author Administrator
 */
@IsDefaultController
public class App extends HttpController {
    public HttpResponse main() throws IOException {
        final Get request = new Get();
        request.setArgs(so.entryPoint.split("/"));
        return request.file();
    }
}
