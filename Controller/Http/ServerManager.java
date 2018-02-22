/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Controller.Http;

import com.google.gson.JsonObject;
import elkserver.Http.HttpController;
import elkserver.Http.HttpEvent;
import java.util.ArrayList;

/**
 *
 * @author razvan
 */
public class ServerManager extends HttpController{

    @Override
    public void main(HttpEvent e, ArrayList<String> get_data, JsonObject post_data) {
        
    }
    
    public void stopListening(HttpEvent e, ArrayList<String> get_data, JsonObject post_data){
        
    }

    @Override
    public void onClose() {}
    
}
