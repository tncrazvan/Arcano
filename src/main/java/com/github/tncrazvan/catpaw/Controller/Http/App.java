/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Controller.Http;

import com.github.tncrazvan.catpaw.Http.HttpController;
import com.github.tncrazvan.catpaw.Http.HttpResponse;
import java.io.IOException;
import com.github.tncrazvan.catpaw.Beans.Web;

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
