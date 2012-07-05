/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on June 22, 2005
 */
package gov.nasa.ial.mde.util;

/**
 * The <code>ArrayUtil</code> class is a utility class for arrays.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class ArrayUtil {

    /**
     * Determines the index to the first entry in the array that matches the
     * specified value.
     * 
     * @param value
     *            the value to search for a match.
     * @param array
     *            the array of strings to search.
     * @return the index in the array that matches the specified value or -1 if
     *         no match was found.
     */
    public static int indexOfFirstMatch(String value, String[] array) {
        if (array == null) {
            return -1;
        }
        int len = array.length;
        for (int i = 0; i < len; i++) {
            if (value.equalsIgnoreCase(array[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determines the index to the first entry in the array that matches the
     * specified value.
     * 
     * @param value
     *            the value to search for a match.
     * @param array
     *            the array of integers to search.
     * @return the index in the array that matches the specified value or -1 if
     *         no match was found.
     */
    public static int indexOfFirstMatch(int value, int[] array) {
        if (array == null) {
            return -1;
        }
        int len = array.length;
        for (int i = 0; i < len; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determine if the code is valid given the array of valid flag bit values
     * 
     * @param code
     *            the code to validate.
     * @param maskFlagBitValues
     *            the mask bit values.
     * @return true if the code is valid, false otherwise.
     */
    public static boolean isCodeValid(int code, int[] maskFlagBitValues) {
        if (maskFlagBitValues == null) {
            return false;
        }
        int len = maskFlagBitValues.length;

        // Create a mask with all the specified flag bit values set.
        int allMaskBits = 0;
        for (int i = 0; i < len; i++) {
            allMaskBits |= maskFlagBitValues[i];
        }

        // The mask is valid if no other bits are set outside of the valid
        // values specified.
        return ((code & ~allMaskBits) == 0);
    }

}
