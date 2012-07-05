/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;

/**
 * A lemniscate is the locus of points in the plane, the product of whose
 * distances to two points located symmetrically about the origin is
 * exactly the square of the distance of each point from the origin. For
 * example, if the two points in question are (-a, 0) and (a, 0), then the
 * polar equation of the curve will be <code>r^2 = 2a^2*cos(2*theta)</code>. 
 * This curve will be the locus of points, the product of whose distances from 
 * the two points is exactly <code>a^2</code>. The general polar equation of a 
 * lemniscate is <code>r^2 = 2*a^2*cos(2*(\theta-\phi))</code> where <code>a</code>
 * is the distance from each point to the origin and <code>\phi</code> is the 
 * inclination of the line joining the two points.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarLemniscateModel extends PolarModel {

    private int[][] lemniscate = { { 3, 6, 7 } };

    /**
     * Constructs a Polar Lemniscate Model.
     * 
     * @param p the Polar model builder.
     */
    public PolarLemniscateModel(PolarModelBuilder p) {
        evaluate(p, lemniscate);
        name = "lemniscate";
        identity = PolarClassifier.POLAR_LEMNISCATE;
    } // end PolarLemniscateModel
    
} // end class PolarLemniscateModel
