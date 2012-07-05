package gov.nasa.ial.mde.solver.features.individual;

import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.features.GraphFeature;

public interface VertexFeature extends GraphFeature {
	public static String PATH = GraphFeature.GRAPH_DATA_PATH;
	public static String KEY = "vertex";
	
	public PointXY getVertex();

}
