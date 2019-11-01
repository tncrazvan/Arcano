/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.catpaw.Controller.Http;

import space.catpaw.Http.HttpController;
import space.catpaw.Http.HttpResponse;
import java.io.IOException;
import space.catpaw.Bean.Web;

/**
 *
 * @author Administrator
 */
@Web
public class App extends HttpController{
    @Web
    public HttpResponse main() throws IOException {
        Get request = new Get();
        request.setArgs(entryPoint.split("/"));
        return request.file();
    }
}
