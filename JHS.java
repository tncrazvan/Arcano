/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver;
import com.google.gson.Gson;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javahttpserver.Http.HttpEvent;
import javahttpserver.WebSocket.WebSocketEvent;

/**
 *
 * @author Razvan
 */
public class JHS {
    public static String HTTPS_CERTIFICATE = "";
    public static String HTTPS_CERTIFICATE_PASSWORD = "";
    public static String DOMAIN_NAME = "127.0.0.1";
    public static String PUBLIC_WWW = "./src/public";
    public static String INDEX_FILE = "/index.html";
    public static int CACHE_MAX_AGE = 60*60*24*365; //1 year
    public static String RESOURCE_NOT_FOUND_FILE = "/404.html";
    public static String HTTP_CONTROLLER_PACKAGE_NAME = "javahttpserver.Controller.Http";
    public static String WS_CONTROLLER_PACKAGE_NAME = "javahttpserver.Controller.WebSocket";
    public static String HTTP_CONTROLLER_NOT_FOUND = "ControllerNotFound";
    public static String WS_CONTROLLER_NOT_FOUND = "ControllerNotFound";
    public static final ArrayList<WebSocketEvent> EVENT_WS = new ArrayList<>();  
    public static final ArrayList<HttpEvent> EVENT_HTTP = new ArrayList<>();
    public static String WS_ACCEPT_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    public static int PORT = 443;
    public static int WS_MTU = 65536;
    public static final Date DATE = new Date();
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static final Gson JSON_PARSER = new Gson();
    public static boolean running = false;
    

    
    
    public static void rmdir(File folder){
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    rmdir(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    

    public static byte[] trim(byte[] bytes){
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }
    
    public static String atob(String value){
        return new String(Base64.getMimeDecoder().decode(value));
    }
    
    public static byte[] atobByte(String value){
        return Base64.getMimeDecoder().decode(value);
    }
    
    public static String btoa(String value){
        return new String(Base64.getMimeEncoder().encode(value.getBytes()));
    }
    
    public static byte[] btoaByte(String value){
        return Base64.getMimeEncoder().encode(value.getBytes());
    }
    
    public static String getSha1String(String str){
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            
            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JHS.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(JHS.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static byte[] getSha1Bytes(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        return MessageDigest.getInstance("SHA-1").digest(input.getBytes("UTF-8"));
    }
    

    
    public static String decodeUrl(String data) {
      try {
         data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
         data = data.replaceAll("\\+", "%2B");
         data = URLDecoder.decode(data, "utf-8");
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }
    
    public static String processContentType(String location){
        String tmp_type = "";
        String[] tmp_type0 = location.split("/");

        
        if(tmp_type0.length > 0){
            String[] tmp_type1 = tmp_type0[tmp_type0.length-1].split("\\.");
            if(tmp_type1.length>1){
                tmp_type = tmp_type1[tmp_type1.length-1];
            }else{
                tmp_type = "";
            }
        }else{
            tmp_type = "";
        }
        
        switch(tmp_type){
            case "html":return "text/html";
            case "css": return "text/css";
            case "csv": return "text/csv";
            case "ics": return "text/calendar";
            case "txt": return "text/plain";

            case "ttf": return "font/ttf";
            case "woff": return "font/woff";
            case "woff2": return "font/woff2";

            case "aac":return "audio/aac";
            case "mid": 
            case "midi":return "audio/midi";
            case "oga":return "audio/og";
            case "wav":return "audio/x-wav";
            case "weba":return "audio/webm";

            case "ico":return "image/x-icon";
            case "jpeg": 
            case "jpg":return "image/jpeg";
            case "png":return "image/png";
            case "gif":return "image/gif";
            case "bmp":return "image/bmp";
            case "svg":return "image/svg+xml";
            case "tif": 
            case "tiff":return "image/tiff";
            case "webp":return "image/webp";

            case "avi":return "video/x-msvideo";
            case "mpeg":return "video/mpeg";
            case "ogv":return "video/ogg";
            case "webm":return "video/webm";
            case "3gp":return "video/3gpp";
            case "3g2":return "video/3gpp2";
            case "jpgv":return "video/jpg";

            case "abw":return "application/x-abiword";
            case "arc":return "application/octet-stream";
            case "azw":return "application/vnd.amazon.ebook";
            case "bin":return "application/octet-stream";
            case "bz":return "application/x-bzip";
            case "bz2":return "application/x-bzip2";
            case "csh":return "application/x-csh";
            case "doc":return "application/msword";
            case "epub":return "application/epub+zip";
            case "jar":return "application/java-archive";
            case "js":return "application/javascript";
            case "json":return "application/json";
            case "mpkg":return "application/vnd.apple.installer+xml";
            case "odp":return "application/vnd.oasis.opendocument.presentation";
            case "ods":return "application/vnd.oasis.opendocument.spreadsheet";
            case "odt":return "application/vnd.oasis.opendocument.text";
            case "ogx":return "application/ogg";
            case "pdf":return "application/pdf";
            case "ppt":return "application/vnd.ms-powerpoint";
            case "rar":return "application/x-rar-compressed";
            case "rtf":return "application/rtf";
            case "sh":return "application/x-sh";
            case "swf":return "application/x-shockwave-flash";
            case "tar":return "application/x-tar";
            case "vsd":return "application/vnd.visio";
            case "xhtml":return "application/xhtml+xml";
            case "xls":return "application/vnd.ms-excel";
            case "xml":return "application/xml";
            case "xul":return "application/vnd.mozilla.xul+xml";
            case "zip":return "application/zip";
            case "7z":return "application/x-7z-compressed";
            case "apk":return "application/vnd.android.package-archive";
            
            default: return "";
        }
    }
    
    

    /**
     * Return a new byte array containing a sub-portion of the source array
     * 
     * @param source
     *          The source array of bytes
     * @param srcBegin
     *          The beginning index (inclusive)
     * @return The new, populated byte array
     */
    public static byte[] subBytes(byte[] source, int srcBegin) {
        return subBytes(source, srcBegin, source.length);
    }
    /**
     * Return a new byte array containing a sub-portion of the source array
     * 
     * @param source
     *          The source array of bytes
     * @param srcBegin
     *          The beginning index (inclusive)
     * @param srcEnd
     *          The ending index (exclusive)
     * @return The new, populated byte array
     */
    public static byte[] subBytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[];

        destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);

        return destination;
    }


    /**
     * Copies bytes from the source byte array to the destination array
     * 
     * @param source
     *          The source array
     * @param srcBegin
     *          Index of the first source byte to copy
     * @param srcEnd
     *          Index after the last source byte to copy
     * @param destination
     *          The destination array
     * @param dstBegin
     *          The starting offset in the destination array
     */
    public static void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
        int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }
    
}
