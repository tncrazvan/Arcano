/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

/**
 *
 * @author Administrator
 * @param <T>
 */
public abstract class Action<T>{
    public abstract void callback(T o);
}