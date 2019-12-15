package com.github.tncrazvan.arcano.Tool.Http;

import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

/**
 *
 * @author Administrator
 */
public interface Fetch {
        public default FetchResult get(String targetURL){
            return request("GET", targetURL, null, null);
        }
        public default FetchResult get(String targetURL, HashMap<String,String> headers){
            return request("GET", targetURL, null, headers);
        }
        public default FetchResult post(String targetURL){
            return request("POST", targetURL, null, null);
        }
        public default FetchResult post(String targetURL, String data){
            return request("POST", targetURL, data, null);
        }
        public default FetchResult post(String targetURL, String data, HashMap<String,String> headers){
            return request("POST", targetURL, data, headers);
        }
        public default FetchResult request(String method, String targetURL, String data) {
            return request(method, targetURL, data, null);
        }
        public default FetchResult request(String method, String targetURL, String data, HashMap<String,String> headers) {
            try {
                //Create connection
                URL url = new URL(targetURL);
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                
                if(headers != null)
                    headers.keySet().forEach((key) -> {
                        connection.setRequestProperty(key, headers.get(key));
                    });
                
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                if(method.equals("POST")){
                    //Send request
                    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
                    wr.writeBytes(data==null?"":data);
                    wr.close();
                }

                //Get Response  
                InputStream is = connection.getInputStream();
                
                return new FetchResult(is.readAllBytes());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
                return null;
            }
        }
}
