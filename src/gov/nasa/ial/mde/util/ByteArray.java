/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Apr 19, 2004
 */
package gov.nasa.ial.mde.util;

/**
 * The <code>ByteArray</code> class represents a variable length array of bytes
 * that automatically grows in capacity as needed to hold the added data.
 *  
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class ByteArray {

    private int    size = 0;

    private byte[] data = new byte[0];

    /**
     * Default constructor.
     */
    public ByteArray() {
        super();
    }

    /**
     * Adds the specified byte to the array.
     * @param b the byte to add to the array.
     */
    public void add(byte b) {
        ensureCapacity(size + 1);
        data[size++] = b;
    }

    /**
     * Adds the specified array of bytes to the array.
     * @param b array of bytes to add to the array.
     */
    public void add(byte[] b) {
        add(b, b.length);
    }

    /**
     * Adds the length number of bytes from the specified array to the byte-array.
     * @param b array of bytes to add to the array.
     * @param length the number of bytes to add.
     */
    public void add(byte[] b, int length) {
        ensureCapacity(size + length);
        System.arraycopy(b, 0, data, size, length);
        size += length;
    }

    /**
     * Ensures the array has at least the specified capacity.
     * @param capacity the desired capacity of the array.
     */
    public void ensureCapacity(int capacity) {
        if (capacity >= data.length) {
            int newCapacity = 2 * data.length + 1;
            if (capacity >= newCapacity) {
                newCapacity = capacity + 1;
            }
            byte[] newData = new byte[newCapacity];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    /**
     * Clears the array of all data and the size will be zero.
     */
    public void clear() {
        size = 0;
        data = new byte[0];
    }

    /**
     * Returns the bytes contained in the varaiable as a fixed size array.
     * @return a fixed size array of the data bytes in the array.
     */
    public byte[] getBytes() {
        byte[] barray = new byte[size];
        System.arraycopy(data, 0, barray, 0, size);
        return barray;
    }

}
