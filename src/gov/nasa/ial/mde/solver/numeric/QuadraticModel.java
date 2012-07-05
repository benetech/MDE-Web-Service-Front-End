/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.Expression;
import gov.nasa.ial.mde.solver.symbolic.Polynomial;

/**
 * A model for a Quadratic.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class QuadraticModel extends PolynomialModel {
    
    private int[][] qModelSignature = new int[1][6];

    /**
     * Constructs a Quadratic model.
     * 
     * @param p the polynomial model builder.
     */
    public QuadraticModel(PolynomialModelBuilder p) {
        int dx = p.getXDegree(), dy = p.getYDegree(), i, j, k, n;

        for (i = k = n = 0; i <= dy; i++)
            for (j = 0; j <= dx; j++, k++)
                if (i + j <= 2)
                    qModelSignature[0][n++] = k;

        evaluate(p, qModelSignature);
        complexity = 0.0;
    } // end QuadraticModel

    /**
     * Returns the quadratic expression as a string.
     * 
     * @return the quadratic expression as a string.
     */
    public String toString() {
        return getPolynomial().toString();
    } // end toString

    /**
     * Returns the analyzed equation of the quadratic.
     * 
     * @return the analyzed equation of the quadratic.
     */
    public AnalyzedEquation getAnalyzedEquation() {
        return new AnalyzedEquation(toString() + " = 0");
    } // end getAnalyzedEquation

    /**
     * Returns the polynomial for the quadratic.
     * 
     * @return the polynomial for the quadratic.
     */
    public Polynomial getPolynomial() {
        double[] mv = QuadraticClassifier.makeInteger(modelVector, 100);
        Polynomial p = new Polynomial(new Expression("" + mv[0]));

        p = p.sum(new Polynomial(new Expression(mv[1] + "*x")));
        p = p.sum(new Polynomial(new Expression(mv[2] + "*x^2")));
        p = p.sum(new Polynomial(new Expression(mv[3] + "*y")));
        p = p.sum(new Polynomial(new Expression(mv[4] + "*x*y")));
        p = p.sum(new Polynomial(new Expression(mv[5] + "*y^2")));
        return p;
    } // end getPolynomial
    
} // end class QuadraticModel
