/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Controller.Http;

import com.google.gson.JsonObject;
import elkserver.Http.HttpEvent;
import elkserver.Http.HttpInterface;
import java.util.ArrayList;

/**
 *
 * @author razvan
 */
public class ControllerNotFound implements HttpInterface{
    @Override
    public void main(HttpEvent e, ArrayList<String> get_data, JsonObject post_data) {
        e.setStatus(elkserver.Http.HttpEvent.STATUS_NOT_FOUND);
        e.send("Page not found");
    }

    @Override
    public void onClose() {}
}
