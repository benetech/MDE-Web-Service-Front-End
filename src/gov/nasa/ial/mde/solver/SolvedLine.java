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
import gov.nasa.ial.mde.solver.features.individual.SlopeFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * Subclass of SolvedGraph responsible for recording features unique to lines.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedLine extends SolvedConic implements SlopeFeature {
    
    /** Identify new features so we can access them with SolvedGraph.putFeature */
    protected String[] newFeatures = { 
                "slope",
                "inclination",
                "slopeDefined",
                "incrad", // inclination in radians
                "reducedEquation" // in case the original is in an oddball form
    };
    
    /**
     * Creates a solved line from the specified analyzed equation.
     * 
     * @param equation the analyzed equation.
     */
    public SolvedLine(AnalyzedEquation equation) {
        super(equation);

        /*
         * QC is the QuadraticClassifier field in SolvedConic ID is one of the values associated
         * with lines e.g. QuadraticClassifier.SLOPING_LINE
         */
        QuadraticClassifier.QuadraticType ID = QC.getIdentity();
        double alpha = QC.getRotation(); // rotation angle in degrees
        /*
         * Get the initial polynomial coefficients -- coeffs={a,b,c,d,e,f} where
         * a*x^2+b*x*y+c*y^2+d*x+e*y+f=0 specifies the equation
         */
        double[] coeffs = QC.getOriginalCoefficients();
        putNewFeatures(newFeatures); // enable use of new features
        //putFeature("graphName", "line"); // self-explanatory
        putFeature("equationType", "linear equation"); // ditto
        putFeature("graphClosure", "false"); // might be hard to determine in general
        /*
         * First take out rotation. This section determines how the equation should look if it were
         * presented in cannonical form, i.e. equation defined by coeffs[3]*x+coeffs[4]*y+coeffs[5] = 0
         * Note that the rotated case can only be recognized when the user enters something like
         * (a*x + b*y + c)^2 = 0 in which the cannonical form is squared and equated to 0. This
         * confuses the QC and requires treatment of this case to find the cannonical coefficients
         */
        if (alpha != 0.0) {
            double[] offsets = QC.getTranslation();
            double[][] newAxes = QC.getNewAxes();
            switch (ID) {
                case HorizontalLine :
                    /*******************************************************************************
                     * i.e bPrime and v = newAxis[1][0*x + newAxis[1][1]*y
                     */
                    coeffs[3] = newAxes[1][0];
                    coeffs[4] = newAxes[1][1];
                    coeffs[5] = -offsets[1];
                    break;
                case VerticalLine :
                    /*******************************************************************************
                     * i.e. aPrime where u = newAxis[0][0]*x + newAxis[0][1]*y
                     */
                    coeffs[3] = newAxes[0][0];
                    coeffs[4] = newAxes[0][1];
                    coeffs[5] = -offsets[0];
                    break;
                default :
                    /*
                     * The only other single-line case is QC.SLOPING_LINE which is not recognized
                     * in the rotated case
                     */
                    throw new IllegalArgumentException("ID " + ID + "illegal for rotated case.");
            } // end switch
            /*
             * the equation must have been entered by the user in a funky form, so unscramble
             */
            putFeature(
                "reducedEquation",
                QuadraticClassifier.getEquationOfALine(new PointXY(0.0, -coeffs[5] / coeffs[4]), alpha, analyzedEq.getActualVariables()));
            /*
             * to complete the fakery, we need to tell doLineFeatures that it's a sloping line,
             * which it is
             */
            ID = QuadraticType.SlopingLine;
        } // end if
        else if (coeffs[0] != 0.0 || coeffs[2] != 0.0) {
            /*
             * No rotation, but there are still quadratic terms, so the user has entered something
             * like (x-2)^2 = 0. If we do nothing, then coeffs will look like coeffs = {1, 0, 0, 4, 0, 4}
             * and the x-intercept will drop out as 1. Wrong! The answer is 2! Grab offsets to
             * modify coeffs appropriately
             */
            double[] offsets = QC.getTranslation();
            switch (ID) { // we're in an else, so ID hasn't been dittled
                case HorizontalLine :
                    /* const*(y-offsets[1])^2 = 0 */
                    coeffs[3] = 0.0; // shouldn't need to do this
                    coeffs[4] = 1.0;
                    coeffs[5] = -offsets[1];
                    putFeature(
                        "reducedEquation",
                        QuadraticClassifier.getEquationOfALine(new PointXY(0.0, offsets[1]), 0.0, analyzedEq.getActualVariables()));
                    break;
                case VerticalLine :
                    /* const*(x-offset[0])^2 = 0 */
                    coeffs[3] = 1.0;
                    coeffs[4] = 0.0; // shouldn't need to do this
                    coeffs[5] = -offsets[0];
                    putFeature("reducedEquation", QuadraticClassifier.getEquationOfALine(offsets[0], analyzedEq.getActualVariables()));
                    break;
                default :
                    throw new IllegalStateException("We should never be here.");
            } // end switch
        } // end if
        /*
         * Should work with cannonical coeffs or those appropriately modified for special cases
         */
        doLineFeatures(coeffs[3], coeffs[4], coeffs[5], ID);
       // getXIntercepts();
       // getDomain();
       // getRange();
       // getSlope();
       //System.out.println(getXMLString());
    } // end SolvedLine
    
    private void doLineFeatures(double a, double b, double c, QuadraticClassifier.QuadraticType ID) {
        double M = 0.0; // slope
        double z; // atan of slope i.e. incrad
        IntervalXY D; // domain
        IntervalXY R; // range
        D = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
        R = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        R.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
        if ((ID == QuadraticType.SlopingLine) || (ID == QuadraticType.HorizontalLine)) {
            putFeature("slopeDefined", "true");
            putFeature("slope", new NumberModel(M = -a / b));
            putFeature("incrad", new NumberModel(z = Math.atan(M)));
            putFeature("inclination", new NumberModel(180.0 * z / Math.PI));
            putFeature("domain", D);
        }
        switch (ID) {
            case SlopingLine :
                putFeature("graphName", "line"); // self-explanatory
                if (M > 0.0)
                    putFeature("ascendingRegions", D);
                if (M < 0.0)
                    putFeature("descendingRegions", D);
                putFeature("range", R);
                break;
            case HorizontalLine : // no xIntercept
                putFeature("graphName", "horizontal line"); // self-explanatory
                putFeature("range", new IntervalXY(analyzedEq.getActualVariables()[1], -c / b, -c / b));
                break;
            case VerticalLine : // handle as a separate case
                putFeature("graphName", "vertical line"); // self-explanatory
                putFeature("slopeDefined", "false");
                putFeature("inclination", new NumberModel(90.0));
                putFeature("incrad", new NumberModel(Math.PI/2.0));
                //putFeature("incrad", "pi/2");
                putFeature("domain", new IntervalXY(analyzedEq.getActualVariables()[0], -c / a, -c / a));
                putFeature("range", R);
                /*
                 * putFeature ("reducedEquation", QuadraticClassifier.getEquationOfALine(-c/a,
                 * analyzedEquation.getActualVariables()));
                 */
                break;
            default :
                throw new IllegalArgumentException("Bad identity for a line: ID = " + ID);
        } // end switch
    } // end doLineFeatures

	public double getSlope() {
		Object value = this.getValue(SlopeFeature.PATH, SlopeFeature.KEY);
		String slopeString = (String)value;
		double slope = new Double(slopeString);
		//System.out.println("Slope is: " + slope);
		return slope;
	}
	
	public double getYIntercept()
	{
		Double[] array = getYIntercepts();
		return array[0];
	}
	public double getXIntercept()
	{
		Double[] array = getXIntercepts();
		return array[0];
	}
    
} // end class SolvedLine
