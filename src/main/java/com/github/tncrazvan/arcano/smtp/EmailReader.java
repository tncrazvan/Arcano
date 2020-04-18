package com.github.tncrazvan.arcano.smtp;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.InvalidControllerConstructorException;
import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.tool.reflect.ConstructorFinder;

/**
 *
 * @author Razvan Tanase
 */
public class EmailReader extends SmtpMessageManager{
    String 
                clientHostName = "", 
                currentFrameContent = "", 
                type = "", 
                boundaryId="", 
                //charset="", 
                subject="", 
                sender="",
                line;
    
    String[]    bodyContentType = null;
    String[]    headerContentType = null;
    
    boolean 
                readingFrame = false, 
                readingBody = false;
    
    int 
                currentFrame = 0, 
                lastFrame = 0;
    
    ArrayList<EmailFrame> frames = new ArrayList<>();
    
    boolean
                //conversation checks
                checkHELO = false,
                checkEHLO = false,
                checkMAIL_FROM = false,
                checkDATA = false,
                //checkFROM = false,
                //checkCC = false,
                checkSUBJECT = false,
                checkQUIT = false;
    
    ArrayList<String> checkRCPT_TO = new ArrayList<>();
    ArrayList<SmtpListener> listeners;
    
    public EmailReader(final SmtpServer server, final Socket client, final ArrayList<SmtpListener> listeners)
            throws IOException {
        super(server, client);
        this.listeners = listeners;
    }

    public final void parse() throws IOException {
        sayReady();
        line = read();
        while (line != null && !checkQUIT) {
            if (checkDATA) {
                readData();
            } else if (isHelo(line) && !checkHELO) { // client says HELO
                sayOk(hostname + ", I'm glad to meet you");
                checkHELO = true;
            } else if (isEhlo(line) && !checkEHLO) { // client says EHLO
                checkEHLO = true;
                clientHostName = jumpOnceAndGetRemaining(line);
                sayOkExtended(hostname + " Hello " + clientHostName);
                sayOkExtended("SIZE 14680064");
                sayOkExtended("PIPELINING");
                sayOk("HELP");
            } else if (isMailFrom(line) && !checkMAIL_FROM) { // this message is sent by...
                checkMAIL_FROM = true;
                sender = getMailAddress(line);
                sayOk("Ok");
            } else if (isRecipient(line)) { // this message is meant to be received by...
                sayOk("Ok");
                checkRCPT_TO.add(getMailAddress(line));
            } else if (isData(line) && !checkDATA) { // content of the message
                checkDATA = true;
                sayEndDataWith();
            } else if (isQuit(line) && !checkQUIT) { // client wants to quit
                checkQUIT = true;
                sayBye();
                client.close();
            }
            if (!checkQUIT) {
                line = read();
            } else {
                if (client.isConnected()) {
                    client.close();
                }
            }
        }

    }// end read()

    private boolean frameHeadersDone = false;

    private final void readData() throws IOException {
        /*
         * Since a message body can contain a line with just a period as part of the
         * text, the client sends two periods every time a line starts with a period;
         * correspondingly, the server replaces every sequence of two periods at the
         * beginning of a line with a single one. Such escaping method is called
         * dot-stuffing.
         */
        if (line.length() > 1) {
            if (line.substring(0, 2).equals("..")) {
                line = line.substring(1);
            }
        }

        if (headerContentType != null && headerContentType.length > 1 && isNewBoundary(line, headerContentType[1])) { // new
                                                                                                                      // frame
                                                                                                                      // detected
            saveFrame();
        } else if (headerContentType != null && headerContentType.length > 1
                && isLastBoundary(line, headerContentType[1])) { // end of message
            saveLastFrame();
        } else if (isEndOfData(line)) {
            closeAndNotifyListeners();
        } else if (readingBody) {
            continueReadingBody();
        } else {
            if (currentFrame > lastFrame || readingFrame) { // new frame to be read
                readNewFrame();
            } else {
                readCurrentFrameHeaders();
            }
        }
    }// end readData()

    private final void readCurrentFrameHeaders() throws IOException {
        if (isSubject(line) && !checkSUBJECT) {
            checkSUBJECT = true;
            subject = getSubject(line);
        } else if (isFrom(line)) {
            sender = getMailAddress(line);
        } else if (isContentType(line)) {
            headerContentType = new String[] { getContentType(line), getBoundary(line) };

            if (!type.trim().equals("multipart/alternative")) {
                sayByeAndClose();
            }
        }
    }

    private final void readNewFrame() {
        // reading a frame right now...
        readingFrame = true;
        if (isContentType(line)) {
            bodyContentType = new String[] { getContentType(line), getCharset(line) };

            // type of the content, this must be: multipart/alternative
            type = bodyContentType[0].trim();
        }
    }

    private final void continueReadingBody() {
        if (!frameHeadersDone) {
            if (line.trim().equals("")) {
                frameHeadersDone = true;
            } else if (isContentType(line)) {
                bodyContentType = new String[] { getContentType(line), getCharset(line) };
            }
        } else {
            // reading the actual body of the message right now
            currentFrameContent += "\n" + line;
        }
    }

    private final void closeAndNotifyListeners() throws IOException {
        // "." means end of DATA
        // (normaly this should be sent right before the last frame "QUIT"),
        // however, it seems gmail closes the socket by force
        // as soon as it sends the end of data frame, containing the character "."

        checkDATA = false;
        sayOkAndQueue(12345);
        checkQUIT = true;
        sayByeAndClose();
        final Email email = new Email(subject, frames, sender, checkRCPT_TO);
        listeners.forEach((listener) -> {
            listener.onEmailReceived(email);
        });
        try {
            WebObject wo = server.so.SMTP_ROUTE;
            final Class<?> cls = Class.forName(wo.getClassName());
            final Constructor<?> constructor = ConstructorFinder.getNoParametersConstructor(cls);
            if (constructor == null) {
                throw new InvalidControllerConstructorException(String.format(
                        "\nController %s does not contain a valid constructor.\n"
                                + "A valid constructor for your controller is a constructor that has no parameters.\n"
                                + "Perhaps your class is an inner class and it's not static or public? Try make it a \"static public class\"!",
                        wo.getClassName()));
            }
            try {
                final SmtpController controller = (SmtpController) constructor.newInstance();
                final Method method = controller.getClass().getDeclaredMethod("onEmailReceived", Email.class);
                method.invoke(controller,email);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException | InvalidControllerConstructorException | IllegalArgumentException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private final void saveLastFrame(){
        //I'm saving the last frame
        frames.add(new EmailFrame(currentFrameContent, bodyContentType[0], bodyContentType[1]));
        readingFrame = false;
        readingBody = false;
    }
    
    private final void saveFrame(){
        frameHeadersDone = false;
        readingBody = true;

        //I'm saving the current frame before starting to read the next one
        if(currentFrame > 0){
            frames.add(new EmailFrame(currentFrameContent, bodyContentType[0], bodyContentType[1]));
            currentFrameContent = "";
            bodyContentType = null;
        }
        currentFrame++;
    }
}
