/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier.QuadraticType;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * Handles degenerate case of two parallel lines.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedTwoLines extends SolvedConic {

    /** Edentify new features so we can access them with */
    protected String[] newFeatures = { "inclination", "separation", "equationStrings" };
    
    private QuadraticType ID;
    private double alpha;
    private double inclination;
    private double separation;
    private String[] vars;

    /**
     * Constructs a solved two lines for the specified analyzed equation.
     * 
     * @param equation the analyzed equation.
     */
    public SolvedTwoLines(AnalyzedEquation equation) {
    	super(equation);

    	
        /* QC is the QuadraticClassifier field in SolvedConic */
        ID = QC.getIdentity();
        alpha = QC.getRotation(); // rotation angle in degrees     
        vars = equation.getActualVariables();
    
        

        putNewFeatures(newFeatures); // enable use of new features
        putFeature("graphName", "two lines"); // self-explanatory
        putFeature("equationType", "degenerate parabola"); // ditto
        putFeature("graphClosure", "false"); // might be hard to determine in general

        switch (ID) {
            case TwoHorizontalLines :
                inclination = alpha;
                break;

            case TwoVerticalLines :
                inclination = alpha + 90.0;
                break;

            default :
                throw new IllegalArgumentException("Invalid ID for SolvedTwoLines");
        } // end switch
        
        putFeature("inclination", new NumberModel(inclination));

        if (Math.abs(inclination) <= 45.0) {
            double d = 0.0;

            if (yInts.length == 2)
                d = Math.abs(yInts[0] - yInts[1]);
            if (MdeSettings.DEBUG)
                System.out.println("d: " + d);
            separation = d * Math.cos(Math.PI * inclination / 180.0);
            if (MdeSettings.DEBUG)
                System.out.println("separation: " + separation);
            //Need to put approximately zero for separation if it's not truly zero
            //or you got the solution is two parallel lines a distance of 0 units apart.
            putFeature("separation", new NumberModel(separation));
            putFeature("equationStrings", QuadraticClassifier.getEquationOfALine(new PointXY(0.0, yInts[0]), inclination, vars));
            if (yInts.length == 2)
                addToFeature("equationStrings", QuadraticClassifier.getEquationOfALine(new PointXY(0.0, yInts[1]), inclination, vars));
        } // end if
        else {
            double d = 0.0;

            if (xInts.length == 2)
                d = Math.abs(xInts[0] - xInts[1]);
            if (MdeSettings.DEBUG)
                System.out.println("d: " + d);
            separation = d * Math.abs(Math.sin(Math.PI * inclination / 180.0));
            if (MdeSettings.DEBUG)
                System.out.println("separation: " + separation);
            putFeature("separation", new NumberModel(separation));
            putFeature("equationStrings", QuadraticClassifier.getEquationOfALine(new PointXY(xInts[0], 0.0), inclination, vars));
            if (xInts.length == 2)
                addToFeature("equationStrings", QuadraticClassifier.getEquationOfALine(new PointXY(xInts[1], 0.0), inclination, vars));
        } // end else
    } // end SolvedTwoLines


//    public static void main(String[] args) {
//        SolvedTwoLines stl = new SolvedTwoLines(new AnalyzedEquation("(x-2y)^2=4"));
//
//        System.out.println(stl.toString());
//        stl = new SolvedTwoLines(new AnalyzedEquation("(2x-y)^2 = 9"));
//        System.out.println(stl.toString());
//        stl = new SolvedTwoLines(new AnalyzedEquation("y^2 - 3y + 2= 0"));
//        System.out.println(stl.toString());
//        stl = new SolvedTwoLines(new AnalyzedEquation("x^2 - 3x + 2= 0"));
//        System.out.println(stl.toString());
//    } // end main

} // end class SolvedTwoLines
