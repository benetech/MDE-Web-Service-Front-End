/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.classifier;

import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.SolvedEllipse;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.SolvedHyperbola;
import gov.nasa.ial.mde.solver.SolvedLine;
import gov.nasa.ial.mde.solver.SolvedParabola;
import gov.nasa.ial.mde.solver.SolvedTwoIntersectingLines;
import gov.nasa.ial.mde.solver.SolvedTwoLines;
import gov.nasa.ial.mde.solver.SolvedXYGraph;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.Polynomial;
import gov.nasa.ial.mde.util.MathUtil;

import java.util.Hashtable;

/**
 * A classifier for Quadratics.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class QuadraticClassifier extends MDEClassifier {

	public static enum QuadraticType {
		Unknown, NullSet, SinglePoint, TwoPoints, AllPoints, 
		VerticalLine, HorizontalLine, TwoVerticalLines, TwoHorizontalLines,
		SlopingLine, Parabola, Cross, Hyperbola, Ellipse
	};
	
	public static enum ClassificationFailureReason {
		NoReason, DegreeGreatherThan2, TooManyVariables, NonPolynomial, Polar
	};
	
	//This probably shouble be kept generic for now, since it gets passed to anothedr class
    private final static Hashtable<?, ?> emptyHash = new Hashtable<Object, Object>();

    /*
     * Used for evaluating constant Expressions -- need another evaluate method in
     * Expression
     */
    private QuadraticType identity = QuadraticType.Unknown;
    private ClassificationFailureReason reason = ClassificationFailureReason.NoReason;
    private Polynomial lhs = null;
    private int degree;
    private double A, B, C, D, E, F; // Ax^2+Bxy+Cy^2+Dx+Ey+F=0
    
    /*
     * Threshold for comparison with 0 -- a million times smaller than the ``average''
     * coefficient
     */
    private double norm = 0.0;
    private double alpha = 0.0; // rotation angle
    private double[][] newAxes = {
            { 1.0, 0.0 },
            { 0.0, 1.0 }
    }; // self-explanatory
    
    /* Coefficients with xy term removed by rotation of axes */
    private double aPrime, bPrime, cPrime, dPrime, ePrime;
    
    /*
     * u0 and v0 are offsets for the rotated axes u and v respectively. After resolving
     * rotations and translations (see ``completeSquare''), the equation will be of the
     * form: aPrime(u-u0)^2 + bPrime(v-v0)^2 + ePrime = 0
     */
    private double u0 = 0.0, v0 = 0.0;
    private String[] userVariables;
    private String[] actualVariables = { "x", "y" };
    private String[] transVars = { "x", "y" };

    /**
     * Creates a quadratic classifier for the specified polynomial.
     * 
     * @param lhs Left hand side polynomial.
     */
    public QuadraticClassifier(Polynomial lhs) {
        this.lhs = lhs;
        
        classify();
    } // end QuadraticClassifier

    /**
     * Returns the actual variables in the quadratic.
     * 
     * @return the actual variables in the quadratic.
     */
    public String[] getActualVariables() {
        int i, n = actualVariables.length;
        String[] r = new String[n];

        for (i = 0; i < n; i++)
            r[i] = actualVariables[i];
        return r;
    } // end getActualVariables

    /**
     * Returns the original coefficients.
     * 
     * @return the original coefficients.
     */
    public double[] getOriginalCoefficients() {
        double[] r = { A, B, C, D, E, F };
        return r;
    } // end getOriginalCoefficients

    /**
     * Returns the normalized coefficients.
     * 
     * @return the normalized coefficients.
     */
    public double[] getNormalizedCoefficients() {
        double[] r = { aPrime, bPrime, cPrime, dPrime, ePrime };

        return r;
    } // end getNormalizedCoefficients

    /**
     * Returns the new axes.
     * 
     * @return the new axes.
     */
    public double[][] getNewAxes() {
        double[][] r = {
                { newAxes[0][0], newAxes[0][1] },
                { newAxes[1][0], newAxes[1][1] }
        };

        return r;
    } // end getNewAxes

    /**
     * Returns the rotation.
     * 
     * @return the rotation.
     */
    public double getRotation() {
        return alpha;
    } // end getRotation

    /**
     * Returns the translation.
     * 
     * @return the translation.
     */
    public double[] getTranslation() {
        double[] r = { u0, v0 };

        return r;
    } // end getTranslation

    /**
     * Returns the identity.
     * 
     * @return the identity.
     */
    public QuadraticType getIdentity() {
        return identity;
    } // end getIdentity

    /**
     * Returns the solved graph features of the analyzed equation.
     * 
     * @param analyzedEquation the analyzed equation.
     * @see gov.nasa.ial.mde.solver.classifier.MDEClassifier#getFeatures(gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation)
     */
    public SolvedGraph getFeatures(AnalyzedEquation analyzedEquation) {
        SolvedGraph features;

        switch (identity) {
        case HorizontalLine:
        case VerticalLine:
        case SlopingLine:
            features = new SolvedLine(analyzedEquation);
            break;
        case Parabola:
            features = new SolvedParabola(analyzedEquation);
            break;
        case SinglePoint:
        case Ellipse:
            features = new SolvedEllipse(analyzedEquation);
            break;
        case Hyperbola:
            features = new SolvedHyperbola(analyzedEquation);
            break;
        case NullSet:
            features = new SolvedXYGraph(analyzedEquation, "null set");
            break;
        case AllPoints:
            features = new SolvedXYGraph(analyzedEquation, "all points");
            break;
        case TwoHorizontalLines:
        case TwoVerticalLines:
            features = new SolvedTwoLines(analyzedEquation);
            break;
        case Cross:
            features = new SolvedTwoIntersectingLines(analyzedEquation);
            break;
        default:
            // Use the default features.
            features = super.getFeatures(analyzedEquation);
            break;
        } // end switch
        
        // Make sure we add the graphBoundaries feature.
        addGraphBoundariesFeature(analyzedEquation,features);
        
        return features;
    } // end getFeatures

    /**
     * Get the reason for the failure to classify.
     * 
     * @return the reason for the failure to classify.
     */
    public ClassificationFailureReason getReason() {
        return reason;
    } // end getReason

    /**
     * Returns the normalized equation.
     * 
     * @return the normalized equation.
     */
    public String getNormalizedEquation() {
        StringBuffer r = new StringBuffer(32);
        //double[] c = getNormalizedCoefficients();
        boolean leading = true;

        if (!ToleranceTester.isWithinToleranceOfZero(aPrime)) {
            r.append(makeCoefficient(aPrime, leading)).append("*(").append(transVars[0]).append(makeCoefficient(-u0, false))
                    .append(")^2 ");
            leading = false;
        } // end if

        if (!ToleranceTester.isWithinToleranceOfZero(bPrime)) {
            r.append(makeCoefficient(bPrime, leading)).append("*(").append(transVars[1]).append(makeCoefficient(-v0, false))
                    .append(")^2 ");
            leading = false;
        } // end if

        if (!ToleranceTester.isWithinToleranceOfZero(cPrime)) {
            r.append(makeCoefficient(cPrime, leading)).append("*").append(transVars[0]).append(" ");
            leading = false;
        } // end if

        if (!ToleranceTester.isWithinToleranceOfZero(dPrime)) {
            r.append(makeCoefficient(dPrime, leading)).append("*").append(transVars[1]).append(" ");
            leading = false;
        } // end if

        if (leading) {
            return null;
        }

        r.append(" = ").append(MathUtil.trimDouble(-ePrime, 6));
        return r.toString();
    } // end getNormalizedEquation

    /**
     * Returns the rotation transform.
     * 
     * @return the rotation transform.
     */
    public String getRotationTransform() {
        if (ToleranceTester.isWithinToleranceOfZero(alpha)) {
            return "There was no rotation, so the transform is the identity.";
        }

        StringBuffer r = new StringBuffer(32);

        r.append(transVars[0]).append(" = ")
         .append(makeCoefficient(newAxes[0][0], true)).append("*").append(actualVariables[0])
         .append(" ")
         .append(makeCoefficient(newAxes[0][1], false)).append("*").append(actualVariables[1])
         .append("\n")
         .append(transVars[1]).append(" = ")
         .append(makeCoefficient(newAxes[1][0], true)).append("*").append(actualVariables[0])
         .append(" ")
         .append(makeCoefficient(newAxes[1][1], false)).append("*").append(actualVariables[1]);

        return r.toString();
    } // getRotationTransform

    /**
     * Returns the XY array converted from the UV array.
     * 
     * @param uv the uv array of length 2.
     * @return the XY array of length 2.
     */
    public double[] UV2XY(double[] uv) {
        if (uv.length != 2) {
            throw new IllegalArgumentException("UV array in UV2XY must have length exactly 2");
        }

        double x = uv[0] * newAxes[0][0] + uv[1] * newAxes[1][0];
        double y = uv[0] * newAxes[0][1] + uv[1] * newAxes[1][1];
        double[] xy = { x, y };

        return xy;
    } // end UV2XY

    /**
     * Returns the UV array converted from the XY array.
     * 
     * @param xy the XY array of length 2.
     * @return the UV array converted from the XY array.
     */
    public double[] XY2UV(double[] xy) {
        if (xy.length != 2) {
            throw new IllegalArgumentException("XY array in XY2UV must have length exactly 2");
        }
        
        double u = newAxes[0][0] * xy[0] + newAxes[0][1] * xy[1];
        double v = newAxes[1][0] * xy[0] + newAxes[1][1] * xy[1];
        double[] uv = { u, v };

        return uv;
    } // end XY2UV

    /**
     * Obtains equation of a line in the form a*x + strBuff applies logic to 
     * find integral values of coefficients if possible Pretty-prints the result.
     * 
     * @param p a point.
     * @param inc an increment.
     * @param vars the variables.
     * @return the equation of a line.
     */
    public static String getEquationOfALine(PointXY p, double inc, String[] vars) {
        if (ToleranceTester.isWithinTolerance(Math.abs(inc) - 90.0, 1.0e-8)) {
            return getEquationOfALine(p.x, vars);
        }

        double M = Math.tan(Math.PI * inc / 180.0);
        double[] coeffs = { M, -1.0, p.y - M * p.x };
        double t = Math.sqrt(0.33 * (coeffs[0] * coeffs[0] + coeffs[1] * coeffs[1] + coeffs[2] * coeffs[2]));

        return prettyPrintLinearEquation(makeInteger(coeffs, 100), vars, t);
    } // end getEquationOfALine

    /**
     * Returns the equation of a line.
     * 
     * @param x a value of X.
     * @param vars the variables.
     * @return the equation of a line.
     */
    public static String getEquationOfALine(double x, String[] vars) {
        // trivial case
        double[] coeffs = { 1.0, 0.0, -x };
        double t = Math.sqrt(0.5 * (1.0 + x * x));

        return prettyPrintLinearEquation(makeInteger(coeffs, 100), vars, t);
    } // end getEquationOfALine

    /*
     * returns a value v such that -180 < v <= 180 by adding/subtracting the appropriate
     * multiple of 360
     */
    /**
     * Returns a value v such that -180 < v <= 180 by adding/subtracting the 
     * appropriate multiple of 360.
     * 
     * @param angle an angle in degrees.
     * @return the normalized angle in degrees.
     */
    public static double normalizeAngleInDegrees(double angle) {
        double numTurns = 0.5 + angle / 360.0; // add an extra half turn
        double nextTurn = Math.ceil(numTurns); // the next full turn
        double fractionalTurn = 1.0 + numTurns - nextTurn; // 0 < ft <= 1

        return 360.0 * (fractionalTurn - 0.5); // subtract out the added half-turn
    } // end normalizeAngleInDegrees

    /**
     * A text description of the equation.
     * 
     * @return text description of the equation.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (lhs != null) {
            return "Describes the equation:\n" + lhs.toString() + " = 0";
        }

        return "Could not read the equation -- syntax error.";
    } // end toString

    private static String makeCoefficient(double c, boolean leading) {
        String xs = MathUtil.trimDouble(Math.abs(c), 6);
        if (c > 0.0) {
            return leading ? xs : ("+" + xs);
        } // end if
        return ("-" + xs);
    } // end makeCoefficient

    private void normalizeRotation() {
        if (!ToleranceTester.isWithinToleranceOfZero(B)) { // lhs has a significant xy term
            /*
             * diagonalize the quadratic form Ax^2 + Bxy + Cy^2 by diagonalizing the
             * matrix Q = {{A, B/2},{B/2, C}} lambdas[0] and lambdas[1] are the
             * eigenvalues of Q
             */
            double sigma = A + C;
            double delta = A - C;
            double disc = Math.sqrt(delta * delta + B * B);
            double[] lambdas = { 0.5 * (sigma + disc), 0.5 * (sigma - disc) };
            /* Construct corresponding eigenvectors (u1, v1) and (u2, v2) */
            double u1 = 0.5 * B;
            double v1 = lambdas[0] - A;
            double u2 = lambdas[1] - C;
            double v2 = 0.5 * B;
            /* n1 and n2 are reciprocals of the norms of the unnormalized eigenvectors */
            double n1 = 1.0 / Math.sqrt(u1 * u1 + v1 * v1);
            double n2 = 1.0 / Math.sqrt(u2 * u2 + v2 * v2);
            /* Collect the normalized eigenvectors into a single array */
            double[][] xi = {
                    { n1 * u1, n1 * v1 },
                    { n2 * u2, n2 * v2 }
            };
            /*
             * Pick off the eigenvector most nearly horizontal (largest magnitude X
             * component)
             */
            int iMax = (Math.abs(xi[0][0]) > Math.abs(xi[1][0])) ? 0 : 1;
            /*
             * and make it point to the right by flipping so as to make the X component
             * positive
             */
            double aMax = Math.abs(xi[iMax][0]);
            double unit = xi[iMax][0] / aMax;

            xi[iMax][0] *= unit;
            xi[iMax][1] *= unit;
            /*
             * Finally, construct the new axes which are the Q eigenvectors normalized
             * and chosen to most closely align with the original axes
             */
            newAxes[0][0] = xi[iMax][0];
            newAxes[0][1] = xi[iMax][1];
            /*
             * We know the second axis is perpendicular to the first, so we construct it
             * by twisting the first one 90 degrees to the left
             */
            newAxes[1][0] = -xi[iMax][1];
            newAxes[1][1] = xi[iMax][0];
            /*
             * This is a really inefficient way to obtain the new coefficients, aPrime
             * ... ePrime. The procedure is now commented out in favor of a more direct
             * approach and left as a comment for documentation only
             * u+("+newAxes[1][0]+")*v)"; u+("+newAxes[1][1]+")*v)"; String a="("+A+")";
             * String b="("+B+")"; String c="("+C+")"; String d="("+D+")"; String
             * e="("+E+")"; String f="("+F+")"; "+x+"^2 + "+b+"*"+x+"*"+y+" +
             * "+c+"*"+y+"^2 + "+ "+x+" + "+e+"*"+y+" + "+f; Polynomial
             * p=Polynomial.makePolynomial(new Expression(pString), false); int[]
             * one={1}; int[] two={2}; int[] ones={1,1}; String[]u={"u"}; String[]
             * v={"v"}; String[] uv={"u","v"}; double newB=p.getCoefficient(uv,
             * ones).evaluate(emptyHash); aPrime = p.getCoefficient(u,
             * two).evaluate(emptyHash); bPrime = p.getCoefficient(v,
             * two).evaluate(emptyHash); cPrime = p.getCoefficient(u,
             * one).evaluate(emptyHash); dPrime = p.getCoefficient (v,
             * one).evaluate(emptyHash);
             */

            aPrime = lambdas[iMax];
            bPrime = lambdas[1 - iMax];
            cPrime = newAxes[0][0] * D + newAxes[0][1] * E;
            dPrime = newAxes[1][0] * D + newAxes[1][1] * E;
            ePrime = F;

            alpha = Math.atan2(newAxes[0][1], newAxes[0][0]) * 180.0 / Math.PI;
            setTransformVariables();
        } // end if
        else {
            aPrime = A;
            bPrime = C;
            cPrime = D;
            dPrime = E;
            ePrime = F;
        } // end else
    } // end normalizeRotation

    private void completeSquare() {
        if (!ToleranceTester.isWithinToleranceOfZero(aPrime)) {
            u0 = -0.5 * cPrime / aPrime;
            ePrime -= (0.25 * cPrime * cPrime / aPrime);
            cPrime = 0.0;
        } // end if

        if (!ToleranceTester.isWithinToleranceOfZero(bPrime)) {
            v0 = -0.5 * dPrime / bPrime;
            ePrime -= (0.25 * dPrime * dPrime / bPrime);
            dPrime = 0.0;
        } // end if
    } // end completeSquare

    private void normalizeConstant() {
        if (ToleranceTester.isWithinToleranceOfZero(ePrime)) {
            ePrime = 0.0;
            return;
        } // end if

        double factor = -1.0 / ePrime;

        aPrime *= factor;
        bPrime *= factor;
        cPrime *= factor;
        dPrime *= factor;
        ePrime *= factor;
        norm = Math.sqrt(0.2 * (aPrime * aPrime + bPrime * bPrime + cPrime * cPrime + dPrime * dPrime + ePrime * ePrime));
        
        if (ToleranceTester.isWithinToleranceOfZero(aPrime)) aPrime = 0.0;
        if (ToleranceTester.isWithinToleranceOfZero(bPrime)) bPrime = 0.0;
        if (ToleranceTester.isWithinToleranceOfZero(cPrime)) cPrime = 0.0;
        if (ToleranceTester.isWithinToleranceOfZero(dPrime)) dPrime = 0.0;
        if (ToleranceTester.isWithinToleranceOfZero(ePrime)) ePrime = 0.0;
    } // end normalizeConstant

    private void classify() {
    	
    	//TODO: check here
        String[] temp = lhs.toExpression().varStrings;
        
        degree = lhs.getDegree();
        if (degree > 2) {
            reason = ClassificationFailureReason.DegreeGreatherThan2;
        } // end if

        if (temp.length > 2) {
            actualVariables = temp;
            reason = ClassificationFailureReason.TooManyVariables;
            return;
        } // end if

        if (!lhs.hasConstantCoefficients())
            reason = ClassificationFailureReason.NonPolynomial;

        userVariables = lhs.getVariables();

        int n = userVariables.length;

        switch (n) {
        case 2:
            actualVariables = userVariables; // adopt variables entered by user
            break;

        case 1:
            /* handle the polar case */
            if (userVariables[0].equals("r") || userVariables[0].equals("theta")) {
                actualVariables[0] = "r";
                actualVariables[1] = "theta";
                break;
            } // end if

            /*
             * if there's just one variable in the equation entered by the user, and that
             * variable is not ``y'', then the variable in the equation is the abscissa,
             * and ``y'' is the ordinate.
             */
            if (!userVariables[0].equals(actualVariables[1]))
                actualVariables[0] = userVariables[0];
            break;

        default:
            break;
        } // end switch

        if (actualVariables[0].equals("r") && actualVariables[1].equals("theta"))
            reason = ClassificationFailureReason.Polar;

        if (reason != ClassificationFailureReason.NoReason)
            return;

        transVars[0] = actualVariables[0];
        transVars[1] = actualVariables[1];

        int[] one = { 1 };
        int[] two = { 2 };
        int[] ones = { 1, 1 };
        String[] x = { actualVariables[0] };
        String[] y = { actualVariables[1] };
        String[] xy = actualVariables;

        A = lhs.getCoefficient(x, two).evaluate(emptyHash);
        B = lhs.getCoefficient(xy, ones).evaluate(emptyHash);
        C = lhs.getCoefficient(y, two).evaluate(emptyHash);
        D = lhs.getCoefficient(x, one).evaluate(emptyHash);
        E = lhs.getCoefficient(y, one).evaluate(emptyHash);
        F = lhs.getConstant().evaluate(emptyHash);
        norm = 0.17 * Math.sqrt(A * A + B * B + C * C + D * D + E * E + F * F);
        if (flunksInfinityTest())
            return;

        normalizeRotation();
        completeSquare();
        normalizeConstant();
        this.identity = IdentityComputer.computeIdentity(aPrime, bPrime, cPrime, dPrime, ePrime);
    } // end classify

    private boolean flunksInfinityTest() {
        if (norm != Double.POSITIVE_INFINITY && norm != Double.NaN)
            return false;

        double[] f = { A, B, C, D, E, F };
        int which = -1;
        int count = 0;

        for (int i = 0; i < f.length; i++)
            if (Math.abs(f[i]) == Double.POSITIVE_INFINITY || Math.abs(f[i]) == Double.NaN) {
                if (++count > 1)
                    return true;
                which = i;
            } // end if

        for (int i = 0; i < f.length; i++)
            f[i] = 0.0;

        f[which] = 1.0;

        A = f[0];
        B = f[1];
        C = f[2];
        D = f[3];
        E = f[4];
        F = f[5];
        norm = 1.0;
        return false;
    } // end flunksInfinityTest

    private static String prettyPrintLinearEquation(double[] coeffs, String[] vars, double t) {
        boolean leading = true;
        StringBuffer r = new StringBuffer();

        if (!ToleranceTester.isWithinTolerance(coeffs[0], t)) {
            r.append(makeCoefficient(coeffs[0], leading)).append("*").append(vars[0]);
            leading = false;
        } // end if

        if (!ToleranceTester.isWithinTolerance(coeffs[1], t)) {
            r.append(makeCoefficient(coeffs[1], leading)).append("*").append(vars[1]);
            leading = false;
        } // end if

        if (!ToleranceTester.isWithinTolerance(coeffs[2], t)) {
            r.append(makeCoefficient(coeffs[2], leading));
        }
        r.append(" = 0");
        return r.toString();
    } // end prettyPrintLinearEquation

    /**
     * Returns true if the floating point value is nearly an integer.
     * 
     * @param x a floating point value.
     * @return true if the floating point value is nearly an integer, false
     * otherwise.
     */
    public static boolean isNearlyInteger(double x) {
        return ToleranceTester.isWithinTolerance(x - Math.rint(x), Math.abs(x));
    } // end isNearlyInteger

    /**
     * Makes the array of floating point values an integer.
     * 
     * @param x floating point values.
     * @param lim the try limit.
     * @return an arrary of floating point values converted to an integer.
     */
    public static double[] makeInteger(double[] x, int lim) {
        int i, l, n = x.length;
        double mx = 0.0, t;
        double[] r = new double[n];

        for (i = 0; i < n; i++)
            if ((t = Math.abs(x[i])) > mx)
                mx = t;

        for (l = 0; l < n; l++)
            if (!ToleranceTester.isWithinTolerance(x[l], mx + Double.MIN_VALUE))
                break;

        if (l == n)
            return x;

        // double t=Math.abs(x[l])/x[l];
        t = 1.0 / x[l];

        for (i = 0; i < n; i++)
            x[i] *= t;

        for (i = 0; i < n; i++)
            r[i] = x[i];

        for (l = 1; l <= lim; l++) {
            for (i = 0; i < n; i++)
                if (!isNearlyInteger(l * r[i]))
                    break;
            if (i == n)
                break;
        } // end for l

        if (l > lim)
            return x;

        for (i = 0; i < n; i++)
            r[i] = Math.rint(l * r[i]);

        return r;
    } // end makeInteger

    private void setTransformVariables() {
        String[][] temp = {
                { "u", "v" },
                { "r", "s" },
                { "x", "y" }
        };
        int i, n = temp.length;

        for (i = 0; i < n; i++) {
            String[] t = temp[i];

            if (!(actualVariables[0].equals(t[0]) || actualVariables[0].equals(t[1]) || actualVariables[1].equals(t[0]) || actualVariables[1]
                    .equals(t[1]))) {
                transVars = t;
                return;
            } // end if
        } // end for i
    } // end setTransformVariables

} // end class QuadraticClassifier
