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
    public static ArrayList<String> getClassesForPackage(final String pckgname) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname. There may be more
        // than one if a package is split over multiple jars/paths
        final ArrayList<File> directories = new ArrayList<>();
        final String packageToPath = pckgname.replace('.', '/');
        try {
            final ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }

            // Ask for all resources for the packageToPath
            final Enumeration<URL> resources = cld.getResources(packageToPath);
            while (resources.hasMoreElements()) {
                directories.add(new File(
                        URLDecoder.decode(resources.nextElement().getPath().replaceAll("\\\\", "/"), "UTF-8")));
            }
        } catch (final NullPointerException x) {
            throw new ClassNotFoundException(
                    pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (final UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(
                    pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (final IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }

        final ArrayList<String> classes = new ArrayList<>();
        // For every directoryFile identified capture all the .class files
        while (!directories.isEmpty()) {
            final File directoryFile = directories.remove(0);

            if (directoryFile.exists()) {
                // Get the list of the files contained in the package
                final File[] files = directoryFile.listFiles();

                for (final File file : files) {
                    // we are only interested in .class files
                    if ((file.getName().endsWith(".class")) && (!file.getName().contains("$"))) {
                        // removes the .class extension
                        final int index = directoryFile.getPath().replaceAll("\\\\", "/").indexOf(packageToPath);
                        final String packagePrefix = directoryFile.getPath().replaceAll("\\\\", "/").substring(index)
                                .replace('/', '.');
                        try {
                            final String className = packagePrefix + '.'
                                    + file.getName().substring(0, file.getName().length() - 6);
                            classes.add(className);
                        } catch (final NoClassDefFoundError e) {
                            // do nothing. this class hasn't been found by the loader, and we don't care.
                        }
                    } else if (file.isDirectory()) { // If we got to a subdirectory
                        directories.add(new File(file.getPath()));
                    }
                }
            } else {
                throw new ClassNotFoundException(
                        pckgname + " (" + directoryFile.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to
     * the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws java.net.URISyntaxException
     */
    public static ArrayList<String> getClasses(final String packageName)
            throws ClassNotFoundException, IOException, URISyntaxException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assert classLoader != null;
        final String path = packageName.replace('.', '/');

        final Enumeration resources = classLoader.getResources(path);
        final ArrayList<String> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            final URL resource = (URL) resources.nextElement();
            final String filename = resource.getFile();
            /*
             * if(new File(filename).exists()){ System.out.println("FILE EXISTS:"+filename);
             * }else{ System.out.println("FILE DOESN'T EXIST:"+filename); }
             */
            dirs.add(filename);
        }
        ArrayList<String> classes;
        classes = new ArrayList<>();
        for (final String directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param filename
     * @param packageName The package name for classes found inside the base
     *                    directory
     * @return The classes
     * @throws ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public static ArrayList<String> findClasses(final String filename, final String packageName)
            throws ClassNotFoundException, URISyntaxException {
        final File directory = new File(filename);
        final ArrayList<String> classes = new ArrayList();
        if (!directory.exists()) {
            // System.out.println("directory doesn't exist:"+directory.toURI()+"\n");
            return classes;
        } else {
            // System.out.println("directory EXISTS:"+directory.toURI()+"\n");
        }
        final File[] files = directory.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(filename, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                final String name = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(name);
            }
        }
        return classes;
    }
}
