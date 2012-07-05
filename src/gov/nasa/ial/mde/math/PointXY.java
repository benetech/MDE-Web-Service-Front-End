/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.solver.MdeFeatureNode;
import gov.nasa.ial.mde.util.MathUtil;

/**
 * This class represents a point with <code>double</code> <code>x</code>
 * and <code>y</code> values.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PointXY {
    
	/** The x value of the point. */
    public double x;
    
    /** The y value of the point. */
    public double y;

    // digits to display and left/right delimiters for ordered pair notation
    private int numDigits = 3;
    private MdeNumberFormat mnf = null;
    private String leftDelim = "(";
    private String rightDelim = ")";

    /** Default constructor not allowed. */
    @SuppressWarnings("unused")
	private PointXY() {
    	throw new RuntimeException("Default constructor not allowed.");
    }
    
    /**
     * Creates an instance of <code>PointXY</code> with the <code>x</code>
     * and <code>y</code> point values specified by the <code>xy</code> array.
     * <p>
     * The <code>x</code> value is specified at array index 0.
     * <p>
     * The <code>y</code> value is specified at array index 1.
     * 
     * @param xy the x and y point values specified in an array.
     * @exception IllegalArgumentException is thrown if the <code>xy</code> array
     * 		length is not excatly 2.
     */
    public PointXY(double[] xy) {
    	this(xy[0],xy[1]);
    	
        if (xy.length != 2) {
            throw new IllegalArgumentException("The xy array length must be exactly 2.");
        }
    } // end PointXY

    /**
     * Creates an instance of <code>PointXY</code> with the <code>x</code>
     * and <code>y</code> point values specified.
     * 
     * @param x the x value of the point.
     * @param y the y value of the point.
     */
    public PointXY(double x, double y) {
        this.x = x;
        this.y = y;
        setDisplayDigits();
    }

    /**
     * Translates this point by the specified delta's in x and y.
     * 
     * @param deltax the value to add to this points x value.
     * @param deltay the value to add to this points y value.
     */
    public void translate(double deltax, double deltay) {
        x += deltax;
        y += deltay;
    }

    /**
     * Returns a new <code>PointXY</code> instance representing the
     * translation of this point by the specified delta's in x and y.
     * 
     * @param deltax the value to add to this points x value.
     * @param deltay the value to add to this points y value.
     * @return a new point representing the translation of this point
     * 		by the specified delta's in x and y.
     */
    public PointXY newTranslatedPoint(double deltax, double deltay) {
        return new PointXY(x + deltax, y + deltay);
    }

    /**
     * Returns a new <code>PointXY</code> instance representing the sum
     * of this point and the specified point.
     * 
     * @param p the point to add to the values of this point.
     * @return a new <code>PointXY</code> instance representing the sum
     * of this point and the specified point.
     */
    public PointXY sum(PointXY p) {
        return newTranslatedPoint(p.x, p.y);
    } // end sum

    /**
     * Returns a new <code>PointXY</code> instance representing the difference
     * between this point and the specified point.
     * 
     * @param p the point to subtract the values from this point.
     * @return a new <code>PointXY</code> instance representing the difference
     * between this point and the specified point.
     */
    public PointXY difference(PointXY p) {
        return newTranslatedPoint(-p.x, -p.y);
    } // end difference

    /**
     * Returns a new <code>PointRT</code> instance representing this <code>PointXY</code>
     * in Polar coordinates.
     * 
     * @return a new <code>PointRT</code> instance of this point in Polar coordinates.
     */
    public PointRT toPolar() {
        double r = Math.sqrt(x * x + y * y);
        // double theta = Math.atan(y/x);
        // use four-quadrant atan function to handle angles in 2nd and 4th
        // quads
        double theta = Math.atan2(y, x);

        return new PointRT(r, theta);
    } // end toPolar

    // various pretty print methods
    // e.g., that print values with specified number of digits,
    // printing with (,{,[,),},}, whatever

    // How about one really good one?

    /**
	 * Returns a string representation of this <code>PointXY</code> object using
	 * the <code>[</code> character as the left delimiter and the <code>]</code>
	 * character as the right delimiter.
	 * 
	 * @return a string representation of this <code>PointXY</code> object.
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        StringBuffer strBuff = new StringBuffer(20);
        strBuff.append(leftDelim).append(MathUtil.trimDouble(x, numDigits))
               .append(", ").append(MathUtil.trimDouble(y, numDigits))
               .append(rightDelim);
        return strBuff.toString();
    } // end toString

    /**
     * Returns the formatted string representation of the <code>x</code>
     * value of this point.
     * 
     * @return the formatted string representation of the <code>x</code>
     * 		value of this point.
     */
    public String getXString() {
        return getNumberFormat().format(x);
    } // end getXString

    /**
     * Returns the formatted string representation of the <code>y</code>
     * value of this point.
     * 
     * @return the formatted string representation of the <code>y</code>
     * 		value of this point.
     */
    public String getYString() {
        return getNumberFormat().format(y);
    } // end getYString

    /**
     * Set the number of digits to display for the formatted <code>x</code>
     * and <code>y</code> point values.
     * 
     * @param numDigits the number of digits to format the <code>x</code>
     * 		and <code>y</code> point values for display.
     */
    public void setDisplayDigits(int numDigits) {
        this.numDigits = numDigits;
        setDisplayDigits();
    } // end setDisplayDigits

    private void setDisplayDigits() {
        getNumberFormat().setMinimumFractionDigits(0);
        getNumberFormat().setMaximumFractionDigits(numDigits);
    } // end setDisplayDigits

    /**
     * Sets the left and right delimiters used for the string representation
     * of this string returned by the <code>toString()</code> method.
     * 
     * @param ld the left delimiter
     * @param rd the right delimiter
     * @see #toString()
     */
    public void setDelimiters(String ld, String rd) {
        this.leftDelim = ld;
        this.rightDelim = rd;
    } // end setDelimiters

    /**
     * Returns the MDE Feature Node for this point.
     * 
     * @return the MDE Feature Node for this point.
     */
    public MdeFeatureNode getMFN() {
        MdeFeatureNode t = new MdeFeatureNode();
        t.addKey("X");
        t.addValue("X", getXString());

        t.addKey("Y");
        t.addValue("Y", getYString());
        return t;
    } // end getMFN
    
    /**
     * This method is called to dispose of resources used by this.
     * <code>PointXY</code>. The <code>PointXY</code> will be
     * invalid and must not be used once this method has been called.
     */
    public void dispose() {
        mnf = null;
    }
    
    private MdeNumberFormat getNumberFormat() {
        // We use delayed instantiation.
        if (mnf == null) {
            mnf = MdeNumberFormat.getInstance();
        }
        return mnf;
    }
    
} // end class PointXY
