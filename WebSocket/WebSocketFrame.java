/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.WebSocket;

import elkserver.Elk;


/**
 *
 * @author razvan
 */
public class WebSocketFrame {
    private final byte opCode;
    private final byte[] payload;
    private byte[] 
            mask = new byte[4], 
            digest;
    private final int 
            lengthByte,
            bytes;
    private final boolean fin;
    private int 
            length,
            maskOffset,
            payloadOffset;
    private final WebSocketFrame 
            originalFrame;
    private WebSocketFrame
            nextFrame;
    public WebSocketFrame(byte[] payload,int bytes,WebSocketFrame originalFrame) {
        this.payload=payload;
        fin = (int)(payload[0] & 0x77) != 1;
        opCode = (byte)(payload[0] & 0x0F);
        this.bytes = bytes;
        
        //if the opcode is 0x0 (a continuation frame) then keep track of the originalframe
        /*
            NOTE: it looks like google chrome (and maybe opera) likes to send frames which have
            the FIN field set to FALSE and the opcode set to 0x01, which normally would indicate the
            beginning of a new series of frames that are all part of the same message. Normally
            in that case, the browser is meant to send subsequent frames with FIN set to FALSE, and
            opcode set to 0x0, unless the frame is the last frame of the message. 
            If the frame is the last frame of the message, then FIN should be set to TRUE, and opcode set to 0x0.
            
            It looks like chrome sends a frame indicating it will send more for the same message, but the drops the intent.
        */
        if(opCode == 0x0){
            this.originalFrame = originalFrame;
        }else{
            this.originalFrame = null;
        }
        lengthByte = this.originalFrame != null?this.originalFrame.getLengthByte():(int)payload[1] & 127;
        
        parseLength();
        parseMask();
        parsePayloadOffset();
        parsePayload();
    }
    
    private void parseLength(){
        if(originalFrame != null){
            length = originalFrame.getLength();
        }else if(lengthByte > 0 && lengthByte < 125){
            //found the length
            length = lengthByte;
        }else if(lengthByte == 126){
            length = ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
        }else if(lengthByte == 127){
            int tmp = payload[2] & 0xff, pos;
            for(pos = 3;pos < 9;pos++){
                tmp = ((tmp & 0xff) << 8) | (payload[pos] & 0xff);
            }
            length = tmp;
        }
    }
    
    private void parseMask(){
        if(originalFrame != null){
            maskOffset = 10;
            mask = originalFrame.getMask();
        }else{
            if(length > 0 && length < 125){
                maskOffset = 2;
            }else if(lengthByte == 126){
                maskOffset = 4;
            }else if(lengthByte == 127){
                maskOffset = 10;
            }
            mask[0] = payload[maskOffset];
            mask[1] = payload[++maskOffset];
            mask[2] = payload[++maskOffset];
            mask[3] = payload[++maskOffset];
        }
    }
    
    private void parsePayloadOffset(){
        if(originalFrame != null){
            payloadOffset = 0;
        }else if(lengthByte > 0 && lengthByte < 125){
            payloadOffset = 6;
        }else if(lengthByte == 126){
            payloadOffset = 8;
        }else if(lengthByte == 127){
            payloadOffset = 14;
        }
    }
    
    private void parsePayload(){
        int i = 0, digestIndex = 0;
        byte currentByte;
        digest = new byte[length];
        while(digestIndex < digest.length && (payloadOffset+i) < bytes){
            currentByte = (byte) (payload[(payloadOffset+i)] ^ mask[digestIndex%mask.length]);
            digest[digestIndex] = currentByte;
            digestIndex++;
            i++;
        }
        
        /*try{
            String[] tmp = new String(digest).split("base64,");
            if(tmp.length>1)
                System.out.println("digested:"+Elk.atob(tmp[1]));
            else
                System.out.println("digested:"+tmp[0]);
        }catch(IllegalArgumentException ex){
            ex.printStackTrace();
        }*/
    }
    
    public byte[] getDigestedPayload(){
        return digest;
    }
    
    public int getLength(){
        return length;
    }
    
    public byte[] getMask(){
        return mask;
    }
    
    public boolean isFin(){
        return fin;
    }
    
    public byte getOpCode(){
        return opCode;
    }
    
    public int getLengthByte(){
        return lengthByte;
    }

    public WebSocketFrame getNextFrame(){
        return nextFrame;
    }
    
    public void setNextFrame(WebSocketFrame frame){
        nextFrame = frame;
    }
}
