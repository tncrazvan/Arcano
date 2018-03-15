/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.WebSocket;

import java.nio.ByteBuffer;


/**
 *
 * @author razvan
 */
public class WebSocketFrameTools {

    public int parseByteCount(int lengthByte){
        if(lengthByte == 127){
            return 14;
        }else if(lengthByte == 126){
            return 8;
        }
        return 6;
    }
    
    public int parseLength(ByteBuffer buf, int length){
        byte[] tmp;
        switch(length){
            case 127:
                tmp = new byte[8];
                buf.get(tmp, 0, 8);
                length = (int)ByteBuffer.wrap(tmp).getLong();
                break;
            case 126:
                tmp = new byte[2];
                buf.get(tmp, 0, 2);
                length = ((tmp[0] & 0xff) << 8) | (tmp[1] & 0xff);
                break;
        }
        return length;
    }
    
    public byte[] parseMask(ByteBuffer buf){
        byte[] mask = new byte[4];
        buf.get(mask, 0, 4);
        return mask;
    }
    
}