/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Apr 5, 2004
 */
package gov.nasa.ial.mde.solver;

/**
 * The class represents the description of an interval.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class IntervalDescription {
    
    /** Flag indicating the state of the interval. */
    protected final static int DECREASES = -1, 
                               REMAINS_CONSTANT = 0, 
                               INCREASES = 1, 
                               UNDEFINED = 2;
    
    private final static double DEAD_BAND = 1.0e-3;
    
    /** The minimum interval length. */
    protected final static double MIN_INTERVAL_LENGTH = Math.pow(IntervalDescription.DEAD_BAND, 0.25);

    private int direction = IntervalDescription.REMAINS_CONSTANT;
    private IntervalEndpoint left, right;

    /**
     * Constructs an intereval description given the specified left and right
     * interval endpoints.
     * 
     * @param left the left interval endpoint.
     * @param right the right interval endpoint.
     */
    public IntervalDescription(IntervalEndpoint left, IntervalEndpoint right) {
        this.left = left;
        this.right = right;
        direction = getDirection(left, right);
    } // end IntervalDescription

    /**
     * Returns the direction of the interval.
     * 
     * @param l the left interval endpoint.
     * @param r the right interval endpoint.
     * @return the direction of the interval, which is one of DECREASES, 
     * REMAINS_CONSTANT, INCREASES, or UNDEFINED.
     */
    public static int getDirection(IntervalEndpoint l, IntervalEndpoint r) {
        double dydx = (r.leftYValue - l.rightYValue) / (r.xValue - l.xValue);

        if (Double.isNaN(dydx)) {
            if (r.leftYValue > l.rightYValue)
                return IntervalDescription.INCREASES;

            if (r.leftYValue < l.rightYValue)
                return IntervalDescription.DECREASES;

            return IntervalDescription.UNDEFINED;
        } // end if

        if (dydx > IntervalDescription.DEAD_BAND)
            return IntervalDescription.INCREASES;

        if (dydx < -IntervalDescription.DEAD_BAND)
            return IntervalDescription.DECREASES;

        return IntervalDescription.REMAINS_CONSTANT;
    } // end getDirection

    /**
     * Returns the MDE feature node for the interval.
     * 
     * @return the MDE feature node for the interval.
     */
    public MdeFeatureNode getMFN() {
        MdeFeatureNode r = new MdeFeatureNode();

        r.addKey("left");

        r.addValue("left", left.getMFN());

        r.addKey("right");
        r.addValue("right", right.getMFN());

        r.addKey("direction");

        switch (direction) {
        case IntervalDescription.INCREASES:
            r.addValue("direction", "increases");
            break;
        case IntervalDescription.DECREASES:
            r.addValue("direction", "decreases");
            break;

        case IntervalDescription.REMAINS_CONSTANT:
            r.addValue("direction", "remains constant");
            break;
        } // end switch

        return r;
    } // end getMFN

}
