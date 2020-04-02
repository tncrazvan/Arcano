package com.github.tncrazvan.arcano.Smtp;

/**
 *
 * @author Razvan Tanase
 */
public interface SmtpListener {
    public void onEmailReceived(Email email);
}
