/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.util;

/**
 * The <code>Comparison</code> interface allows for objects to be compared.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface Comparison {
    
    /**
     * Compares two objects.
     * 
     * @param a the first of two objects to compare.
     * @param b the second of two objects to compare.
     * @return zero if <code>a == b</code>, a positive number if 
     * <code>a > b</code>, or a negative number if <code>a < b</code>.
     */
    public int compare(Object a, Object b);
    
} // end interface Comparison
