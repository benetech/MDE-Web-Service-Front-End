/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.util.SortedKeyStrings;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The polynomial terms.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolyTerm {
    
    // see description of `termHash' in Polynomial.java
    private String signature = Polynomial.CONSTANT;
    
    private Hashtable<String, Integer> exponents; // keys are variables, values are exponents
    private Expression coefficient;
    private String[] variables = new String[0];
    private int degree;

    /**
     * Default constructor.
     */
    public PolyTerm() {
        exponents = new Hashtable<String,Integer>();
        degree = -1; // flag value that triggers first-time computation of the degree
    } // end PolyTerm

    /**
     * Returns the coefficient as an expression.
     * 
     * @return the coefficient as an expression.
     */
    public Expression getCoefficient() {
        return coefficient;
    } // end getCoefficient

    /**
     * Returns the variables.
     * 
     * @return the variables.
     */
    public String[] getVariables() {
        return variables;
    } // end getVariables

    /**
     * Returns the degree of the specified variable.
     * 
     * @param var the variable.
     * @return the degree.
     */
    public int getDegreeOfVariable(String var) {
        Integer Deg = (Integer)exponents.get(var);

        if (Deg == null)
            return 0;

        return Deg.intValue();
    } // end getDegreeOfVariable

    /**
     * Returns the degree.
     * 
     * @return the degree.
     */
    public int getDegree() {
        if (degree < 0) {
            int i, n = variables.length;

            for (i = degree = 0; i < n; i++)
                degree += ((Integer)exponents.get(variables[i])).intValue();
        } // end if

        return degree;
    } // end getDegree

    /**
     * Returns the signature.
     * 
     * @return the signature.
     */
    public String getSignature() {
        return signature;
    } // end getSignature

    /**
     * Sets the coefficient.
     * 
     * @param c the coefficient.
     */
    public void setCoefficient(Expression c) {
        coefficient = c;
    } // end setCoefficient

    /**
     * Set the exponent for the specified variable.
     * 
     * @param v the variable.
     * @param e the exponent.
     */
    public void setExponent(Expression v, int e) {
        exponents.put(v.toString(), new Integer(e));
        doSignature();
    } // end setExponent

    /**
     * Returns the sum of this PolyTerm with the specified other PolyTerm.
     * 
     * @param other the other polyTerm.
     * @return the sum of this PolyTerm with the specified other PolyTerm.
     */
    public PolyTerm sum(PolyTerm other) {
        PolyTerm r = new PolyTerm();
        r.exponents = exponents;
        r.coefficient = coefficient.sum(other.coefficient);

        if (r.coefficient.valueString != null)
            r.coefficient = new Expression(r.coefficient.valueString);
        r.doSignature();

        return r;
    } // end sum

    /**
     * Returns the negative of this PolyTerm.
     * 
     * @return the negative of this PolyTerm.
     */
    public PolyTerm makeNegative() {
        PolyTerm t = new PolyTerm();

        t.exponents = exponents;
        t.signature = signature;
        t.coefficient = Expression.negate(coefficient);

        if (t.coefficient.valueString != null)
            t.coefficient = new Expression(t.coefficient.valueString);
        t.doSignature();

        return t;
    } // end makeNegatigve

    /**
     * Negates this PolyTerm.
     */
    public void negate() {
        coefficient = Expression.negate(coefficient);

        if (coefficient.valueString != null)
            coefficient = new Expression(coefficient.valueString);

    } // end negate

    /**
     * Returns the product of this PolyTerm and the other PolyTerm.
     * 
     * @param other the other PolyTerm.
     * @return the product of this PolyTerm and the other PolyTerm.
     */
    public PolyTerm product(PolyTerm other) {
        PolyTerm r = new PolyTerm();
        Enumeration<String> k = exponents.keys();

        r.coefficient = coefficient.product(other.coefficient);

        if (r.coefficient.valueString != null)
            r.coefficient = new Expression(r.coefficient.valueString);

        while (k.hasMoreElements()) {
            String s = (String)k.nextElement();
            Integer I = (Integer)exponents.get(s), J = (Integer)other.exponents.get(s);

            if (J != null)
                r.exponents.put(s, new Integer(I.intValue() + J.intValue()));
            else
                r.exponents.put(s, I);
        } // end while

        for (k = other.exponents.keys(); k.hasMoreElements();) {
            String s = (String)k.nextElement();

            if (exponents.get(s) != null)
                continue; // skip since common vars are already done

            r.exponents.put(s, other.exponents.get(s));
        } // end for
        r.doSignature();
        return r;
    } // end product

    /**
     * Returns an expression for the PolyTerm.
     * 
     * @return the expression for the PolyTerm.
     */
    public Expression makeExpression() {
        if (coefficient == null)
            return null;

        int i, n = exponents.size();

        if (n == 0)
            return coefficient;

        ParseNode p = new ParseNode(n + 1, Action.PRODUCT);

        p.children[0] = coefficient.root;

        for (i = 0; i < n; i++) {
            int d = getDegreeOfVariable(variables[i]);

            if (d > 1) {
                p.children[i + 1] = new ParseNode(2, Action.POWER);
                p.children[i + 1].children[0] = new ParseNode(variables[i]);
                p.children[i + 1].children[1] = new ParseNode("" + d);
            } // end if
            else
                p.children[i + 1] = new ParseNode(variables[i]);
        } // end for i

        return new Expression(p);
    } // end makeExpression

    /**
     * Returns a derivative given the specified variable.
     * 
     * @param variable the variable in the PolyTerm.
     * @return the derivative.
     */
    public PolyTerm makeDerivative(String variable) {
        PolyTerm p = new PolyTerm();
        Integer N = (Integer)exponents.get(variable);
        int n;

        if (N == null)
            return p;

        p.coefficient = coefficient.product(new Expression(N.toString()));
        if (p.coefficient.valueString != null)
            p.coefficient = new Expression(p.coefficient.valueString);

        for (Enumeration<String> k = exponents.keys(); k.hasMoreElements();) {
            String v = k.nextElement();

            if (v.equals(variable)) {
                if ((n = N.intValue()) > 1)
                    p.exponents.put(v, new Integer(n - 1));
            } // end if
            else
                p.exponents.put(v, exponents.get(v));
        } // end for k

        p.doSignature();

        return p;
    } // end differentiate

    /**
     * Returns the signature given the exponents.
     * 
     * @param e the exponents.
     * @return the signature.
     */
    public static String[] makeSignature(Hashtable<String, Integer> e) {
        if (e.isEmpty())
            return new String[0];

        StringBuffer b = new StringBuffer();
        SortedKeyStrings s = new SortedKeyStrings(e);
        int i, n = s.theKeys.length;

        b.append(s.theKeys[0] + "^" + ((Integer)e.get(s.theKeys[0])).toString());

        for (i = 1; i < n; i++)
            b.append(":" + s.theKeys[i] + "^" + ((Integer)e.get(s.theKeys[i])).toString());

        String[] r = new String[n + 1];

        for (i = 0; i < n; i++)
            r[i] = s.theKeys[i];

        r[n] = b.toString();

        return r;
    } // end makeSignature

    private void doSignature() {
        String[] r = makeSignature(exponents);
        int i, n = r.length - 1;

        if (n < 0)
            return;

        variables = new String[n];
        for (i = 0; i < n; i++)
            variables[i] = r[i];

        signature = r[n];
    } // end doSignature
    
//    public void autoTest() {
//        if (variables.length != exponents.size()) {
//            System.err.println("Oops!");
//            System.exit(1);
//        } // end if
//    } // end autoTest
    
} // end class PolyTerm
