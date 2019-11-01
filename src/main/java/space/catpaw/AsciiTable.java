/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.catpaw;

import java.util.ArrayList;

/**
 *
 * @author Razvan
 */
public class AsciiTable {
    private final ArrayList<Integer> columns = new ArrayList<>();
    private final ArrayList<String[]> rows = new ArrayList<>();
    private String title;
    private String titleFormat;
    private String titleSeparatorFormat;
    private String rowSeparatorFormat;
    private String rowFormat;
    private int titlePush = 1;
    
    public AsciiTable(String title) {
        this.title=title;
        titleFormat = "";
        titleSeparatorFormat = "";
        rowSeparatorFormat = "";
        rowFormat = "";
    }
    
    public void setTitle(String title){
        this.title=title;
    }
    //+----------+----------+-------------+--------------+----------------+-------------+
    private void fixWidths(){
        int maxWidth = 1;
        for(String[] values : rows){
            int localWidth = 1;
            for(int i = 0; i<values.length; i++){
                int valueLength = values[i].length();
                if(columns.size() < i+1){
                    columns.add(valueLength);
                    localWidth += valueLength+1+2;
                    if(localWidth > maxWidth) maxWidth=localWidth;
                    continue;
                }
                int columnLength = columns.get(i);
                columnLength = valueLength > columnLength?valueLength:columnLength;
                localWidth += columnLength+1+2;
                if(localWidth > maxWidth) maxWidth=localWidth;
                columns.set(i, columnLength);
            }
        }
        
        int titleLength = title.length()+6;
        if(titleLength > maxWidth){
            int size = columns.size();
            columns.set(size-1, columns.get(size-1)+titleLength-maxWidth);
        }
    }
    
    public void addRow(String... values){
        String[] result = new String[values.length];
        for(int i=0;i<result.length;i++){
            result[i] = values[i].trim();
        }
        rows.add(result);
    }
    
    
    public void draw(){
        fixWidths();
        buildFormat();
        System.out.format(titleSeparatorFormat);
        System.out.format(titleFormat," ",title);
        int rowsCounter = rows.size();
        for(int i = 0;i < rowsCounter; i++){
            if(i <= 1){
                System.out.format(rowSeparatorFormat);
            }
            String[] row = rows.get(i);
            System.out.format(rowFormat,row);
        }
        System.out.format(rowSeparatorFormat);
    }
    
    private void buildFormat(){
        int totalWidth = 0;
        titleFormat                 = "";
        titleSeparatorFormat        = "";
        rowFormat                   = "";
        rowSeparatorFormat          = "";
        boolean first = true;
        for(int width : columns){
            totalWidth += width+3;
            if(first){
                first = false;
                titleSeparatorFormat    += "+";
            }else{
                titleSeparatorFormat    += "-";
            }
            rowSeparatorFormat      += "+";
            rowFormat               += "|";
            
            titleSeparatorFormat    += new String(new char[width+2]).replace("\0", "-");
            rowSeparatorFormat      += new String(new char[width+2]).replace("\0", "-");
            rowFormat               += " %-"+width+"s ";
        }
        titleSeparatorFormat        += "+%n";
        rowSeparatorFormat          += "+%n";
        rowFormat                   += "|%n";
        totalWidth ++;
        
        totalWidth -= 2;
        
        titlePush += (int) Math.floor((totalWidth-title.length())/2)-1;
        
        totalWidth -= titlePush;
        if(titlePush < 0) {
            titleFormat                 = "|%-s%-"+totalWidth+"s|%n";
        }else{
            titleFormat                 = "|%-"+titlePush+"s%-"+totalWidth+"s|%n";
        }
        
    }
    
}
