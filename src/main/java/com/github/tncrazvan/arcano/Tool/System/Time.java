/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.System;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author Razvan Tanase
 */
public interface Time {
    
    public static long toSeconds(final ZoneId zone, final LocalDateTime time) {
        return Time.toLocalDateTime().atZone(zone).toEpochSecond();
    }

    public static LocalDateTime toLocalDateTime(final ZoneId zone, final long value) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(value), zone);
    }

    public static LocalDateTime toLocalDateTime() {
        return LocalDateTime.now();
    }

    public static long now(final ZoneId zone) {
        return toSeconds(zone, LocalDateTime.now());
    }
    
    /*public static LocalDateTime future(String value){
        return LocalDateTime.toLocalDateTime().plusSeconds(Integer.parseInt(value));
    }
    
    public static LocalDateTime future(long seconds){
        return LocalDateTime.toLocalDateTime().plusSeconds(seconds);
    }*/
    
}
