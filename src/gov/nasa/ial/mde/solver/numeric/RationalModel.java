/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.symbolic.Polynomial;

/**
 * A model for a Rational.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class RationalModel extends PolynomialModel {
    
    private int[][] rModelVector;
    private int numeratorDegree, denominatorDegree;

    /**
     * Constructs a rational model using the polynomimal model builder and the
     * specified numerator and denominator degree.
     * 
     * @param p the polynomimal model builder.
     * @param numeratorDegree the degree of the numerator.
     * @param denominatorDegree the degree of the denominator.
     */
    public RationalModel(PolynomialModelBuilder p, int numeratorDegree, int denominatorDegree) {
        int dx = p.getXDegree(), dy = p.getYDegree();

        if (dy < 1)
            throw new IllegalArgumentException("Need a PolynomialModelBuilder with larger y degree");

        this.numeratorDegree = Math.min(numeratorDegree, dx);
        this.denominatorDegree = Math.min(denominatorDegree, dx);
        rModelVector = new int[1][0];
        rModelVector[0] = makeModel(denominatorDegree, numeratorDegree, dx);

        evaluate(p, rModelVector);
    } // end RationalModel

    /**
     * Returns the numerator polynomial.
     * 
     * @return the numerator polynomial.
     */
    public Polynomial getNumerator() {
        double[] c = new double[numeratorDegree + 1];

        for (int i = 0; i <= numeratorDegree; i++)
            c[i] = -modelVector[numeratorDegree - i];

        return Polynomial.doubles2Poly(QuadraticClassifier.makeInteger(c, 100), "x");
    } // end getNumerator

    /**
     * Returns the denominator polynomial.
     * 
     * @return the denominator polynomial.
     */
    public Polynomial getDenominator() {
        double[] c = new double[denominatorDegree + 1];

        for (int i = 0; i <= denominatorDegree; i++)
            c[denominatorDegree - i] = modelVector[1 + i + numeratorDegree];

        return Polynomial.doubles2Poly(QuadraticClassifier.makeInteger(c, 100), "x");
    } // end getDenominator

    /**
     * Returns the a string representation of the rational.
     * 
     * @return a string representation of the rational.
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Numerator = ").append(getNumerator().toString());
        b.append("\nDenominator = ").append(getDenominator().toString());
        return b.toString();
    } // end toString

    private int[] makeModel(int d, int n, int dx) {
        int dSize = d + 1, nSize = n + 1, xSize = dx + 1;
        int[] r = new int[dSize + nSize];

        for (int i = 0; i < nSize; i++)
            r[i] = i;
        for (int i = 0; i < dSize; i++)
            r[i + nSize] = i + xSize;

        return r;
    } // end makeModel
    
} // end class RationalModel
