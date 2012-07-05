/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A symbolic quantity.
 * 
 * @author ddexter
 * @version 1.0
 * @since 1.0
 */
public class Quantity {
    
    /** The children of the quantitiy. */
    Object[] children;

    /**
     * Constructs a Quantity given specifed value as a string.
     * 
     * @param s quantity value as a string.
     */
    public Quantity(String s) {
        int c = 0, i, n = s.length();
        byte[] byteArray = s.getBytes();
        Vector<Integer> subStarts = new Vector<Integer>(), subEnds = new Vector<Integer>();

        subStarts.addElement(new Integer(0));

        for (i = 0; i < n; i++) {
            if (c < 0) {
                children = null;
                return;
            } // end if
            switch (byteArray[i]) {
                case '(' :
                    if (c++ == 0) {
                        subEnds.addElement(new Integer(i));
                        subStarts.addElement(new Integer(i + 1));
                    } // end if
                    break;

                case ')' :
                    if (--c == 0) {
                        subEnds.addElement(new Integer(i));
                        subStarts.addElement(new Integer(i + 1));
                    } // end if
                    break;

                default :
                    break;
            } // end switch
        } // end for
        subEnds.addElement(new Integer(n));

        if (c > 0) {
            children = null;
            return;
        } // end if
        if ((n = subStarts.size()) != subEnds.size()) {
            children = null;
            return;
        } // end if

        if ((n & 1) == 0) {
            children = null;
            return;
        } // end if

        Enumeration<Integer> se = subStarts.elements();
        Enumeration<Integer> ee = subEnds.elements();
        children = new Object[n];

        for (i = 0;;) {
            int st = se.nextElement().intValue();
            int e = ee.nextElement().intValue();

            children[i++] = new String(byteArray, st, e - st);
            if (i >= n)
                break;

            st = se.nextElement().intValue();
            e = ee.nextElement().intValue();
            children[i] = new Quantity(new String(byteArray, st, e - st));
            if (((Quantity)children[i++]).children == null) {
                children = null;
                return;
            } // end if
        } // end for i
    } // end Quantity

    /**
     * Returns the string representation of the quantity.
     * 
     * @return the string representation of the quantity.
     */
    public String toString() {
        String r = (String)children[0];

        for (int i = 1; i < children.length; i++)
            if (children[i] instanceof Quantity)
                r = r + "[" + ((Quantity)children[i]).toString() + "]";
            else if (children[i] instanceof String)
                r = r + (String)children[i];
            else
                System.err.println("Internal error:  Child of unknown type.");

        return r;
    } // end toString
    
} // end class Quantity
