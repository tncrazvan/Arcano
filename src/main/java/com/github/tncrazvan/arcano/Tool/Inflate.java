/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 *
 * @author Administrator
 */
public interface Inflate {
    static byte[] inflate(final byte[] input) throws IOException{
        if(input == null || input.length == 0)
            return input;
        ByteArrayInputStream bais = new ByteArrayInputStream(input);
        InflaterInputStream iis = new InflaterInputStream(bais);
        return iis.readAllBytes();
    }
}
