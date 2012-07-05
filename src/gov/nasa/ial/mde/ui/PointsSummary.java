/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.ui;

import gov.nasa.ial.mde.math.MdeNumberFormat;
import gov.nasa.ial.mde.math.MultiPointXY;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Used to generate the points summary at the location of sonification.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class PointsSummary {

    private double x;
    private TreeSet<Double> y = new TreeSet<Double>();

    private MdeNumberFormat mdeNumberFormat = MdeNumberFormat.getInstance();

    // reuse the string buffer for generating the summary
    private StringBuffer strBuff = new StringBuffer(40);

    /**
     * Default constructor.
     */
    public PointsSummary() {
        this(3);
    }

    /**
     * Constructs a points summary that will use the specified number of
     * digits for the numbers.
     * 
     * @param numDigits number of digits in number to display.
     */
    public PointsSummary(int numDigits) {
        mdeNumberFormat.setMinimumFractionDigits(0);
        mdeNumberFormat.setMaximumFractionDigits(numDigits);
    }

    /**
     * Returns the number of points in the summary.
     * 
     * @return the number of points in the summary.
     */
    public int size() {
        return y.size();
    }

    /**
     * Determines if there are no points in the summary.
     * 
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return y.isEmpty();
    }

    /**
     * Clears the points from the summary.
     */
    public void clear() {
        y.clear();
    }

    /**
     * Sets the x coordinate value.
     * 
     * @param x the x coordinate value.
     */
    public void setX(double x) {
        this.x = x;
    }

    // There can only be one X for a Cartesian graph.
    /**
     * Returns the x coordinate value, which there can only be one value
     * for a Cartesian graph.
     * 
     * @return the x coordinate value.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the first y coordinate value.
     * 
     * @return the first y coordinate value.
     */
    public double getFirstY() {
        Double d = y.first();
        return d.doubleValue();
    }

    /**
     * Add a y coordinate value to the summary.
     * 
     * @param value the y coordinate value to the summary.
     */
    public void addY(double value) {
        y.add(new Double(value));
    }

    /**
     * Adds the array of y coordinate values to the summary.
     * 
     * @param yArray the array of y coordinate values to the summary.
     */
    public void addY(double[] yArray) {
        if (yArray != null) {
            int len = yArray.length;
            for (int i = 0; i < len; i++) {
                y.add(new Double(yArray[i]));
            }
        }
    }

    /**
     * Add the point to the summary.
     * 
     * @param point the point to add to the summary,
     */
    public void add(MultiPointXY point) {
        if (point != null) {
            setX(point.x);
            addY(point.yArray);
        }
    }

    /**
     * Returns the points summary as a string.
     * 
     * @return the points summary as a string.
     */
    public String toString() {
        // clear the string buffer
        strBuff.setLength(0);

        strBuff.append("x: ").append(mdeNumberFormat.format(x)).append("\n").append("y values:\n");

        if (y.isEmpty()) {
            strBuff.append("(none)");
        } else {
            Double value;
            boolean firstValue = true;

            for (Iterator<Double> iter = y.iterator(); iter.hasNext();) {
                value = iter.next();
                if (firstValue) {
                    firstValue = false;
                } else {
                    strBuff.append(", ");
                }
                strBuff.append(mdeNumberFormat.format(value.doubleValue()));
            } // end for Iterator
        }
        return strBuff.toString();
    }
}
