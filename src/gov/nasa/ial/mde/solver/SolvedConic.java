/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The parent class for all conic sections.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedConic extends SolvedXYGraph {
    
    /** The quadratic classifier. */
    protected QuadraticClassifier QC;

    /**
     * Default constructor.
     */
    public SolvedConic() {
        super();
    }

    /**
     * Constructs a solved conic for the specified analyzed equation.
     * 
     * @param e the analyzed equation.
     */
    public SolvedConic(AnalyzedEquation e) {
        super(e);
        QC = (QuadraticClassifier) e.getClassifier();
    } // end SolvedQuadratic
    
} // end class SolvedConic
