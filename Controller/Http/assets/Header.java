/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.Http.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javahttpserver.Http.HttpEvent;
import javahttpserver.JHS;

/**
 *
 * @author Razvan
 */
public class Header {
    public void main(HttpEvent e, ArrayList<String> args) throws FileNotFoundException, IOException{
        e.sendFileContents("/view/_header/header.html");
    }
}
