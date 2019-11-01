/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.catpaw.Tool;

/**
 *
 * @author Administrator
 * @param <T>
 */
public abstract class Callable<T>{
    public abstract void callback(T o);
}