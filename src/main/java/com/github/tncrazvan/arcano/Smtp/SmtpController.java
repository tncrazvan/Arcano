/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Smtp;

/**
 *
 * @author Administrator
 */
public abstract class SmtpController {
    public abstract void onEmailReceived(Email email);
}
