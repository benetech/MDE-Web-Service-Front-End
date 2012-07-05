package gov.nasa.ial.mde.solver.features.individual;

import gov.nasa.ial.mde.solver.features.GraphFeature;

public interface SlopeFeature extends GraphFeature {
	public static String PATH = GraphFeature.GRAPH_DATA_PATH+"slope/";
	public static String KEY = "approximateDecimalValue";
	
	public double getSlope();
}
