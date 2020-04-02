/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Cluster;

/**
 *
 * @author Razvan Tanase
 */
public class NoSecretFoundException extends Exception{
    private static final long serialVersionUID = 6525068845546526095L;

    public NoSecretFoundException(final String message) {
        super(message);
    }
}
