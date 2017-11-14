/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Http;

import com.google.gson.JsonObject;
import java.util.ArrayList;

/**
 *
 * @author Razvan
 */
public interface HttpInterface {
    public void main(HttpEvent e, ArrayList<String> args,JsonObject post);
    public void onClose();
}
