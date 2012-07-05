/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Jan 19, 2004
 */
package gov.nasa.ial.mde.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import javax.xml.transform.stream.StreamSource;

/**
 * A utility for loading resources files, which could be located in a Jar file.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class ResourceUtil {
    
    private String pathToResources;
    
    /**
     * Default constructor with a null path to the resources.
     */
    public ResourceUtil() {
        this(null);
    }
    
    /**
     * Constructs a <code>ResourceUtil</code> that will load resources from
     * the specified path.
     * 
     * @param pathToResources path to the resources.
     */
    public ResourceUtil(String pathToResources) {
        //setPathToResources(pathToResources);
    	this.pathToResources = pathToResources;
    }
    
    /**
     * Return the path to the resources.
     * 
     * @return the path to the resources, or null is one has not been set.
     */
    public String getPathToResources() {
        return this.pathToResources;
    }
    
    /**
     * Sets the path to the resources.
     * 
     * @param path the path to the resources.
     */
    public void setPathToResources(String path) {
        if (path != null) {
            path = path.trim();
            if (!path.endsWith("/")) {
                path += "/";
            }
        }
        this.pathToResources = path;
    }
    
    /**
     * Resolves the path to the specified file.
     * 
     * @param file the file to resolve the path to.
     * @return the resolved path to the specified resource file.
     */
    public String resolvePathTo(String file) {
        if ((pathToResources != null) && !file.startsWith(pathToResources)) {
            while (file.startsWith("/")) {
                file = (file.length() > 1) ? file.substring(1).trim() : "";
            }
            file = pathToResources + file;
        }

        //Add res directory prefix if file not found
        /*File tempFile = new File(file);
        if (!tempFile.exists()) {
            file = "res" + file;
        }*/
        return file;
    }
    
    /**
     * Returns an <code>Image</code> for the specified image file.
     *  
     * @param file name of the image file.
     * @return the image.
     * @throws IOException if the file could not be loaded.
     */
    public Image getImage(String file) throws IOException {
        InputStream in = null;
        try {
            in = getResourceAsStream(file);
            int numBytes = in.available();
            byte buffer[] = new byte[numBytes];
            for (int i = 0; i < numBytes; i++) {
                buffer[i] = (byte)in.read();
            }
            return Toolkit.getDefaultToolkit().createImage(buffer);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) { }
            }
        }
    }
    
    /**
     * Returns an <code>InputStream</code> to the specified file.
     * 
     * @param file the file to get the stream for.
     * @return an InputStream to the file, or null if the resource is 
     * not found.
     */
    public InputStream getResourceAsStream(String file) {
        return getClass().getResourceAsStream(resolvePathTo(file));
    }

    /**
     * Returns a <code>StreamSource</code> to the specified file.
     * 
     * @param file the file to get the stream for.
     * @return a StreamSource to the file.
     */
    public StreamSource getResourceAsSource(String file) {
        String fileString = resolvePathTo(file);
        /*String systemID = null;
        try {
            systemID = new File(fileString).toURL().toExternalForm( );
            
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        StreamSource source = new StreamSource(fileString);
        source.setSystemId(systemID);*/
        
        ClassLoader cl = ResourceUtil.class.getClassLoader();
        java.io.InputStream xslScriptStream = cl.getResourceAsStream(fileString);
        StreamSource source = new StreamSource(xslScriptStream);
        source.setSystemId(cl.getResource(fileString).toString());
        
        return source;
    }
    
    /**
     * Returns a <code>BufferedReader</code> to the specified file.
     * 
     * @param file the file to get the reader for.
     * @return a BufferedReader to the file.
     * @throws FileNotFoundException is thrown if the file is not found.
     */
    public BufferedReader getResourceAsReader(String file) throws FileNotFoundException {
        InputStream in = getResourceAsStream(file);
        return (in != null) ? (new BufferedReader(new InputStreamReader(in))) :
                              (new BufferedReader(new FileReader(resolvePathTo(file))));
    }
    
    /**
     * Returns the contents of the specified file as an array of bytes.
     * 
     * @param file the name of the file to read.
     * @return the contents of the specified file as an array of bytes.
     * @throws IOException is thrown if the file could not be read.
     */
    public static byte[] readFile(String file) throws IOException {
        ByteArray barray = new ByteArray();
        
        int num;
        byte[] buff = new byte[128];
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            
            while ((num = in.read(buff)) != -1) {
                barray.add(buff,num);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) { }
            }
        }
        
        byte[] data = barray.getBytes();
        barray.clear();
        
        return data;
    }
    
    /**
     * Saves an array of data to the specified file.
     * 
     * @param file the name of the file to save the data to.
     * @param data the array of bytes to save to the file.
     */
    public static void saveFile(String file, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) { }
            }
        }
    }
    
}
