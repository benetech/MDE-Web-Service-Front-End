package gov.nasa.ial.mde.solver.features.individual;

import gov.nasa.ial.mde.solver.features.GraphFeature;

public interface YInterceptFeature extends GraphFeature {
	public static String PATH = GraphFeature.GRAPH_DATA_PATH;
	public static String KEY = "yIntercepts";
	
	public Double[] getYIntercepts();
}
