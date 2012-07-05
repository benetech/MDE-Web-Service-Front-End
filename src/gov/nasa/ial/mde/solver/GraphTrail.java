/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.PointXY;

/**
 * Class for holding a "trail" of PointXY's. A "trail: can be thought of as a
 * curve which can be drawn without raising pen from paper. This is important
 * because each trail corresponds to a chain of Graphics.drawLine commands that
 * are sequenced from point i to i+1. Note that we need at least two points to
 * comprise a GraphTrail.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class GraphTrail {
    
    /**
     * The points along the graph-trail.
     */
    protected PointXY[] points;

    /** Default constructor not allowed. */
    @SuppressWarnings("unused")
	private GraphTrail() {
        throw new RuntimeException("Default constructor not allowed.");
    } // end GraphTrail
    
    /**
     * Constructs a <code>GraphTrail</code> for the given array of points.
     * 
     * @param doubleArray the double array of points.
     */
    public GraphTrail(double[][] doubleArray) {
        int length = doubleArray.length;
        if (length < 1) {
            throw new IllegalArgumentException("Must have at least one point to make a GraphTrail");
        }

        points = new PointXY[length];
        for (int i = 0; i < length; i++) {
            points[i] = new PointXY(doubleArray[i][0], doubleArray[i][1]);
        }
    } // end GraphTrail
    
    /**
     * Constructs a <code>GraphTrail</code> for the given array of <code>PointXY</code>.
     * 
     * @param p the points along the graph trail.
     */
    public GraphTrail(PointXY[] p) {
        setPoints(p);
    } // end GraphTrail
    
    /**
     * Sets the <code>PointXY</code> points to use with this <code>GraphTrail</code>.
     * @param p the <code>PointXY</code> points to use with this <code>GraphTrail</code>.
     */
    public void setPoints(PointXY[] p) {
        if (p == null) {
            throw new NullPointerException("Null points");
        }
        this.points = p;
    } // end setPoints

    /**
     * Get a copy of the <code>GraphTrail</code> points.
     * @return a copy of the <code>GraphTrail</code> <code>PointXY</code> points.
     */
    public PointXY[] getPointsCopy() {
        int length = points.length;
        PointXY[] p = new PointXY[length];
        for (int i = 0; i < length; i++) {
            p[i] = new PointXY(points[i].x, points[i].y);
        }
        return p;
    } // end getPointsCopy

    /**
     * The length of the <code>GraphTrail</code>.
     * @return the length of the <code>GraphTrail</code>.
     */
    public int getLength() {
        return points.length;
    }

    /**
     * Returns a reference to the <code>GraphTrail</code> points object.
     * 
     * @return a reference to the <code>GraphTrail</code> points object.
     */
    public PointXY[] getPoints() {
        return points;
    }

    /**
     * Finds the left side of the <code>GraphTrail</code>.
     * 
     * @return the left side of the <code>GraphTrail</code>.
     */
    public double findLeft() {
        int length = points.length;
        double l = Double.POSITIVE_INFINITY;
        for (int i = 0; i < length; i++) {
            if (points[i].x < l) {
                l = points[i].x;
            }
        }
        return l;
    } // end findLeft

    /**
     * Finds the right side of the <code>GraphTrail</code>.
     * 
     * @return the right side of the <code>GraphTrail</code>.
     */
    public double findRight() {
        int length = points.length;
        double r = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < length; i++) {
            if (points[i].x > r) {
                r = points[i].x;
            }
        }
        return r;
    } // end find left

    /**
     * Dispose of any resources used.
     */
    public void dispose() {
        if (points != null) {
            int len = points.length;
            for (int i = 0; i < len; i++) {
                if (points[i] != null) {
                    points[i].dispose();
                    points[i] = null;
                }
            }
            points = null;
        }
    }
    
    /**
     * Returns a string representation of the <code>GraphTrail</code>.
     * 
     * @return a string representation of the <code>GraphTrail</code>.
     */
    public String toString() {
        int length = points.length;
        StringBuffer strBuff = new StringBuffer(64);
        strBuff.append(getClass().getName()).append("[");
        strBuff.append("Length: ").append(length).append("\n");
        for (int i = 0; i < length; i++) {
            strBuff.append(points[i].toString()).append("\n");
        }
        strBuff.append("]");
        return strBuff.toString();
    }
    
} // end class GraphTrail
