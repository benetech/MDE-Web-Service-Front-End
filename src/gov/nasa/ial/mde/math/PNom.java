/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 29, 2004
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.solver.symbolic.Expression;
import gov.nasa.ial.mde.solver.symbolic.Polynomial;

/**
 * The <code>PNom</code> class represents a Polynomial in terms of its 
 * coefficients
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PNom {
    
    private int      degree;

    private double[] coefficients;

    private double   epsilon = 0.0;

    /**
     * Default constructor with a degree of -1 and no coefficients.
     */
    public PNom() {
        degree = -1;
        coefficients = new double[0];
    } // end PNom

    /**
     * Constructs a polynomial with the specified coefficients.
     * 
     * @param c coefficients of the polynomial.
     */
    public PNom(double[] c) {
        initialize(c);
    } // end PNom

    /**
     * Constructs a polynomial that represents the specified constant.
     * 
     * @param s the constant.
     */
    public PNom(double s) {
        double[] c = { s };
        initialize(c);
    } // end PNom

    /**
     * Constructs a polynomial from the specified polynomial.
     * 
     * @param p a polynomial.
     */
    public PNom(PNom p) {
        initialize(p.coefficients);
    } // end PNom

    private void initialize(double[] c) {
        int i, j, k, n = c.length;

        for (i = 0; i < n; i++) {
            epsilon += Math.abs(c[i]);
        }

        epsilon /= (n + 1.0);
        epsilon = 1.0e-8 * epsilon + Double.MIN_VALUE;

        for (i = 0; i < n; i++) {
            if (Math.abs(c[i]) > epsilon) {
                break;
            }
        }

        degree = n - i - 1;

        coefficients = new double[degree + 1];

        for (k = 0, j = i; j < n;) {
            coefficients[k++] = c[j++];
        }
    } // end initialize

    private static PNom privateGCD(PNom big, PNom little) {
        if (little.degree > big.degree) {
            throw new IllegalArgumentException("privateGCD: argument reversal");
        }

        PNom r = big.quotient(little)[1];

        if (r.isTrivial()) {
            return little;
        }

        return privateGCD(little, r);
    } // end privateGCD

    /**
     * Is the polynomial trivial.
     * 
     * @return true if trivial, false otherwise.
     */
    public boolean isTrivial() {
        return (degree < 0);
    } // end isTrivial

    /**
     * Is the polynomial for a constant.
     * 
     * @return true if constant, false otherwise.
     */
    public boolean isConstant() {
        return degree < 1;
    } // end isConstant

    /**
     * Returns the degree of the polynomial.
     * 
     * @return the degree of the polynomial.
     */
    public int getDegree() {
        return degree;
    } // end getDegree

    /**
     * Returns the coefficients of the polynomial.
     * 
     * @return the coefficients of the polynomial.
     */
    public double[] getCoefficients() {
        return new PNom(coefficients).coefficients;
    } // end getCoefficients

    /**
     * Returns a <code>Polynomial</code> expression object.
     * 
     * @return a <code>Polynomial</code> expression object.
     */
    public Polynomial toPolynomial() {
        return Polynomial.doubles2Poly(coefficients);
    } // end toPolynomial

    /**
     * Returns a string representation of the polynomial.
     * 
     * @return a string representation of the polynomial.
     */
    public String toString() {
        return toPolynomial().toString();
    } // end toString

    
    /**
     * Sums two polynomials together.
     * 
     * @param other the other polynomial to sum with this one.
     * @return the sum of the two polynomials.
     */
    public PNom sum(PNom other) {
        int d0 = other.degree, d1 = degree, d = Math.max(d0, d1), i;
        double[] c = new double[d + 1];

        for (i = d; d0 >= 0;)
            c[i--] = other.coefficients[d0--];

        while (i >= 0)
            c[i--] = 0.0;

        for (i = d; d1 >= 0;)
            c[i--] += coefficients[d1--];

        return new PNom(c);
    } // end sum

    /**
     * The product of two polynomials.
     * 
     * @param other the other polynomial.
     * @return the product of the two polynomials.
     */
    public PNom product(PNom other) {
        if (degree < 0 || other.degree < 0)
            return new PNom();

        int i, j, newD = degree + other.degree;
        double[] c = new double[newD + 1];

        for (i = 0; i <= newD; i++)
            c[i] = 0.0;

        for (i = 0; i <= degree; i++)
            for (j = 0; j <= other.degree; j++)
                c[i + j] += (coefficients[i] * other.coefficients[j]);

        return new PNom(c);
    } // end product

    /**
     * Makes the polynomial negative.
     * 
     * @return the polynomial negative.
     */
    public PNom makeNegative() {
        double[] c = new double[degree + 1];

        for (int i = 0; i <= degree; i++)
            c[i] = -coefficients[i];

        return new PNom(c);
    } // end makeNegative

    /**
     * The difference of two polynomials.
     * 
     * @param other the other polynomial.
     * @return the difference of the two polynomials.
     */
    public PNom difference(PNom other) {
        return sum(other.makeNegative());
    } // end difference

    /**
     * The quotient of two polynomials.
     * 
     * @param other the other polynomial.
     * @return the quotient of the two polynomials.
     */
    public PNom[] quotient(PNom other) {
        if (other.degree < 0) {
            throw new IllegalArgumentException("PNom: divide by 0");
        }

        if (degree < other.degree) {
            PNom[] rv = { new PNom(), new PNom(coefficients) };

            return rv;
        } // end if

        double[] qr = new double[degree + 1];
        int i, j, newD = degree - other.degree;

        for (i = 0; i <= degree; i++) {
            qr[i] = coefficients[i];
        }

        for (i = 0; i <= newD; i++) {
            qr[i] /= other.coefficients[0];

            for (j = 1; j <= other.degree; j++) {
                qr[i + j] -= (qr[i] * other.coefficients[j]);
            }
        } // end for i

        double[] q = new double[newD + 1];

        for (i = 0; i <= newD; i++) {
            q[i] = qr[i];
        }

        PNom qp = new PNom(q);

        for (; i <= degree; i++) {
            if (Math.abs(qr[i]) > qp.epsilon) {
                break;
            }
        }

        if (i > degree) {
            PNom[] rv = { qp, new PNom() };

            return rv;
        } // end if

        double[] r = new double[degree - i + 1];

        for (j = 0; i <= degree;) {
            r[j++] = qr[i++];
        }

        PNom[] rv = { qp, new PNom(r) };

        return rv;
    } // end quotient

    /**
     * Returns the derivative of the polynomial.
     * 
     * @return the derivative of the polynomial.
     */
    public PNom derivative() {
        if (degree < 1)
            return new PNom();

        double[] r = new double[degree];

        for (int i = 0; i < degree; i++)
            r[i] = (degree - i) * coefficients[i];

        return new PNom(r);
    } // end derivative

    /**
     * Evaluates the polynomial for the given value of x.
     * 
     * @param x the value to evaluate the polynomial over.
     * @return the value of the polynomial evaluated for the value of x.
     */
    public double eval(double x) {
        if (isTrivial()) {
            return 0.0;
        }

        if (isConstant()) {
            return coefficients[0];
        }

        if (Double.isInfinite(x)) {
            if ((degree & 1) == 0) {
                return (coefficients[0] > 0.0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            }

            if (x > 0.0) {
                return (coefficients[0] > 0.0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            }

            return (coefficients[0] < 0.0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        } // end if isInfinite

        double[] d = { 1.0, -x };
        PNom[] q = quotient(new PNom(d));

        return q[1].isTrivial() ? 0.0 : q[1].coefficients[0];
    } // end eval

    /**
     * Returns the real roots.
     * 
     * @return the real roots.
     */
    public Roots.RootFactor[] getRealRoots() {
        return Roots.getRealRootsWithMultiplicities(getCoefficients(), degree);
    } // end getRealRoots

    /**
     * Returns the real zeros of the polynomial.
     *
     * @return the real zeros of the polynomial.
     */
    public RealZero[] getRealZeros() {
        if (isTrivial())
            return null;

        if (isConstant())
            return new RealZero[0];

        Roots.RootFactor[] rf = getRealRoots();
        int i, n = rf.length;
        RealZero[] rz = new RealZero[n];

        if (n == 0)
            return rz;

        int previousSign = (eval(rf[0].rootValues[0] - 1.0) > 0) ? 2 : 0;

        for (i = 0; i < n; i++) {
            int nextSign = ((rf[i].multiplicity & 1) == 0) ? previousSign >> 1 : 1 - (previousSign >> 1);

            rz[i] = new RealZero(rf[i].rootValues[0], previousSign | nextSign);
            previousSign = (nextSign << 1);
        } // end for i

        return rz;
    } // end getRealZeros 

    /**
     * Returns the greatest common divisor given two polynomials.
     * 
     * @param p the first polynomial.
     * @param q the second polynomial.
     * @return the greatest common divisor given two polynomials.
     */
    public static PNom gcd(PNom p, PNom q) {
        if (p.isTrivial()) {
            return q;
        }
        if (q.isTrivial()) {
            return p;
        }

        if (p.degree <= q.degree) {
            return privateGCD(q, p);
        }
        return privateGCD(p, q);
    } // end gcd

    /**
     * Returns a <code>PNom</code> object given a <code>Polynomial</code> object
     * and a polynomial string expression.
     * 
     * @param p the Polynomial object.
     * @param v the polynomial string expression.
     * @return the PNom object.
     */
    public static PNom getPNom(Polynomial p, String v) {
        Expression[] ec = p.getCoefficientsAsExpressions(v);
        int n = ec.length;
        double[] c = new double[n];
        Polynomial.evaluateCoefficients(ec, c);
        return new PNom(c);
    } // end getPNom

    /**
     * Returns the coefficients of the specified <code>Polynomial</code> object
     * and polynomial string expression.
     * 
     * @param p the Polynomial object.
     * @param v the polynomial string expression.
     * @return the coefficients.
     */
    public static double[] getCoefficients(Polynomial p, String v) {
        return getPNom(p, v).coefficients;
    } // end getCoefficients
    
 
//    public static void main(String[] args) {
//        int i, n = args.length;
//        double[] c = new double[n];
//
//        for (i = 0; i < n; i++)
//            try {
//                c[i] = new Double(args[i]).doubleValue();
//            } // end try
//            catch (NumberFormatException nfe) {
//                System.err.println("Arg " + i + "=\"" + args[i] + "\" is not an integer");
//                return;
//            } // end catch
//
//        PNom p = new PNom(c);
//
//        System.out.println("p(-infinity) = " + p.eval(Double.NEGATIVE_INFINITY));
//        System.out.println("p(+infinity) = " + p.eval(Double.POSITIVE_INFINITY));
//        System.out.println("p(3) = " + p.eval(3.0));
//
//        double[] newC = { 1, 2, 3 };
//        PNom q = new PNom(newC);
//        PNom r = q.product(p);
//
//        System.out.println("Result = " + r);
//
//        r = p.product(q);
//
//        System.out.println("Result = " + r);
//
//        r = p.difference(q);
//        System.out.println("Result = " + r);
//        r = q.difference(p);
//        System.out.println("Result = " + r);
//
//        PNom[] qr = p.quotient(q);
//
//        System.out.println("quotient = " + qr[0]);
//        System.out.println("remainder = " + qr[1]);
//
//        qr = q.quotient(p);
//        System.out.println("quotient = " + qr[0]);
//        System.out.println("remainder = " + qr[1]);
//
//        qr = p.product(q).quotient(q);
//        System.out.println("quotient = " + qr[0]);
//        System.out.println("remainder = " + qr[1]);
//
//        qr = p.product(q).quotient(p);
//        System.out.println("quotient = " + qr[0]);
//        System.out.println("remainder = " + qr[1]);
//
//        PNom p1 = q.product(p).sum(q.product(q));
//        PNom p2 = q.product(q);
//
//        System.out.println("GCD of " + p1 + " and " + p2 + " =\n" + gcd(p1, p2));
//
//        System.out.println("The derivative of " + p1 + " =\n" + p1.derivative());
//    }

}
