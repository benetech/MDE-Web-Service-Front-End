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
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.GraphTrail;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.classifier.MDEClassifier;
import gov.nasa.ial.mde.util.PointsUtil;
import gov.nasa.ial.mde.util.TrailUtil;

import java.util.Arrays;
import java.util.TreeMap;


/**
 * Analyzes arrays of real number data to extract features associated with it.
 *
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class AnalyzedData implements AnalyzedItem, Cloneable {
    
	
	
    // The name associated with the x and y data values.
    private String xName;
	private String yName;
    
	private double[] xData;
	private double[] yData;
    
    // The minimum and maximum X and Y data values.
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
    
	private Bounds preferredBounds = new Bounds(-DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, -DEFAULT_BOUND_VALUE);
    
	private SolvedGraph features = null;
    
    // The left and right indexes to the real-data points over the given bounds.
	private int leftIndexBound = -1;
	private int rightIndexBound = -1;
    
    // Used for quick x-value to point index lookup.
	private double[] xPointValues = null;
    
	private MultiPointXY[] points = null;
	private GraphTrail[] graphTrails = null;
    
    private double maxJump = 0.0; // tolerance for breaking a GraphTrail
    
    @SuppressWarnings("unused")
	private AnalyzedData() {
        throw new RuntimeException("Default constructor not allowed.");
    }
    
    /**
     * Creates an instance of <code>AnalyzedData</code> using the specified
     * X and Y-axis names and values.
     * <p>
     * The values of xData must be in ascending order and it is not checked.
     * 
     * @param xName the name to use for the X-axis column of data.
     * @param yName the name to use for the Y-axis column of data.
     * @param xData the X-axis values.
     * @param yData the Y-axis values. 
     */
    public AnalyzedData(String xName, String yName, double[] xData, double[] yData) {
        if (xName == null) {
            throw new NullPointerException("Null X-data name.");
        }
        if (yName == null) {
            throw new NullPointerException("Null Y-data name.");
        }
        if (xData == null) {
            throw new NullPointerException("Null X-data array.");
        }
        if (yData == null) {
            throw new NullPointerException("Null Y-data array.");
        }
        // The lengths of the data arrays must be the same.
        if (xData.length != yData.length) {
            throw new IllegalArgumentException("X and Y data arrays are not the same length.");
        }
        if (xData.length <= 0) {
            throw new IllegalArgumentException("X and Y data arrays must contain data.");
        }
        
        // DONE: Do we need to check to make sure the x-data is in ascending order?
        // It's checked for file input. Why not here?
        //ANDREW: I'll do it then.  It's an O(n) operation, so why not 
        //ANDREW: Sorting should be a O(n log_2 n)
        // we would waste time checking if it's in order and then sorting.
        // we should just go ahead and sort UNLESS we know in advance 
        
        this.xName = xName;
        this.yName = yName;
        
        sort(xData, yData);
   
        
        //this.xData = xData;
        //this.yData = yData;
        
        
        // Initialize the X and Y data statistics.
        initStatistics();
        
        // Set the preferred bounds based on the min and max values of the data.
        preferredBounds.setBounds(xMin,xMax,yMax,yMin);
    }

    

	private void sort(double[] xData2, double[] yData2) {
		TreeMap<Double, Double> map = new TreeMap<Double, Double>();
		
		//TreeMap map = new TreeMap();
		
		for(int i = 0; i < xData2.length; i++)
		{
			map.put(xData2[i], yData2[i]);
			//System.out.println(xData2[i] + " " + yData2[i]);
		}
		
		//System.out.println();
		//System.out.println();
		int size = map.size();
		
		for(int i = 0  ; i < size ; i++)
		{
			xData2[i] = map.firstKey();
			yData2[i] = map.remove(xData2[i]);
			//System.out.println(xData2[i] + " " + yData2[i]);
		}
		this.xData = xData2;
		this.yData = yData2;
	}
	

	/**
     * Initialize the statistics for the X and Y data values.
     */
    private void initStatistics() {
        double x,y;
        int len = xData.length;
        
        if (len > 0) {
            x = xData[0];
            xMin = x;
            xMax = x;
            y = yData[0];
            yMin = y;
            yMax = y;
        } else {
            xMin = -DEFAULT_BOUND_VALUE;
            xMax = DEFAULT_BOUND_VALUE;
            yMin = -DEFAULT_BOUND_VALUE;
            yMax = DEFAULT_BOUND_VALUE;
        }
        
        for (int i = 1; i < len; i++) {
            x = xData[i];
            if (x < xMin) {
                xMin = x;
            }
            if (x > xMax) {
                xMax = x;
            }
            y = yData[i];
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
        }
    }
    
    /**
     * Returns the number of data values this <code>AnalyzedData</code> has.
     * 
     * @return the number of data values this <code>AnalyzedData</code> has.
     */
    public int getDataSize() {
        return (xData != null) ? xData.length : 0;
    }
    
    /**
     * Returns the minimum X-axis value.
     * 
     * @return the minimum X-axis value.
     */
    public double getMinimumX() {
        return this.xMin;
    }
    
    /**
     * Returns the maximum X-axis value.
     * 
     * @return the maximum X-axis value.
     */
    public double getMaximumX() {
        return this.xMax;
    }
    
    /**
     * Returns the minimum Y-axis value.
     * 
     * @return the minimum Y-axis value.
     */
    public double getMinimumY() {
        return this.yMin;
    }
    
    /**
     * Returns the maximum Y-axis value.
     * 
     * @return the maximum Y-axis value.
     */
    public double getMaximumY() {
        return this.yMax;
    }
    
    /**
     * Returns the name associated with the X-axis values.
     * 
     * @return the name associated with the X-axis values.
     */
    public String getXName() {
        return this.xName;
    }
    
    /**
     * Returns the name associated with the Y-axis values.
     * 
     * @return the name associated with the Y-axis values.
     */
    public String getYName() {
        return this.yName;
    }
    
    /**
     * Returns the X value at the specified array index.
     * 
     * @param index the index into the X data array.
     * @return the value at the specified array index.
     */
    public double getXValueAt(int index) {
        return xData[index];
    }
    
    /**
     * Returns the Y value at the specified array index.
     * 
     * @param index the index into the Y data array.
     * @return the value at the specified array index.
     */
    public double getYValueAt(int index) {
        return yData[index];
    }
    
    /**
     * Returns the array of X values.
     * 
     * @return the array of X values.
     */
    public double[] getXValues() {
        return xData;
    }
    
    /**
     * Returns the array of Y values.
     * 
     * @return the array of Y values.
     */
    public double[] getYValues() {
        return yData;
    }
    
    /**
     * Returns the index to the data value associated with the left bound.
     * 
     * @return the index to the data value associated with the left bound.
     */
    public int getLeftIndexBound() {
        return leftIndexBound;
    }
    
    /**
     * Returns the index to the data value associated with the right bound.
     * 
     * @return the index to the data value associated with the right bound.
     */
    public int getRightIndexBound() {
        return rightIndexBound;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#computePoints(gov.nasa.ial.mde.math.Bounds)
     */
    public void computePoints(Bounds b) {
        computePoints(b.left,b.right,b.top,b.bottom);
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#computePoints(double, double, double, double)
     */
    public void computePoints(double left, double right, double top, double bottom) {
        // Calculate the points and graph-trails for the given bounds
        int lastIndex = xData.length - 1;
        
        int leftIndex = Arrays.binarySearch(xData,left);
        if (leftIndex < 0) {
            leftIndex = -leftIndex - 1;
        }
        if (leftIndex >= xData.length) {
            leftIndex = lastIndex;
        }
        // Use the point to the left of the index found in the binary search,
        // which puts us to the left of the desired left position.
        while ((leftIndex > 0) && (xData[leftIndex] > left)) {
            leftIndex--;
        }
        
        int rightIndex = Arrays.binarySearch(xData,right);
        if (rightIndex < 0) {
            rightIndex = -rightIndex - 1;
        }
        if (rightIndex >= xData.length) {
            rightIndex = lastIndex;
        }
        // Use the index found in the binary search, which puts us to the right
        // of the desired right position.
        while ((rightIndex < lastIndex) && (xData[rightIndex] < right)) {
            rightIndex++;
        }

        if (leftIndex > rightIndex) {
            throw new IllegalArgumentException("Can not have left bound > right bound");
        }
        
        // The left and right indexes for all the real data points in the given bounds.
        this.leftIndexBound = leftIndex;
        this.rightIndexBound = rightIndex;
        
        maxJump = Math.abs(top - bottom);
        int totalPts = (rightIndex - leftIndex) + 1;
        
        // NOTE: Our current model is to either interpolate or decimate the real data points.
        // TODO: Use a real data model.
        
        // Dispose of the current points array so we don't have a memory leak.
        disposePoints();
        
        // Generate the points that will be used for sonifying the model.
        if (totalPts < NUM_POINTS) {
            // Interpolate the data to a length of NUM_POINTS.
            points = PointsUtil.interpolatePoints(leftIndex,rightIndex,NUM_POINTS,xData,yData);
        } else if (totalPts > NUM_POINTS) {
            // Decimate the data to a length of NUM_POINTS.
            points = PointsUtil.decimatePoints(leftIndex,rightIndex,NUM_POINTS,xData,yData);
        } else {
            // Make a copy of the points that is a length of NUM_POINTS.
            points = PointsUtil.copyPoints(leftIndex,rightIndex,xData,yData);
        }
        
        // Create the array of x-values of the points for quick x-value to index lookup.
        // We assume that the x values from the points array are sorted in ascending order.
        if (points == null) {
            xPointValues = null;
        } else {
            int len = points.length;
            if ((xPointValues == null) || (xPointValues.length != len)) {
                xPointValues = new double[len];
            }
            for (int i = 0; i < len; i++) {
                xPointValues[i] = points[i].x;
            }
        }
        
        // Dispose of the current graph trails array so we don't have a memory leak.
        disposeGraphTrails();
        
        // Generate the trails used for graphing the model of the real data.
        graphTrails = TrailUtil.getGraphTrailsFrom(points,maxJump);
        
        // Update the preferred bounds we keep for this analyzed data.
        preferredBounds.setBounds(left, right, top, bottom);
    }
    
    /**
     * Returns the bounds of the data which represents the minimum and maximum
     * X and Y values.
     * 
     * @return the he bounds of the data which represents the minimum and
     * 		maximum X and Y values.
     */
    public Bounds getDataBounds() {
        return new Bounds(xMin,xMax,yMax,yMin);
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPreferredBounds()
     */
    public Bounds getPreferredBounds() {
        return preferredBounds;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getClassifier()
     */
    public MDEClassifier getClassifier() {
        //TODO: get the real-data classifier
        return new MDEClassifier();
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#updateFeatures()
     */
    public void updateFeatures() {
        MDEClassifier c = getClassifier();
        this.features = (c != null) ? c.getFeatures(this) : null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getFeatures()
     */
    public SolvedGraph getFeatures() {
        return features;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPoint(double)
     */
    public MultiPointXY getPoint(double position) {
        if ((points == null) || (points.length <= 0) || (position < 0.0) || (position > 1.0)) {
            return null;
        }
        int index = (int)Math.floor(position * (points.length-1));
        return points[index];
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPoint(int)
     */
    public MultiPointXY getPoint(int index) {
        return ((points != null) && (index >= 0) && (index < points.length)) ? points[index] : null;
    }
    
    /**
     * Returns the index in the array to the X-axis data value that is the closest
     * to the specified x-value.
     * 
     * @param x the X-axis value to find the index for.
     * @return the index in the array to the X-axis data value that is the closest
     * 		to the specified x-value.
     */
    public int getPointIndexNear(double x) {
        if ((xPointValues == null) || (xPointValues.length <= 0)) {
            return -1;
        }
        
        // Determine if the x-value is out of range.
        if ((x < xPointValues[0]) || (x > xPointValues[xPointValues.length-1])) {
            return -1;
        }
        
        // Find the index of the point close to the specified x-value.
        int index = Arrays.binarySearch(xPointValues,x);
        
        // For a positive index, we found the point with the exact same x value.
        if (index >= 0) {
            return index;
        }

        index = -index - 1;
        if (index >= xPointValues.length) {
            index = xPointValues.length - 1;
        }
        
        // Determine which of the two points the x-value is closer to the specified x-value.
        if ((index > 0) && (Math.abs(xPointValues[index-1] - x) < Math.abs(xPointValues[index] - x))) {
            index--;
        }
        
        return index;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPoints()
     */
    public MultiPointXY[] getPoints() {
        return points;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getGraphTrails()
     */
    public GraphTrail[] getGraphTrails() {
        return graphTrails;
    }
    
    /**
     * Returns the real data point that has the specified x-value, otherwise return null.
     * 
     * @param x the read data value to find the point for.
     * @return the real data point that has the specified x-value, otherwise return null.
     */
    public PointXY getRealDataPoint(double x) {
        int index = Arrays.binarySearch(xData,x);
        return (index >= 0) ? new PointXY(xData[index],yData[index]) : null;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#dispose()
     */
    public void dispose() {
        xName = null;
        yName = null;
        xData = null;
        yData = null;
        preferredBounds = null;
        features = null;
        xPointValues = null;
        disposeGraphTrails();
        disposePoints();
    }
    
    private void disposeGraphTrails() {
        if (graphTrails != null) {
            int len = graphTrails.length;
            for (int i = 0; i < len; i++) {
                if (graphTrails[i] != null) {
                    graphTrails[i].dispose();
                    graphTrails[i] = null;
                }
            }
            graphTrails = null;
        }
    }
    
    private void disposePoints() {
        if (points != null) {
            int len = points.length;
            for (int i = 0; i < len; i++) {
                if (points[i] != null) {
                    points[i].dispose();
                    points[i] = null;
                }
            }
            points = null;
        }
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getName()
     */
    public String getName() {
        return getYName();
    }
    
    /**
     * Creates a clone of this <code>AnalyzedData</code> object.
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new AnalyzedData(getXName(),getYName(),getXValues(),getYValues());
    }
    
    /**
	 * Checks whether two <code>AnalyzedData</code> objects have equal values.
	 * 
	 * @return true if the specified object and this <code>AnalyzedData</code> object are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AnalyzedData) {
            AnalyzedData ad = (AnalyzedData)obj;
            return (this.getYName().equals(ad.getYName()) &&
                    this.getXName().equals(ad.getXName()) &&
                    Arrays.equals(this.yData,ad.yData) &&
                    Arrays.equals(this.xData,ad.xData));
        }
        return false;
    }
    
    /**
	 * Returns a string representation of this <code>AnalyzedData</code> object.
	 * 
	 * @return a string representation of this <code>AnalyzedData</code> object
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        StringBuffer strBuff = new StringBuffer(128);
        strBuff.append(getClass().getName()).append("[")
               .append("xDataName=").append(xName).append(",")
               .append("yDataName=").append(yName).append(",")
               .append("length=").append((xData != null) ? xData.length : 0).append(",");
        if ((xData == null) || (yData == null)) {
            strBuff.append("(null,null)");
        } else {
            int len = xData.length;
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    strBuff.append(",");
                }
                strBuff.append("(").append(xData[i]).append(",").append(yData[i]).append(")");
            }
        }
        strBuff.append("]");
        return strBuff.toString();
    }
    
}
