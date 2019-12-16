package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Http.HttpController;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;

/**
 *
 * @author razvan
 */
@NotFound
public class ControllerNotFound extends HttpController {  
    
    public String main() {
        setResponseStatus(STATUS_NOT_FOUND);
        return "";
    }
}
