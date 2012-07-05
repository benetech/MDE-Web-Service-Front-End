/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Properties;

/**
 * Abstract representation of the settings of an application.
 * 
 * @version 1.0
 */
public abstract class Settings {

    /** The cached properties of the application settings. */
    protected Properties properties = null;
    
    private String filename;

    private String description;
    
    private static final boolean LOCAL_DEBUG = false;
    
    /**
     * Default constructor not allowed.  Will always throw a RuntimeException.
     */
    protected Settings() {
        throw new RuntimeException("Default constructor not allowed");
    }

    /**
     * Creates an instance of <code>Settings</code> using the values from the
     * specified properties file.
     * 
     * @param filename name of the properties file to use the settings from.
     * @param description description to use for the settings.
     */
    protected Settings(String filename, String description) {
        this.filename = filename;
        this.description = description;
    }

    /**
     * Sets the default settings to the values given in the specified
     * properties object.
     * 
     * @param defaults the properties to use as the defaults.
     */
    protected abstract void setDefaults(Properties defaults);

    /**
     * Updates the applications settings from the cached properties.
     */
    protected abstract void updateSettingsFromCachedProperties();

    /**
     * Updated the cached properties from the application settings.
     */
    protected void updateCachedPropertiesFromSettings() {
        setDefaults(properties);
    }
    
    /**
     * Gets the cached properties.
     * 
     * @return the cached properties.
     */
    public Properties getCachedProperties() {
        return properties;
    }
    
    /**
     * Sets the cached properties to the specified properties object and
     * updates the applications settings from the new properties.
     * 
     * @param p the new chached properties.
     */
    public void setCachedProperties(Properties p) {
        if (p == null) {
            throw new NullPointerException("Null properties");
        }
        this.properties = p;
        updateSettingsFromCachedProperties();
    }
    
    /**
     * Load the application settings.
     */
    protected void loadSettings() {
        Properties defaults = new Properties();
        FileInputStream in = null;

        setDefaults(defaults);

        properties = new Properties(defaults);

        try {
            String folder = System.getProperty("user.home");
            in = new FileInputStream(folder + File.separator + filename);

            if (LOCAL_DEBUG) {
                System.out.println(getClass().getName() + ".loadSettings() Loading MDE settings from " +
                                   folder + File.separator + filename);
            }
            properties.load(in);
        } catch (FileNotFoundException fnfe) {
            in = null;
            if (LOCAL_DEBUG) {
                System.out.println("Can't find properties file. Using defaults.");
            }
        } catch (IOException ioe) {
            if (LOCAL_DEBUG) {
                System.out.println("Can't read properties file. Using defaults.");
            }
        } catch (Exception e) {
            System.out.println("Can't access properties file. " + "Using defaults.");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
                in = null;
            }
        }

        updateSettingsFromCachedProperties();
    }

    /**
     * Update the cached properties from the application settings then save
     * the cached properties.
     */
    public void save() {
        updateCachedPropertiesFromSettings();
        FileOutputStream out = null;
        try {
            String folder = System.getProperty("user.home");
            out = new FileOutputStream(folder + File.separator + filename);
            //properties.save(out, description);
            properties.store(out, description);
        } catch (IOException ioe) {
            if (LOCAL_DEBUG) {
                System.out.println("Can't store properties.");
            }
        } catch (AccessControlException ace) {
            if (LOCAL_DEBUG) {
                System.out.println("Can't access property file to save properties.");
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                }
                out = null;
            }
        }
    }

}
