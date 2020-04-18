package com.github.tncrazvan.arcano.tool.http;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.http.HttpHeaders;

/**
 *
 * @author Razvan Tanase
 */
public interface Fetch {
    public static FetchResult get(final String targetURL) {
        return request("GET", targetURL, null, new HashMap<String, String>());
    }

    public static FetchResult get(final String targetURL, final HashMap<String, String> headers) {
        return request("GET", targetURL, null, headers);
    }

    public static FetchResult get(final String targetURL, final HttpHeaders headers) {
        return request("GET", targetURL, null, headers.getHashMap());
    }

    public static FetchResult post(final String targetURL) {
        return request("POST", targetURL, null, new HashMap<String, String>());
    }

    public static FetchResult post(final String targetURL, final String data) {
        return request("POST", targetURL, data, new HashMap<String, String>());
    }

    public static FetchResult post(final String targetURL, final String data, final HashMap<String, String> headers) {
        return request("POST", targetURL, data, headers);
    }

    public static FetchResult post(final String targetURL, final String data, final HttpHeaders headers) {
        return request("POST", targetURL, data, headers.getHashMap());
    }

    public static FetchResult request(final String method, final String targetURL, final String data) {
        return request(method, targetURL, data, new HashMap<String, String>());
    }

    public static FetchResult request(final String method, final String targetURL, final String data,
            final HttpHeaders headers) {
        return request(method, targetURL, data, headers.getHashMap());
    }

    public static FetchResult request(final String method, final String targetURL, final String data, final HashMap<String, String> headers) {
        try {
            // Create connection
            final URL url = new URL(targetURL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            if (headers != null)
                headers.keySet().forEach((key) -> {
                    connection.setRequestProperty(key, headers.get(key));
                });

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            if (method.equals("POST")) {
                // Send request
                final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(data == null ? "" : data);
                wr.close();
            }

            // Get Response
            final InputStream is = connection.getInputStream();

            return new FetchResult(is.readAllBytes());
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return null;
        }
    }
}
