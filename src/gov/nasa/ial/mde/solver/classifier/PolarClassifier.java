/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.classifier;

import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.SolvedPolarConic;
import gov.nasa.ial.mde.solver.SolvedPolarLemniscate;
import gov.nasa.ial.mde.solver.SolvedPolarLine;
import gov.nasa.ial.mde.solver.SolvedPolarRose;
import gov.nasa.ial.mde.solver.SolvedPolarTrochoid;
import gov.nasa.ial.mde.solver.numeric.PolarModel;
import gov.nasa.ial.mde.solver.numeric.PolarModelBuilder;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

import java.util.ArrayList;

/**
 * A classifier for Polar functions.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarClassifier extends MDEClassifier {
    
    /** Constant for a type of Polar function. */
    public final static int POLAR_UNKNOWN = 0, 
                            POLAR_CONIC = 1, 
                            POLAR_LINE = 2, 
                            POLAR_ROSE = 3, 
                            POLAR_TROCHOID = 4, 
                            POLAR_LEMNISCATE = 5, 
                            POLAR_ENCHILADA = 6;
    
    private PolarModelBuilder pmb = new PolarModelBuilder();
    private PolarModel bestGuess = null;

    /**
     * Incorporates available polar data points and performs classification.
     * 
     * @param polarPoints the array of polar points defining the curve.
     * @param worstFit the largest log base 10 residual for a model.
     */
    public PolarClassifier(MultiPointXY[] polarPoints, double worstFit) {
        int i, n = polarPoints.length;

        for (i = 0; i < n; i++)
            pmb.addNewPoint(polarPoints[i]);

        PolarModel[] rpm = pmb.getRankedModels();
        ArrayList<PolarModel> finalists = new ArrayList<PolarModel>();

        n = rpm.length;
        for (i = 0; i < n; i++) {
            if (rpm[i].fit > worstFit)
                break;

            finalists.add(rpm[i]);
        } // end for i

        if ((n = finalists.size()) == 0)
            return;

        bestGuess = finalists.get(0);

        for (i = 1; i < n; i++) {
            PolarModel t = finalists.get(i);

            if (t.complexity < bestGuess.complexity)
                bestGuess = t;
        } // end for i
    } // end PolarClassifier

    /**
     * Creates a polor classifier for thte given polar points.
     * 
     * @param polarPoints the points of the polar function.
     */
    public PolarClassifier(MultiPointXY[] polarPoints) {
        this(polarPoints, -10.0);
    } // end PolarClassifier

    /**
     * Returns the best guess at the polar model.
     * 
     * @return the best guess at the polar model.
     */
    public PolarModel getBestGuess() {
        return bestGuess;
    } // end getBestGuess
    
    /**
     * Returns the solved graph features for the polar analyzed equation.
     * 
     * @param analyzedEquation the analyzed equation.
     * @see gov.nasa.ial.mde.solver.classifier.MDEClassifier#getFeatures(gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation)
     */
    public SolvedGraph getFeatures(AnalyzedEquation analyzedEquation) {
        SolvedGraph features = null;

        if (bestGuess != null) {
            switch (bestGuess.identity) {
                case PolarClassifier.POLAR_LINE :
                    features = new SolvedPolarLine(analyzedEquation);
                    break;
    
                case PolarClassifier.POLAR_CONIC :
                    features = new SolvedPolarConic(analyzedEquation);
                    break;
                    
                    case PolarClassifier.POLAR_ROSE :
                    features = new SolvedPolarRose(analyzedEquation);
                    break;
                    
                    case PolarClassifier.POLAR_LEMNISCATE :
                    features = new SolvedPolarLemniscate(analyzedEquation);
                    break;
                    
                    case PolarClassifier.POLAR_TROCHOID :
                    features = new SolvedPolarTrochoid(analyzedEquation);
            } // end switch
        }
        
        if (features == null) {
            // Use the default features.
            features = super.getFeatures(analyzedEquation);
        }
        
		// Make sure we add the graphBoundaries feature.
        addGraphBoundariesFeature(analyzedEquation,features);
        return features;
    } // end getFeatures
    

//    // Main routine for testing purposes
//    // @param args the input equation as one or more discrete strings
//    public static void main(String[] args) {
//        AnalyzedEquation ae = new AnalyzedEquation(
//            gov.nasa.ial.mde.util.StringSplitter.combineArgs(args));
//        try {
//            PolarClassifier pc = (PolarClassifier)ae.getClassifier();
//            System.out.println("Equation is a " + pc.getBestGuess());
//        } // end try
//        catch (ClassCastException cce) {
//            System.out.println("Not a polar equation.");
//        } // end catch
//    } // end main

} // end class PolarClassifier
