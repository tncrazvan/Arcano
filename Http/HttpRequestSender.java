/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


/**
 *
 * @author Razvan
 */
public class HttpRequestSender {
    private final HttpHeader header;    //the header sent to the other side
    private final Socket s; //the socket used for the communication
    private final byte[] chunk; //the data fetched during the current cycle
    //private final byte[] result;    //the final result of the request (updated throughout the cycles, or if you will: the sum of the chunks)
    private int offset = 0; //offset of the stream pointer
    private int read_bytes = 0; //bytes read so far from the stream
    public HttpRequestSender(String hostname,int timeout) throws IOException {
        header=new HttpHeader(false);
        s=new Socket();
        SocketAddress addr = new InetSocketAddress(hostname, 80);
        s.connect(addr, timeout);
        InputStream stream = s.getInputStream();
        chunk = new byte[elkserver.ELK.HTTP_MTU];
        while((read_bytes = stream.read(chunk, offset, chunk.length)) > 0){
            System.out.println(new String(chunk));
        }
        
    }
    
    
    
}
