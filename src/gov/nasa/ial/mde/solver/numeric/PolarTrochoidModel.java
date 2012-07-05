/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;

/**
 * Trochoids are curves traced by points fixed to circles which roll about other 
 * circles in the plane. Families of trochoids are distinguished by their 
 * frequency <code>\omega</code> and amplitude <code>a</code>. A common example
 * is the heart-shaped cardioid given by <code>r = 1 - sin (theta)</code> where 
 * both <code>\omega</code> and <code>a</code> are unity. The general equation 
 * is given by <code>r = 1 - a*cos(\omega*(\theta-\phi)</code> where <code>a</code> 
 * is the amplitude, <code>\omega</code> is the frequency, and <code>\phi</code> 
 * determines the orientation of the figure. Values of a less than unity result 
 * in a wiggly cy simple closed curve topologically equivalent to a circle; 
 * values greater than 1 cause the curve to have loops that appear in pairs -- a
 * major loop and a corresponding smaller reflected loop. As a approaches 
 * infinity,  the minor reflections grow to approach the size of their major 
 * counterparts, and the curve begins to look like a polar rose.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarTrochoidModel extends PolarModel {
    
    private int[][] trochoids = {
            { 0, 1, 4, 5 },
            { 0, 1, 6, 7 },
            { 0, 1, 8, 9 },
            { 0, 1, 10, 11 }
    };

    /**
     * Constructs a Polar Trochoid Model.
     * 
     * @param p the Polar model builder.
     */
    public PolarTrochoidModel(PolarModelBuilder p) {
        evaluate(p, trochoids);
        identity = PolarClassifier.POLAR_TROCHOID;
        name = "trochoid";
    } // end PolarTrochoidModel
    
} // end class PolarTrochoidModel
