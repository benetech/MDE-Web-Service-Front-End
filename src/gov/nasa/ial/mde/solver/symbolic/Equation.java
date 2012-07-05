/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.util.StringSplitter;

/**
 * Representation of an Equation.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Equation {
    
    /** The equation is bad if true, false if ok. */
    public boolean bad = true;
    
    /** The left side of the equation. */
    public Expression left = null;
    
    /** The right side of the equation. */
    public Expression right = null;
    
    private Polynomial mainPolynomial = null;

    /**
     * Constructs an Equation object given the equation as a String.
     * 
     * @param s the equation as a String.
     */
    public Equation(String s) {
        StringSplitter ss = new StringSplitter("=", s);
        if (ss.pieces.length != 3) {
            return;
        }
        
        if ((ss.pieces[0].indexOf("=") >= 0) || (ss.pieces[2].indexOf("=") >= 0)) {
            return;
        }

        if (ss.pieces[0].trim().length() == 0 || ss.pieces[2].trim().length() == 0) {
            return;
        }

        left = new Expression(ss.pieces[0]);
        right = new Expression(ss.pieces[2]);
        if ((left == null) || (right == null)) {
            return;
        }

        if (left.root == null || right.root == null) {
            return;
        }
        
        bad = (left.root.badFlag || right.root.badFlag);
        if (bad) {
            return;
        }
        if (!solveRational()) {
            determineMainPolynomial();
        }
    } // end Equation

    /**
     * Returns the equation as a simple expression.
     * 
     * @return the equation as a simple expression.
     */
    public Expression getSimple() {
        if (left.isSimple()) {
            return left;
        }
        if (right.isSimple()) {
            return right;
        }
        return null;
    } // end getSimple

    /**
     * Returns the other side of the equation as an expression.
     * 
     * @param e one side of the equation as an expression.
     * @return the other side of the equation as an expression.
     */
    public Expression getOther(Expression e) {
        if (e == left) {
            return right;
        }
        if (e == right) {
            return left;
        }
        return null;
    } // end getOther

    /**
     * Returns a polynomial of the equation.
     * 
     * @return polynomial of the equation.
     */
    public Polynomial getPolynomial() {
        return mainPolynomial;
    } // end getPolynomial

    /**
     * Returns the main polynomial of the equation viewed as a polynomial in its
     * string argument.
     * 
     * @param var is the string representation of the desired variable If the
     *            equation does not contain the desired variable, then a constant
     *            Polynomial is returned.
     * @return the main polynomial of the equation viewed as a polynomial in its
     * string argument.
     */
    public Polynomial getOneVariablePolynomial(String var) {
        String[] t = { var };

        return new Polynomial(mainPolynomial.toExpression(), t);
    } // end getOneVariablePolynomial

    /**
     * Returns a string representation of the equation.
     * 
     * @return string representation of the equation.
     */
    public String toString() {
        return left + " = " + right;
    } // end toString

    private boolean solveRational() {
        RationalExpression l = new RationalExpression(left);

        if (!l.isRationalExpression()) {
            return false;
        }

        RationalExpression r = new RationalExpression(right);

        if (!r.isRationalExpression()) {
            return false;
        }

        Polynomial pl = l.getNumerator().product(r.getDenominator());
        Polynomial pr = r.getNumerator().product(l.getDenominator());

        mainPolynomial = pl.sum(pr.makeNegative());

        return true;
    } // end solveRational

    private void determineMainPolynomial() {
        Polynomial pl = new Polynomial(left, false);
        Polynomial pr = new Polynomial(right, false);

        pr.negate();
        mainPolynomial = pl.sum(pr);
    } // end determineMainPolynomial


//    public static void main(String[] args) {
//        AnalyzedEquation a = new AnalyzedEquation(StringSplitter.combineArgs(args));
//        a.setParameterValue("a", -2);
//        a.setParameterValue("b", -1);
//        System.out.println(a.printEquation());
//    } // end main

} // end class Equation
