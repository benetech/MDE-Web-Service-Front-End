/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 30, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.PNom;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.math.RealZero;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.Expression;
import gov.nasa.ial.mde.solver.symbolic.RationalExpression;

import java.util.ArrayList;
import java.util.Collections;

/**
 * The class represents a solved rational function.
 * 
 * @author ddexter
 * @version 1.0
 * @since 1.0
 */
public class SolvedRationalFunction extends SolvedXYGraph {

    private PNom               fNumerator, fDenominator;

    private PNom               dfNumerator, dfDenominator;

    private PNom[]             qf;

    private PointXY            left, right;

    private IntervalEndpoint[] endPoints;

    /** Identify new features so we can access them with SolvedGraph.putFeature */
    protected String[]         newFeatures = { "FunctionAnalysisData" };

    /**
     * Constructs a solved rational function from the specified analyzed equation.
     * 
     * @param ae the analyzed equation.
     */
    public SolvedRationalFunction(AnalyzedEquation ae) {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, ae);
    } // end SolvedRationalFunction
    
    /**
     * Constructs a solved rational function from the specified analyzed equation
     * and left and right values.
     * 
     * @param lft the left endpoint.
     * @param rgt the right endpoint.
     * @param ae the analyzed equation.
     */
    public SolvedRationalFunction(double lft, double rgt, AnalyzedEquation ae) {
        super(ae);

        Expression e = ae.getFunction();

        if (e == null)
            throw new IllegalArgumentException("Input equation is not a function.");

        RationalExpression r = new RationalExpression(new Expression(e.toString()));

        if (!r.isRationalExpression())
            throw new IllegalArgumentException("Not a rational function");

        r.setParameterHash(ae.getParameterHash());

        if ((!r.getNumerator().hasConstantCoefficients()) || (!r.getDenominator().hasConstantCoefficients()))
            throw new IllegalArgumentException("Not a rational function");

        String iv = ae.getIndependentVariable();

        fNumerator = PNom.getPNom(r.getNumerator(), iv);
        fDenominator = PNom.getPNom(r.getDenominator(), iv);

        // take out common factors
        PNom cf = PNom.gcd(fNumerator, fDenominator);

        if (!cf.isConstant()) {
            fNumerator = fNumerator.quotient(cf)[0];
            fDenominator = fDenominator.quotient(cf)[0];
        } // end if

        qf = fNumerator.quotient(fDenominator);

        // d(Hi/Ho) = (Ho dHi - Hi dHo)/(HoHo)
        dfNumerator = fDenominator.product(fNumerator.derivative()).difference(
                fNumerator.product(fDenominator.derivative()));

        dfDenominator = fDenominator.product(fDenominator);

        // take out common factors
        if (!(cf = PNom.gcd(dfNumerator, dfDenominator)).isConstant()) {
            dfNumerator = dfNumerator.quotient(cf)[0];
            dfDenominator = dfDenominator.quotient(cf)[0];
        } // end if

        setEndPoints(lft, rgt);
        findPoints();
        putNewFeatures(newFeatures);

        if (qf[1].isTrivial()) {
            putFeature("graphName", "polynomial");
            
            
            //TODO mess with degree, get it for polynomial
            putNewFeature("FunctionAnalysisData", "degree", "" + qf[0].getDegree(), true);
        
        
        } // end if
        else
            putFeature("graphName", "RationalFunction");
        putFeature("FunctionAnalysisData", getMFN());
    } // end SolvedRationalFunction

    /**
     * Evaluates the function for the given value of x.
     * 
     * @param x the value to evaluate.
     * @return value of the function for the given value of x.
     */
    public double eval(double x) {
        if (Double.isInfinite(x))
            return qf[0].eval(x);

        return qf[0].eval(x) + qf[1].eval(x) / fDenominator.eval(x);
    } // end eval

    /**
     * Sets the left and right endpoints.
     * 
     * @param lft the left endpoint.
     * @param rgt the right endpoint.
     */
    public void setEndPoints(double lft, double rgt) {
        left = new PointXY(lft, eval(lft));
        right = new PointXY(rgt, eval(rgt));
    } // end setEndPoints

    private MdeFeatureNode getMFN() {
        MdeFeatureNode r = new MdeFeatureNode();
        int i, n = endPoints.length;

        r.addKey("EndPoint");
        r.addKey("intervalDescription");

        for (i = 0; i < n; i++)
            r.addValue("EndPoint", endPoints[i].getMFN());

        for (i = 1; i < n; i++)
            r.addValue("intervalDescription", new IntervalDescription(endPoints[i - 1], endPoints[i])
                    .getMFN());

        return r;
    } // end getMFN

    private void findPoints() {
        boolean addLeft = true, addRight = true;
        ArrayList<IntervalEndpoint> points = new ArrayList<IntervalEndpoint>();
        RealZero[] criticals = dfNumerator.getRealZeros();
        RealZero[] singulars = fDenominator.getRealZeros();
        IntervalEndpoint newPoint = null;
        int i, nc = criticals.length, ns = singulars.length;

        for (i = 0; i < nc; i++) {
            double x = criticals[i].getX();

            if (x < left.x)
                continue;

            if (x > right.x)
                break;

            if (x == left.x)
                addLeft = false;

            if (x == right.x)
                addRight = false;

            switch (criticals[i].getSignature(dfDenominator)) {
            case RealZero.PLUS_PLUS:
            case RealZero.MINUS_MINUS:
                newPoint = new IntervalEndpoint(criticals[i], IntervalEndpoint.INFLECTION_POINT);
                break;

            case RealZero.MINUS_PLUS:
                newPoint = new IntervalEndpoint(criticals[i], IntervalEndpoint.LOCAL_MIN);
                break;

            case RealZero.PLUS_MINUS:
                newPoint = new IntervalEndpoint(criticals[i], IntervalEndpoint.LOCAL_MAX);
                break;
            } // end switch

            newPoint.leftYValue = newPoint.rightYValue = fNumerator.eval(x) / fDenominator.eval(x);

            points.add(newPoint);
        } // end for i

        for (i = 0; i < ns; i++) {
            double x = singulars[i].getX();

            if (x < left.x)
                continue;

            if (x > right.x)
                break;

            if (x == left.x)
                addLeft = false;

            if (x == right.x)
                addRight = false;

            newPoint = new IntervalEndpoint(singulars[i], IntervalEndpoint.VERTICAL_ASYMPTOTE);

            switch (singulars[i].getSignature()) {
            case RealZero.MINUS_MINUS:
                newPoint.leftYValue = newPoint.rightYValue = Double.NEGATIVE_INFINITY * fNumerator.eval(x);
                break;

            case RealZero.PLUS_PLUS:
                newPoint.leftYValue = newPoint.rightYValue = Double.POSITIVE_INFINITY * fNumerator.eval(x);
                break;

            case RealZero.PLUS_MINUS:
                newPoint.leftYValue = Double.POSITIVE_INFINITY * fNumerator.eval(x);
                newPoint.rightYValue = Double.NEGATIVE_INFINITY * fNumerator.eval(x);
                break;

            case RealZero.MINUS_PLUS:
                newPoint.rightYValue = Double.POSITIVE_INFINITY * fNumerator.eval(x);
                newPoint.leftYValue = Double.NEGATIVE_INFINITY * fNumerator.eval(x);
                break;
            } // end switch

            points.add(newPoint);
        } // end for i

        if (addLeft)
            points.add(new IntervalEndpoint(left));

        if (addRight)
            points.add(new IntervalEndpoint(right));

        Collections.sort(points);
        endPoints = points.toArray(new IntervalEndpoint[0]);
    } // end findPoints


//     public static void main(String[] args) {
//         SolvedRationalFunction srf = new SolvedRationalFunction(
//                 new AnalyzedEquation(gov.nasa.ial.mde.util.StringSplitter.combineArgs(args)));
//         System.out.println(srf.getXMLString());
//     }
    
}
