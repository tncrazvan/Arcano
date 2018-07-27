/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.razshare.elkserver;

import java.util.ArrayList;

/**
 *
 * @author Razvan
 */
public class AsciiTable {
    private ArrayList<Integer> columns = new ArrayList<>();
    private ArrayList<String[]> rows = new ArrayList<>();
    private String title;
    private String rowSeparator;
    private String rowFormat;
    private String labelSeparator;
    private String labelFormat;
    private int rowCounter = 0, columnCounter = 0;
    
    public AsciiTable(String title) {
        this.title=title;
        labelSeparator = "";
        labelFormat = "";
        rowSeparator = "";
        rowFormat = "";
    }
    
    public void setTitle(String title){
        this.title=title;
    }
    
    public void addColumn(int width){
        columns.add(width);
        updateCollumnFormat();
        columnCounter++;
    }
    
    public void addRow(String... values){
        int counter = 0;
        for(String value : values){
            if(columnCounter < values.length){
                addColumn(15);
            }
            
            int valueLength = value.length();
            if(value.length() > columns.get(counter)){
                columns.set(counter, valueLength);
                updateCollumnFormat();
            }
            counter++;
        }
        rows.add(values);
        rowCounter++;
    }
    
    private void extendToRight(int extension){
        int lastColWidth = columns.get(columnCounter-1);
        columns.set(columnCounter-1, lastColWidth+extension);
        updateCollumnFormat();
    }
    
    public void draw(){
        int titleLength = title.length();
        int labelLength = labelSeparator.length()-4;
        if(titleLength > labelLength){
           extendToRight(titleLength-labelLength+2);
        }
        
        System.out.format(labelSeparator);
        String centeredTitle = "";
        
        if(titleLength < labelLength){
            int extra = (labelLength - titleLength)/2;
            centeredTitle = new String(new char[extra-1]).replace("\0", " ")+title;
        }else{
            centeredTitle = title;
        }
        
        System.out.format(labelFormat,centeredTitle);
        
        System.out.format(rowSeparator);
        boolean first = true;
        for(String[] row : rows){
            System.out.format(rowFormat,row);
            if(first){
                first = false;
                System.out.format(rowSeparator);
            }
        }
        
        System.out.format(rowSeparator);
    }
    
    private void updateCollumnFormat(){
        labelSeparator  = "";
        labelFormat     = "";
        rowFormat       = "";
        rowSeparator    = "";
        for(int width : columns){
            if(labelSeparator.equals("")) 
                labelSeparator  += "+";
            else
                labelSeparator  += "-";
            
            labelSeparator      += new String(new char[width+2]).replace("\0", "-");
            
            rowSeparator        += "+";
            rowSeparator        += new String(new char[width+2]).replace("\0", "-");
            
            rowFormat           += "| %-"+width+"s ";
        }
        labelFormat     += "| %-"+(labelSeparator.length()-3)+"s |%n";
        labelSeparator  += "+%n";
        rowSeparator    += "+%n";
        rowFormat       += "|%n";
    }
    
}
