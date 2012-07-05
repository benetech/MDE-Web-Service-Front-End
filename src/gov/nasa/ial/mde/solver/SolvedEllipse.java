/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier.QuadraticType;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * Subclass of SolvedGraph responsible for recording features unique to ellipses.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedEllipse extends SolvedConic {
    
    /** Identify new features so we can access them with SolvedGraph.putFeature */
    protected String[] newFeatures = {
            "center",
            "focus",
            "focalLength",
            "eccentricity",
            "semiMajorAxis",
            "semiMinorAxis",
            "majorAxis",
            "minorAxis",
            "majorAxisInclination",
            "minorAxisInclination",
            "radius" // for the special case of a circle
    };
    
    // enums for major axis direction */
    private final static int NO_D = 0, HORIZONTAL = 1, VERTICAL = 2;
    
    private int majorAxisD = NO_D;

    /**
     * Constructs a solved ellipse for the specified analyzed equation.
     * 
     * @param equation the analyzed equation.
     */
    public SolvedEllipse(AnalyzedEquation equation) {
        super(equation);

        // specificFeatureNames = new String[newFeatures.length];
        // System.arraycopy(newFeatures, 0, specificFeatureNames, 0, newFeatures.length);

        /* QC is the QuadraticClassifier field in SolvedConic */
        double alpha = QC.getRotation(); // rotation angle in degrees
        /*
         * coeffs={a, b, c, d, e} where a(u-h)^2 + b(v-k)^2 + cu + dv + e = 0
         */
        double[] coeffs = QC.getNormalizedCoefficients();
        PointXY center = new PointXY(QC.UV2XY(QC.getTranslation()));
        String[] vars = analyzedEq.getActualVariables(); // saves a lot of typing
        double majorAxisInclination, minorAxisInclination, A, /* semi-major axis */
        B, /* semi-minor axis */
        C; /* focal length */

        putNewFeatures(newFeatures); // enable use of new features

        if (QC.getIdentity() == QuadraticType.SinglePoint) {
            putFeature("graphName", "single point"); // self-explanatory
            putFeature("center", center);
            return;
        }


        putFeature("center", center);
        putFeature("equationType", "conic section"); // ditto
        putFeature("graphClosure", "true"); // might be hard to determine in general

        /* normalize so that coeffs[4] = -1 in order to solve for A and B */
        for (int i = 0; i < 5; i++)
            coeffs[i] /= (-coeffs[4]);

        /* Satisfy the java compiler's obsessive need for initializations */
        A = B = -1.0;

        /*
         * coeffs[0] and coeffs[1] better both be positive. Determine horizontal/vertical direction
         * of major axis by which of 1/Math.sqrt(coeffs[0]) or 1/Math.sqrt(coeffs[1]) is larger.
         * horizontal case : (u-h)^2)/A^2 + (v-k)^2/B^2 = 1 vertical case: (v-k)^2/A^2 +
         * (u-h)^2/B^2 = 1 First make sure coeffs are compatible with design assumptions
         */
        if (coeffs[0] <= 0 || coeffs[1] <= 0 || coeffs[2] != 0.0 || coeffs[3] != 0.0)
            throw new IllegalArgumentException("SolvedEllipse instantiated with bad args.");
        if (coeffs[0] < coeffs[1]) {
            majorAxisD = HORIZONTAL;
            A = 1.0 / Math.sqrt(coeffs[0]);
            B = 1.0 / Math.sqrt(coeffs[1]);
        } // end if

        if (coeffs[1] < coeffs[0]) {
            majorAxisD = VERTICAL;
            A = 1.0 / Math.sqrt(coeffs[1]);
            B = 1.0 / Math.sqrt(coeffs[0]);
        } // end if

        /*
         * if coeffs[0] == coeffs[1] then we have a circle. In this case, all we need is a center
         * and radius. We already have the center, and we overwrite the graphName attribute, define
         * the radius domain and range, and quit
         */
        if (coeffs[0] == coeffs[1]) {
            putFeature("graphName", "circle");
            putFeature("radius", new NumberModel(A = 1.0 / Math.sqrt(coeffs[0])));
            putFeature("domain", new IntervalXY(vars[0], center.x - A, center.x + A));
            putFeature("range", new IntervalXY(vars[1], center.y - A, center.y + A));
            return;
        } // end if
        
        putFeature("graphName", "ellipse"); // self-explanatory
        /* might as well take care of the obvious */
        putFeature("semiMajorAxis", new NumberModel(A));
        putFeature("semiMinorAxis", new NumberModel(B));

        /*
         * Determine axis inclinations which depend on horizontal/vertical orientation
         */
        switch (majorAxisD) {
            case HORIZONTAL :
                majorAxisInclination = alpha;
                break;

            case VERTICAL :
                majorAxisInclination = alpha + 90.0;
                break;

            default :
                throw new IllegalStateException("Internal error in init");
        } // end switch

        minorAxisInclination = majorAxisInclination + 90.0;

        /* normalize angles between -179.9999 and 180.0 */
        majorAxisInclination = QuadraticClassifier.normalizeAngleInDegrees(majorAxisInclination);
        minorAxisInclination = QuadraticClassifier.normalizeAngleInDegrees(minorAxisInclination);

        putFeature ("majorAxisInclination", new NumberModel(majorAxisInclination));
        putFeature ("minorAxisInclination", new NumberModel(minorAxisInclination));

        /* on with the rest of the calculations -- focalLength */
        C = Math.sqrt(A * A - B * B);

        double[] focalDisplacement = { C * Math.cos(Math.PI * majorAxisInclination / 180.0), C * Math.sin(Math.PI * majorAxisInclination / 180.0)};
        PointXY F1 = center.sum(new PointXY(focalDisplacement));
        PointXY F2 = center.difference(new PointXY(focalDisplacement));

        putFeature("focalLength", new NumberModel(C));
        putFeature("eccentricity", new NumberModel(C / A));
        putFeature("focus", F1);
        addToFeature("focus", F2);
        putFeature("majorAxis", QuadraticClassifier.getEquationOfALine(center, majorAxisInclination, vars));
        putFeature("minorAxis", QuadraticClassifier.getEquationOfALine(center, minorAxisInclination, vars));

        /*
         * do domain and range for alpha = 0; otherwise it's a pretty complicated calculation
         */
        if (alpha == 0)
            switch (majorAxisD) {
                case HORIZONTAL :
                    putFeature("domain", new IntervalXY(vars[0], center.x - A, center.x + A));
                    putFeature("range", new IntervalXY(vars[1], center.y - B, center.y + B));
                    break;

                case VERTICAL :
                    putFeature("domain", new IntervalXY(vars[0], center.x - B, center.x + B));
                    putFeature("range", new IntervalXY(vars[1], center.y - A, center.y + A));
                    break;

                default : // no need to throw exception -- condition has already been tested
                    break;
            } // end switch
    } // end SolvedEllipse
    
} // end class SolvedEllipse
