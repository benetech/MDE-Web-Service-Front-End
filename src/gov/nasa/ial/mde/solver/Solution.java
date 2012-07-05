/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Feb 23, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.Bounds;
import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;

import java.util.EventListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * Solution is a container class for a MDE graph solution. A Solution object
 * contains an AnalyzedItem, a show graph boolean value, a sonify graph boolean
 * value, and a list of registered showFlagsChangeEvent listeners.
 * 
 * Solution provides convenience methods for accessing elements of the Solution
 * and its AnalyzedItem object.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class Solution {

    /** An solved item, that is, a AnalyzedEquation or AnalyzedData object. */
    protected AnalyzedItem analyzedItem;

    /** Show graph flag. */
    protected boolean showGraph;
    
    /** Sonify graph flag. */
    protected boolean sonifyGraph;

    /** Components registered to listen for showGraph or sonifyGraph change events. */
    protected EventListenerList listenerList = new EventListenerList();

    @SuppressWarnings("unused")
	private Solution() {
        throw new RuntimeException("Default constructor not allowed.");
    }
    
    /**
     * Construct a new Solution object with with given AnalyzedItem object.
     * showGraph and sonifyGraph flags default to true.
     *
     * @param analyzedItem the solution for the analyzed item.
     */
    public Solution(AnalyzedItem analyzedItem) {
        if (analyzedItem == null) {
            throw new NullPointerException("Null analyzed-item.");
        }
        this.analyzedItem = analyzedItem;
        this.showGraph = true;
        this.sonifyGraph = true;
    }

    /**
     * Return this Solution object's AnalyzedItem object.
     * 
     * @return this Solution object's AnalyzedItem object.
     */
    public AnalyzedItem getAnalyzedItem() {
        return this.analyzedItem;
    }

    /**
     * Return the equation associated with this Solution, if the 
     * AnalyzedItem object represents an equation. Return null otherwise.
     * 
     * @return the equation associated with this Solution.
     */
    public String getInputEquation() {
        return (analyzedItem instanceof AnalyzedEquation) ? ((AnalyzedEquation)analyzedItem).printEquation() : null;
    }

    /**
     * Determine whether this Solution represents a Polar equation solution.
     * 
     * @return true if this Solution represents a Polar equation solution.
     */
    public boolean isPolar() {
        return ((analyzedItem instanceof AnalyzedEquation) && ((AnalyzedEquation)analyzedItem).isPolar());
    }

    /**
     * Determine whether this solution represents a bad equation.
     * 
     * @return true if this solution represents a bad equation.
     */
    public boolean isBadEquation() {
        return (analyzedItem == null) ||
               ((analyzedItem instanceof AnalyzedEquation) &&
                       (((AnalyzedEquation)analyzedItem).isBad()||((AnalyzedEquation)analyzedItem).hasMoreThanTwoVariables()));
    } // end isBadEquation

    /**
     * Determine whether this Solution is describable.
     * 
     * @return true if this Solution is describable.
     */
    public boolean isDescribable() {
        return (getFeatures() != null);
    } // end isDescribable

    /**
     * Determine whether this Solution is graphable.
     * 
     * @return true if this Solution is graphable.
     */
    public boolean isGraphable() {
        return (getGraphTrails() != null);
    } // end isGraphable

    /**
     * Determine whether this Solution is sonifiable.
     * 
     * @return true if this Solution is sonifiable.
     */
    public boolean isSonifiable() {
        return (getPoints() != null);
    } // end isSonifiable

    /**
     * Get the SolvedGraph associated with this Solution. The graph's 
     * describable features can be accessed from the SolvedGraph object.
     * 
     * @return the SolvedGraph associated with this Solution.
     */
    public SolvedGraph getFeatures() {
        return (analyzedItem != null) ? analyzedItem.getFeatures() : null;
    }

    /**
     * Get this Solution's sonification data point at the specified array index.
     * 
     * @param index the index to the point.
     * @return this Solution's sonification data point at the specified array index.
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedData#getPoint(int)
     */
    public MultiPointXY getPoint(int index) {
        return analyzedItem.getPoint(index);
    }
    
    /**
     * Get this Solution's sonification data point near the specified array index.
     * 
     * @param x the x value.
     * @return this Solution's sonification data point near the specified array index.
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedData#getPoint(int)
     */
    public MultiPointXY getPointNear(double x) {
        if (analyzedItem == null) {
            return null;
        }

        if (analyzedItem instanceof AnalyzedData) {
            // For real data, we need to use the method that finds the index
            // to the point that is as close to the given x as possible.
            AnalyzedData analyzedData = (AnalyzedData)analyzedItem;
            return analyzedData.getPoint(analyzedData.getPointIndexNear(x));
        }
        
        // Otherwise calculate a relative position given the bounds and the
        // specified x-value.
        Bounds b = analyzedItem.getPreferredBounds();
        double left = b.left;
        double right = b.right;
        if ((x < left) || (x > right)) {
            return null;
        }
        double position = (x - left)/(right - left);
        return analyzedItem.getPoint(position);
    }
    
    /**
     * Return the sonification points for this Solution.
     * 
     * @return the sonification points for this Solution.
     */
    public MultiPointXY[] getPoints() {
        return (analyzedItem != null) ? analyzedItem.getPoints() : null;
    } // end getPoints

    /**
     * Return the set of graph plotting points for this Solution.
     * 
     * @return the set of graph plotting points for this Solution.
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getGraphTrails
     */
    public GraphTrail[] getGraphTrails() {
        return (analyzedItem != null) ? analyzedItem.getGraphTrails() : null;
    } // end getGraphTrails

    /**
     * Dispose of this Solution object (free up associated memory).
     * This Solution object should not be used after this method is called because
     * the reference to the AnalyzedItem is immutable and we set it to null in 
     * this method. All change listeners are removed as well.
     * 
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#dispose()
     */
    public void dispose() {
        if (analyzedItem != null) {
            this.analyzedItem.dispose();
            this.analyzedItem = null;
        }
        this.showGraph = false;
        this.sonifyGraph = false;
        removeAllChangeListeners();
        this.listenerList = null;
    }

    /**
     * The graph for each analyzed-item can either be displayed or not.
     * 
     * @param visible true - enable the drawing and sonification of the graph. 
     * false - disable display and sonification of the graph.
     */
    public void setShowGraph(boolean visible) {
        boolean showChanged = (visible != showGraph);
        boolean sonifyChanged = (!visible && sonifyGraph);
        
        // Disable sonification if we disable showing the graph.
        if (!visible) {
            sonifyGraph = false;
        }
        showGraph = visible;
        
        if (showChanged || sonifyChanged) {
            fireStateChanged(new SolutionFlagsStateChange(showChanged,sonifyChanged));
        }
    }

    /**
     * Determine whether this Solution's graph is currently visible or hidden.
     * 
     * @return true if this Solution's graph is currently visible.
     */
    public boolean isShowGraph() {
        return showGraph;
    }

    /**
     * Sonification for the Solution can either be enabled or disabled.
     * 
     * @param sonify true to enable sonification of the graph, false to disable it.
     * @exception IllegalArgumentException is thrown if the graph will not be shown
     *                and you try to enabled sonification.
     */
    public void setSonifyGraph(boolean sonify) {
        if (sonify && !showGraph) {
            throw new IllegalArgumentException("Graph can not be sonified if it is not shown.");
        }
        if (sonify != sonifyGraph) {
            sonifyGraph = sonify;
            fireStateChanged(new SolutionFlagsStateChange(false,true));
        }
    }

    /**
     * Determine whether this graph's sonification is enabled or disabled.
     * 
     * @return true if the graph is to be sonified, false to disable sonification.
     */
    public boolean isSonifyGraph() {
        return sonifyGraph;
    }


    /**
     * Determine whether the input object and this Solution object are identical
     * objects.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Solution) {
            Solution s = (Solution)obj;
            return this.getAnalyzedItem().equals(s.getAnalyzedItem());
        }
        return false;
    }

    
    /**
     * Output Solution name and show flag values.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer strBuff = new StringBuffer(64);
        strBuff.append(getClass().getName()).append("(")
               .append(analyzedItem.getClass().getName()).append(", ")
               .append("showGraph=").append(showGraph).append(", ")
               .append("sonifyGraph=").append(sonifyGraph).append(")");
        return strBuff.toString();
    }
    
    /**
     * Register a component to listen for show flag change events.
     * 
     * @param l the change listener.
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    } // end addChangeListener

    /**
     * Unregister a show flag listener component.
     * 
     * @param l the change listener.
     */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    } // end removeChangeListener

    /**
     * Clear all the show flag change listeners for this Solution object.
     */
    public void removeAllChangeListeners() {
        EventListener[] listeners = listenerList.getListeners(ChangeListener.class);
        ChangeListener cl;
        for (int i = 0; i < listeners.length; i++) {
            // added a typecast to get past Xlint ROS 2/3/05
            cl = (ChangeListener)listeners[i];
        	listenerList.remove(ChangeListener.class, cl);
        }
    } // end removeAllChangeListeners

    /**
     * Notify all registered listeners that a show flag change event has occurred.
     * 
     * @param showSonifyStateChange true to show sonify state change.
     */
    protected void fireStateChanged(SolutionFlagsStateChange showSonifyStateChange) {
        Object[] l = listenerList.getListenerList();
        ChangeEvent changeEvent = (l.length > 0) ? new ChangeEvent(showSonifyStateChange) : null;
        for (int i = l.length - 2; i >= 0; i -= 2) {
            if (l[i] == ChangeListener.class) {
                ((ChangeListener)l[i + 1]).stateChanged(changeEvent);
            }
        }
    } // end fireStateChanged

    /**
     * A class representing a change in either the show graph or sonify graph
     * flags for the solution.
     * 
     * @author Dan Dexter
     * @version 1.0
     * @since 1.0
     */
    public class SolutionFlagsStateChange {
        /** A reference to the solution. */
        public Solution solution;
        
        /** Flag indicating that the show-graph state changed. */
        public boolean showGraphChanged;
        
        /** Flag indicating that the sonify-graph state changed. */
        public boolean sonifyGraphChanged;

        /**
         * Creates an instance of <code>SolutionFlagsStateChange</code> using the
         * specified <code>showGraphChanged</code> and <code>sonifyGraphChanged</code>
         * flag values.  A reference to the solution is automatically set.
         * 
         * @param showGraphChanged true to indicate the show graph state changed.
         * @param sonifyGraphChanged true to indicate the sonify graph state changed.
         */
        public SolutionFlagsStateChange(boolean showGraphChanged, boolean sonifyGraphChanged) {
            this.solution = Solution.this;
            this.showGraphChanged = showGraphChanged;
            this.sonifyGraphChanged = sonifyGraphChanged;
        }
    }

}
