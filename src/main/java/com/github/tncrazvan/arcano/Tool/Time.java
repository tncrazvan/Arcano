/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import com.github.tncrazvan.arcano.SharedObject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author Administrator
 */
public interface Time {
    
    public static long toTimestamp(LocalDateTime time){
        return Time.now().atZone(SharedObject.londonTimezone).toEpochSecond();
    }
    
    public static LocalDateTime now(ZoneId zone, String value){
        return Time.now(zone, Integer.parseInt(value));
    }
    public static LocalDateTime now(ZoneId zone, long value){
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(value),zone);
    }
    public static LocalDateTime now(){
        return LocalDateTime.now();
    }
}
