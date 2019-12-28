package com.github.tncrazvan.arcano.Smtp;

import com.github.tncrazvan.arcano.SharedObject;
import static com.github.tncrazvan.arcano.Tool.Http.MultipartFormData.generateMultipartBoundary;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author razvan
 */
public class EmailComposer extends SmtpMessageManager{
    private final Email email;
    public EmailComposer(final Email email, final SmtpServer server, final String clientHostname, final int port,
            final boolean ssl) throws IOException {
        super(server, (ssl ? SSLSocketFactory.getDefault().createSocket(InetAddress.getByName(clientHostname), port)
                : new Socket(clientHostname, port)));
        this.email = email;
    }

    public EmailComposer(final Email email, final SmtpServer server, final String clientHostname, final int port)
            throws IOException {
        super(server, new Socket(clientHostname, port));
        this.email = email;
    }

    public boolean submit() throws IOException {
        String error, eodLine;
        if (!isReady(read()))
            return false;
        sayHelo();
        if (!isOk(read()))
            return false;
        sayMailFrom(email.getSender());
        if (!isOk(read()))
            return false;

        for (final String recipient : email.getRecipients()) {
            sayRecipient(recipient);

            if (!isOk((error = read()))) {
                System.err.println("[WARNING] Server replied with: " + error);
            }
        }
        sayData();
        if (!isEndDataWith((eodLine = read())))
            return false;
        getEndDataWithValue(eodLine);
        
        sayDataFrom(email.getSender());
        sayDataDate(SharedObject.CALENDAR.getTime().getTime());
        sayDataSubject(email.getSubject());
        sayDataTo((String[]) email.getRecipients().toArray());
        setMultipartBoundaryId(generateMultipartBoundary());
        sayDataContentType(); // Content-Type:multipart/alternative
        sayNothing();
        sayDataFrames(email.getAllBodyFrames());
        say(".");
        if(!isOk(read())) return false;
        sayQuitAndClose();
        return true;
    }
}
