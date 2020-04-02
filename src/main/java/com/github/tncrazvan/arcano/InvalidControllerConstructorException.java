/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano;

/**
 *
 * @author Razvan Tanase
 */
public class InvalidControllerConstructorException extends Exception{

    /**
     *
     */
    private static final long serialVersionUID = -2402208777522538925L;

    public InvalidControllerConstructorException(final String message) {
        super(message);
    }
    
}
