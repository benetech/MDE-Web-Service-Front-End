/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The <code>StringSplitter</code> class splits a String into an array of
 * Strings based on the splitting string.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class StringSplitter {
    
    /** The pieces of the string. */
    public String        pieces[];

    /** The Comparison class to use to sort the string pieces. */
    protected Comparison theComparer = null;

    /**
     * Constructs a <code>StringSplitter</code> class with null <code>pieces</code>
     * and the specified comparison class.
     * 
     * @param c the comparision class for sorting the strings.
     */
    public StringSplitter(Comparison c) {
        pieces = null;
        theComparer = c;
    } // end Stringsplitter (trivial case)

    /**
     * Constructs a <code>StringSplitter</code> class with the specified string
     * to split and the string used for splitting.
     * 
     * @param splitString the string used for splitting.
     * @param stringToSplit the string to split.
     */
    public StringSplitter(String splitString, String stringToSplit) {
        int i, l = splitString.length(), n = stringToSplit.length();

        if ((i = stringToSplit.indexOf(splitString)) >= 0) {
            char leftChars[] = new char[i];
            char rightChars[] = new char[n - i - l];
            stringToSplit.getChars(0, i, leftChars, 0);
            stringToSplit.getChars(i + l, n, rightChars, 0);
            pieces = new String[3];
            pieces[0] = new String(leftChars);
            pieces[1] = splitString; // Strings are immultable, was: new String(splitString);
            pieces[2] = new String(rightChars);
        } // end if
        else {
            pieces = new String[1];
            pieces[0] = splitString; // Strings are immutable, was: new String(splitString);
        } // end else
    } // end StringSplitter

    /**
     * Splits the specified enumeration of strings.
     * 
     * @param stringList the list of splitting strings.
     * @param stringToSplit the strin to split.
     * @return the array of split strings.
     */
    public String[] multiSplit(Enumeration<String> stringList, String stringToSplit) {
        int i, j, k, l;
        Vector<StringSplitter> vSplits = new Vector<StringSplitter>();

        while (stringList.hasMoreElements()) {
            String s = (String) stringList.nextElement();
            StringSplitter t = new StringSplitter(s, stringToSplit);

            switch (t.pieces.length) {
            case 3:
                vSplits.addElement(t);
                break;

            case 1:
                break;

            default:
                System.err.println("" + new Exception());
            } // end switch
        } // end while

        StringSplitter[] splits = new StringSplitter[vSplits.size()];
        Enumeration<StringSplitter> e = vSplits.elements();
        for (i = 0; e.hasMoreElements();) {
            splits[i++] = (StringSplitter) e.nextElement();
        }

        QSorter temp = new QSorter(splits, theComparer);

        for (i = 0; i < splits.length; i++) {
            splits[i] = (StringSplitter) temp.theData[i];
        }

        String[] r = new String[(splits.length << 1) + 1];
        r[0] = stringToSplit; // Strings are immuatable so use it, was: new String(stringToSplit);

        for (i = 0, l = 1; i < splits.length; i++)
            for (j = 0; j < l; j += 2) {
                StringSplitter t = new StringSplitter(splits[i].pieces[1], r[j]);
                if (t.pieces.length != 3)
                    continue;

                for (k = l + 1; k > j + 2; k--) {
                    r[k] = r[k - 2];
                }
                for (k = 0; k < 3; k++) {
                    r[j + k] = t.pieces[k];
                }
                l += 2;
                break;
            } // end for j

        String[] theAnswer = new String[l];
        for (i = 0; i < l; i++) {
            theAnswer[i] = r[i];
        }

        return theAnswer;
    } // end multiSplit
    
    /**
     * Combines the array of strings into one String.
     * 
     * @param theStrings the strings to combine.
     * @return the combined string.
     */
    public static String combineArgs(String[] theStrings) {
        int i, n = theStrings.length;
        StringBuffer b = new StringBuffer();

        for (i = 0; i < n; i++) {
            b.append(theStrings[i]);
        }

        return b.toString();
    } // end combineArgs
    
} // end class StringSplitter
