/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.classifier;

import gov.nasa.ial.mde.math.Bounds;
import gov.nasa.ial.mde.solver.SolvedEquationData;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.SolvedRationalFunction;
import gov.nasa.ial.mde.solver.SolvedXYGraph;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;

/**
 * A parent class for all MDE classifiers. These classes resolve various types 
 * of equations into cannonical forms and perform other analyses.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MDEClassifier {

    /**
     * Default Constructor.
     */
    public MDEClassifier() {
        super();
    }

    /**
     * The default features for an analyzed-equation.
     * 
     * @param analyzedEq the analyzed equation.
     * @return the solved graph features for the analyzed equation.
     */
    public SolvedGraph getFeatures(AnalyzedEquation analyzedEq) {
        SolvedGraph features;

    	System.out.println("MARK: "+ analyzedEq.getInputEquation());
        
        if (analyzedEq.isSolvableFunction()) {
            if (!analyzedEq.isPolynomial()){
            	features = new SolvedEquationData(analyzedEq);
            }
            else
            {
                features = new SolvedRationalFunction(analyzedEq);
            }
        } // end if
        else{
            features = new SolvedXYGraph(analyzedEq);
        }

        // Make sure we add the graphBoundaries feature.
        addGraphBoundariesFeature(analyzedEq, features);

        return features;
    } // end getFeatures

    /**
     *  The default features for analyzed-data.
     *  
     * @param analyzedData the analyzed data.
     * @return the solved graph features for the analyzed data.
     */
    public SolvedGraph getFeatures(AnalyzedData analyzedData) {
		SolvedGraph features = new SolvedEquationData(analyzedData);

        // Make sure we add the graphBoundaries feature.
        addGraphBoundariesFeature(analyzedData, features);

        return features;
    } // end getFeatures

    /**
     * The XML for the graphBoundaries.
     * 
     * @param item the analyzed item.
     * @param features the solved graph features for the analyzed item.
     */
    public void addGraphBoundariesFeature(AnalyzedItem item, SolvedGraph features) {
        if ((features != null) && (item != null)) {
            Bounds b = item.getPreferredBounds();
            String s = "x = " + b.left + " to " + b.right + " and y = " + b.bottom + " to " + b.top;
            features.putFeature("graphBoundaries", s);
        }
    }

} // end class MDEClassifier
