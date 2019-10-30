/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

/**
 *
 * @author Administrator
 */
public interface Deflate {
    static byte[] deflate(final byte[] input) throws IOException{
        if(input == null || input.length == 0)
            return input;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write(input);
            dos.flush();
        }
        return  baos.toByteArray();
    }
}
