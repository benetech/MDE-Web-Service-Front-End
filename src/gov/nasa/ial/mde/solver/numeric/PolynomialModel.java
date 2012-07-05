/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.symbolic.Polynomial;

/**
 * The Polynomial Model.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolynomialModel {
    
    /** The fit of the model. */
    public double fit = Double.POSITIVE_INFINITY;
    
    /** The model vector. */
    public double[] modelVector;
    
    /** The model signature. */
    public int[] modelSignature;
    
    /** The model complexity. */
    public double complexity;
    
    /** Which signature the model is for. */
    public int whichSignature = 0;
    
    /** The model name. */
    public String name;
    
    /** The polynomial. */
    public Polynomial thePolynomial;

    /**
     * Evaluates the model signatures given the polynomial model builder.
     * 
     * @param p the polynomial model builder.
     * @param modelSignatures the model signatures.
     */
    public void evaluate(PolynomialModelBuilder p, int[][] modelSignatures) {
        int i, n = modelSignatures.length;

        for (i = 0; i < n; i++) {
            p.buildModel(modelSignatures[i]);
            if (p.getFit() <= fit) {
                fit = p.getFit();
                modelVector = p.getModel();
                modelSignature = modelSignatures[whichSignature = i];
            } // end if
        } // end for i

        if (fit < Double.POSITIVE_INFINITY) {
            complexity = modelSignature.length;
            pruneModel();
        } // end if
        else
            complexity = Double.POSITIVE_INFINITY;
    } // end evaluate

    /**
     * Returns the model name.
     * 
     * @return the model name.
     */
    public String toString() {
        return name;
    } // end toString

    private void pruneModel() {
        int i, n = modelVector.length;
        double t, u = 0.0;

        for (i = 0; i < n; i++)
            if ((t = Math.abs(modelVector[i])) > u)
                u = t;

        t = u * 1.0e-8;

        for (i = 0; i < n; i++)
            if (Math.abs(modelVector[i]) < t) {
                complexity -= 1.0 / n;
                modelVector[i] = 0.0;
            } // end if
    } // end pruneModel
    
} // end class PolynomialModel
