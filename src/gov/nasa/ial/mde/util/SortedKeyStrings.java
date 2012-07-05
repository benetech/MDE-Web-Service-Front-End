/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The <code>SortedKeyStrings</code> class creates a sorted array of string
 * keys from a given <code>Hashtable</code>.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SortedKeyStrings {
    
    /** Comparison of strings. */
    private static final Comparison compareStrings = new Comparison() {
        public int compare(Object o1, Object o2) {
            return ((String)o1).compareTo((String)o2);
        } // end compare
    }; // end compareStrings

    /** The sorted string keys. */
    public String[] theKeys;

    /**
     * Constructs a <code>SortedKeyStrings</code> class using the specified
     * Hashtable and default string comparison.
     * 
     * @param ht the Hashtable to sort the string keys of.
     */
    public SortedKeyStrings(Hashtable ht) {
        finish(ht, compareStrings);
    } // end SortedKeyStrings

    /**
     * Constructs a <code>SortedKeyStrings</code> class using the specified
     * Hashtable and Comparison class.
     * 
     * @param ht the Hashtable to sort the string keys of.
     * @param c the Comparison class to sort the keys by.
     */
    public SortedKeyStrings(Hashtable ht, Comparison c) {
        finish(ht, c);
    } // end SortedKeyStrings

    private void finish(Hashtable ht, Comparison c) {
        int i, n;
        Vector<Object> v = new Vector<Object>();
        Enumeration k = ht.keys();

        while (k.hasMoreElements()) {
            Object temp = k.nextElement();
            if (temp instanceof String) {
                v.addElement(temp);
            }
        } // end while

        theKeys = new String[n = v.size()];
        for (i = 0; i < n; i++) {
            theKeys[i] = (String)v.elementAt(i);
        }
        
        QSorter q = new QSorter(theKeys, c);
        for (i = 0; i < n; i++) {
            theKeys[i] = (String)q.theData[i];
        }
    } // end finish
    
} // end class SortedKeyStrings
