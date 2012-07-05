/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

/**
 * This class represents a Polar point with <code>double</code> <code>r</code>
 * and <code>theta</code> values.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PointRT {

    // just leave the fields public and forget the setters and accessors
    // it would be more convenient than having to call a method
    
	/** The r value (radius) of the Polar point. */
    public double r;
    
    /** The theta value (angle) in radians of the Polar point. */
    public double theta;

    /** Default constructor not allowed. */
    @SuppressWarnings("unused")
	private PointRT() {
    	throw new RuntimeException("Default constructor not allowed.");
    }
    
    /**
     * Creates an instance of <code>PointRT</code> with the specified
     * <code>r</code> and <code>theta</code> values.
     * 
     * @param r the radius
     * @param theta the angle in radians
     */
    public PointRT(double r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    /**
     * Returns a new <code>PointXY</code> instance representing this <code>PointRT</code>
     * but in Cartesian coordinates.
     * 
     * @return a new <code>PointXY</code> instance of this point in Cartesian coordinates.
     */
    public PointXY toCartesian() {
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return new PointXY(x, y);
    }

}
