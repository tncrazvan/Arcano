/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Http;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author Razvan
 */
public class HttpHeader {
    private Map<String, String> header = new HashMap();
    private Map<String, String[]> cookies = new HashMap();
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM, yyyy HH:mm:ss z",Locale.US);
    public HttpHeader(boolean createSuccessHeader) {
        if(createSuccessHeader){
            header.put("Status", "HTTP/1.1 200 OK");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            header.put("Date",dtf.format(LocalDateTime.now()));
            header.put("Cache-Control","no-store");
        }
    }
    public HttpHeader(){
        this(true);
    }
    
    public String fieldToString(String key){
        String value = header.get(key);
        
        if(key.equals("Resource") || key.equals("Status") || value.equalsIgnoreCase(key)){
            return value+"\r\n";
        }
        return key+": "+value+"\r\n";
    }
    
    public String cookieToString(String key){
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String[] c = cookies.get(key);
        Date time = new Date(Integer.parseInt(c[3])*1000L);
        //Thu, 01 Jan 1970 00:00:00 GMT
        return c[4]+": "
                +key+"="+c[0]
                +(c[1]==null?"":"; path="+c[1])
                +(c[2]==null?"":"; domain="+c[2])
                +(c[3]==null?"":"; expires="+sdf.format(time))
                +"\r\n";
    }
    
    public String toString(){
        String str= "";
        for(String key : header.keySet()){
            str +=this.fieldToString(key);
        }
        
        for(String key : cookies.keySet()){
            str +=this.cookieToString(key);
        }
        return str;
    }
    
    public boolean isdefined(String key){
        if(header.get(key) == null)
            return false;
        return true;
    }
    
    public void set(String a, String b){
        header.put(a, b);
    }
    
    public String get(String key){
        if(header.get(key) != null)
        switch(key){
            case "Status":
            case "Resource":
                return header
                        .get(key)
                        .split(" ")[1].trim();
            case "Method":
                return header
                        .get(key)
                        .split(" ")[0].trim();
            default:
                return header.get(key).trim();
        }
        return null;
    }
    
    public boolean cookieIsset(String key){
        Iterator i = cookies.entrySet().iterator();
        Map.Entry pair;
        String tmp = "";
        while(i.hasNext()){
            pair = (Map.Entry)i.next();
            tmp = (String) pair.getKey();
            if(tmp.trim().equals(key.trim())){
                return true;
            }
        }
        return false;
    }
    
    public String getCookie(String key){
        Iterator i = cookies.entrySet().iterator();
        Map.Entry pair;
        String tmp = "";
        String[] tmp2 = new String[5];
        while(i.hasNext()){
            pair = (Map.Entry)i.next();
            tmp = (String) pair.getKey();
            if(tmp.trim().equals(key.trim())){
                tmp2 = (String[]) pair.getValue();
                return new String(Base64.getDecoder().decode(tmp2[0]));
            }
        }
        return null;
    }
    
    public void setCookie(String key, String v, String path, String domain, String expire){
        String [] b = new String[5];
        b[0] = new String(Base64.getEncoder().encode(v.getBytes()));
        b[1] = path;
        b[2] = domain;
        b[3] = expire;
        b[4] = "Set-Cookie";
        cookies.put(key.trim(), b);
    }
    
    public void setCookie(String key,String v,String path, String domain){
        setCookie(key, v, path, domain,null);
    }
    
    
    public void setCookie(String key,String v, String path){
        setCookie(key, v, path, null, null);
    }
    
    public void setCookie(String key, String v){
        setCookie(key, v, "/", null, null);
    }
    
    public Map<String,String> getMap(){
        return header;
    }
    
    public static HttpHeader fromString(String string){
        HttpHeader header = new HttpHeader(false);
        String[] tmp = string.split("\\r\\n");
        boolean end = false;
        for(int i=0;i<tmp.length;i++){
            if(tmp[i].equals("")) continue;
            
            String[] item = tmp[i].split(":(?=\\s)");
            if(item.length>1){
                if(item[0].equals("Cookie")){
                    String[] c = item[1].split(";");
                    for(int j=0;j<c.length;j++){
                        String[] cookieInfo = c[j].split("=(?!\\s|\\s|$)");
                        if(cookieInfo.length > 1){
                            
                            String [] b = new String[5];
                            b[0] = cookieInfo[1];
                            b[1] = null;
                            b[2] = null;
                            b[3] = null;
                            b[4] = "Cookie";
                            header.cookies.put(cookieInfo[0], b);
                        }
                        
                        
                    }
                }else{
                    header.set(item[0],item[1]);
                }
            }else{
                if(tmp[i].substring(0,3).equals("GET")){
                    header.set("Resource",tmp[i]);
                    header.set("Method","GET");
                }else if(tmp[i].substring(0,4).equals("POST")){
                    header.set("Resource",tmp[i]);
                    header.set("Method","POST");
                }else{
                    header.set(tmp[i],tmp[i]);
                }
            }
        }
        return header;
    }

    
}
