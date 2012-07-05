package gov.nasa.ial.mde.solver.features.individual;

import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.features.GraphFeature;

public interface MaximaFeature extends GraphFeature {
	public static String PATH = GraphFeature.GRAPH_DATA_PATH;
	public static String KEY = "Maxima";
	
	public PointXY[] getMaxima();
	public boolean hasMaxima();
	public boolean canCalculateMaxima();
}
