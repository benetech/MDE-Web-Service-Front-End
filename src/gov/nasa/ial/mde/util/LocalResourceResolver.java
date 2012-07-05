/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on 9/12/2003
 */
package gov.nasa.ial.mde.util;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

/**
 * This resolver will load the given URI from the class path, which the
 * file could be in either a Jar file or in a directory.
 *
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class LocalResourceResolver extends ResourceUtil implements URIResolver {
    
    /**
     * Default constructor.
     */
    public LocalResourceResolver() {
        this(null);
    }
    
    /**
     * Constructs a local resource resolver with the specified path to the
     * resources.
     * 
     * @param pathToResources the path to the resources.
     */
    public LocalResourceResolver(String pathToResources) {
        super(pathToResources);
    }
    
    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    public Source resolve(String href, String base) {
        return getResourceAsSource(href);
    }
    
} // end class LocalResourceResolver
