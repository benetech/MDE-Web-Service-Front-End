/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Feb 20, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.AngleModel;
import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointRT;
import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.numeric.PolarModel;
import gov.nasa.ial.mde.solver.numeric.PolarRoseModel;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The class represents a solved polar rose.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedPolarRose extends SolvedGraph {
    
	private double A; // coefficient of cos(n*theta)
	private double B; // coefficient of sin(n*theta)
	private int numPetals; // what do you think
	private double theta; // inclination of 1st petal in deg.
	private double petalLength; // what do you think
    
    /** Identify new features so we can access them with SolvedGraph.putFeature */
	protected String[] newFeatures = {
            "numPetals", "petalLength", "petalInclinations", "petalTips" };

	/**
     * Constructs a solved polar rose from the specified analyzed equation.
     * 
	 * @param ae the analyzed equation.
	 */
	public SolvedPolarRose(AnalyzedEquation ae) {
		PolarClassifier pc = (PolarClassifier) ae.getClassifier();
		PolarRoseModel prm = (PolarRoseModel) pc.getBestGuess();
		int n = prm.whichSignature + 1;

		putNewFeatures(newFeatures); // enable use of new features
		numPetals = ((n & 1) == 0) ? 2 * n : n;
		A = -prm.modelVector[1] / prm.modelVector[0];
		B = -prm.modelVector[2] / prm.modelVector[0];
		petalLength = PolarModel.amplitude(A, B);
		theta = PolarModel.phaseInRads(A, B) / n;

		putFeature("graphName", "polar rose"); // self-explanatory
		putFeature("equationPrint", ae.printEquation());
		putFeature("numPetals", "" + numPetals);
		putFeature("petalLength", new NumberModel(petalLength));
		putNewFeature("petalInclinations", "angleInfo", null, true); // make a
																	 // new node
		putNewFeature("petalTips", "pointInfo", null, true); // make a new node
		for (int i = 0; i < numPetals; i++) {
			double r = theta + 2.0 * Math.PI * i / numPetals;
			AngleModel a = new AngleModel();

			a.setAngleInRads(r);
			putFeature("petalInclinations", "angleInfo", a.getMFN());
			putFeature("petalTips", "pointInfo", new PointRT(petalLength, r).toCartesian());
		} // end for i
	} // end SolvedPolarRose
    
} // end class SolvedPolarRose
