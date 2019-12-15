/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 *
 * @author Administrator
 */
public interface Time {
    public static LocalDateTime time(String value){
        return time(Integer.parseInt(value));
    }
    public static LocalDateTime time(long value){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value),TimeZone.getDefault().toZoneId());
    }
    public static LocalDateTime time(){
        return LocalDateTime.now();
    }
}
