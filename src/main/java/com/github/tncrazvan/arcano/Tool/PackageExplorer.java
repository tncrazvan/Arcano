/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author Administrator
 */
public class PackageExplorer {
    
    /**
   * Attempts to list all the classes in the specified package as determined
   * by the context class loader, recursively, avoiding anonymous classes
   * 
   * @param pckgname
   *            the package name to search
   * @return a list of classes that exist within that package
   * @throws ClassNotFoundException
   *             if something went wrong
   */
    public static ArrayList<String> getClassesForPackage(String pckgname) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        ArrayList<File> directories = new ArrayList<>();
        String packageToPath = pckgname.replace('.', '/');
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }

            // Ask for all resources for the packageToPath
            Enumeration<URL> resources = cld.getResources(packageToPath);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath().replaceAll("\\\\", "/"), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }

        ArrayList<String> classes = new ArrayList<>();
        // For every directoryFile identified capture all the .class files
        while (!directories.isEmpty()){
            File directoryFile  = directories.remove(0);
            
            if (directoryFile.exists()) {
                // Get the list of the files contained in the package
                File[] files = directoryFile.listFiles();
                
                for (File file : files) {
                    // we are only interested in .class files
                    if ((file.getName().endsWith(".class")) && (!file.getName().contains("$"))) {
                        // removes the .class extension
                        int index = directoryFile.getPath().replaceAll("\\\\", "/").indexOf(packageToPath);
                        String packagePrefix = directoryFile.getPath().replaceAll("\\\\", "/").substring(index).replace('/', '.');
                      try {
                        String className = packagePrefix + '.' + file.getName().substring(0, file.getName().length() - 6);
                        classes.add(className);
                      } catch (NoClassDefFoundError e)
                      {
                        // do nothing. this class hasn't been found by the loader, and we don't care.
                      }
                    } else if (file.isDirectory()){ // If we got to a subdirectory
                        directories.add(new File(file.getPath()));
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directoryFile.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    } 
    
    
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws java.net.URISyntaxException
     */
    public static ArrayList<String> getClasses(String packageName)
            throws ClassNotFoundException, IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        
        Enumeration resources = classLoader.getResources(path);
        ArrayList<String> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            String filename = resource.getFile();
            /*if(new File(filename).exists()){
                System.out.println("FILE EXISTS:"+filename);
            }else{
                System.out.println("FILE DOESN'T EXIST:"+filename);
            }*/
            dirs.add(filename);
        }
        ArrayList<String> classes;
        classes = new ArrayList<>();
        for (String directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param filename
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public static ArrayList<String> findClasses(String filename, String packageName) throws ClassNotFoundException, URISyntaxException {
        File directory = new File(filename);
        ArrayList<String> classes = new ArrayList();
        if (!directory.exists()) {
            //System.out.println("directory doesn't exist:"+directory.toURI()+"\n");
            return classes;
        }else{
            //System.out.println("directory EXISTS:"+directory.toURI()+"\n");
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(filename, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String name = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(name);
            }
        }
        return classes;
    }
}
