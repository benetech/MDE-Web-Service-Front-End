/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;

/**
 * The polar equation of a line is given by <code>r = a/cos(\theta-\phi)</code> 
 * where <code>a</code> is the length of a perpendicular from the origin to the 
 * line and <code>\phi</code> is the inclination of the perpendicular.
 * 
 * @author ddexter
 * @version 1.0
 * @since 1.0
 */
public class PolarLineModel extends PolarModel {
    
    private int[][] line = { { 2, 4, 5 } };

    /**
     * Constructs a Polar line model.
     * 
     * @param p the Polar model builder.
     */
    public PolarLineModel(PolarModelBuilder p) {
        evaluate(p, line);
        name = "line";
        identity = PolarClassifier.POLAR_LINE;
    } // end PolarLineModel
    
} // end class PolarLineModel
