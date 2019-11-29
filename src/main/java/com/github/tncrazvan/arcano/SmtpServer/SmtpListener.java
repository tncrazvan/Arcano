package com.github.tncrazvan.arcano.SmtpServer;

/**
 *
 * @author razvan
 */
public interface SmtpListener {
    public void onEmailReceived(Email email);
}
