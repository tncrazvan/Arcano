package com.github.tncrazvan.arcano.Smtp;

/**
 *
 * @author razvan
 */
public interface SmtpListener {
    public void onEmailReceived(Email email);
}
