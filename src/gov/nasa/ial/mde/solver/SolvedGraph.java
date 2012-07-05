/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointRT;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.features.combinations.DomainAndRangeFeature;
import gov.nasa.ial.mde.solver.features.combinations.InterceptsFeature;
import gov.nasa.ial.mde.solver.features.combinations.MinimaAndMaximaFeature;
import gov.nasa.ial.mde.solver.features.individual.DomainFeature;
import gov.nasa.ial.mde.solver.features.individual.RangeFeature;
import gov.nasa.ial.mde.solver.features.individual.XInterceptFeature;
import gov.nasa.ial.mde.solver.features.individual.YInterceptFeature;

import java.util.ArrayList;

/**
 * The class represents a solved graph.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedGraph implements DomainAndRangeFeature, InterceptsFeature, MinimaAndMaximaFeature {

	public static enum GraphFeature {
		graphName, graphBoundaries, equationType,
        equationPrint, originalEquationPrint, graphDescriptionDomain,
        graphDescriptionRange, domain, range, abscissaSymbol, ordinateSymbol,
        abscissaLabel, ordinateLabel, coordinateSystem, graphClosure,
        xIntercepts, yIntercepts, maxima, minima, ascendingRegions,
        descendingRegions
	};
	
	public static enum CompassDirection {
            East, ENE, NE, NNE, North, NNW, NW, WNW, 
            West, WSW, SW, SSW, South, SSE, SE, ESE
    };
    
    private final static String[] GENERAL_DIRECTIONS = { 
    	"nowhere", "upwards", "downwards", "to the right", "to the left" };

    private MdeFeatureNodeManager featureTree;

    /**
     * Default constructor.
     */
    public SolvedGraph() {
        featureTree = new MdeFeatureNodeManager();
        featureTree.addNode(MdeFeatureNodeManager.ROOT_PATH, MdeFeatureNodeManager.MDE_NAME);
        featureTree.setCurrent(MdeFeatureNodeManager.MDE_NAME);
        featureTree.addNode(MdeFeatureNodeManager.ROOT_PATH, MdeFeatureNodeManager.GRAPH_DATA_NAME);
        featureTree.setCurrent(MdeFeatureNodeManager.GRAPH_DATA_NAME);
        
        // load default features
        for(GraphFeature feature : GraphFeature.values()) {
        	this.featureTree.addKey(feature.name());
        }
    } // end SolvedGraph

    /**
     * Puts a feature for the specified key and value.
     * 
     * @param k the key.
     * @param v the value.
     */
    public void putFeature(String k, Object v) {
        if ((v instanceof String) || (v instanceof MdeFeatureNode))
            featureTree.addValue(k, v);
        else if ((v instanceof IntervalXY) || (v instanceof PointXY) || (v instanceof PointRT))
            featureTree.addValue(k, v.toString());
        else if (v instanceof NumberModel)
            featureTree.addValue(k, ((NumberModel)v).getMFN());
        else
            featureTree.addValue(k, new MdeFeatureNode(v));
    } // end putFeature

    /**
     * Puts a feature for the specified node path, key, and value to the last
     * node.
     * 
     * @param path the path to the feature node.
     * @param key the key.
     * @param value the value.
     */
    public void putFeature(String path, String key, Object value) {
        putFeature(path, key, value, MdeFeatureNodeManager.ADD_LAST);
    } // end putFeature
    
    /**
     * Puts a feature for the specified node path, key, value and node flag.
     * 
     * @param path the path to the feature node.
     * @param k the key.
     * @param v the value.
     * @param whichNode which node to put the feature which is one of 
     * MdeFeatureNodeManager.ADD_LAST|ADD_ALL.
     */
    public void putFeature(String path, String k, Object v, int whichNode) {
        if ((v instanceof String) || (v instanceof MdeFeatureNode))
            featureTree.addValue(path, k, v, whichNode);
        else if ((v instanceof IntervalXY) || (v instanceof PointXY) || (v instanceof PointRT))
            featureTree.addValue(path, k, v.toString(), whichNode);
        else if (v instanceof NumberModel)
            featureTree.addValue(path, k, ((NumberModel)v).getMFN(), whichNode);
        else
            featureTree.addValue(path, k, new MdeFeatureNode(v), whichNode);
    } // end putFeature

    /**
     * Puts a new feature for the specified key and value.
     * 
     * @param k the key.
     * @param v the value.
     */
    public void putNewFeature(String k, Object v) {
        featureTree.addKey(k);
        if (v != null)
            putFeature(k, v);
    } // end putNewFeature

    /**
     * Puts the new features.
     * 
     * @param features the array of features.
     */
    public void putNewFeatures(String[] features) {
        int i, n = features.length;

        for (i = 0; i < n; i++)
            putNewFeature(features[i], null);
    } // end putNewFeatures
    
    /**
     * Puts a new feature for the specified node path, key, value, and flag for
     * a new node.
     * 
     * @param path path for the node.
     * @param k the key.
     * @param v the value.
     * @param newNode true to add a node for the given path, false to not.
     */
    public void putNewFeature(String path, String k, Object v, boolean newNode) {
        if (newNode)
            featureTree.addNode("", path);

        featureTree.addKey(path, k);

        if (v != null)
            putFeature(path, k, v);
    } // end putNewFeature

    /**
     * Adds to the feature.
     * 
     * @param k the key.
     * @param v the value.
     */
    public void addToFeature(String k, Object v) {
        putFeature(k, v);
    } // end addToFeature

    /**
     * Copies from the specified solved graph to this solved graph.
     * 
     * @param other the other solved graph to copy.
     */
    public void copyFrom(SolvedGraph other) {
        featureTree = other.featureTree;
    } // end copyFrom

    /**
     * Returns the compass direction based on the angle.
     * 
     * @param theta the angle.
     * @return the compase direction which is one of COMPASS_DIRECTIONS.
     */
    public static CompassDirection getCompassDir(double theta) {
        double zeta = theta + 11.25;
        double turns = zeta / 360.0;
        double t = Math.floor(turns);
        double phi = 360.0 * (turns - t);
        int n = (int)Math.floor(phi / 22.5);
        
        System.out.println("theta = " + theta);
        System.out.println("zeta = " + zeta);
        System.out.println("turns = " + turns);
        System.out.println("t = " + t);
        System.out.println("phi = " + phi);
        System.out.println("n = " + n);
        
        CompassDirection direction = (CompassDirection.values())[n];

        return direction;
    } // end getCompassDir
    
    /**
     * Returns the general direction based on the angle.
     * 
     * 
     * 
     * @param int of value 0-4
     * @return the direction which is one of GENERAL_DIRECTIONS.
     */
    public static String getGeneralDir(int n) {
        return GENERAL_DIRECTIONS[n];
    } // end getCompassDir
    

    /**
     * Returns an XML represntation of the solved graph.
     * 
     * @return an XML represntation of the solved graph.
     */
    public String toString() {
        return getXMLString();
    } // end toString

    /**
     * Returns an XML represntation of the solved graph.
     * 
     * @return an XML represntation of the solved graph.
     */
    public String getXMLString() {
        MdeFeatureNode[] nodes = featureTree.getNodes("/MDE");
        int i, n = nodes.length;
        StringBuffer b = new StringBuffer();

        for (i = 0; i < n; i++)
            b.append(nodes[i].getXMLString());

        return b.toString();
    } // end getXMLString
    
    public Object getValue(String path, String key) throws NullPointerException {
    	Object value = null;
    	//System.out.println(this.featureTree.getCurrent());
    	MdeFeatureNode[] nodes = this.featureTree.getNodes(path);
    	for(MdeFeatureNode node : nodes) {
    		try {
    			value = node.getValue(key);
    		} catch(NullPointerException e) {
    			// it's okay if the value is null here.  Just not okay if all the values are null.
    		}
    		if(value != null) break;
    	}
    	if(value == null) {
    		throw new NullPointerException();
    	}
    	return value;
    }
    
    public Object getValues(String path, String key) throws NullPointerException {
    	Object value = null;
    //	System.out.println(this.featureTree.getCurrent());
    	MdeFeatureNode[] nodes = this.featureTree.getNodes(path);
    	for(MdeFeatureNode node : nodes) {
    		try {
    			value = node.getValues(key);
    		} catch(NullPointerException e) {
    			// it's okay if the value is null here.  Just not okay if all the values are null.
    		}
    		if(value != null) break;
    	}
    	if(value == null) {
    		throw new NullPointerException();
    	}
    	return value;
    }

	public Double[] getYIntercepts() {
		Object values = this.getValues(YInterceptFeature.PATH, YInterceptFeature.KEY);
		ArrayList<?> list = (ArrayList<?>) values;
		Double[] intercepts = new Double[list.size()];
		for(int i=0;i<list.size();i++)
		{
			//System.out.println(list.get(i));
			intercepts[i]=Double.valueOf(((String) list.get(i)));
		}
		
		return intercepts;
	}

	public Double[] getXIntercepts() {
		Object values = this.getValues(XInterceptFeature.PATH, XInterceptFeature.KEY);
		ArrayList<?> list = (ArrayList<?>)values;
		Double[] intercepts = new Double[list.size()];
		for(int i=0;i<list.size();i++)
		{
			intercepts[i]=Double.valueOf(((String) list.get(i)));
		}
		
		return intercepts;
	}

	public String getRange() {
		Object value = this.getValue(RangeFeature.PATH, RangeFeature.KEY);
		String rangeString = (String)value;
		return rangeString;
	}

	public String getDomain() {
		Object value = this.getValue(DomainFeature.PATH, DomainFeature.KEY);
		String domainString = (String)value;
		return domainString;
	}

	public PointXY[] getMinima() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasMinima() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canCalculateMinima() {
		// TODO Auto-generated method stub
		return false;
	}

	public PointXY[] getMaxima() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasMaxima() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canCalculateMaxima() {
		// TODO Auto-generated method stub
		return false;
	}




   
//    private static void doPrint(String[] f) {
//        int i, n = f.length;
//
//        if (n > 0) {
//            for (i = 0; i < n; i++)
//                System.out.println(f[i]);
//        } // end if
//        else
//            System.out.println("[NOTHING TO PRINT]");
//    } // end doPrint
//
//    // Main routine for test only
//    public static void main(String[] args) {
//        try {
//            double d = new Double(args[0]).doubleValue();
//            System.out.println(SolvedGraph.getCompassDir(d));
//        } // end try
//        catch (NumberFormatException nfe) {
//        }
//    } // end main
} // end class SolvedGraph
