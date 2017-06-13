/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Razvan
 */
public class LogicFile {
    private final File file;
    private final String filename;
    private final String[] row;
    private final ArrayList<String> list = new ArrayList<>();
    private final FileReader fr;
    private FileWriter fw;
    private int lastMaxY = 0;
    private int delta = 0;
    
    public LogicFile(String filename) throws FileNotFoundException, IOException {
        this.filename = filename;
        file = new File(filename);
        fr = new FileReader(file);
        char[] cbuf = new char[(int)file.length()];
        fr.read(cbuf);
        row = new String(cbuf).split("\n");
        for(int i=0;i<row.length;i++){
            try{
                list.set(i, row[i]);
            }catch(Exception e){
                //if row doesn't exist, add it
                list.add(i, row[i]);
            }
        }
        
    }
    
    public void setRowContent(int y,String content, boolean delete){
        if(delete){
            try{
                list.set(y,null);
            }catch(Exception e){
                System.err.println("Row doesn't seem to exist");
            }
        }else{
            try{
                list.set(y, content);
            }catch(Exception e){
                //if row doesn't exist, add it
                list.add(y, content);
            }
        }
    }
    
    public void commitRows(){
        try {
            String tmp = "";
            Iterator i = list.iterator();
            boolean firstRow = true;
            while(i.hasNext()){
                Object o = i.next();
                if(o==null)
                    continue;
                String s = (String) o;
                if(firstRow){
                    tmp +=s;
                    firstRow = false;
                }else{
                    tmp +="\n"+s;
                }
                
            }
            fw = new FileWriter(file);
            fw.write(tmp);
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(LogicFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] getRows(){
        Iterator i = list.iterator();
        String[] s1 = new String[list.size()];
        int c = 0;
        boolean defined = true;
        while(i.hasNext() && defined){
            Object o = i.next();
            if(o==null){
                defined=false;
                continue;
            }
            s1[c] = (String) o;
            c++;
        }
        String[] s2 = new String[c];
        for(int j=0;j<c;j++){
            s2[j] = s1[j];
        }
        return s2;
    }
}

