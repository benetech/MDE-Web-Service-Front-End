/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The class represents a solved function for two intersecting lines.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedTwoIntersectingLines extends SolvedConic {

    /** Identify new features so we can access them with SolvedGraph.putFeature */
    private String[] newFeatures = { "intersectionPoint", "inclination", "equationStrings" };

    /**
     * Constructs a solved function for two intersecting lines from the specified
     * analyzed equation.
     * 
     * @param e the analyzed equation.
     */
    public SolvedTwoIntersectingLines(AnalyzedEquation e) {
        super(e);
        // specificFeatureNames = new String[newFeatures.length];
        // System.arraycopy(newFeatures, 0, specificFeatureNames, 0, newFeatures.length);

        /* QC is the QuadraticClassifier field in SolvedConic */
        double alpha = QC.getRotation(); // rotation angle in degrees

        /*
         * coeffs={a, b, c, d, e} where a(u-h)^2 + b(v-k)^2 + cu + dv + e = 0
         */
        double[] coeffs = QC.getNormalizedCoefficients();
        double a = Math.sqrt(Math.abs(coeffs[0]));
        double b = Math.sqrt(Math.abs(coeffs[1]));
        PointXY intersectionPoint = new PointXY(QC.UV2XY(QC.getTranslation()));
        String[] vars = analyzedEq.getActualVariables(); // saves a lot of typing
        double phi = 180.0 * Math.atan2(a, b) / Math.PI;
        double i1 = alpha - phi, i2 = alpha + phi;

        putNewFeatures(newFeatures);
        putFeature("graphName", "two intersecting lines");
        putFeature("intersectionPoint", intersectionPoint);
        putFeature("inclination", new NumberModel(i1));
        addToFeature("inclination", new NumberModel(i2));

        putFeature("equationStrings", QuadraticClassifier.getEquationOfALine(intersectionPoint, i1, vars));
        addToFeature("equationStrings", QuadraticClassifier.getEquationOfALine(intersectionPoint, i2, vars));
    } // end SolvedTwoIntersectingLines


//    public static void main(String[] args) {
//        SolvedTwoIntersectingLines stil;
//
//        stil = new SolvedTwoIntersectingLines(new AnalyzedEquation("(x-2y+1)*(2x+y+1) = 0"));
//        System.out.println(stil.toString());
//        stil = new SolvedTwoIntersectingLines(new AnalyzedEquation("4*(2x-y+1)^2 = (x+2y-1)^2"));
//        System.out.println(stil.toString());
//    } // end main

} // end Class SolvedTwoIntersectingLines
