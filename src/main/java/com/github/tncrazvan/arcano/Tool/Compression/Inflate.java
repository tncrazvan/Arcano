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
        final ByteArrayInputStream bais = new ByteArrayInputStream(input);
        final InflaterInputStream iis = new InflaterInputStream(bais);
        return iis.readAllBytes();
    }
}
