package com.github.tncrazvan.arcano.websocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.tool.encoding.Hashing.getSha1Bytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import com.github.tncrazvan.arcano.EventManager;
import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.http.HttpRequestReader;
import com.github.tncrazvan.arcano.tool.Strings;
import com.github.tncrazvan.arcano.tool.http.Status;

/**
 *
 * @author Razvan Tanase
 */
public abstract class WebSocketEventManager extends EventManager{
    private boolean connected = true;
    private WebSocketCommit message;
    private final String uuid = Strings.uuid();
    private InputStream read = null;
    //private final HttpHeaders responseHeaders;
    public WebSocketEventManager(HttpRequestReader reader, SharedObject so) throws UnsupportedEncodingException {
        super(reader,so);
    }
    
    public final InputStream getRead(){
        try {
            if(read == null)
                read = request.reader.client.getInputStream();
            return read;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public boolean isConnected(){
        return connected;
    }
    
    public final String getUuid(){
        return uuid;
    }

    public final void execute() {
        try {
            
            final String acceptKey = 
                    DatatypeConverter
                        .printBase64Binary(
                            getSha1Bytes(
                                request
                                    .reader
                                        .content
                                            .headers
                                                .get("Sec-WebSocket-Key") + 
                                so
                                    .WEBSOCKET_ACCEPT_KEY, 
                                so
                                    .config
                                        .charset
                            )
                        );

            response.headers.setStatus(Status.STATUS_SWITCHING_PROTOCOLS);
            response.headers.set("Connection", "Upgrade");
            response.headers.set("Upgrade", "websocket");
            response.headers.set("Sec-WebSocket-Accept", acceptKey);
            request.reader.output.write((response.headers.toString()+ "\r\n").getBytes());
            request.reader.output.flush();
            onOpen();
            so.webSocketEventManager.put(uuid, this);
        } catch (final IOException ex) {
            close();
        } catch (final NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /*
     * WEBSOCKET FRAME:
     * 
     * 
     * 0 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-------+-+-------------+-------------------------------+ |F|R|R|R|
     * opcode|M| Payload len | Extended payload length | |I|S|S|S| (4) |A| (7) |
     * (16/64) | |N|V|V|V| |S| | (if payload len==126/127) | | |1|2|3| |K| | |
     * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - + | Extended
     * payload length continued, if payload len == 127 | + - - - - - - - - - - - - -
     * - - +-------------------------------+ | |Masking-key, if MASK set to 1 |
     * +-------------------------------+-------------------------------+ |
     * Masking-key (continued) | Payload Data | +-------------------------------- -
     * - - - - - - - - - - - - - - + : Payload Data continued ... : + - - - - - - -
     * - - - - - - - - - - - - - - - - - - - - - - - - + | Payload Data continued
     * ... | +---------------------------------------------------------------+
     */

    private final int FIRST_BYTE = 0, SECOND_BYTE = 1, LENGTH2 = 2, LENGTH8 = 3, MASK = 4, PAYLOAD = 5, DONE = 6;
    private int lengthKey = 0, reading = FIRST_BYTE, lengthIndex = 0, maskIndex = 0, payloadIndex = 0,
            payloadLength = 0;
    // private boolean fin,rsv1,rsv2,rsv3;
    private byte opcode;
    private byte[] payload = null, mask = null, length = null;

    public void unmask(final byte[] bytes) throws IOException{
        for(int i = 0;i<bytes.length;i++){
            unmask(bytes[i]);
            if(!connected) return;
        }
    }
    // private final String base = "";
    public void unmask(final byte b) throws UnsupportedEncodingException, IOException {
        // System.out.println("=================================");
        switch (reading) {
            case FIRST_BYTE:
                // fin = ((b & 0x80) != 0);
                // rsv1 = ((b & 0x40) != 0);
                // rsv2 = ((b & 0x20) != 0);
                // rsv3 = ((b & 0x10) != 0);
                opcode = (byte) (b & 0x0F);
                if (opcode == 0x8) { // fin
                    close();
                }
                mask = new byte[4];
                reading = SECOND_BYTE;
            break;
            case SECOND_BYTE:
                lengthKey = b & 127;
                if (lengthKey <= 125) {
                    length = new byte[1];
                    length[0] = (byte) lengthKey;
                    payloadLength = lengthKey & 0xff;
                    reading = MASK;
                } else if (lengthKey == 126) {
                    reading = LENGTH2;
                    length = new byte[2];
                } else if (lengthKey == 127) {
                    reading = LENGTH8;
                    length = new byte[8];
                }
            break;
            case LENGTH2:
                length[lengthIndex] = b;
                lengthIndex++;
                if (lengthIndex == 2) {
                    payloadLength = ((length[0] & 0xff) << 8) | (length[1] & 0xff);
                    reading = MASK;
                }
            break;
            case LENGTH8:
                length[lengthIndex] = b;
                lengthIndex++;
                if (lengthIndex == 8) {
                    payloadLength = length[0] & 0xff;
                    for (int i = 1; i < length.length; i++) {
                        payloadLength = ((payloadLength) << 8) | (length[i] & 0xff);
                    }
                    reading = MASK;
                }
                break;
            case MASK:
                mask[maskIndex] = b;
                maskIndex++;
                if (maskIndex == 4) {
                    reading = PAYLOAD;
                    // int l = (int)ByteBuffer.wrap(length).getLong();
                    payload = new byte[payloadLength];
                }
            break;
            case PAYLOAD:
                if (payload.length == 0) {
                    this.message = new WebSocketCommit();
                    this.message.data = payload;
                    onMessage(this.message);
                    break;
                }
                try {
                    payload[payloadIndex] = (byte) (b ^ mask[payloadIndex % 4]);
                } catch (final Exception e) {
                    e.printStackTrace(System.out);
                }
                payloadIndex++;
                if (payloadIndex == payload.length) {
                    reading = DONE;

                    this.message = new WebSocketCommit();
                    this.message.data = payload;
                    onMessage(this.message);
                    lengthKey = 0;
                    reading = FIRST_BYTE;
                    lengthIndex = 0;
                    maskIndex = 0;
                    payloadIndex = 0;
                    payload = null;
                    mask = null;
                    length = null;
                }
            break;
        }
    }

    /**
     * Close the WebSocket connection.
     */
    public void close() {
        try {
            connected = false;
            request.reader.client.close();
            onClose();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private final LinkedList<WebSocketCommit> commits = new LinkedList<>();
    
    public LinkedList<WebSocketCommit> getCommits(){
        return commits;
    }
    
    public void commit(WebSocketCommit commit){
        commits.add(commit);
    };
    
    /**
     * Send data to the client.
     * 
     * @param data data to be sent to the client.
     */
    public void push(final String data) {
        try {
            WebSocketEventManager.this.push(data.getBytes(so.config.charset), false);
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send data to the client.
     * 
     * @param data data to be sent to the client.
     */
    public void push(final byte[] data) {
        WebSocketEventManager.this.push(data, true);
    }

    /**
     * Send data to the client.
     * 
     * @param data   data to be sent to the client.
     * @param binary the WebSocket standard requires the server to specify when the
     *               content of the message should be trated as binary or not. If
     *               this value is true, the server will set the binary flag to 0x82
     *               otherwise it will be set to 0x81. Note that this won't encode
     *               or convert your data in any way.
     */
    public void push(final byte[] data, final boolean binary) {
        int offset = 0;
        final int maxLength = so.config.webSocket.mtu - 1;
        if (data.length > maxLength) {
            while (offset < data.length) {
                if (offset + maxLength > data.length) {
                    encodeAndPushBytes(Arrays.copyOfRange(data, offset, data.length), binary);
                    offset = data.length;
                } else {
                    encodeAndPushBytes(Arrays.copyOfRange(data, offset, offset + maxLength), binary);
                    offset += maxLength;
                }
            }
        } else {
            encodeAndPushBytes(data, binary);
        }
    }

    private void encodeAndPushBytes(final byte[] messageBytes, final boolean binary) {
        try {
            request.reader.output.flush();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        try {
            // We need to set only FIN and Opcode.
            request.reader.output.write(binary ? 0x82 : 0x81);

            // Prepare the payload length.
            if (messageBytes.length <= 125) {
                request.reader.output.write(messageBytes.length);
            } else { // We assume it is 16 but length. Not more than that.
                request.reader.output.write(0x7E);
                final int b1 = (messageBytes.length >> 8) & 0xff;
                final int b2 = messageBytes.length & 0xff;
                request.reader.output.write(b1);
                request.reader.output.write(b2);
            }

            // Write the data.
            request.reader.output.write(messageBytes);
            try {
                request.reader.output.flush();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } catch (final IOException ex) {
            close();
        }

    }

    /**
     * Send data to the client.
     * 
     * @param data data to be sent to the client.
     */
    public void push(final int data) {
        WebSocketEventManager.this.push("" + data);
    }

    /**
     * Broadcast data to all connected clients except for some of them.
     * 
     * @param data    payload to push.
     * @param ignores list of clients to ignore. The server won't push the payload
                to these clients.
     */
    public void broadcast(final String data, final WebSocketEvent[] ignores) {
        try {
            broadcast(data.getBytes(so.config.charset), ignores, false);
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Broadcast data to all connected clients except for some of them.
     * 
     * @param data    payload to push.
     * @param ignores list of clients to ignore. The server won't push the payload
                to these clients.
     */
    public void broadcast(final byte[] data, final WebSocketEvent[] ignores) {
        broadcast(data, ignores, true);
    }

    /**
     * Broadcast data to all connected clients except for some of them.
     * 
     * @param data    payload to push.
     * @param ignores list of clients to ignore. The server won't push the payload
                to these clients.
     * @param binary  the WebSocket standard requires the server to specify when the
     *                content of the message should be trated as binary or not. If
     *                this value is true, the server will set the binary flag to
     *                0x82 otherwise it will be set to 0x81. Note that this won't
     *                encode or convert your data in any way.
     */
    public void broadcast(final byte[] data, final WebSocketEvent[] ignores, final boolean binary) {
        boolean skip;
        for (final WebSocketEvent e : so.WEB_SOCKET_EVENTS.get(ignores.getClass().getCanonicalName())) {
            skip = false;
            for (final Object ignore : ignores) {
                if (ignore == this) {
                    skip = true;
                    break;
                }
            }
            if (!skip)
                e.push(data, binary);
        }
    }

    /**
     * Send data to a WebSocketGroup.
     * 
     * @param data   data to be sent to the group.
     */
    public void push(final WebSocketCommit data) {
        if(data.getWebSocketGroup() != null){
            data.getWebSocketGroup().getMap().keySet().forEach((key) -> {
                final WebSocketEvent e = data.getWebSocketGroup().getMap().get(key);
                if((WebSocketEventManager)e != this){
                    e.push(data.data,data.isBinary());
                }
            });
        }else{
            this.push(data.data, data.isBinary());
        }
    }
    
    protected abstract void onOpen();
    protected abstract void onMessage(WebSocketCommit payload);
    protected abstract void onClose();
}
