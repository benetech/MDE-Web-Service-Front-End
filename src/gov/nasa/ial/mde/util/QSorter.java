/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.util;

/**
 * A quick sort class.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class QSorter {
    
    /** The array sorted data. */
    public Object theData[];
    
    private Comparison theComparer;

    /**
     * Constructs a quick sorter class.
     * 
     * @param data the array of data to sort.
     * @param c the comparison class used for sorting the data.
     */
    public QSorter(Object[] data, Comparison c) {
        int n = data.length - 1;

        theData = new Object[n + 1];
        for (int i = 0; i <= n; i++) {
            theData[i] = data[i];
        }
        theComparer = c;
        quickSort(0, n);
    } // end QSorter

    /**
     * Do the quick sort given the initial low and hi indexes to sort the 
     * data array over.
     * 
     * @param lo0 initial low starting index.
     * @param hi0 initial high starting index.
     */
    protected void quickSort(int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        Object mid;

        if (hi0 > lo0) {

            mid = theData[(lo0 + hi0) >> 1];

            // loop through the array until indices cross
            while (lo <= hi) {
                // find the first element that is greater than or equal to the 
                // partition element starting from the left Index.
                while ((lo < hi0) && (theComparer.compare(theData[lo], mid) < 0)) {
                    ++lo;
                }

                // find an element that is smaller than or equal to the 
                // partition element starting from the right Index.
                while ((hi > lo0) && (theComparer.compare(theData[hi], mid) > 0)) {
                    --hi;
                }

                // if the indexes have not crossed, swap
                if (lo <= hi) {
                    Object temp = theData[lo];
                    theData[lo] = theData[hi];
                    theData[hi] = temp;
                    ++lo;
                    --hi;
                } // end if
            } // end while

            // If the right index has not reached the left side of array must 
            // now sort the left partition.
            if (lo0 < hi) {
                quickSort(lo0, hi);
            }

            // If the left index has not reached the right side of array must 
            // now sort the right partition.
            if (lo < hi0) {
                quickSort(lo, hi0);
            }
        } // end if
    } // end QuickSort
    
} // end class QSorter
