/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;

/**
 * Includes features of all special cases -- the general equation is given by 
 * <code>c0 + c1*r + c2/r + c3*r^2 + c4*cos(\theta) + c5*sin(\theta) + ... + 
 * c10*cos(4*\theta) + c11*sin(4*\theta) = 0</code>
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarEnchiladaModel extends PolarModel {

    private int[][] enchilada = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 } };

    /**
     * Constructs a polar model for all special cases.
     * 
     * @param p the Polar model builder.
     */
    public PolarEnchiladaModel(PolarModelBuilder p) {
        evaluate(p, enchilada);
        name = "enchilada";
        identity = PolarClassifier.POLAR_ENCHILADA;
    } // end PolarEnchiladaModel
    
} // end class PolarEnchiladaModel
