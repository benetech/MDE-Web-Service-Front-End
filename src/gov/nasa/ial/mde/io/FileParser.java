/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Apr 23, 2004
 */
package gov.nasa.ial.mde.io;

import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * An interface representing a file parser.
 *
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public interface FileParser {
    
    /**
     * Parse the file and return a <code>List</code> of <code>AnalyzedItem</code>'s.
     *
     * @return a <code>List</code> of <code>AnalyzedItem</code>'s
     * @throws IOException is thrown for file Input/Output errors
     * @throws ParseException is thrown for parse errors
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem
     */
    public List<? extends AnalyzedItem> parse() throws IOException, ParseException;
    
    /**
     * Clear the file parser so that the parse() method can be called again if needed.
     */
    public void clear();
    
    /**
     * Dispose of resources being used. The file parser will be invalid after this method is called
     * and must not be reused, so a new file parser must be instantiated.
     */
    public void dispose();
    
    /**
     * The <code>UNKNOWN_DELIM</code> constant represents an unknown delimiter.
     */
    public static final int UNKNOWN_DELIM = 0;
	
    /**
     * The <code>COMMA_DELIM</code> constant represents a comma character delimiter.
     */
    public static final int COMMA_DELIM = 1;
	
    /**
     * The <code>TAB_DELIM</code> constant represents a tab character delimiter.
     */
    public static final int TAB_DELIM = 2;
	
    /**
     * The <code>NEWLINE_DELIM</code> constant represents a new-line character delimiter.
     */
    public static final int NEWLINE_DELIM = 4;
	
    /**
     * The <code>VERTICAL_TAB_DELIM</code> constant represents a vertical-tab character delimiter.
     */
    public static final int VERTICAL_TAB_DELIM = 8;
	
    /**
     * The <code>FORM_FEED_DELIM</code> constant represents a form-feed character delimiter.
     */
    public static final int FORM_FEED_DELIM = 16;
	
    /**
     * The <code>CARRIAGE_RETURN_DELIM</code> constant represents a carriage-return character delimiter.
     */
    public static final int CARRIAGE_RETURN_DELIM = 32;
	
    /**
     * The <code>SPACE_DELIM</code> constant represents a space character delimiter.
     */
    public static final int SPACE_DELIM = 64;
    
    /**
     * The <code>VERTICAL_TAB</code> constant represents a vertical-tab character.
     */
    public static final char VERTICAL_TAB = 0x0B;
    
}
