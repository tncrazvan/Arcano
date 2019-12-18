/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import com.github.tncrazvan.arcano.SharedObject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 *
 * @author Administrator
 */
public interface Time {
    
    public static long toTimestamp(LocalDateTime time){
        return Time.now().atZone(SharedObject.londonTimezone).toEpochSecond();
    }
    
    public static LocalDateTime now(String value){
        return Time.now(Integer.parseInt(value));
    }
    public static LocalDateTime now(long value){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value),TimeZone.getDefault().toZoneId());
    }
    public static LocalDateTime now(){
        return LocalDateTime.now();
    }
}
