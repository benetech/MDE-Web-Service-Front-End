/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.util.MathUtil;

/**
 * The <code>IntervalXY</code> class represents an interval between two points.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class IntervalXY {
    
    /** The value to exclude. */
    public final static int EXCLUDE_LOW_X = 1, 
                            EXCLUDE_HIGH_X = 2, 
                            EXCLUDE_LOW_Y = 4, 
                            EXCLUDE_HIGH_Y = 8;
    
    private String[] inEqSgns = { " <= ", " < " };
    private String varX = "x";
    private String varY = "y";
    private final static int X = 1;
    private final static int Y = 2;
    private int used = 0;
    private int excludeEndPoints = 0; // none excluded by default
    private PointXY[] endPoint = new PointXY[2];
    private int numDigits = 3;

    /**
     * Constructs an interval between the two specified points.
     * 
     * @param p1 first point of the interval.
     * @param p2 second point of the interval.
     */
    public IntervalXY(PointXY p1, PointXY p2) {
        setBothEndPoints(p1, p2);
    } // end IntervalXY

    /**
     * Constructs an interval between the two specified points with the given
     * variables in x and y.
     * 
     * @param vars the variables in X and Y.
     * @param p1 first point of the interval.
     * @param p2 second point of the interval.
     */
    public IntervalXY(String[] vars, PointXY p1, PointXY p2) {
        if (vars.length != 2) {
            throw new IllegalArgumentException("Must specify exactly two variables for new IntervalXY");
        }
        
        varX = vars[0];
        varY = vars[1];
        setBothEndPoints(p1, p2);
    } // end IntervalXY

    /**
     * Constructs an interval between the two specified end points in X and the
     * given x variable.
     * 
     * @param var the variable on X.
     * @param val1 first endpoint x value.
     * @param val2 second endpoint x value.
     */
    public IntervalXY(String var, double val1, double val2) {
        // to specify a domain, e.g., input "x", -5, 5
        // and y values will be set to 0.

        if (val1 > val2) {
            barf(var);
        }

        // How about we use the variable the user gives us?
        // and make "varX" equal to this variable
        endPoint[0] = new PointXY(val1, 0.0);
        endPoint[1] = new PointXY(val2, 0.0);
        varX = var; // Strings are immuatable so don't need to create a new one
        used |= X;
    } // end IntervalXY

    /**
     * Sets the end point to exclude.
     * 
     * @param e end point to exclude which is one of <code>EXCLUDE_LOW_X,
     * EXCLUDE_HIGH_X, EXCLUDE_LOW_Y, or EXCLUDE_HIGH_Y</code>.
     */
    public void setEndPointExclusions(int e) {
        excludeEndPoints = e;

        if (endPoint[0].x == endPoint[1].x) {
            if ((excludeEndPoints & (EXCLUDE_LOW_X | EXCLUDE_HIGH_X)) != 0) {
                throw new IllegalArgumentException(
                        "Operation results in empty interval for\nvariable " + varX);
            }
        }

        if (endPoint[0].y == endPoint[1].y) {
            if ((excludeEndPoints & (EXCLUDE_LOW_Y | EXCLUDE_HIGH_Y)) != 0) {
                throw new IllegalArgumentException(
                        "Operation results in empty interval for\nvariable " + varY);
            }
        }
    } // end setEndPointExclusions

    /**
     * Returns the low x value.
     * 
     * @return the low x value.
     */
    public double getLowX() {
        return endPoint[0].x;
    } // end getLowX

    /**
     * Returns the high x value.
     * 
     * @return the high x value.
     */
    public double getHighX() {
        return endPoint[1].x;
    } // end getHighX

    /**
     * Returns the low y value.
     * 
     * @return the low y value.
     */
    public double getLowY() {
        return endPoint[0].y;
    } // end getLowY

    /**
     * Returns the high y value.
     * 
     * @return the high y value.
     */
    public double getHighY() {
        return endPoint[1].y;
    } // end getHighY

    
    /**
     * Returns the a formated string of the low x value.
     * 
     * @return a formated string of the low x value.
     */
    public String strLowX() {
        return MathUtil.trimDouble(getLowX(), numDigits);
    } // end strLowX

    /**
     * Returns the a formated string of the low y value.
     * 
     * @return a formated string of the low y value.
     */
    public String strLowY() {
        return MathUtil.trimDouble(getLowY(), numDigits);
    } // end strLowY

    /**
     * Returns the a formated string of the high x value.
     * 
     * @return a formated string of the high x value.
     */
    public String strHighX() {
        return MathUtil.trimDouble(getHighX(), numDigits);
    } // end strHighX

    /**
     * Returns the a formated string of the high y value.
     * 
     * @return a formated string of the high y value.
     */
    public String strHighY() {
        return MathUtil.trimDouble(getHighY(), numDigits);
    } // end strHighY

    /**
     * Returns a string representation of the interval.
     * 
     * @return a string representation of the interval.
     */
    public String toString() {
        StringBuffer r = new StringBuffer();
        String string;
        
        if(!MdeSettings.ACCESSIBLE_TTS){
        	if ((used & X) != 0) {
                r.append("{" + varX + " such that " + strLowX() + 
                        GS(0, 0) + varX + GS(0, 1) + strHighX() + "}");
            }
            if ((used & Y) != 0) {
                if (r.length() > 0) {
                    r.append(" and\n");
                }
                r.append("{" + varY + " such that " + strLowY() + 
                        GS(1, 0) + varY + GS(1, 1) + strHighY() + "}");
            } // end if
          
        }else{
        	if ((used & X) != 0) {
        		r.append(varX+ " from " + strLowX() + " to " + strHighX() + "");
            }
            if ((used & Y) != 0) {
                if (r.length() > 0) {
                    r.append(" and\n");
                }
                r.append(varY+ " from " + strLowY() + " to " + strHighY() + "");
            } // end if
        	
        }

        string = r.toString();
        return string;
    } // end toString

    private String GS(int whichVar, int whichPoint) {
        int mask = (1 << (whichPoint + (whichVar << 1)));

        if ((excludeEndPoints & mask) != 0) {
            return inEqSgns[1];
        }

        return inEqSgns[0];
    } // end GS

    private void barf(String offendingVariable) {
        throw new IllegalArgumentException("Low limit for\nvariable " + 
                offendingVariable + " is greater than its upper limit");
    } // end barf

    private void setBothEndPoints(PointXY p1, PointXY p2) {
        if (p1.x > p2.x) {
            barf(varX);
        }
        if (p1.y > p2.y) {
            barf(varY);
        }
        
        endPoint[0] = p1;
        endPoint[1] = p2;
        used = (X | Y);
    } // end setBothEndPoints

    // Main routine for test purposes
    public static void main(String[] args) {
        IntervalXY i1 = new IntervalXY(new PointXY(-1, -2), new PointXY(3, 4));
        IntervalXY i2 = new IntervalXY("x", -2, Double.POSITIVE_INFINITY);
        IntervalXY i3 = new IntervalXY("u", Double.NEGATIVE_INFINITY, -2);
        String[] rt = { "r", "theta" };

        System.out.println(i1);
        i1.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_LOW_Y);
        System.out.println(i1);

        System.out.println(i2);
        System.out.println(i3);

        i1 = new IntervalXY(rt, new PointXY(0.0, 0.0), new PointXY(1.0, 2.0 * Math.PI));
        System.out.println(i1);
        i2 = new IntervalXY("t", 1, -2);
        System.out.println(i2);
   } // end main

} // end class IntervalXY
