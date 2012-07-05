/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Feb 26, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.AngleModel;
import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.numeric.PolarLemniscateModel;
import gov.nasa.ial.mde.solver.numeric.PolarModel;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The class represents a solved Polar lemniscate.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedPolarLemniscate extends SolvedGraph {
    
	private double A; // coefficient of cos(2*theta)
	private double B; // coefficient of sin(2*theta)
	private double theta; // angle figure is rotated from X-axis
	private double length; // length of blades

    /** Identify new features so we can access them with SolvedGraph.putFeature */
	protected String[] newFeatures = {"inclination", "bladeLength"};

	/**
     * Constructs a solved Polar lemniscate from the specified analyzed equation.
     * 
	 * @param equation the analyzed equation.
	 */
	public SolvedPolarLemniscate(AnalyzedEquation equation) {
		AngleModel inc = new AngleModel();
		PolarClassifier pc = (PolarClassifier) equation.getClassifier();
		PolarLemniscateModel plm = (PolarLemniscateModel) pc.getBestGuess();

		putNewFeatures(newFeatures); // enable use of new features
		A = -plm.modelVector[1] / plm.modelVector[0];
		B = -plm.modelVector[2] / plm.modelVector[0];
		length = Math.sqrt(PolarModel.amplitude(A, B));
		theta = 0.5 * PolarModel.phaseInRads(A, B);
		inc.setAngleInRads(theta);
		putFeature("graphName", "polar lemniscate");
		putFeature("equationPrint", equation.printEquation());
		putFeature("bladeLength", new NumberModel(length));
		putFeature("inclination", inc.getMFN());
	}
    
}
