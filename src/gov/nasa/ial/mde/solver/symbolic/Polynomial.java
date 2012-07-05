/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.util.Comparison;
import gov.nasa.ial.mde.util.QSorter;
import gov.nasa.ial.mde.util.SortedKeyStrings;

import java.util.*;

/**
 * Class to handle polynomial expressions Handles some algebraic simplification 
 * by polynomial multiplication and consolidation of like terms.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Polynomial {
    
    /**
     * Special string to indicate no variables i.e. all exponents = 0 use as a
     * signature key (see below) in place of the empty string which would result if
     * there were no variables
     */
    public final static String CONSTANT = "#$CONSTANT$#";
    
    // Determines whether or not the constructor algorithm will treat non-polynomial
    // sub-expressions as constants or generalized variables
    private boolean useGeneralizedVariables = false;
    
    private int highestDegree = -1;

    // Expression from which this Polynomial is constructed
    private Expression theExpression;

    // Keys are term signatures, values are PolyTerms where a signature is a
    // colon-separated string consisting of variables followed by '^', followed by
    // the exponent -- a positive integer. e.g. the term 3*x*y^2 would have the
    // signature "x^1:y^2" Note that signatures only contain variables and powers;
    // coefficients reside in the PolyTerm class uniqueness is enforced by requiring
    // that variable strings appear in lexicographical order.
    private Hashtable termHash;

    // Keys are string representations of variables the corresponding value is an
    // Integer Object representing the highest degree of the variable over all terms
    private Hashtable<String, Integer> degree;
    
    // Keys are String representation of variables; the corresponding value is an
    // Expression array containing the coefficients of this Polynomial considered as
    // a polynomial in the single key variable
    private Hashtable<String, Expression[]> coefficientHash;
    
    // Table of variables to consider as parameters and their corresponding values
    private Hashtable parameterHash = new Hashtable();
    
    // String representation of all variables, sorted by degree highest to lowest
    private String[] variables = new String[0];
    
    // Array of string reps. of required variables, i.e. construct as a polynomial in
    // only these variables. For example, if requiredVariables = {"x", "y"} and the
    // expression is (a+b)*(x+y)^2, then the resulting polynomial is (a+b)*x^2 +
    // 2*(a+b)*x*y + (a+b)*y^2
    private String[] requiredVariables = null;
    
    // Term Compare
    private Comparison TC  = new Comparison() {
        public int compare(Object o1, Object o2) {
            PolyTerm t1 = (PolyTerm)o1, t2 = (PolyTerm)o2;
            int d1 = t1.getDegree(), d2 = t2.getDegree();

            if (d1 < d2)
                return 1;

            if (d2 < d1)
                return -1;

            int i, n = variables.length;

            for (i = 0; i < n; i++) {
                int dov1 = t1.getDegreeOfVariable(variables[i]);
                int dov2 = t2.getDegreeOfVariable(variables[i]);

                if (dov1 < dov2)
                    return 1;

                if (dov2 < dov1)
                    return -1;
            } // end for i

            return 0;
        } // end compare
    }; // end new Comparison

    private Polynomial() {
        termHash = new Hashtable();
        degree = new Hashtable<String, Integer>();
        coefficientHash = new Hashtable<String, Expression[]>();
    } // end Polynomial

    /**
     * Constructs a polynomial from the specifed expression.
     * 
     * @param e the expression.
     */
    public Polynomial(Expression e) {
        Polynomial p = makePolynomial(e);
        copyFrom(p);
    } // end Polynomial

    /**
     * Constructs a polynomial from the specifed expression and generealized
     * variables flag.
     * 
     * @param e the expression.
     * @param ugv true to use generalized variables.
     */
    public Polynomial(Expression e, boolean ugv) {
        useGeneralizedVariables = ugv;
        Polynomial p = makePolynomial(e);
        copyFrom(p);
    } // end Polynomial

    /**
     * Constructs a polynomial from the specifed expression and required
     * variables.
     * 
     * @param e the expression.
     * @param v the required variables.
     */
    public Polynomial(Expression e, String[] v) {
        requiredVariables = v;
        theExpression = e;
        Polynomial p = makePolynomial(e);
        copyFrom(p);
    } // end Polynomial

    /**
     * Constructs a polynomial from the specifed expression, required variables,
     * and generealized variables flag. 
     * 
     * @param e the expression.
     * @param v the required variables.
     * @param ugv true to use generalized variables.
     */
    public Polynomial(Expression e, String[] v, boolean ugv) {
        requiredVariables = v;
        useGeneralizedVariables = ugv;
        theExpression = e;
        Polynomial p = makePolynomial(e);
        copyFrom(p);
    } // end Polynomial

    /**
     * Creates a polynomial from the specified coefficients an variable.
     * 
     * @param c the coefficients of the polynomial.
     * @param var the variable.
     * @return the polynomial.
     */
    public static Polynomial doubles2Poly(double[] c, String var) {
        int d = c.length - 1, i;
        Polynomial r = new Polynomial(new Expression("0.0"));

        for (i = 0; i <= d; i++) {
            Polynomial t = new Polynomial(new Expression(c[i] + "*" + var + "^" + (d - i)));

            r = r.sum(t);
        } // end for i

        return r;
    } // end doubles2Poly

    /**
     * Creates a polynomial from the specified coefficients.
     * 
     * @param coefficients the coeficients of the polynomial.
     * @return the polynomial.
     */
    public static Polynomial doubles2Poly(double[] coefficients) {
        return doubles2Poly(coefficients, "x");
    } // end doubles2Poly

    /**
     * Makes a polynomial of a constant term expression.
     * 
     * @param c the constant term expresson.
     * @return the polynomial.
     */
    public static Polynomial makeConstant(Expression c) {
        Polynomial p = new Polynomial();
        p.addTerm(makeConstantTerm(c));
        return p;
    } // end makeConstant

    /**
     * Makes a polynomial from a variable term expression.
     * 
     * @param v the variable term expression.
     * @return the polynomial.
     */
    public static Polynomial makeVariable(Expression v) {
        Polynomial p = new Polynomial();
        p.addTerm(makeVariableTerm(v));
        return p;
    } // end makeVariable

    /**
     * Returns true if the polynomial is monomial.
     * 
     * @return true if the polynomial is monomial, false otherwise.
     */
    public boolean isMonomial() {
        return (termHash.size() < 2);
    } // end isMonomial

    /**
     * Returns the sum of this polynomial and the other polynomial.
     * 
     * @param other the other polynomial.
     * @return the sum of this polynomial and the other polynomial.
     */
    public Polynomial sum(Polynomial other) {
        Polynomial p = new Polynomial();
        Hashtable otherTerms = other.termHash;
        Enumeration k = termHash.keys();

        while (k.hasMoreElements()) {
            String s = (String)k.nextElement();
            PolyTerm t0, t1 = (PolyTerm)termHash.get(s), t2 = (PolyTerm)otherTerms.get(s);

            if (t2 != null) // a matching term exists in the other polynomial
                t0 = t1.sum(t2);
            else
                t0 = t1;

            p.addTerm(t0);
        } // end while

        for (k = otherTerms.keys(); k.hasMoreElements();) {
            String s = (String)k.nextElement();
            PolyTerm t1 = (PolyTerm)termHash.get(s), t2 = (PolyTerm)otherTerms.get(s);

            if (t1 != null)
                continue; // already done common terms

            p.addTerm(t2);
        } // end for

        p.finish();

        return p;
    } // end sum

    /**
     * Negates this polynomial.
     */
    public void negate() {
        Enumeration k = termHash.keys();

        while (k.hasMoreElements())
            ((PolyTerm)termHash.get(k.nextElement())).negate();

        coefficientHash = new Hashtable();
    } // end negate

    /**
     * Returns the negative of this polynomial.
     * 
     * @return the negative of this polynomial.
     */
    public Polynomial makeNegative() {
        Polynomial p = new Polynomial();
        Enumeration k = termHash.keys();

        while (k.hasMoreElements())
            p.addTerm(((PolyTerm)termHash.get(k.nextElement())).makeNegative());

        p.finish();

        return p;
    } // end makeNegative

    /**
     * Constructs the Polynomial that is the partial derivative of this Polynomial
     * with respect to a given variable
     * 
     * @param variable String representation of variable with which the
     *            differentiation is to be performed
     * @return the differentiated Polynomial
     */
    public Polynomial makeDerivative(String variable) {
        Polynomial p = new Polynomial();
        Enumeration k = termHash.keys();

        while (k.hasMoreElements())
            p.addTerm(((PolyTerm)termHash.get(k.nextElement())).makeDerivative(variable));

        p.finish();
        p.setParameterHash(parameterHash);

        return p;
    } // end makeDerivative

    /**
     * Constructs the Polynomial equivalent to this Polynomial expressed as a
     * Polynomial in the given variables. The coefficients of the constructed
     * Polynomial are Expressions which contain all dependences on other variables
     * The implementation calls this.toExpression and a Polynomial constructor, thus
     * efficiency could be an issue
     * 
     * @param vars an array of String representations of the polynomial variables of
     *            the constructed Polynomial
     * @return the Polynomial in the specified variables.
     */
    public Polynomial asAPolynomialIn(String[] vars) {
        Polynomial p = new Polynomial(toExpression(), vars);

        p.setParameterHash(parameterHash);
        return p;
    } // end asAPolynomialIn

    /**
     * One-variable case of asAPolynomial.
     * 
     * @param theVariable String representation of the desired polynomial variable
     * @return the one-variable equivalent of this Polynomial
     */
    public Polynomial oneVariablePolynomial(String theVariable) {
        String[] vars = { theVariable };
        return asAPolynomialIn(vars);
    } // end oneVariablePolynomial

    /**
     * Extracts coefficients of the one-variable equivalent of this Polynomial as an
     * array of Expressions Caches the resulting Expression array, thus repetitive
     * calls require little overhead
     * 
     * @param var String representation of the variable in which we want the
     *            polynomial described by the coefficients to be written
     * @return an array of Expressions of this Polynomial considered as a polynomial
     *         in the variable var. If d is the degree of this Polynomial in var,
     *         then this Polynomial is equivalent to e[0]*(var)^d + e[1]*(var)^(d-1) +
     *         ... + e[d].
     */
    public Expression[] getCoefficientsAsExpressions(String var) {
        Expression[] e;

        if ((e = (Expression[])coefficientHash.get(var)) == null) {
            e = Polynomial.getCoefficientsAsExpressions(oneVariablePolynomial(var), var);
            coefficientHash.put(var, e);
        } // end if

        return e;
    } // end getCoefficientsAsExpressions

    /**
     * Constructs an array of Expressions representing coefficients from all terms of
     * a Polynomial p that are pure powers of the variable var. Note that when p
     * contains other variables, one can obtain unexpected results. Normally you
     * should call the corresponding instance method to retrieve coefficients of a
     * Polynomial in more than one variable.
     * 
     * @param p the Polynomial
     * @param var the String representation of the variable
     * @return the coefficients as an array of Expressions
     */
    public static Expression[] getCoefficientsAsExpressions(Polynomial p, String var) {
        String[] vars = { var };
        int d = p.getDegree();
        Expression[] r = new Expression[d + 1];

        if (d < 0)
            return r;

        r[d] = p.getConstant();

        for (int i = 0; i < d; i++) {
            int[] J = { d - i };

            r[i] = p.getCoefficient(vars, J);
        } // end for i

        return r;
    } // end getCoefficientsAsExpressions

    /**
     * Evaluates the coefficients and places them in the <code>cd</code> array.
     * 
     * @param ce the expression to evaluate.
     * @param cd the coefficients from the evaluation.
     */
    public static void evaluateCoefficients(Expression[] ce, double[] cd) {
        int i, n = ce.length;

        for (i = 0; i < n; i++)
            if (ce[i].theValue == null)
                cd[i] = ce[i].evaluate(null);
            else
                cd[i] = ce[i].theValue.doubleValue();
    } // end evaluateCoefficients

    /**
     * Evaluates the coefficients and places them in the <code>cd</code> array
     * for the specified variable and value.
     * 
     * @param ce the expression.
     * @param var the variable.
     * @param value the variable value.
     * @param cd the coefficients.
     */
    public static void evaluateCoefficients(Expression[] ce, String var, double value, double[] cd) {
        int i, n = ce.length;
        Hashtable<String, Double> h = new Hashtable<String, Double>();

        h.put(var, new Double(value));

        for (i = 0; i < n; i++)
            cd[i] = ce[i].evaluate(h);
    } // end evaluateCoefficients

    /**
     * Returns the product of this polynomial and the other polynomimal.
     * 
     * @param other the other polynomial.
     * @return the product of this polynomial and the other polynomimal.
     */
    public Polynomial product(Polynomial other) {
        Enumeration k1 = termHash.keys();
        Polynomial r = new Polynomial(); // no terms initially

        while (k1.hasMoreElements()) {
            PolyTerm t = (PolyTerm)termHash.get(k1.nextElement());
            Polynomial partialSum = new Polynomial();
            Enumeration k2 = other.termHash.keys();

            while (k2.hasMoreElements())
                partialSum.addTerm(t.product((PolyTerm)other.termHash.get(k2.nextElement())));

            r = r.sum(partialSum);
        } // end while

        return r;
    } // end product

    /**
     * Returns the variables in the polynomial.
     * 
     * @return the variables in the polynomial.
     */
    public String[] getVariables() {
        return variables;
    } // end getVariables

    /**
     * Returns the degree of the polynomial.
     * 
     * @return the degree of the polynomial.
     */
    public int getDegree() {
        if (highestDegree < 0) {
            Enumeration k = termHash.keys();

            while (k.hasMoreElements()) {
                int d = ((PolyTerm)termHash.get(k.nextElement())).getDegree();

                if (d > highestDegree)
                    highestDegree = d;
            } // end while
        } // end if

        return highestDegree;
    } // end getDegree

    /**
     * Returns constant term expression.
     * 
     * @return constant term expression.
     */
    public Expression getConstant() {
        PolyTerm pt = (PolyTerm)termHash.get(CONSTANT);

        if (pt == null)
            return new Expression("0");

        return pt.getCoefficient();
    } // end getConstant

    /**
     * Returns an expression of the coefficients.
     * 
     * @param v the variables.
     * @param e the variable values.
     * @return the expression.
     */
    public Expression getCoefficient(String[] v, int[] e) {
        PolyTerm pt = getTerm(v, e);

        if (pt == null)
            return new Expression("0");

        return pt.getCoefficient();
    } // end getCoefficient

    /**
     * Determines if all coefficients including the "constant" term are numerical
     * constants This Polynomial will be constant iff each coefficient has an empty
     * variables list
     * 
     * @return true if the polynomial has constant coefficients.
     */
    public boolean hasConstantCoefficients() {
        Enumeration<PolyTerm> k = (Enumeration<PolyTerm>) termHash.keys();

        while (k.hasMoreElements()) {
            Expression e = ((PolyTerm)termHash.get(k.nextElement())).getCoefficient();

            if (e.varStrings.length > 0)
                return false;
        } // end while

        return true;
    } // end hasConstantCoefficients

    private void copyFrom(Polynomial p) {
        highestDegree = p.highestDegree;
        theExpression = p.theExpression;
        termHash = p.termHash;
        degree = p.degree;
        variables = p.variables;
        requiredVariables = p.requiredVariables;
        coefficientHash = p.coefficientHash;
        setParameterHash(p.parameterHash);
    } // end copyFrom

    private PolyTerm getTerm(String[] vars, int[] exps) {
        int i, n;

        if ((n = vars.length) != exps.length)
            return null;

        Hashtable h = new Hashtable();

        for (i = 0; i < n; i++)
            h.put(vars[i], new Integer(exps[i]));

        String[] r = PolyTerm.makeSignature(h);

        if ((n = r.length - 1) < 0)
            return null;

        return (PolyTerm)termHash.get(r[n]);
    } // end getTerm

    private Polynomial makePolynomial(Expression e) {
        Polynomial p = makePoly(e.root);

        p.theExpression = e;
        p.finish();

        return p;
    } // end makePolynomial

    private Polynomial makePoly(ParseNode r) {
        if (r.value != null)
            return makeConstant(new Expression(r));

        if (requiredVariables != null) {
            int i, n = requiredVariables.length;
            Hashtable h = (Hashtable)theExpression.variables.get(r);

            for (i = 0; i < n; i++)
                if (h.get(requiredVariables[i]) != null)
                    break;

            if (i >= n)
                return makeConstant(new Expression(r));
        } // end if

        switch (r.operator) {
        case Action.U_MINUS:
            {
                Polynomial p = makePoly(r.children[0]);

                p.negate();
                return p;
            } // end case

        case Action.SUM:
            {
                int i, n = r.children.length;
                Polynomial p = new Polynomial();

                for (i = 0; i < n; i++)
                    p = p.sum(makePoly(r.children[i]));

                return p;
            } // end case

        case Action.PRODUCT:
            {
                int i, n = r.children.length;
                Polynomial p = makeConstant(new Expression("1"));

                for (i = 0; i < n; i++)
                    p = p.product(makePoly(r.children[i]));

                return p;
            } // end case

        case Action.POWER:
            {
                Expression pow = new Expression(r.children[1]);

                if (pow.theValue == null)
                    return punt(new Expression(r));

                double pf = pow.theValue.doubleValue();
                int n = (int)Math.floor(pf);

                if (n != pf)
                    return punt(new Expression(r));

                if (n < 0)
                    return punt(new Expression(r));

                Polynomial p = makeConstant(new Expression("1"));
                Polynomial f = makePoly(r.children[0]);

                for (int i = 0; i < n; i++)
                    p = p.product(f);
                return p;
            } // end case

        case Action.NO_OP:
            return makeVariable(new Expression(r));

        default:
            return punt(new Expression(r));
        } // end switch
    } // end makePoly

    private void finish() {
        Enumeration k = termHash.keys();
        Vector hitList = new Vector();

        coefficientHash = new Hashtable();

        while (k.hasMoreElements()) {
            Object o = k.nextElement();
            Expression c = ((PolyTerm)termHash.get(o)).getCoefficient();

            if (c == null)
                c = new Expression("0");

            if (c.theValue == null)
                continue;

            if (c.theValue.doubleValue() == 0.0)
                hitList.addElement(o);
        } // end while

        for (Enumeration e = hitList.elements(); e.hasMoreElements();)
            termHash.remove(e.nextElement());

        k = termHash.keys();
        while (k.hasMoreElements()) {
            PolyTerm t = (PolyTerm)termHash.get(k.nextElement());
            String[] v = t.getVariables();
            int i, n = v.length;

            // t.autoTest();
            for (i = 0; i < n; i++) {
                int d = t.getDegreeOfVariable(v[i]);
                Integer Deg = (Integer)degree.get(v[i]);

                if (Deg != null) {
                    if (d > Deg.intValue())
                        degree.put(v[i], new Integer(d));
                } // end if
                else
                    degree.put(v[i], new Integer(d));
            } // end for i
        } // end while

        SortedKeyStrings s = new SortedKeyStrings(degree);
        int i, n = s.theKeys.length;

        variables = new String[n];

        for (i = 0; i < n; i++)
            variables[i] = s.theKeys[i];
    } // end finish

    /**
     * Converts the polynomial to an expression.
     * 
     * @return the polynomial as an expression.
     */
    public Expression toExpression() {
        int i, n = termHash.size();
        Enumeration k = termHash.keys();
        PolyTerm[] t = new PolyTerm[n];

        if (n == 0)
            return new Expression("0");

        for (i = 0; i < n; i++)
            t[i] = (PolyTerm)termHash.get(k.nextElement());

        QSorter q = new QSorter(t, TC);

        for (i = 0; i < n; i++)
            t[i] = (PolyTerm)q.theData[i];

        ParseNode p = new ParseNode(n, Action.SUM);

        for (i = 0; i < n; i++)
            p.children[i] = t[i].makeExpression().root;

        Expression newE = new Expression(p);

        newE.setParameterHash(parameterHash);
        return newE;
    } // end toExpression

    /**
     * Sets the parameter hashtable for the polynomial.
     * 
     * @param ph the parameter hashtable.
     */
    public void setParameterHash(Hashtable ph) {
        Enumeration k = termHash.keys();

        while (k.hasMoreElements()) {
            Object o = k.nextElement();
            PolyTerm t = (PolyTerm)termHash.get(o);

            t.getCoefficient().setParameterHash(ph);
        } // end while

        parameterHash = ph;

        int i, n = variables.length;
        ArrayList<String> v = new ArrayList<String>();

        for (i = 0; i < n; i++) {
            if (ph.get(variables[i]) != null)
                continue;

            v.add(variables[i]);
        } // end for i

        if (v.size() < n) { // found a var that's really a param
            Polynomial p = asAPolynomialIn((String[])v.toArray(new String[v.size()]));

            copyFrom(p);
        } // end if
    } // end setParameterHash

    /**
     * Returns the string representation of the polynomial.
     * 
     * @return the string representation of the polynomial.
     */
    public String toString() {
        return toExpression().toString();
    } // end toString

    private void addTerm(PolyTerm t) {
        termHash.put(t.getSignature(), t);
    } // end addTerm

    private static PolyTerm makeConstantTerm(Expression c) {
        PolyTerm t = new PolyTerm();

        t.setCoefficient(c);

        return t;
    } // end makeConstantTerm

    private static PolyTerm makeVariableTerm(Expression v) {
        PolyTerm t = new PolyTerm();

        t.setCoefficient(new Expression("1"));
        t.setExponent(v, 1);

        return t;
    } // end makeVariableTerm

    private Polynomial punt(Expression e) {
        if (useGeneralizedVariables)
            return makeVariable(e);

        return makeConstant(e);
    } // end punt

//    public static void main(String[] args) {
//        String v = "x";
//        Polynomial p = null;
//
//        switch (args.length) {
//        case 2:
//            v = args[1];
//
//        case 1:
//            p = new Polynomial(new Expression(args[0]));
//            break;
//
//        default:
//            System.err.println("Usage: java Polynomial <POLYNOMIAL_STRING> [<VARIABLE_NAME_TO_DERIVE>]");
//            System.exit(1);
//        } // end switch
//
//        // System.out.println ("dP/d" + v + " = " +
//        // p.makeDerivative(v).toString());
//
//        Expression[] c = p.getCoefficientsAsExpressions(v);
//
//        c = p.getCoefficientsAsExpressions(v);
//
//        for (int i = 0; i < c.length; i++)
//            System.out.println("Coefficient " + i + " = " + c[i]);
//    } // end main

} // end class Polynomial
