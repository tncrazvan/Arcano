package com.github.tncrazvan.arcano.smtp;

/**
 *
 * @author Razvan Tanase
 */
public interface SmtpListener {
    public void onEmailReceived(Email email);
}
