package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_NOT_FOUND;

import com.github.tncrazvan.arcano.Bean.Web.HttpNotFound;

/**
 *
 * @author razvan
 */
public class ControllerNotFound extends HttpController {  
    @HttpNotFound
    public String main() {
        setResponseStatus(STATUS_NOT_FOUND);
        return "";
    }
}
