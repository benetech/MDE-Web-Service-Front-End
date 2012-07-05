package gov.nasa.ial.mde.solver.features.individual;

import gov.nasa.ial.mde.solver.features.GraphFeature;

public interface FocalLengthFeature extends GraphFeature{
	public static String PATH = GraphFeature.GRAPH_DATA_PATH+"/focalLength";
	public static String KEY = "decimalValue";
	
	public Double getFocalLength();
}

