/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import javahttpserver.Http.HttpEventListener;

/**
 *
 * @author Razvan
 */
public class JavaHttpServer {
    /**
     * @param args the command line arguments
     */
    public static void listen(String[] args) throws IOException, NoSuchAlgorithmException {
        if(args.length > 0){
            JHS.PUBLIC_WWW = args[0];
            if(args.length > 1) JHS.PORT = Integer.parseInt(args[1]);
        }
        
        ServerSocket ss = new ServerSocket(JHS.PORT);
        while(true){
            new HttpEventListener(ss.accept()).execute();
        }
    }
}
