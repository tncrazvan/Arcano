package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Http.HttpController;

/**
 *
 * @author razvan
 */
@NotFound
public class ControllerNotFound extends HttpController {  
    
    public String main() {
        return "";
    }
}
