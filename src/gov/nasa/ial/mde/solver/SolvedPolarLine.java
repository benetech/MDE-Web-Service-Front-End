/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The class represents a solved Polar line.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedPolarLine extends SolvedLine {
    
    /**
     * Constructs a solved Polar line from the specified analyzed equation.
     * 
     * @param ae the analyzed equation.
     */
    public SolvedPolarLine(AnalyzedEquation ae) {
        /* Kludge to obviate need for a good default constructor for parent */
        super(new AnalyzedEquation("y = x"));

        copyFrom(new SolvedLine(new AnalyzedEquation(getCartesianEquationString(ae))));
        putNewFeature("equationPrint", ae.printEquation());
        if (ae.getParameters().length > 0)
            putFeature("originalEquationPrint", ae.printOriginalEquation());
        putNewFeature("equationType", "polar form of a line");
    } // end Class SolvedPolarLine

    private String getCartesianEquationString(AnalyzedEquation ae) {
        // return "2x + 3y = 6";
        PolarClassifier pc = (PolarClassifier)ae.getClassifier();
        double[] mv = pc.getBestGuess().modelVector;

        return mv[1] + "*x+(" + mv[2] + ")*y+(" + mv[0] + ")=0";
    } // end getCartesianEquationString
    
} // end class SolvedPolarLine
