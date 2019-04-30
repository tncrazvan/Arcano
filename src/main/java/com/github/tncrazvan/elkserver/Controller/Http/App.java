/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.elkserver.Controller.Http;

import com.github.tncrazvan.elkserver.Http.HttpController;
import com.github.tncrazvan.elkserver.Http.HttpEvent;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author Razvan
 */
public class App extends HttpController{

    @Override
    public void main(HttpEvent e, String[] args, StringBuilder content) {
        try {
            e.sendFileContents(entryPoint);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onClose() {}
    
}
