package com.github.tncrazvan.arcano.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.EventManager;
import com.github.tncrazvan.arcano.Http.HttpHeaders;
import com.github.tncrazvan.arcano.Http.HttpRequestReader;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getSha1Bytes;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getSha1String;
import com.github.tncrazvan.arcano.Tool.Http.Status;

/**
 *
 * @author Razvan
 */
public abstract class WebSocketEventManager extends EventManager{
    private boolean connected = true;
    private WebSocketMessage message;
    //private final HttpHeaders responseHeaders;
    public WebSocketEventManager() {}
    
    /**
     * Get the default language of the user agent that made the request.
     * 
     * @return a String that identifies the language.
     */
    public String getUserDefaultLanguage() {
        return userLanguages.get("DEFAULT-LANGUAGE");
    }

    public void execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String acceptKey = DatatypeConverter.printBase64Binary(getSha1Bytes(
                            reader.request.headers.get("Sec-WebSocket-Key") + so.WEBSOCKET_ACCEPT_KEY, so.config.charset));

                    responseHeaders.set("@Status", Status.STATUS_SWITCHING_PROTOCOLS);
                    responseHeaders.set("Connection", "Upgrade");
                    responseHeaders.set("Upgrade", "websocket");
                    responseHeaders.set("Sec-WebSocket-Accept", acceptKey);
                    reader.output.write((responseHeaders.toString() + "\r\n").getBytes());
                    reader.output.flush();
                    manageOnOpen();
                    final InputStream read = reader.client.getInputStream();
                    while (connected) {
                        unmask((byte) read.read());
                    }
                } catch (final IOException ex) {
                    close();
                } catch (final NoSuchAlgorithmException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }).start();

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
                this.message = new WebSocketMessage();
                this.message.data = payload;
                manageOnMessage(this.message);
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

                this.message = new WebSocketMessage();
                this.message.data = payload;
                manageOnMessage(this.message);
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
            reader.client.close();
            manageOnClose();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send data to the client.
     * 
     * @param data data to be sent to the client.
     */
    public void send(final String data) {
        try {
            send(data.getBytes(so.config.charset), false);
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send data to the client.
     * 
     * @param data data to be sent to the client.
     */
    public void send(final byte[] data) {
        send(data, true);
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
    public void send(final byte[] data, final boolean binary) {
        int offset = 0;
        final int maxLength = so.config.webSocket.mtu - 1;
        if (data.length > maxLength) {
            while (offset < data.length) {
                if (offset + maxLength > data.length) {
                    encodeAndSendBytes(Arrays.copyOfRange(data, offset, data.length), binary);
                    offset = data.length;
                } else {
                    encodeAndSendBytes(Arrays.copyOfRange(data, offset, offset + maxLength), binary);
                    offset += maxLength;
                }
            }
        } else {
            encodeAndSendBytes(data, binary);
        }
    }

    private void encodeAndSendBytes(final byte[] messageBytes, final boolean binary) {
        try {
            reader.output.flush();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        try {
            // We need to set only FIN and Opcode.
            reader.output.write(binary ? 0x82 : 0x81);

            // Prepare the payload length.
            if (messageBytes.length <= 125) {
                reader.output.write(messageBytes.length);
            } else { // We assume it is 16 but length. Not more than that.
                reader.output.write(0x7E);
                final int b1 = (messageBytes.length >> 8) & 0xff;
                final int b2 = messageBytes.length & 0xff;
                reader.output.write(b1);
                reader.output.write(b2);
            }

            // Write the data.
            reader.output.write(messageBytes);
            try {
                reader.output.flush();
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
    public void send(final int data) {
        send("" + data);
    }

    /**
     * Broadcast data to all connected clients except for some of them.
     * 
     * @param data    payload to send.
     * @param ignores list of clients to ignore. The server won't send the payload
     *                to these clients.
     */
    public void broadcast(final String data, final WebSocketController[] ignores) {
        try {
            broadcast(data.getBytes(so.config.charset), ignores, false);
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Broadcast data to all connected clients except for some of them.
     * 
     * @param data    payload to send.
     * @param ignores list of clients to ignore. The server won't send the payload
     *                to these clients.
     */
    public void broadcast(final byte[] data, final WebSocketController[] ignores) {
        broadcast(data, ignores, true);
    }

    /**
     * Broadcast data to all connected clients except for some of them.
     * 
     * @param data    payload to send.
     * @param ignores list of clients to ignore. The server won't send the payload
     *                to these clients.
     * @param binary  the WebSocket standard requires the server to specify when the
     *                content of the message should be trated as binary or not. If
     *                this value is true, the server will set the binary flag to
     *                0x82 otherwise it will be set to 0x81. Note that this won't
     *                encode or convert your data in any way.
     */
    public void broadcast(final byte[] data, final WebSocketController[] ignores, final boolean binary) {
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
                e.send(data, binary);
        }
    }

    /**
     * Send data to a WebSocketGroup.
     * 
     * @param data  data to be sent to the group.
     * @param group the group of clients that should receive the payload.
     */
    public void send(final WebSocketMessage data, final WebSocketGroup group) {
        send(data, group, true);
    }

    /**
     * Send data to a WebSocketGroup.
     * 
     * @param data   data to be sent to the group.
     * @param group  the group of clients that should receive the payload.
     * @param binary the WebSocket standard requires the server to specify when the
     *               content of the message should be trated as binary or not. If
     *               this value is true, the server will set the binary flag to 0x82
     *               otherwise it will be set to 0x81. Note that this won't encode
     *               or convert your data in any way.
     */
    public void send(final WebSocketMessage data, final WebSocketGroup group, final boolean binary) {
        
        group.getMap().keySet().forEach((key) -> {
            final WebSocketController c = group.getMap().get(key);
            if((WebSocketEventManager)c != this){
                c.send(data.data,binary);
            }
        });
    }
    protected abstract void manageOnOpen();
    protected abstract void manageOnMessage(WebSocketMessage payload);
    protected abstract void manageOnClose();
}
