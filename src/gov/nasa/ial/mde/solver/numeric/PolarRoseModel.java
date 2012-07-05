/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;

/**
 * A polar "rose" is a curve formed by graphing an equation of the form 
 * <code>r = a*cos(n*theta)</code> or <code>r = sin(n*theta)</code>. The name 
 * "rose" stems (no pun intended) from the shape of the curve which can be 
 * imagined as a number of identical loops located symmetrically about the 
 * origin. For <code>n</code> an odd integer, the number of such loops or petals
 * is <code>n</code>, and for <code>n = 1</code>, the lone loop is a circle
 * passing through the origin. If <code>n</code> is an even integer, then the 
 * curve will have <code>2*n</code> loops. This is because in both cases, the 
 * curve will trace out <code>n</code> loops as theta goes from 0 to <code>\PI</code>,
 * and another <code>n</code> loops as theta goes from <code>\PI</code> to 
 * <code>2*\PI</code>. When <code>n</code> is odd, the second set of <code>n</code>
 * loops coincides exactly with the first set, but when <code>n</code> is even, 
 * the second set is shifted so that the curve displays all <code>2*n</code> loops.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarRoseModel extends PolarModel {

    private int[][] roses = {
            { 1, 4, 5 },
            { 1, 6, 7 },
            { 1, 8, 9 },
            { 1, 10, 11 }
    };

    /**
     * Constructs a Polar Rose model.
     * 
     * @param p the Polar model builder.
     */
    public PolarRoseModel(PolarModelBuilder p) {
        evaluate(p, roses);
        identity = PolarClassifier.POLAR_ROSE;
        name = "rose";
    } // end PolarRoseModel
    
} // end class PolarRoseModel
