/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.util.MathUtil;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * The symbolic rational expression.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class RationalExpression extends ProtoExpression {
    
    private Expression unity = new Expression("1");
    private Polynomial numerator = new Polynomial(unity);
    private Polynomial denominator = new Polynomial(unity);
    private boolean IRE = false;

    /**
     * Default constructor.
     */
    public RationalExpression() {
        super();
    }

    /**
     * Constructs a rational expression from the specified expression.
     * 
     * @param e an expression.
     */
    public RationalExpression(Expression e) {
        super();

        copyFromExpression(e);

        RationalExpression r = makeIt(root);

        if (r == null)
            return;

        numerator = r.numerator;
        denominator = r.denominator;
        IRE = true;
    } // end RationalExpression

    /**
     * Constructs a rational expression from the specified string expression.
     * 
     * @param eString the expression as a string.
     */
    public RationalExpression(String eString) {
        this(new Expression(eString));
    } // end RationalExpression

    /**
     * Returns the numerator polynomial.
     * 
     * @return the numerator polynomial.
     */
    public Polynomial getNumerator() {
        return numerator;
    } // end getNumerator

    /**
     * Returns the denominiator polynomial.
     * 
     * @return the denominiator polynomial.
     */
    public Polynomial getDenominator() {
        return denominator;
    } // end getDenominator

    /**
     * Copies the values from the specified expression to this rational expression.
     * 
     * @param e the expression.
     */
    public void copyFromExpression(Expression e) {
        this.varStrings = e.varStrings;
        this.root = e.root;
        this.knowns = e.knowns;
        this.parameters = e.parameters;
        this.variables = e.variables;
        this.legalVariables = e.legalVariables;
        this.theValue = e.theValue;
        this.valueString = e.valueString;
    } // end copyFromExpression

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.ProtoExpression#setParameterHash(java.util.Hashtable)
     */
    public void setParameterHash(Hashtable h) {
        super.setParameterHash(h);
        numerator.setParameterHash(h);
        denominator.setParameterHash(h);
    } // end setParameterHash

    /**
     * Returns true if this is a rational expression.
     * 
     * @return true if this is a rational expression, false otherwise.
     */
    public boolean isRationalExpression() {
        return IRE;
    } // end isRational

    /**
     * Returns the sum of this rational expression and the specified other
     * rational expression.
     * 
     * @param other the other rational expression.
     * @return the sum of this rational expression and the other rational 
     * expression.
     */
    public RationalExpression sum(RationalExpression other) {
        RationalExpression r = privateSum(other);
        r.transformExpression();
        return r;
    } // end sum

    /**
     * Returns the product of this rational expression and the specified other
     * rational expression.
     * 
     * @param other the other rational expression.
     * @return the product of this rational expression and the other rational 
     * expression.
     */
    public RationalExpression product(RationalExpression other) {
        RationalExpression r = privateProduct(other);

        r.transformExpression();
        return r;
    } // end product

    /**
     * Returns the difference between this rational expression and the specified other
     * rational expression.
     * 
     * @param other the other rational expression.
     * @return the difference between this rational expression and the other rational 
     * expression.
     */
    public RationalExpression difference(RationalExpression other) {
        RationalExpression r = privateDifference(other);

        r.transformExpression();
        return r;
    } // end difference

    /**
     * Returns the quotient between this rational expression and the specified other
     * rational expression.
     * 
     * @param other the other rational expression.
     * @return the quotient between this rational expression and the other rational 
     * expression.
     */
    public RationalExpression quotient(RationalExpression other) {
        RationalExpression r = privateQuotient(other);

        r.transformExpression();
        return r;
    } // end quotient

    /**
     * Returns the string representation of this rational expression.
     * 
     * @return the string representation of this rational expression.
     */
    public String toString() {
        String n = numerator.isMonomial() ? numerator.toString() : "(" + numerator.toString() + ")";
        String d = "(" + denominator.toString() + ")";

        return n + "/" + d;
    } // end toString

    private RationalExpression privateProduct(RationalExpression other) {
        RationalExpression r = new RationalExpression();

        r.numerator = numerator.product(other.numerator);
        r.denominator = denominator.product(other.denominator);

        return r;
    } // end privateProduct

    private RationalExpression privateNegative() {
        RationalExpression r = new RationalExpression();

        r.numerator = numerator.makeNegative();
        r.denominator = r.denominator.product(denominator);

        return r;
    } // end privateNegative

    private RationalExpression privateReciprocal() {
        RationalExpression r = new RationalExpression();

        r.numerator = r.numerator.product(denominator);
        r.denominator = r.denominator.product(numerator);

        return r;
    } // end privateReciprocal

    private RationalExpression privateSum(RationalExpression other) {
        Polynomial t1 = numerator.product(other.denominator);
        Polynomial t2 = other.numerator.product(denominator);
        RationalExpression r = new RationalExpression();

        r.numerator = t1.sum(t2);
        r.denominator = denominator.product(other.denominator);
        return r;
    } // end privateSum

    private RationalExpression privateDifference(RationalExpression other) {
        return privateSum(other.privateNegative());
    } // end privateDifference

    private RationalExpression privateQuotient(RationalExpression other) {
        return privateProduct(other.privateReciprocal());
    } // end privateQuotient

    private void transformExpression() {
        copyFromExpression(new Expression(toString()));
    } // end transformExpression

    private RationalExpression makeIt(ParseNode q) {
        int i, n = (q.children == null) ? 0 : q.children.length;
        RationalExpression r = new RationalExpression(), temp;

        switch (q.operator) {
        case Action.SUM:
            r.numerator = new Polynomial(new Expression("0"));

            for (i = 0; i < n; i++) {
                if ((temp = makeIt(q.children[i])) == null)
                    return null;

                r = temp.privateSum(r);
            } // end for i

            break;

        case Action.PRODUCT:
            for (i = 0; i < n; i++) {
                if ((temp = makeIt(q.children[i])) == null)
                    return null;

                r = temp.privateProduct(r);
            } // end for i
            break;

        case Action.U_MINUS:
            if ((temp = makeIt(q.children[0])) == null)
                return null;

            r = temp.privateNegative();

            break;

        case Action.RECIPROCAL:
            if ((temp = makeIt(q.children[0])) == null)
                return null;

            r = temp.privateReciprocal();

            break;

        case Action.POWER:
            {
                Expression e = new Expression(q.children[1]);

                if (e.theValue == null)
                    return null;

                double pf = e.theValue.doubleValue();

                if ((n = (int)Math.rint(pf)) != pf)
                    return null;

                if (n < 0) {
                    if ((temp = makeIt(q.children[0])) == null)
                        return null;

                    temp = temp.privateReciprocal();
                } // end if
                else {
                    if ((temp = makeIt(q.children[0])) == null)
                        return null;
                } // end else

                for (i = Math.abs(n) - 1; i >= 0; i--)
                    r = r.privateProduct(temp);
            } // end block
            break;

        case Action.NO_OP:
            r.numerator = new Polynomial(new Expression(q));
            break;

        default:
            if (q.value != null) {
//                if (q.value instanceof Double)
                    return new RationalExpression(q.value.toString());
            }
            return null;
        } // end switch

        return r;
    } // end makeIt

    /**
     * The continued fraction of a real number.
     * 
     * @author Dr. Robert Shelton
     * @version 1.0
     * @since 1.0
     */
    public static class ContinuedFraction {
        private int first = -1, size;
        private int[] a;
        private double[] theta;
        private ArrayList<Residual> residuals = new ArrayList<Residual>();
        private double errorProbability;
        private final static double MAX_ERROR_PROBABILITY = 0.5;
        private static double tolerance;
        private static double logProbCorrect;
        private final static double LOG_LIMIT = MathUtil.logOnePlusX(-MAX_ERROR_PROBABILITY);

        /**
         * Constructs a continued fraction of the specfied real number.
         * 
         * @param x the real number.
         * @param maxSize the maximum size.
         */
        public ContinuedFraction(double x, int maxSize) {
            ArrayList<Remainder> remainders = new ArrayList<Remainder>();
            int n;
            Remainder r = new Remainder(x);

            tolerance = 1.0e-10;
            logProbCorrect = 0.0;
            adjustTolerance(r);

            remainders.add(new Remainder(0.0));

            for (n = 1; acceptableErr(n) && (first = fcm(remainders, r)) < 0; n++) {
                remainders.add(r);
                r = new Remainder(1.0 / r.theta);
                adjustTolerance(r);
            } // end for n

            if (first < 0)
                fcm(remainders, r);

            remainders.add(r);

            a = new int[size = n];
            theta = new double[n];

            for (int i = 1; i <= n; i++) {
                r = remainders.get(i);
                a[i - 1] = (int)r.a;
                theta[i - 1] = r.theta;
            } // end for i
            errorProbability = (logProbCorrect > Double.NEGATIVE_INFINITY) ? 
                                    -MathUtil.stump(logProbCorrect, 1) : 1.0;
        } // end ContinuedFraction

        /**
         * The string representation of the continued fraction.
         */
        public String toString() {
            StringBuffer b = new StringBuffer("Continued fraction:\nSize = " + size);

            b.append("\nRational value : " + isRational());
            b.append("\nQuadratic root: " + isQuadratic());

            b.append("\nPartial quotients:");
            for (int i = 0; i < size; i++)
                b.append(" " + a[i]);

            b.append("\nResiduals:");
            for (int i = 0; i < size; i++)
                b.append("\n" + residuals.get(i));

            /*
             * b.append ("\nRemainders:"); for (int i = 0; i < size; i++) b.append (" " +
             * theta[i]);
             */
            b.append("\nFirst = " + getFirst() + ", Last = " + getLast());
            b.append("\nProbability of incorrect classification = " + errorProbability);
            String sq = getQuadraticEquation();
            String sr = getRational();

            if (sr != null)
                b.append("\nRational value = " + sr);

            if (sq != null)
                b.append("\nQuadratic equation: " + sq);

            return b.toString();
        } // end toString

        /**
         * Returns true if the continued fraction is rational.
         * 
         * @return true if the continued fraction is rational, false otherwise.
         */
        public boolean isRational() {
            return first == 0;
        } // end isRational

        /**
         * Returns true if the continued fraction is Quadratic.
         * 
         * @return true if the continued fraction is Quadratic, false otherwise.
         */
        public boolean isQuadratic() {
            return first > 0;
        } // end isQuadratic

        /**
         * Returns the first index value.
         * 
         * @return the first index value.
         */
        public int getFirst() {
            if (isRational())
                return 0;

            return first - 1;
        } // end getFirst

        /**
         * Returns the last index value.
         * 
         * @return the last index value.
         */
        public int getLast() {
            if (isQuadratic())
                return size - 1;

            return -1;
        } // end getLast

        /**
         * Returns the quadratic polynomial of the continued fraction.
         * 
         * @return the quadratic polynomial of the continued fraction.
         */
        public Polynomial getQuadraticPolynomial() {
            if (isQuadratic()) {
                RationalExpression[] lr = iterate("x", first - 1, size - 1);
                Polynomial l = lr[0].numerator.product(lr[1].denominator);
                Polynomial r = lr[1].numerator.product(lr[0].denominator);
                return l.sum(r.makeNegative());
            } // end if

            return null;
        } // end getQuadraticPolynomial

        /**
         * Returns the polynomial coefficients.
         * 
         * @return the polynomial coefficients.
         */
        public double[] getPolynomialCoefficients() {
            if (isRational()) {
                RationalExpression r = iterate();
                double[] rv = { r.denominator.getConstant().theValue.doubleValue(),
                        -r.numerator.getConstant().theValue.doubleValue() };

                return rv;
            } // end if

            if (isQuadratic()) {
                Expression[] e = Polynomial.getCoefficientsAsExpressions(getQuadraticPolynomial(), "x");
                double[] rv = new double[3];

                Polynomial.evaluateCoefficients(e, rv);

                return rv;
            } // end if

            return new double[0];
        } // end getPolynomialCoefficients

        /**
         * Returns the quadratic equation of the continued fraction.
         * 
         * @return the quadratic equation of the continued fraction.
         */
        public String getQuadraticEquation() {
            Polynomial p = getQuadraticPolynomial();

            if (p != null)
                return p.toString() + " = 0";

            return null;
        } // end getQuadraticEquation

        /**
         * Returns the rational of the continued fraction.
         * 
         * @return the rational of the continued fraction.
         */
        public String getRational() {
            RationalExpression r = iterate();

            if (r != null)
                return r.toString();

            return null;
        } // end getRational

        /**
         * Iterates over the specified variable and first and last indexes to
         * return the rational expression.
         * 
         * @param var the variable.
         * @param f the first index.
         * @param l the last index.
         * @return the rational expressions.
         */
        public RationalExpression[] iterate(String var, int f, int l) {
            if (isQuadratic()) {
                int n = 0;

                RationalExpression r = (new RationalExpression(var)).privateSum(
                        new RationalExpression(new Double(-a[n]).toString()));
                RationalExpression r0 = null, r1;

                while (true) {
                    if (n == f)
                        r0 = new RationalExpression(r.toString());

                    r = r.privateReciprocal();
                    r = r.privateSum(new RationalExpression(new Double(-a[++n]).toString()));

                    if (n == l) {
                        r1 = new RationalExpression(r.toString());
                        break;
                    } // end if
                } // end while

                r0.transformExpression();
                r1.transformExpression();

                RationalExpression[] rv = { r0, r1 };

                return rv;
            } // end if

            return new RationalExpression[0];
        } // end iterate

        /**
         * Interates over the continued fraction to return the rational expression.
         * 
         * @return the rational expression.
         */
        public RationalExpression iterate() {
            if (isRational()) {
                int n = size - 1;
                RationalExpression r = new RationalExpression(new Double(a[n]).toString());

                while (n > 0) {
                    r = r.privateReciprocal();
                    r = r.privateSum(new RationalExpression(new Double(a[--n]).toString()));
                } // end while

                r.transformExpression();

                return r;
            } // end if

            return null;
        } // end iterate

        private boolean acceptableErr(int n) {
            double c = 2.0 * n * tolerance;

            if (c < 1.0)
                logProbCorrect += MathUtil.logOnePlusX(-c);
            else
                logProbCorrect = Double.NEGATIVE_INFINITY;

//            double displayLogProbCorrect = logProbCorrect;

            if (logProbCorrect < LOG_LIMIT)
                return false;

            return true;
        } // end acceptableErr

        private void adjustTolerance(Remainder r) {
            double p = Math.abs(r.a);

            if (p == 0.0)
                return;

            int n = (int)Math.floor(MathUtil.log2(p));

            for (int i = 0; i <= n; i++)
                tolerance *= 2.1;
        } // end adjustTolerance

        private int fcm(ArrayList<Remainder> l, Remainder r) {
            int i, iMin = -1, n = l.size();
            double m = Double.POSITIVE_INFINITY, temp;

            for (i = 0; i < n; i++) {
                Remainder t = l.get(i);

                if ((temp = Math.abs(r.theta - t.theta)) < m) {
                    m = temp;
                    iMin = i;
                } // end if

                if (temp < tolerance) {
                    residuals.add(new Residual(i, temp));
                    return i;
                } // end if
            } // end for i

            residuals.add(new Residual(iMin, m));
            return -1;
        } // end fcm

        /**
         * The residual of the continued fraction.
         * 
         * @author Dr. Robert Shelton
         * @version 1.0
         * @since 1.0
         */
        public static class Residual {
            private int indexOfClosestMatch = -1;
            private double miss;
            private double missRatio;

            /**
             * Constructs a residual from the index of closest match and the
             * amount of miss.
             * 
             * @param indexOfClosestMatch the index of closest match.
             * @param miss the amount of miss.
             */
            public Residual(int indexOfClosestMatch, double miss) {
                this.indexOfClosestMatch = indexOfClosestMatch;
                this.miss = miss;
                this.missRatio = miss / tolerance;
            } // end Residual

            /**
             * Returns a string representation of the residual.
             * 
             * @return string representation of the residual.
             */
            public String toString() {
                return "Closest match = " + indexOfClosestMatch + "\nMiss = " + miss + "\nMiss ratio = "
                        + missRatio;
            } // end toString
        } // end classResidual

        /**
         * The remainder of the continued fraction.
         * 
         * @author Dr. Robert Shelton
         * @version 1.0
         * @since 1.0
         */
        public static class Remainder {
            private double theta;
            private double a;

            /**
             * Constructs a remainder for the specfied value.
             * 
             * @param x the remainder value.
             */
            public Remainder(double x) {
                if (Math.abs(x - Math.rint(x)) < tolerance) {
                    a = Math.rint(x);
                    theta = 0.0;
                    return;
                } // end if

                a = Math.floor(x);
                theta = x - a;
            } // end Remainder
        } // end class Remainder
    } // end class ContinuedFraction


//    public static void main(String[] args) {
//         //****************************************************************************
//         //* if (args.length != 2) { System.err.println ("Usage: java
//         //* RationalExpression \" <E1>\" \" <E2>\""); return; } // end if
//         //* RationalExpression e1=new RationalExpression (args[0]); RationalExpression
//         //* e2=new RationalExpression(args[1]); RationalExpression d, s, p, q; if
//         //* (e1.isRationalExpression() && e2.isRationalExpression()) {
//         //* System.out.println ("Sum = " + (s=e1.sum(e2))); System.out.println
//         //* ("Difference = " + (d=e1.difference(e2))); System.out.println ("Product = " +
//         //* (p=e1.product(e2))); System.out.println ("Quotient = " +
//         //* (q=e1.quotient(e2))); Test parameter set/evaluation Hashtable h=new
//         //* java.util.Hashtable(); p.parameters.put ("a", new Double(2.0));
//         //* p.parameters.put ("strBuff", new Double(-3.0)); System.out.println
//         //* ("Product with substitutions = " + p); h.put ("x", new Double(10.0));
//         //* h.put ("y", new Double( -5.0)); System.out.println ("Product value = " +
//         //* p.evaluate(h)); return; } // end if System.err.println ("Not a rational
//         //* expression");
//         //***************************************************************************
//        Expression e = new Expression(gov.nasa.ial.mde.util.StringSplitter.combineArgs(args));
//
//        RationalExpression.ContinuedFraction c = new RationalExpression.ContinuedFraction(e.evaluate(null), 20); // max
//        // partial
//        // quotients
//
//        System.out.println("C = " + c);
//    } // end main

} // end class RationalExpression
