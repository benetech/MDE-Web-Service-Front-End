/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 2, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.AngleModel;
import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.numeric.PolarModel;
import gov.nasa.ial.mde.solver.numeric.PolarTrochoidModel;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * The class represents a solved Polar trochoid.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedPolarTrochoid extends SolvedGraph {
    
    /** Identify new features so we can access them with SolvedGraph.putFeature */
	protected String[] newFeatures = {
            "thetaMultiple", "hasLoops", "maxLength", "isConvex", "minLength", 
            "loopAngles", "axis", "axisInclination", "oddMultiple" };
    
	private final static double EPSILON = 1.0e-8;
	private final static int CARDIOID = 0, LOOPY = 1, LUMPY = 2;
	private double A, B, phi; // r = A*cos(n*(theta-phi)) + B

	/**
     * Constructs a solved Polar Trochoid from the specified analyzed equation.
     * 
	 * @param ae the analyzed equation.
	 */
	public SolvedPolarTrochoid(AnalyzedEquation ae) {
		PolarClassifier pc = (PolarClassifier) ae.getClassifier();
		PolarTrochoidModel ptm = (PolarTrochoidModel) pc.getBestGuess();
		int n = ptm.whichSignature + 1;
		double[] mv = ptm.modelVector;

		putFeature("equationPrint", ae.printEquation());

		if (mv[1] == 0.0) {
			putFeature("graphName",
					"collection of radial lines through the origin");
			return;
		} // end if

		for (int i = 0; i < mv.length; i++)
			if (i != 1)
				mv[i] /= mv[1];
		mv[1] = 1.0;

		int loopCase; // flag for loops, lumps or cardioid
		double a = -mv[2];
		double b = -mv[3];

		A = PolarModel.amplitude(a, b);
		phi = PolarModel.phaseInRads(a, b) / n;
		B = -mv[0];
		/***********************************************************************
		 * Nasty kludge to avoid writing a lot of extra code Basis: To make r =
		 * A*cos(n*(theta-phi)) - B into r = A*cos(n*(theta-psi)) + B, we need
		 * to add PI/n to phi in order to reverse the sign of the cosine
		 * function, and then replace r with -r and theta with theta-PI
		 **********************************************************************/
		if (B < 0.0) {
			B = -B;
			phi += Math.PI * ((n & 1) - 1.0 / n);
		} // end if

		putNewFeatures(newFeatures); // enable use of new features
		putFeature("maxLength", new NumberModel(A + B));
		putFeature("thetaMultiple", "" + n);
		if ((n & 1) == 1)
			putFeature("oddMultiple", "true");
		else
			putFeature("oddMultiple", "false");

		if (Math.abs(A - B) < EPSILON * (Math.abs(A) + Math.abs(b)))
			loopCase = SolvedPolarTrochoid.CARDIOID;
		else if (A > B)
			loopCase = SolvedPolarTrochoid.LOOPY;
		else {
			loopCase = SolvedPolarTrochoid.LUMPY;
			if (B < (n * n + 1.0) * A)
				putFeature("isConvex", "false");
			else
				putFeature("isConvex", "true");
		} // end else

		if (n == 1)
			switch (loopCase) {
				case SolvedPolarTrochoid.CARDIOID : {
					AngleModel am = new AngleModel();

					am.setAngleInRads(phi);
					putFeature("axis", SolvedGraph.getCompassDir(180.0 + am
							.getDegrees())
							+ " to "
							+ SolvedGraph.getCompassDir(am.getDegrees()));
					putFeature("axisInclination", am.getMFN());
					putFeature("graphName", "cardioid");
					return;
				} // end block

				case SolvedPolarTrochoid.LOOPY : {
					AngleModel am = new AngleModel();

					am.setAngleInRads(phi);
					putFeature("axis", SolvedGraph.getCompassDir(180.0 + am
							.getDegrees())
							+ " to "
							+ SolvedGraph.getCompassDir(am.getDegrees()));
					putFeature("axisInclination", am.getMFN());
					putFeature("graphName", "loopWithinALoop");
					putFeature("minLength", new NumberModel(A - B));
					return;
				} // end block

				case SolvedPolarTrochoid.LUMPY : {
					AngleModel am = new AngleModel();

					am.setAngleInRads(phi + Math.PI);
					putFeature("axis", SolvedGraph.getCompassDir(180.0 + am
							.getDegrees())
							+ " to "
							+ SolvedGraph.getCompassDir(am.getDegrees()));
					putFeature("axisInclination", am.getMFN());
					putFeature("graphName", "eccentricCircle");
					putFeature("minLength", new NumberModel(B - A));
					return;
				} // end block

				default :
					throw new IllegalStateException(
							"This ain't supposed to happen");
			} // end switch

		/* general case -- n > 1 */
		switch (loopCase) {
			case SolvedPolarTrochoid.CARDIOID :
				putFeature("graphName", "pinchedLoops");
				putNewFeature("loopAngles", "graphObject", "loops", true); // make
																		   // a
																		   // new
																		   // node
				putNewFeature("loopAngles", "angleInfo", null, false); // no new
																	   // node
				for (int i = 0; i < n; i++) {
					AngleModel am = new AngleModel();

					am.setAngleInRads(phi + 2.0 * i * Math.PI / n);
					putFeature("loopAngles", "angleInfo", am.getMFN());
				} // end for i
				return;

			case SolvedPolarTrochoid.LUMPY :
				putFeature("graphName", "lumpyCircle");
				putFeature("minLength", new NumberModel(B - A));
				putNewFeature("loopAngles", "graphObject", "bulges", true); // make
																			// a
																			// new
																			// node
				putNewFeature("loopAngles", "angleInfo", null, false); // no new
																	   // node

				for (int i = 0; i < n; i++) {
					AngleModel am = new AngleModel();

					am.setAngleInRads(phi + 2.0 * i * Math.PI / n);
					putFeature("loopAngles", "angleInfo", am.getMFN());
				} // end for i

				if (B < (n * n + 1.0) * A) {
					putNewFeature("loopAngles", "graphObject", "dents", true); // make
																			   // new
																			   // node
					putNewFeature("loopAngles", "angleInfo", null, false); // no
																		   // new
																		   // node

					for (int i = 0; i < n; i++) {
						AngleModel am = new AngleModel();

						am.setAngleInRads(phi + (2.0 * i - 1.0) * Math.PI / n);
						putFeature("loopAngles", "angleInfo", am.getMFN());
					} // end for i
				} // end if

				return;

			case SolvedPolarTrochoid.LOOPY :
				putNewFeature("loopAngles", "graphObject", "longer loops", true);
				putNewFeature("loopAngles", "angleInfo", null, false);

				for (int i = 0; i < n; i++) {
					AngleModel am = new AngleModel();

					am.setAngleInRads(phi + 2.0 * i * Math.PI / n);
					putFeature("loopAngles", "angleInfo", am.getMFN());
				} // end for i

				if ((n & 1) == 0) {
					putFeature("graphName", "alternatingLoops");
					putNewFeature("loopAngles", "graphObject", "shorter loops",
							true); // make new node
					putNewFeature("loopAngles", "angleInfo", null, false);

					for (int i = 0; i < n; i++) {
						AngleModel am = new AngleModel();

						am.setAngleInRads(phi + (2.0 * i - 1.0) * Math.PI / n);
						putFeature("loopAngles", "angleInfo", am.getMFN());
					} // end for i
				} // end if
				else
					putFeature("graphName", "nestedLoops");

				putFeature("minLength", new NumberModel(A - B));
				return;

			default :
				throw new IllegalStateException("This can't be happening");
		} // end switch
	} // end PolarTrochoidModel

}
