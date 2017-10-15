/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Http;

/**
 *
 * @author Razvan
 */
public class Cookie {
    String 
            type,
            value;

    public Cookie(String type,String value) {
        this.type=type;
        this.value=value;
    }
}
