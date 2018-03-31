/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
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
package elkserver;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
import elkserver.WebSocket.WebSocketEvent;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Razvan
 */
public class Elk {
    
    //settings
    protected static boolean 
            listen = true,
            groupsAllowed = false,
            smtpAllowed = false;
    protected static int port = 80;
    protected static String 
            webRoot = "./src/public",
            charset = "UTF-8",
            bindAddress = "127.0.0.1",
            httpControllerPackageName = "elkserver.Controller.Http",
            wsControllerPackageName = "elkserver.Controller.WebSocket",
            httpControllerNotFound = "ControllerNotFound",
            wsControllerNotFound = "ControllerNotFound";
    
    //advanced settings
    protected static final Map<String,ArrayList<WebSocketEvent>> WS_EVENTS = new HashMap<>();
    protected static final int 
            cookieTtl = 60*60,
            wsGroupMaxClient = 10,
            wsMtu = 65536,
            httpMtu = 65536,
            cacheMaxAge = 60*60*24*365; //1 year
    
    protected static String wsAcceptKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    protected static JsonObject mainSettings;
    public static final Calendar calendar = Calendar.getInstance();
    protected static String indexFile = "/index.html";
    
    
    //other vars
    protected static final Date date = new Date();
    protected static final Gson JSON_PARSER = new Gson();
    protected static final JsonParser JSONPARSER = new JsonParser();
    protected static boolean running = false;
    protected static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    protected static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final String patternLeftStart = "<\\s*(?=script)";
    private static final String patternLeftEnd = "<\\s*\\/\\s*(?=script)";
    private static final String patternRightEnd = "(?<=&lt;\\/script)>";
    private static final String patternRightStart1 = "(?<=\\&lt\\;script)\\s*>";
    private static final String patternRightStart2 = "(?<=\\&lt\\;script).*\\s*>";
    
    
    public static Map<String,Object> readAsMultipartFormData(String content){
        Map<String,Object> object = new HashMap<>();
        
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

    
    public static long getUnsignedInt(int x) {
        return x & 0x00000000ffffffffL;
    }
    
    public static String generateMultipartBoundary() {
             StringBuilder buffer = new StringBuilder();
             Random rand = new Random();
             int count = rand.nextInt(11) + 30; // a random size from 30 to 40
             for (int i = 0; i < count; i++) {
             buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
             }
             return buffer.toString();
        }


    public static String capitalize(String value){
        value = value.toLowerCase();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
    
    public static String escapeJavaScript(String js){
        return  js.replaceAll(patternLeftStart, "&lt;")
                .replaceAll(patternLeftEnd, "&lt;/")
                .replaceAll(patternRightEnd, "&gt;")
                .replaceAll(patternRightStart1, "&gt;")
                .replaceAll(patternRightStart2, "&gt;");
        
    }
    
    
    public static long time(){
        return System.currentTimeMillis() / 1000L;
    }
    
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
        try {
            return new String(Elk.BASE64_DECODER.decode(value.getBytes(charset)),charset);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] atobByte(String value){
        try {
            return Elk.BASE64_DECODER.decode(value.getBytes(charset));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] atobByte(byte[] value){
        return BASE64_DECODER.decode(value);
    }
    
    
   
    
    public static String btoa(String value){
        try {
            return new String(BASE64_ENCODER.encode(value.getBytes(charset)),charset);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] btoaByte(String value){
        try {
            return BASE64_ENCODER.encode(value.getBytes(charset));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] btoaByte(byte[] value){
        return BASE64_ENCODER.encode(value);
    }
    
    public static String getSha1String(String str){
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            
            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
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
      } catch (UnsupportedEncodingException e) {
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
    
    public static boolean byteArrayIsEmpty(final byte[] array) {
        int sum = 0;
        for (byte b : array) {
            sum |= b;
        }
        return (sum == 0);
    }
    
    public static String getSha512String(String value, String salt){
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(charset));
            byte[] bytes = md.digest(result.getBytes(charset));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
               sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
            
        } 
            catch (NoSuchAlgorithmException e){
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public static byte[] getSha512Bytes(String value, String salt){
        try {
            return getSha512String(value, salt).getBytes(charset);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String atob(byte[] digest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    
    public static String getBCryptString(String value){
        try {
            return BCrypt.generateStorngPasswordHash(value);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static boolean validateBCryptString(String originalString, String cryptoString){
        try {
            return BCrypt.validatePassword(originalString, cryptoString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Elk.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * 
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
     * 
     * @param subject The string to analyze
     * @param regex Your regex
     * @param group The which you want to extract from the matcher
     * @return the specified matched group
     */
    public static String extractRegexGroup(String subject,String regex,int group){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(subject);
        if(matcher.find()){
            if(group < 0){
                group = matcher.groupCount() + group;
            }
            return matcher.group(group);
        }
        return null;
    }
    
    public static String extractRegex(String subject,String regex){
        return extractRegexGroup(subject, regex, 0);
    }
    
    
}
