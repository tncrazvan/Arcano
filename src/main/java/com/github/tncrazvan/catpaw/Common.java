/**
 * CatPaw is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.tncrazvan.catpaw;
import com.github.tncrazvan.catpaw.Tools.Minifier;
import com.github.tncrazvan.catpaw.Tools.PackageExplorer;
import com.google.gson.JsonParser;
import com.github.tncrazvan.catpaw.WebSocket.WebSocketEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Date;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import com.github.tncrazvan.catpaw.Beans.Web;
import com.github.tncrazvan.catpaw.Tools.JsonTools;
import com.google.gson.JsonObject;

/**
 * 
 * @author Razvan
 */
public class Common implements JsonTools{
    //settings
    protected static HashMap<String, WebObject> routes = new HashMap<>();
    protected static Minifier minifier;
    protected static boolean 
            listen = true,
            groupsAllowed = false,
            smtpAllowed = false;
    protected static long 
            sessionTtl = 1440; //24 minutes
    protected static int 
            port = 80,
            timeout = 30000;
    protected static String 
            jwtSecret = "eswtrweqtr3w25trwes4tyw456t",
            assets = "../webapp/assets.json",
            webRoot = "../webapp",
            charset = "UTF-8",
            bindAddress = "::",
            httpControllerPackageNameOriginal = "com.github.tncrazvan.catpaw.Controller.Http",
            wsControllerPackageNameOriginal = "com.github.tncrazvan.catpaw.Controller.WebSocket",
            httpDefaultNameOriginal = "App",
            httpNotFoundNameOriginal = "ControllerNotFound",
            wsNotFoundNameOriginal = "ControllerNotFound",
            httpControllerPackageName = httpControllerPackageNameOriginal,
            wsControllerPackageName = wsControllerPackageNameOriginal,
            httpDefaultName = httpDefaultNameOriginal,
            httpNotFoundName = httpNotFoundNameOriginal,
            wsNotFoundName = wsNotFoundNameOriginal,
            entryPoint = "/index.html";
    protected static Locale locale = Locale.getDefault();
    protected static ZoneId timezone = ZoneId.systemDefault();
    protected static DateTimeFormatter httpDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM y HH:mm:ss z", locale).withZone(timezone);
    protected static Logger logger = Logger.getLogger(Common.class.getName());
    //advanced settings
    protected static final Map<String,ArrayList<WebSocketEvent>> WS_EVENTS = new HashMap<>();
    protected static int 
            threadPoolSize = 2,
            minify = -1,
            cookieTtl = 60*60*24*30,
            wsGroupMaxClient = 10,
            wsMtu = 65536,
            httpMtu = 65536,
            cacheMaxAge = 60*60*24*365; //1 year
    
    protected static String wsAcceptKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    protected static JsonObject mainSettings;
    public static final Calendar calendar = Calendar.getInstance();
    
    
    //other vars
    protected static final Date date = new Date();
    //protected static final Gson JSON_PARSER = new Gson();
    //public static final JsonParser JSONPARSER = new JsonParser();
    protected static boolean running = false;
    protected static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    protected static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final String patternLeftStart = "<\\s*(?=script)";
    private static final String patternLeftEnd = "<\\s*\\/\\s*(?=script)";
    private static final String patternRightEnd = "(?<=&lt;\\/script)>";
    private static final String patternRightStart1 = "(?<=\\&lt\\;script)\\s*>";
    private static final String patternRightStart2 = "(?<=\\&lt\\;script).*\\s*>";
    
    
    public static final String 
            //INFORMATINOAL RESPONSES
            STATUS_CONTINUE = "100 Continue",
            STATUS_SWITCHING_PROTOCOLS = "101 Switching Protocols",
            STATUS_PROCESSING = "102 Processing",
            
            //SUCCESS
            STATUS_SUCCESS = "200 OK",
            STATUS_CREATED = "201 CREATED",
            STATUS_ACCEPTED = "202 ACCEPTED",
            STATUS_NON_AUTHORITATIVE_INFORMATION = "203 Non-Authoritative Information",
            STATUS_NO_CONTENT = "204 No Content",
            STATUS_RESET_CONTENT = "205 Reset Content",
            STATUS_PARTIAL_CONTENT = "206 Partial Content",
            STATUS_MULTI_STATUS = "207 Multi-Status",
            STATUS_ALREADY_REPORTED = "208 Already Reported",
            STATUS_IM_USED = "226 IM Used",
            
            //REDIRECTIONS
            STATUS_MULTIPLE_CHOICES = "300 Multiple Choices",
            STATUS_MOVED_PERMANENTLY = "301 Moved Permanently",
            STATUS_FOUND = "302 Found",
            STATUS_SEE_OTHER = "303 See Other",
            STATUS_NOT_MODIFIED = "304 Not Modified",
            STATUS_USE_PROXY = "305 Use Proxy",
            STATUS_SWITCH_PROXY = "306 Switch Proxy",
            STATUS_TEMPORARY_REDIRECT = "307 Temporary Redirect",
            STATUS_PERMANENT_REDIRECT = "308 Permanent Redirect",
            
            //CLIENT ERRORS
            STATUS_BAD_REQUEST = "400 Bad Request",
            STATUS_UNAUTHORIZED = "401 Unauthorized",
            STATUS_PAYMENT_REQUIRED = "402 Payment Required",
            STATUS_FORBIDDEN = "403 Forbidden",
            STATUS_NOT_FOUND = "404 Not Found",
            STATUS_METHOD_NOT_ALLOWED = "405 Method Not Allowed",
            STATUS_NOT_ACCEPTABLE = "406 Not Acceptable",
            STATUS_PROXY_AUTHENTICATION_REQUIRED = "407 Proxy Authentication Required",
            STATUS_REQUEST_TIMEOUT = "408 Request Timeout",
            STATUS_CONFLICT = "409 Conflict",
            STATUS_GONE = "410 Gone",
            STATUS_LENGTH_REQUIRED = "411 Length Required",
            STATUS_PRECONDITION_FAILED = "412 Precondition Failed",
            STATUS_PAYLOAD_TOO_LARGE = "413 Payload Too Large",
            STATUS_URI_TOO_LONG = "414 URI Too Long",
            STATUS_UNSUPPORTED_MEDIA_TYPE = "415 Unsupported Media Type",
            STATUS_RANGE_NOT_SATISFIABLE = "416 Range Not Satisfiable",
            STATUS_EXPECTATION_FAILED = "417 Expectation Failed",
            STATUS_IM_A_TEAPOT = "418 I'm a teapot",
            STATUS_MISDIRECTED_REQUEST = "421 Misdirected Request",
            STATUS_UNPROCESSABLE_ENTITY = "422 Unprocessable Entity",
            STATUS_LOCKED = "423 Locked",
            STATUS_FAILED_DEPENDENCY = "426 Failed Dependency",
            STATUS_UPGRADE_REQUIRED = "428 Upgrade Required",
            STATUS_PRECONDITION_REQUIRED = "429 Precondition Required",
            STATUS_TOO_MANY_REQUESTS = "429 Too Many Requests",
            STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE = "431 Request Header Fields Too Large",
            STATUS_UNAVAILABLE_FOR_LEGAL_REASONS = "451 Unavailable For Legal Reasons",
            
            //SERVER ERRORS
            STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error",
            STATUS_NOT_IMPLEMENTED = "501 Not Implemented",
            STATUS_BAD_GATEWAY = "502 Bad Gateway",
            STATUS_SERVICE_UNAVAILABLE = "503 Service Unavailable",
            STATUS_GATEWAY_TIMEOUT = "504 Gateway Timeout",
            STATUS_HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version Not Supported",
            STATUS_VARIANT_ALSO_NEGOTATIES = "506 Variant Also Negotiates",
            STATUS_INSUFFICIENT_STORAGE = "507 Insufficient Storage",
            STATUS_LOOP_DETECTED = "508 Loop Detected",
            STATUS_NOT_EXTENDED = "510 Not Extended",
            STATUS_NETWORK_AUTHENTICATION_REQUIRED = "511 Network Authentication Required";
    
    private static String normalizePathSlashes(String path){
        int classPathLength = path.length();
        if(classPathLength >= 1 && path.charAt(0)!='/'){
            path = '/'+path;
        }
        if(classPathLength > 1 && path.charAt(classPathLength-1)=='/'){
            path = path.substring(0, classPathLength-1);
        }
        
        return path;
    }
    
    public static void mapRoutes(String httpControllerPackageName,String wsControllerPackageName) throws ClassNotFoundException, IOException, URISyntaxException{
        ArrayList<String> httpClasses = PackageExplorer.getClasses(httpControllerPackageName);
        ArrayList<String> webSocketClasses = PackageExplorer.getClasses(wsControllerPackageName);
        
        for(String classname : httpClasses){
            Class cls = Class.forName(classname);
            Web classRoute = (Web) cls.getAnnotation(Web.class);
            Method[] methods = cls.getDeclaredMethods();
            for(Method method : methods){
                Web methodRoute = method.getAnnotation(Web.class);
                if(methodRoute != null){
                    String classPath = normalizePathSlashes(classRoute.path().trim());
                    String methodPath = normalizePathSlashes(methodRoute.path().trim());
                    String path = classPath.toLowerCase()+methodPath.toLowerCase();
                    path = normalizePathSlashes(path);
                    WebObject wo = new WebObject(cls.getCanonicalName(), method.getName(), methodRoute.method().toUpperCase());
                    Common.routes.put(path, wo);
                }
            }
        }
        
        for(String classname : webSocketClasses){
            Class cls = Class.forName(classname);
            Web route = (Web) cls.getAnnotation(Web.class);
            if(route != null){
                String path = normalizePathSlashes(route.path().toLowerCase());
                WebObject wo = new WebObject(cls.getCanonicalName(), null, route.method().toUpperCase());
                Common.routes.put(path, wo);
            }
        }
    }
    
    protected static com.github.tncrazvan.catpaw.Controller.Http.ControllerNotFound 
            httpControllerNotFound = new 
            com.github.tncrazvan.catpaw.Controller.Http.ControllerNotFound();
    
    protected static com.github.tncrazvan.catpaw.Controller.WebSocket.ControllerNotFound 
            webSocketControllerNotFound = new 
            com.github.tncrazvan.catpaw.Controller.WebSocket.ControllerNotFound();

    
    protected static com.github.tncrazvan.catpaw.Controller.Http.Get get = new com.github.tncrazvan.catpaw.Controller.Http.Get();
 
    protected static com.github.tncrazvan.catpaw.Controller.Http.Set set = new com.github.tncrazvan.catpaw.Controller.Http.Set();

    protected static com.github.tncrazvan.catpaw.Controller.Http.Isset isset = new com.github.tncrazvan.catpaw.Controller.Http.Isset();
    
    protected static com.github.tncrazvan.catpaw.Controller.Http.Unset unset = new com.github.tncrazvan.catpaw.Controller.Http.Unset();

    public static LocalDateTime time(String value){
        return time(Integer.parseInt(value));
    }
    public static LocalDateTime time(long value){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value),TimeZone.getDefault().toZoneId());
    }
    public static LocalDateTime time(){
        return LocalDateTime.now();
    }
    
    /**
     * Reads and input string and returns a Map<String, String> 
     * that contains the multipart form data.
     * 
     * @param content the raw characters to read.
     * @return a map containing every pair of key and value of the multipart form data. Both key and value are String.
     */
    public static Map<String,String> readAsMultipartFormData(String content){
        Map<String,String> object = new HashMap<>();
        
        String[] lines = content.split("\r\n");
        String currentLabel = null,
                currentValue = "";
        Pattern pattern1 = Pattern.compile("^Content-Disposition");
        Pattern pattern2 = Pattern.compile("(?<=name\\=\\\").*?(?=\\\")");
        Matcher matcher;
        boolean next = false, skippedBlank = false;
        for(int i = 0; i<lines.length; i++){
            matcher = pattern1.matcher(lines[i]);
            if(matcher.find()){
                matcher = pattern2.matcher(lines[i]);
                if(matcher.find() && currentLabel == null){
                    currentLabel = matcher.group();
                    i +=2;
                    currentValue = lines[i];
                    object.put(currentLabel, currentValue);
                    currentLabel = null;
                }
            }
        }
        
        return object;
    }
    
    private final static char[] MULTIPART_CHARS =
             "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                  .toCharArray();
    
    /**
     * Generates a unique string that can be used to define a multipart form data boundary.
     * @return a unique multipart form data boundary.
     */
    public static String generateMultipartBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
           buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }


    /**
     * 
     * 
     * @param value input string
     * @return a capitalized version of the value.
     */
    public static String capitalize(String value){
        value = value.toLowerCase();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
    
    
    /**
     * Escapes the html script tag by using html character entities.
     * 
     * @param js input javascript code.
     * @return escaped javascript.
     */
    public static String escapeJavaScript(String js){
        return  js.replaceAll(patternLeftStart, "&lt;")
                .replaceAll(patternLeftEnd, "&lt;/")
                .replaceAll(patternRightEnd, "&gt;")
                .replaceAll(patternRightStart1, "&gt;")
                .replaceAll(patternRightStart2, "&gt;");
        
    }
    
    /**
     * Removes the given directory.
     * 
     * @param directory directory to be removed.
     */
    public static void rmdir(File directory){
        File[] files = directory.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    rmdir(f);
                } else {
                    f.delete();
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Removes unset bytes in byte array starting from the right most byte, which is bytes[bytes.length-1].
     * @param bytes input byte array.
     * @return trimmed byte array.
     */
    public static byte[] trim(byte[] bytes){
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }
    
    /**
     * Decodes base64 String.
     * @param value base64 String.
     * @return decoded String.
     */
    public static String atob(String value){
        try {
            return new String(Common.BASE64_DECODER.decode(value.getBytes(charset)),charset);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Decodes base64 String to byte array.
     * @param value encoded String.
     * @return decoded byte array.
     */
    public static byte[] atobByte(String value){
        try {
            return Common.BASE64_DECODER.decode(value.getBytes(charset));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Decodes base64 byte array.
     * @param value encoded byte array.
     * @return decoded byte array.
     */
    public static byte[] atobByte(byte[] value){
        return BASE64_DECODER.decode(value);
    }
    
    
   
   /**
    * Encodes String to base64.
    * @param value input String.
    * @return encoded String.
    */ 
    public static String btoa(String value){
        try {
            return new String(BASE64_ENCODER.encode(value.getBytes(charset)),charset);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Encodes String to base64 byte array.
     * @param value input String.
     * @return encoded byte array.
     */
    public static byte[] btoaByte(String value){
        try {
            return BASE64_ENCODER.encode(value.getBytes(charset));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING,null,ex);
        }
        return null;
    }
    
    /**
     * Encodes byte array to base64.
     * @param value input byte array.
     * @return encoded byte array.
     */
    public static byte[] btoaByte(byte[] value){
        return BASE64_ENCODER.encode(value);
    }
    
    /**
     * Encodes String to sha1.
     * @param str input String.
     * @return encoded String.
     */
    public static String getSha1String(String str){
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            
            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            logger.log(Level.WARNING,null,ex);
            return null;
        }
    }
    
    /**
     * Encodes String to sha1 byte array.
     * @param input input String.
     * @return encoded byte array.
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    public static byte[] getSha1Bytes(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        return MessageDigest.getInstance("SHA-1").digest(input.getBytes("UTF-8"));
    }
    

    /**
     * Decodes URL String.
     * @param data URL String.
     * @return decoded String.
     */
    public static String decodeUrl(String data) {
      try {
         data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
         data = data.replaceAll("\\+", "%2B");
         data = URLDecoder.decode(data, "utf-8");
      } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
      }
      return data;
   }
    
    public static String getContentType(String location){
        return resolveContentType(location);
    }
    /**
     * Returns the mime type of the given resource.
     * For example, given the filename "/index.html", the mime type returned will be "text/html".
     * This can be useful when sending data to your clients.
     * @param location resource name.
     * @return the mime type of the given resource as a String.
     */
    public static String resolveContentType(String location){
        String tmp_type = "";
        String[] tmp_type0 = location.toString().split("/");

        
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
            case "mp3":return "audio/mpeg";

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
            case "mp4":return "video/mp4";
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
    
    /**
     * Checks if the given byte array is emtpy.
     * @param array input byte array.
     * @return true if array is empty, false otherwise.
     */
    public static boolean byteArrayIsEmpty(final byte[] array) {
        int sum = 0;
        for (byte b : array) {
            sum |= b;
        }
        return (sum == 0);
    }
    
    /**
     * Encodes the given String to sha512.
     * @param value input String.
     * @param salt salt String. Can be empty.
     * @return encoded String.
     */
    public static String getSha512String(String value, String salt){
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(charset));
            byte[] bytes = md.digest(value.getBytes(charset));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
               sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
            
        } 
            catch (NoSuchAlgorithmException e){
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING,null,ex);
        }
        return result;
    }
    
    /**
     * Encodes the given String to a sha512 byte array.
     * @param value input String.
     * @param salt salt String. Can be empty.
     * @return encoded byte array.
     */
    public static byte[] getSha512Bytes(String value, String salt){
        try {
            return getSha512String(value, salt).getBytes(charset);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING,null,ex);
            return null;
        }
    }
    
    static class BCrypt{
        private static String generateStorngPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
            int iterations = 1000;
            char[] chars = password.toCharArray();
            byte[] salt = getSalt();

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return iterations + ":" + toHex(salt) + ":" + toHex(hash);
        }

        private static byte[] getSalt() throws NoSuchAlgorithmException{
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return salt;
        }

        private static String toHex(byte[] array) throws NoSuchAlgorithmException{
            BigInteger bi = new BigInteger(1, array);
            String hex = bi.toString(16);
            int paddingLength = (array.length * 2) - hex.length();
            if(paddingLength > 0)
            {
                return String.format("%0"  +paddingLength + "d", 0) + hex;
            }else{
                return hex;
            }
        }
        
        private static boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException{
            String[] parts = storedPassword.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = fromHex(parts[1]);
            byte[] hash = fromHex(parts[2]);

            PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = hash.length ^ testHash.length;
            for(int i = 0; i < hash.length && i < testHash.length; i++)
            {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        }
        private static byte[] fromHex(String hex) throws NoSuchAlgorithmException{
            byte[] bytes = new byte[hex.length() / 2];
            for(int i = 0; i<bytes.length ;i++)
            {
                bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
            }
            return bytes;
        }
    }
    
    /**
     * Encodes the value to BCrypt. Note that encoding the same value twice will generate a different BCrypt string.
     * This means you cannot simply check two encoded string to find out if they were generated from the same value like Sha1.
     * Use Server#validateBCryptString to validate an encoded strings.
     * @param value input String.
     * @return encoded String.
     */
    public static String getBCryptString(String value){
        try {
            return BCrypt.generateStorngPasswordHash(value);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            logger.log(Level.WARNING,null,ex);
            return null;
        }
    }
    
    /**
     * Checks if the given cryptoStrong has been created from the given originalString.
     * @param originalString this is the validation string. The encrypted string will be validated using this value.
     * @param cryptoString this is the encoded string.
     * @return true if the cryptoString was created from the originalString, false otherwise.
     */
    public static boolean validateBCryptString(String originalString, String cryptoString){
        try {
            return BCrypt.validatePassword(originalString, cryptoString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            logger.log(Level.WARNING,null,ex);
            return false;
        }
    }
    
    /**
     * Matches a regular expression on the given subject String.
     * @param subject The string to analyze
     * @param regex Your regex
     * @return the first group matched
     */
    public static boolean matchRegex(String subject, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
    
    
    /**
     * Extracts the nth occurrence of the given regular expression on the given subject String.
     * @param subject the input String.
     * @param regex your regular expression.
     * @param n the occurences counter.
     * @return the nth occurred String.
     */
    public static String extractRegexGroup(String subject,String regex,int n){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(subject);
        if(matcher.find()){
            if(n < 0){
                n = matcher.groupCount() + n;
            }
            return matcher.group(n);
        }
        return null;
    }
    
    /**
     * Extracts the first occurrence of the given regular expression on the given subject String.
     * @param subject
     * @param regex
     * @return the first occurred String.
     */
    public static String extractRegex(String subject,String regex){
        return extractRegexGroup(subject, regex, 0);
    }
    
    
}