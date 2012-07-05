/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Feb 16, 2004
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.math.Bounds;
import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.solver.GraphTrail;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.classifier.MDEClassifier;

/**
 * The <code>AnalyzedItem</code> interface is the generic representation of an
 * item that has been analyzed, which can be solved by the <code>Solver</code>.
 *
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public interface AnalyzedItem {

	/** The number of points used for the analysis. */
    public final static int		NUM_POINTS = 600;
    
    /** The default bound value. */
    public final static double	DEFAULT_BOUND_VALUE = 10.0;

    /**
     * The name associated with this analyzed item.
     * 
     * @return the name associated with this item.
     */
    public String getName();
    
    /**
     * Compute the points and graph-trails over the specified <code>Bounds</code>
     * and determine if it is a function over the bounds as well.
     * 
     * @param b the bounds to compute the points over.
     * @see #computePoints(double, double, double, double) 
     * @see gov.nasa.ial.mde.math.Bounds
     */
    public void computePoints(Bounds b);

    /** 
     * Compute the points and graph-trails over the <code>left</code>,
     * <code>right</code> <code>top</code>, and <code>bottom</code> bounds
     * and determine if it is a function over the bounds as well.
     * 
     * @param left the specified <code>left</code> bound
	 * @param right the specified <code>right</code> bound
	 * @param top the specified <code>top</code> bound
	 * @param bottom the specified <code>bottom</code> bound
	 * @see #computePoints(Bounds)
     */
    public void computePoints(double left, double right, double top, double bottom);

    /**
     * Because the computePoints() method could implement auto-scaling the
     * bounds could be modified so this method provides a way to get the
     * preferred bounds.
     * 
     * @return the preferred bounds for this item.
     */
    public Bounds getPreferredBounds();

    /**
     * Returns the classifier used for this analyzed item.
     * 
     * @return the classifier used for this analyzed item.
     */
    public MDEClassifier getClassifier();

    /**
     * Update the features that are cached for this analyzed item.
     */
    public void updateFeatures();

    /**
     * Returns the cached features for this analyzed item.
     * 
     * @return the cached features for this analyzed item.
     */
    public SolvedGraph getFeatures();

    /**
     * Returns the point at the relative position [0.0,1.0] in the array of points.
     * The 0.0 position represents the start of the points array and 1.0 represents
     * the end of the points array.
     * 
     * @param position the relative position [0.0,1.0] in the array of points.
     * @return the point at the specified position.
     */
    public MultiPointXY getPoint(double position);
    
    /**
     * Returns the point at the specified index in the array of points.
     * 
     * @param index the index of the point in the array of points.
     * @return the point at the specified index.
     */
    public MultiPointXY getPoint(int index);

    /**
     * Returns an array of all the points.
     * 
     * @return an array of all the points.
     */
    public MultiPointXY[] getPoints();

    /**
     * Returns the graph trails for the <code>AnalyzedItem</code>.
     * 
     * @return the graph trails for the <code>AnalyzedItem</code>.
     */
    public GraphTrail[] getGraphTrails();
    
    /**
     * This method is called to dispose of resources used by this
     * <code>AnalyzedItem</code>. The <code>AnalyzedItem</code> will be
     * invalid and must not be used once this method has been called.
     */
    public void dispose();

}
