/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.System;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Tool.Http.MultipartFormData;
import static com.github.tncrazvan.arcano.Tool.Http.ContentType.resolveContentType;
/**
 *
 * @author Razvan Tanase
 */
public class ServerFile extends File{
    private final ArrayList<int[]> ranges = new ArrayList<>();
    private String rangeUnit = "";
    private int totalRangesLength = 0;
    private static final long serialVersionUID = 4567989494529454756L;
    private final  String contentType = resolveContentType(this.getName());
    private final String boundary = MultipartFormData.generateMultipartBoundary();
    private final String contentRange="";

    public ServerFile(final File parent, final File file) {
        super(parent, file.getAbsolutePath());
    }

    public ServerFile(final File file) {
        super(file.getAbsolutePath());
    }

    public ServerFile(final URI filename) {
        super(filename);
    }

    public ServerFile(final String filename) {
        super(filename);
    }

    public ServerFile(final String parent, final String filename) {
        super(parent, filename);
    }

    public ServerFile(final File parent, final String filename) {
        super(parent, filename);
    }

    public final String getMultipartBoundary(){
        return this.boundary;
    }

    public final int getTotalRangesLength(){
        return totalRangesLength;
    }

    public final String getRangeUnit(){
        return this.rangeUnit;
    }

    public final String getContentType(){
        return this.contentType;
    }

    public final boolean isMultipart(){
        return this.ranges.size() > 0;
    }

    public final String getContentRange(){
        if(this.ranges.size() == 0)
            return null;
        int[] range = this.ranges.get(0);
        return this.getRangeUnit()+" "+range[0]+"-"+(range[1]<0?"":range[1])+"/"+this.length();
    }

    public final void resolveRangesFromHeader(String header) throws FileNotFoundException, IOException {
        this.clearRanges();
        String[] headerPieces = header.split("=", 2);
        this.rangeUnit = headerPieces[0];
        String[] ranges = headerPieces[1].split(",");
        for(String range : ranges){
            boolean ending = range.startsWith("-");
            if(ending){
                this.addRange(0, Integer.parseInt(range.substring(1)));
            }else{
                String[] pieces = range.split("-");
                int start = Integer.parseInt(pieces[0]);
                if(pieces.length == 1 && !ending){
                    this.addRange(start, -1);
                }else if(pieces.length == 2)
                    this.addRange(start, Integer.parseInt(pieces[1]));
            }
        }
    }

    public final void addRange(int start,int end){
        int range[] = new int[2];
        range[0] = start;
        if(end < 0)
            end = (int) this.length()-1;
        range[1] = end;
        ranges.add(range);
        totalRangesLength += end-start;
    }

    public final void clearRanges(){
        ranges.clear();
        totalRangesLength=0;
    }

    public final ArrayList<int[]> getRanges(){
        return this.ranges;
    }

    public final byte[] read() throws FileNotFoundException, IOException {
        return read(0,(int) this.length());
    }
    
    public final byte[] read(int offset, int length) throws FileNotFoundException, IOException {
        byte[] result;
        try (FileInputStream fis = new FileInputStream(this)) {
            fis.getChannel().position(offset);
            result = fis.readNBytes(length);
        }
        return result;
    }

    public final byte[] readAsMultipart(HttpHeaders headers) throws IOException {
        int countRanges = this.ranges.size();
        ByteBuffer buffer = ByteBuffer.allocate(0);

        try (FileInputStream fis = new FileInputStream(this)) {
            if(countRanges > 1){
                //+4 because of the initial "--" string and newline "\r\n"
                int boundaryNotFinalLength = this.boundary.length()+4;
    
                //boundaryNotFinalLength + 1 => +4 because of the initial and final "--" strings (note that there's now new line)
                int boundaryFinalLength = boundaryNotFinalLength;
                
                buffer = ByteBuffer.allocate(this.getTotalRangesLength()+boundaryFinalLength+boundaryNotFinalLength+countRanges);
                String contentRange = this.getContentRange();
                for(int[] range : this.ranges){
                    fis.getChannel().position(range[0]);
                    buffer.put(("--"+this.boundary+"\r\n").getBytes());
                    if(headers != null){
                        if(!headers.isDefined("Content-Type"))
                            headers.set("Content-Type",contentType);
                        headers.set("Content-Range",contentRange);   
                    }
                    buffer.put(headers.toString().getBytes());
                    buffer.put(fis.readNBytes(range[0]-range[1]));
                }
                buffer.put(("--"+this.boundary+"--").getBytes());
            }else{
                int[] range = this.ranges.get(0);
                fis.getChannel().position(range[0]);

                buffer.put(fis.readNBytes(range[0]-range[1]));
            }
        }
        return buffer.array();
    }

    public final void write(final String contents, final String charset) throws UnsupportedEncodingException, IOException {
        write(contents.getBytes(charset));
    }

    public final void write(final byte[] contents) throws FileNotFoundException, IOException {
        final FileOutputStream fos = new FileOutputStream(this);
        fos.write(contents);
        fos.close();
    }

    /**
     * Get all information attributes of this file.
     * @return file information map.
     * @throws IOException
     */
    public final Map<String, Object> info() throws IOException {
        return info("*");
    }

    /**
     * Get a specific information field regarding this file.
     * @param selection
     * @return
     * @throws IOException
     */
    public final Map<String, Object> info(final String selection) throws IOException {
        return Files.readAttributes(this.toPath(), selection);
    }

    /**
     * Read all contents of this file as a String.
     * @param charset charset to use when decoding the contents.
     * @return contents of the file.
     * @throws IOException
     */
    public final String readString(final String charset) throws IOException {
        return Files.readString(this.toPath(),Charset.forName(charset));
    }
}