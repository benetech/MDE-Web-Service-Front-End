/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.math.MultiPointXY;

/**
 * The Polynomial Model builder.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolynomialModelBuilder extends DataModelBuilder {
    
    private int xDegree, yDegree;
    private int numGenerators;

    @SuppressWarnings("unused")
	private PolynomialModelBuilder() {
        throw new IllegalArgumentException("Default constructor not allowed.");
    } // end PolynomialModelBuilder

    /**
     * Constructs a polynomial model builder given the x and y degree.
     * 
     * @param xDegree the polynomial x degree.
     * @param yDegree the polynomial y degree.
     */
    public PolynomialModelBuilder(int xDegree, int yDegree) {
        numGenerators = (1 + (this.xDegree = xDegree)) * (1 + (this.yDegree = yDegree));
    } // end PolynomialModelBuilder

    /**
     * Adds a point to the model.
     * 
     * @param x the x coordinate value.
     * @param y the y coordinate value.
     */
    public void addNewPoint(double x, double y) {
        int i, j, k;
        double x2aPower, y2aPower;
        double[] tempData = new double[numGenerators];

        for (i = k = 0, y2aPower = 1.0; i <= yDegree; i++, y2aPower *= y) {
            for (j = 0, x2aPower = 1.0; j <= xDegree; j++, x2aPower *= x) {
                tempData[k++] = x2aPower * y2aPower;
            }
        }

        data.add(tempData);
    } // end addNewPoint

    /**
     * Adds a multiple point to the model.
     * 
     * @param mp the multiple XY point.
     */
    public void addNewPoint(MultiPointXY mp) {
        if (mp == null) {
            return;
        }

        double[] r = mp.yArray;
        int i, n = r.length;

        for (i = 0; i < n; i++) {
            addNewPoint(mp.x, r[i]);
        }
    } // end addNewPoint

    /**
     * Returns the polynomial x degree.
     * 
     * @return the polynomial x degree.
     */
    public int getXDegree() {
        return xDegree;
    } // end getXDegree

    /**
     * Returns the polynomial y degree.
     * 
     * @return the polynomial y degree.
     */
    public int getYDegree() {
        return yDegree;
    } // end getYDegree
    
} // end class PolynomialModelBuilder
