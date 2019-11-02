/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import java.util.Arrays;

/**
 *
 * @author Administrator
 */
public class Storage<T> {
    private byte[] bytes;
    private String strings;
    private int integers;
    private float floats;
    private double doubles;
    private boolean  booleans;
    
    private short type;
    
    private final short 
            STRING = 0,
            INTEGER = 1,
            FLOAT = 2,
            DOUBLE = 3,
            BOOLEAN = 4,
            BYTES = 5
            ;
            
    public Storage(String value) {
        type = STRING;
        strings = value;
    }
    
    public Storage(int value) {
        type = INTEGER;
        integers = value;
    }
    
    public Storage(double value) {
        type = DOUBLE;
        doubles = value;
    }
    
    public Storage(float value) {
        type = FLOAT;
        floats = value;
    }
    
    public Storage(boolean value) {
        type = BOOLEAN;
        booleans = value;
    }
    
    public Storage(byte[] value) {
        type = BYTES;
        bytes = value;
    }
    
    public void add(T value){
        switch(type){
            case STRING:
                strings += (String)value;
                break;
            case INTEGER:
                integers += (int) value;
                break;
            case FLOAT:
                floats += (float) value;
                break;
            case DOUBLE:
                doubles += (double) value;
                break;
            case BOOLEAN:
                booleans = true;
                break;
            case BYTES:
                byte[] tmp = (byte[]) value;
                byte[] joinedArray = Arrays.copyOf(bytes, bytes.length + tmp.length);
                System.arraycopy(tmp, 0, joinedArray, bytes.length, tmp.length);
                bytes = joinedArray;
                break;
        }
    }
    
    public void set(T value){
        switch(type){
            case STRING:
                strings = (String)value;
                break;
            case INTEGER:
                integers = (int) value;
                break;
            case FLOAT:
                floats = (float) value;
                break;
            case DOUBLE:
                doubles = (double) value;
                break;
            case BOOLEAN:
                booleans = (boolean) value;
                break;
            case BYTES:
                bytes = (byte[]) value;
                break;
        }
    }
    
    public T get(){
        switch(type){
            case STRING:
                return (T)strings;
            case INTEGER:
                return (T)(integers+"");
            case FLOAT:
                return (T)(floats+"");
            case DOUBLE:
                return (T)(doubles+"");
            case BOOLEAN:
                return (T)(booleans+"");
            case BYTES:
                return (T)bytes;
        }
        return null;
    }
}
