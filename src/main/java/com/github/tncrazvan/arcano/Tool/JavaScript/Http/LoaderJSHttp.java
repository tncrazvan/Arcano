package com.github.tncrazvan.arcano.Tool.JavaScript.Http;

import static com.github.tncrazvan.arcano.Common.logger;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

/**
 *
 * @author Administrator
 */
public interface LoaderJSHttp {
    public class JSHttp{
        public LoaderJSHttpResult.JSHttpResult get(String targetURL){
            return request("GET", targetURL, null, null);
        }
        public LoaderJSHttpResult.JSHttpResult get(String targetURL, JsonObject headers){
            return request("GET", targetURL, null, headers);
        }
        public LoaderJSHttpResult.JSHttpResult post(String targetURL){
            return request("POST", targetURL, null, null);
        }
        public LoaderJSHttpResult.JSHttpResult post(String targetURL, String data){
            return request("POST", targetURL, data, null);
        }
        public LoaderJSHttpResult.JSHttpResult post(String targetURL, String data, JsonObject headers){
            return request("POST", targetURL, data, headers);
        }
        public LoaderJSHttpResult.JSHttpResult request(String method, String targetURL, String data) {
            return request(method, targetURL, data, null);
        }
        public LoaderJSHttpResult.JSHttpResult request(String method, String targetURL, String data, JsonObject headers) {
            try {
                //Create connection
                URL url = new URL(targetURL);
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                
                if(headers != null)
                    headers.keySet().forEach((key) -> {
                        connection.setRequestProperty(key, headers.get(key).getAsString());
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
                /*BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                  response.append(line);
                  response.append('\r');
                }
                rd.close();
                return response.toString();*/
                
                return new LoaderJSHttpResult.JSHttpResult(is.readAllBytes());
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
                return null;
            }
        }
    }
}
