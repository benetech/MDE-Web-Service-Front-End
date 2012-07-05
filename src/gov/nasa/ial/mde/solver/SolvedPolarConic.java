/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.numeric.PolarConicModel;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The class represents a solved Polar conic.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedPolarConic extends SolvedConic {
    
    /**
     * Constructs a solved polar conic for the specified analyzed equation.
     * 
     * @param ae the analyzed equation.
     */
    public SolvedPolarConic(AnalyzedEquation ae) {
        
        PolarClassifier pc = (PolarClassifier)ae.getClassifier();
        PolarConicModel pcm = (PolarConicModel)pc.getBestGuess();
        AnalyzedEquation ce = new AnalyzedEquation(pcm.getCartesianEquation());
        SolvedConic sc;
        
        switch (pcm.conicIdentity) {
            case Parabola :
                sc = new SolvedParabola(ce);
                break;

            case Ellipse :
                sc = new SolvedEllipse(ce);
                break;

            case Hyperbola :
                sc = new SolvedHyperbola(ce);
                break;

            default :
                sc = null;
        } // end switch

        copyFrom(sc);
        putNewFeature("equationPrint", ae.printEquation());
        if (ae.getParameters().length > 0)
            putNewFeature("originalEquationPrint", ae.printOriginalEquation());
        putNewFeature("equationType", "polar form of a conic section");
    } // end SolvedPolarConic
    
} // end class SolvedPolarConic
