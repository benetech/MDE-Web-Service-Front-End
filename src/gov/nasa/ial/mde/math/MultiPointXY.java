/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.util.MathUtil;

/**
 * A generalization of the <code>PointXY</code> class that includes the idea of
 * multiple (or no) <code>y</code> values corresponding to a given <code>x</code>.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MultiPointXY {
    
	/** The x value of this point. */
    public double x;
    
    /** Array of all the y values for this point. */
    public double[] yArray;

    // digits to display
    private int numDigits = 3;

    /** Default constructor not allowed. */
    @SuppressWarnings("unused")
	private MultiPointXY() {
    	throw new RuntimeException("Default constructor not allowed.");
    }
    
    /**
     * Creates an instance of <code>MultiPointXY</code> with the spceified
     * <code>x</code> value and the <code>yArray</code> field will default
     * to a zero length array.
     * 
     * @param x the x value for the point with no y value.
     */
    public MultiPointXY(double x) {
        this.x = x;
        this.yArray = new double[0];
    } // end MultiPointXY

    /**
     * Creates an instance of <code>MultiPointXY</code> with the spceified
     * <code>x</code> and <code>y</code> values.
     * 
     * @param x the x value for the point.
     * @param y the y value for the point, where the <code>yArray</code> field
     * 		will contain just this one y value.
     */
    public MultiPointXY(double x, double y) {
        this.x = x;
        this.yArray = new double[1];
        this.yArray[0] = y;
    }
    
    /**
     * Creates an instance of <code>MultiPointXY</code> with the spceified
     * <code>x</code> and <code>y</code> values.
     * 
     * @param x the x value for the point.
     * @param y the array of y values for the point, where the <code>yArray</code>
     * 		field will contain all the y values specified.
     */
    public MultiPointXY(double x, double[] y) {
        int n = y.length;
        this.x = x;
        this.yArray = new double[n];
        for (int i = 0; i < n; i++) {
            this.yArray[i] = y[i];
        }
    } // end MultiPointXY
    
    /**
     * This method is called to dispose of resources used by this.
     * <code>MultiPointXY</code>. The <code>MultiPointXY</code> will be
     * invalid and must not be used once this method has been called.
     */
    public void dispose() {
        yArray = null;
    }
    
    /**
     * Sets the number of digits to display in the <code>String</code>
     * representation of this <code>MultiPointXY</code> instance.
     * 
     * @param numDigits the number of digits to display.
     * @see #toString()
     */
    public void setDisplayDigits(int numDigits) {
        this.numDigits = numDigits;
    } // end setDisplayDigits
    
    /**
	 * Returns a string representation of this <code>MultiPointXY</code> object.
	 * 
	 * @return a string representation of this <code>MultiPointXY</code> object.
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        StringBuffer b = new StringBuffer(16);
        b.append("x = ").append(MathUtil.trimDouble(x, numDigits)).append("\ny values:");

        int n = yArray.length;

        if (n == 0) {
            b.append("  (none)");
        } else {
            b.append("\n");
        }
        
        for (int i = 0; i < n; i++) {
            b.append(MathUtil.trimDouble(yArray[i], numDigits));
            if (i < n - 1) {
                b.append(", ");
            }
        } // end for i

        return b.toString();
    } // end toString
    
} // end class MultiPointXY
