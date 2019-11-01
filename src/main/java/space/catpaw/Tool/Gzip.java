/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.catpaw.Tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Administrator
 */
public interface Gzip {
    static byte[] compress(final byte[] input) throws IOException{
        if ((input == null) || (input.length == 0)) {
            return input;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
            gzip.write(input);
            gzip.flush();
        }
        return obj.toByteArray();
    }
    
    static byte[] decompress(final byte[] input) throws IOException{
        if ((input == null) || (input.length == 0))
            return input;
        
        if (isCompressed(input)) {
          final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(input));
          return gis.readAllBytes();
        } else 
            return input;
    }
    
    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
