package com.github.tncrazvan.arcano.Tool.Compression;

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
