/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.Bounds;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solution.SolutionFlagsStateChange;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * Solver is the Math Description Engine's <em>solution</em> engine. Solver
 * takes inputs to be graphed (equations or time-series data) and derives
 * graph-solutions that can be described, sonified or drawn by other MDE
 * components, or your own custom components. Solver internally stores graph
 * solutions as <code>Solution</code> objects.
 * <p>
 * <em>Equation and Data Inputs:</em> Solver accepts input equations as Java
 * <code>String</code> or MDE
 * <code>AnalyzedEquation</code> objects. Solver takes data inputs as
 * <code>AnalyzedData</code> objects. See the 
 * <a href="http://prime.jsc.nasa.gov/MDE">MDE
 * Programmer's Guide </a> for valid equation and data input formats.
 * <p>
 * <em>Setting Graph Bounds:</em> Use Solver <code>setBounds and
 * setPreferredBounds</code> to change Solver bounds used for the current 
 * Solution list. Default bounds of (-10,
 * 10, 10, -10) are provided. Note that setBounds methods do not invoke
 * <code>solve</code>, but calls to
 * <code>solve(Bounds b) and solve(double left, double right, double top, double
 * bottom)</code> <em>do</em>
 * reset the current bounds.
 * <p>
 * <em>Managing One or More Graph Solutions:</em> Solver can accept and manage
 * multiple equation and dataset inputs for simultaneous display. (You can add
 * both equations and data to the same solution list.) To support this, Solver
 * uses a two-step solution process (even if you're graphing one item at a
 * time): <blockquote>First, add the input(s) you want graphed to Solver's
 * solution list with an <code>add</code> method.
 * <p>
 * Second, call one of the <code>solve</code> methods to generate the
 * solution(s). </blockquote>
 * 
 * <em>Clearing the Solution List:</em> When you want to clear the current
 * solution list, call <code>removeAll()</code>.
 * <p>
 * <em>Showing and Hiding Graph Solutions:</em> Solutions in the current list
 * can be given show or hide designations at input, using
 * <p>
 * <blockquote>
 * add(AnalyzedItem item, boolean enableGraph, boolean enableSonification)
 * </blockquote>.
 * These designations can be reset by accessing the Solution of interest with a
 * <code>get</code> method, and changing that Solution object's 
 * Solution.setShowGraph(boolean visible) and/or Solution.setSonifyGraph(boolean
 * visible) method.
 * <p>
 * <em>Checking for Valid Solutions:</em> After Solver has solved your graph
 * inputs, you can check for solution validity with Solver's "any" methods:
 * <p>
 * <blockquote>
 * anyBadEquations<br>
 * anyDescribable<br>
 * anyGraphable<br>
 * anyDescribable</blockquote>
 * <em>Solution Synchronization Among Components:</em> Solver serves as a
 * Solution synchronizer for MDE's built-in description, graphing and
 * sonification components (Describer, Sounder, SoundControl, CartesianGraph,
 * etc.) - if you pass the same Solver object to each component. It can function
 * similarly for custom graphing components you may develop.
 * <p>
 * Solution-synchronized components can register as a Solver ChangeListener,
 * with the <code>addChangeListener</code> method.
 * <p>
 * <a href="http://prime.jsc.nasa.gov/MDE">See the MDE Programmer's Guide </a>
 * for more information on Solver.
 * <p>
 * 
 * @author Dan Dexter, Dr. Robert Shelton, Terry Hodgson, Dat Truong
 * @see Solution
 */
public class Solver {

    /**
     * List of <code>Solution</code> objects this <code>Solver</code> object
     * is currently managing. Any solution updates requested through other
     * Solver methods will be applied to the items in this list.
     */
    protected ArrayList<Solution> solutionList = new ArrayList<Solution>(5);

    /**
     * number of polar <code>Solution</code> objects in the
     * <code>solutionList</code> that have a <code>showGraph</code> value of
     * <code>true</code>
     */
    protected int showPolarCount = 0;

    /**
     * number of Cartesian <code>Solution</code> objects in the
     * <code>solutionList</code> that have a <code>showGraph</code> value of
     * <code>true</code>
     */
    protected int showCartesianCount = 0;

    /**
     * number of polar <code>Solution</code> objects in the
     * <code>solutionList</code> that have a <code>sonifyGraph</code> value
     * of <code>true</code>
     */
    protected int sonifyPolarCount = 0;

    /**
     * Number of polar <code>Solution</code> objects in the
     * <code>solutionList</code> that have a <code>sonifyGraph</code> value
     * of <code>true</code>
     */
    protected int sonifyCartesianCount = 0;

    /**
     * Default solution bounds for all Solution objects in the
     * <code>solutionList</code>. Value is derived from
     * AnalyzedItem.DEFAULT_LIMIT = 10.0, that is, left = -10, right = 10, top =
     * 10 and bottom = -10.
     */
    public static final Bounds DEFAULT_BOUNDS = new Bounds(
            -AnalyzedItem.DEFAULT_BOUND_VALUE, AnalyzedItem.DEFAULT_BOUND_VALUE,
            AnalyzedItem.DEFAULT_BOUND_VALUE, -AnalyzedItem.DEFAULT_BOUND_VALUE);

    /**
     * Specifies the preferred x and y solution bounds (left, right, top and
     * bottom) for all Solution objects in the <code>solutionList</code>.
     * Initially set to DEFAULT_BOUNDS. Use <code>setPreferredBounds</code>
     * methods to change these values.
     */
    protected Bounds preferredBounds = new Bounds(DEFAULT_BOUNDS);

    /**
     * Specifies the <em>current</em> x-y plane solution bounds (left, right,
     * top and bottom) for all Solution objects in the <code>solutionList</code>.
     * Initially set to DEFAULT_BOUNDS. Use <code>setBounds</code> methods to
     * change these values.
     */
    protected Bounds bounds = new Bounds(DEFAULT_BOUNDS);

    /**
     * Listens for state changes to <code>showGraph</code> and
     * <code>sonifyGraph</code> for <em><code>Solution</code></em> objects
     * in the <code>solutionList</code>. Updates show counts accordingly.
     */
    protected ChangeListener solutionFlagsChangeListener = new SolutionFlagsChangeListener();

    /**
     * Cache of the last change event that occurred for this
     * <em><code>Solver</code></em> object. Updated by
     * {@link #fireStateChanged()}.
     */
    protected ChangeEvent changeEvent = null;

    /**
     * List of components that have registered to be notified of
     * <code>Solver</code> change events, through a call to
     * <code>Solver.addChangeListener</code>. Solver change events are fired
     * by the <code>add</code> and <code>solve</code> methods.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Creates a new <code>Solver</code> object.
     */
    public Solver() {
        super();
    } // end Solver

    /**
     * Adds the input equation to this Solver object's solutionList. This add
     * method creates an AnalyzedEquation from the input equation and calls add.
     * 
     * @param equation an equation we want solved, usually so we can graph, 
     * describe, and/or sonify the solution with <code>Describer, Sounder, 
     * SoundControl or CartesianGraph</code>.
     * @return gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation
     */
    public AnalyzedEquation add(String equation) {
        AnalyzedEquation analyzedEq = new AnalyzedEquation(equation);
        add(analyzedEq);
        return analyzedEq;
    } // end add

    /**
     * Creates a Solution object from the input item and adds it to the
     * <code>solutionList</code> list. This add method calls
     * {@link #add(AnalyzedItem, boolean, boolean)}. The enableGraph and
     * enableSonification flags are defaulted to true.
     * 
     * @param item an analyzed item.
     */
    public void add(AnalyzedItem item) {
        add(item, true, true);
    }

    /**
     * Creates a Solution object from the input item and adds it to the
     * <code>solutionList</code> list. Graphing and sonification flags for the
     * Solution object are set according to enablegraph and enableSonification
     * values.
     * 
     * @param item an analyzed item.
     * @param enableGraph true to enable the graph, false to disable.
     * @param enableSonification true to enable sonification, false to disable.
     */
    public void add(AnalyzedItem item, boolean enableGraph, boolean enableSonification) {
        // Handle the AnalyzedEquation special case.
        if ((item instanceof AnalyzedEquation)
                && (((AnalyzedEquation) item).isBad() || ((AnalyzedEquation) item)
                        .hasMoreThanTwoVariables())) {
            if (MdeSettings.DEBUG) {
                System.out.println("AnalyzedEquation is bad");
            }
            fireStateChanged();
            return;
        }
        Solution solution = new Solution(item);
        solution.setShowGraph(enableGraph);
        solution.setSonifyGraph(enableSonification);
        solution.addChangeListener(solutionFlagsChangeListener);
        solutionList.add(solution);

        // Update our show and sonify graph counts.
        int showCountOffset = solution.isShowGraph() ? 1 : 0;
        int sonifyCountOffset = solution.isSonifyGraph() ? 1 : 0;
        updateShowSonifyCounts(solution, showCountOffset, sonifyCountOffset);

        // Change the show and sonify graph settings based on existing
        // solutions.
        applyShowSonifyGraphRule();
    } // end add

    /**
     * Returns a Solution list Iterator.
     * 
     * @return a Solution list Iterator.
     */
    public Iterator<Solution> getSolutionIterator() {
        return solutionList.iterator();
    }

    /**
     * Returns true if there are no Solution objects in solutionList. Returns
     * false otherwise.
     * 
     * @return true if there are no Solution objects in solutionList. Returns
     * 				false otherwise.
     */
    public boolean isEmpty() {
        return solutionList.isEmpty();
    }

    /**
     * Returns the number of Solution objects in solutionList.
     * 
     * @return true if the solution list is empty. false otherwise.
     */
    public int size() {
        return solutionList.size();
    }

    /**
     * Returns the Solution object at the specified position in solutionList.
     * 
     * @param index the index of the solution to get.
     * @return the Solution object
     * @see java.util.ArrayList
     * @see gov.nasa.ial.mde.solver.Solution
     */
    public Solution get(int index) {
        return (Solution) solutionList.get(index);
    }

    /**
     * Returns the solutionList object(s) with the specified name. It is
     * possible to have more than one solution for the same name, such as when
     * the data is segmented and is split across multiple AnalyzedData objects.
     * 
     * @param name AnalyzedItem name
     * @return the Solution object with matching AnalyzedItem name
     * @see gov.nasa.ial.mde.solver.Solution
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedData
     */
    public Solution[] get(String name) {
        if ((name == null) || solutionList.isEmpty()) {
            return null;
        }
        ArrayList<Solution> list = new ArrayList<Solution>(solutionList.size());
        Solution solution;
        int len = solutionList.size();
        for (int i = 0; i < len; i++) {
            solution = (Solution)solutionList.get(i);
            if (name.equals(solution.getAnalyzedItem().getName())) {
                list.add(solution);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        Solution[] solutionArray = (Solution[]) list.toArray(new Solution[list.size()]);
        list.clear();
        return solutionArray;
    }

    /**
     * Returns the Solution object containing the specified item.
     * 
     * @param item the analyzed item to get the solution for.
     * @return the requested Solution object, or null if a match is not found
     */
    public Solution get(AnalyzedItem item) {
        if ((item == null) || solutionList.isEmpty()) {
            return null;
        }
        Solution solution;
        int len = solutionList.size();
        for (int i = 0; i < len; i++) {
            solution = (Solution) solutionList.get(i);
            if (item.equals(solution.getAnalyzedItem())) {
                return solution;
            }
        }
        return null;
    }

    /**
     * Tests whether a Solution object for the given item is in the current
     * solutionList.
     * 
     * @param item the analyzed item.
     * @return true if the solver contains the specified item.
     */
    public boolean contains(AnalyzedItem item) {
        return (get(item) != null);
    }

    /**
     * Removes all Solution objects from the solutionList. Resets show and
     * sonify counts to zero.
     */
    public void removeAll() {
        // It is very important that we dispose of each solution to free
        // resources
        // before we clear the solution list.
        int len = solutionList.size();
        for (int i = 0; i < len; i++) {
            ((Solution) solutionList.get(i)).dispose();
        }
        // Clear the list.
        solutionList.clear();

        showPolarCount = 0;
        showCartesianCount = 0;
        sonifyPolarCount = 0;
        sonifyCartesianCount = 0;
    }

    /**
     * Generate solutions for all the items in the solutionList using the
     * current Solver bounds.
     */
    public void solve() {
        solve(bounds);
    } // end solve

    /**
     * Generate solutions for all the items in the solutionList using the
     * specified Solver bounds, b.
     * 
     * @param b the bounds to solve over.
     */
    public void solve(Bounds b) {
        solve(b.left, b.right, b.top, b.bottom);
    } // end solve

    /**
     * Generate solutions for all the items in the solutionList using the
     * specified Solver bounds, left, right, top, bottom.
     * 
     * @param left the left bound.
     * @param right the right bound.
     * @param top the top bound.
     * @param bottom the bottom bound.
     */
    public void solve(double left, double right, double top, double bottom) {
        // Solve with with new bounds
        bounds.setBounds(left, right, top, bottom);

        if (solutionList.isEmpty()) {
            return;
        }

        Bounds prefBounds;
        Solution solution;
        AnalyzedItem analyzedItem;
        boolean maxBoundsChanged, recomputeSolutions;
        int index;
        int iteration = 0;
        int len = solutionList.size();

        do {
            recomputeSolutions = false;
            for (index = 0; index < len; index++) {
                solution = (Solution) solutionList.get(index);

                // NOTE: We only update the solution if we are to graph/describe
                // or sonify it.
                if (solution.isShowGraph() || solution.isSonifyGraph()) {
                    analyzedItem = solution.getAnalyzedItem();

                    // Optimization: Update solution for the first iteration, or
                    // for later
                    // iterations if the analyzed-item bounds do not match the
                    // global bounds.
                    if ((iteration == 0) || !bounds.equals(analyzedItem.getPreferredBounds())) {

                        // Compute the points and graph-trails for the equation
                        // for the given bounds. The fuction test will also be
                    	// run by this method.
                        analyzedItem.computePoints(bounds);

                        // Update the cached features.
                        analyzedItem.updateFeatures();
                    }

                    // Update the bounds if they are different for the first
                    // iteration of the first item.
                    prefBounds = analyzedItem.getPreferredBounds();
                    if ((iteration == 0) && (index == 0) && !bounds.equals(prefBounds)) {
                        bounds.setBounds(prefBounds);
                    }

                    // Because the computePoints() method could implement
                    // auto-scaling the
                    // bounds could have changed so we need to check for that.
                    maxBoundsChanged = bounds.maximize(prefBounds);
                    if (maxBoundsChanged && (index > 0)) {
                        // Recompute all the solutions if the bounds change and
                        // it was
                        // not for the first item in the list.
                        System.out.println("maxBoundsChanged");
                        recomputeSolutions = true;
                    }
                }
            }
        } while (recomputeSolutions && ((++iteration) < 10));

        // Notify the listeners that we have a solution.
        fireStateChanged();
    } // end solve

    /**
     * Apply the default rules for how we display and sonify a mix of Cartesian
     * and Polar equations.
     */
    private void applyShowSonifyGraphRule() {
        Solution solution;
        for (int index = solutionList.size() - 1; index >= 0; index--) {
            solution = (Solution) solutionList.get(index);
            if (solution.isPolar() && solution.isSonifyGraph()
                    && ((getShowCartesianCount() > 0) || (getSonifyPolarCount() > 1))) {
                solution.setSonifyGraph(false);
            }
        }
    }

    /**
     * Updates the sonify counts for the specified solution.
     * 
     * @param s the solution.
     * @param showOffset the graph offset.
     * @param sonifyOffset the sonification offset.
     */
    private void updateShowSonifyCounts(Solution s, int showOffset, int sonifyOffset) {
        if (s.isPolar()) {
            showPolarCount += showOffset;
            sonifyPolarCount += sonifyOffset;
        } else {
            showCartesianCount += showOffset;
            sonifyCartesianCount += sonifyOffset;
        }
    }

    /**
     * Returns the number of polar equations in the solutionList with
     * showGraph values of true.
     * 
     * @return the number of polar equations in the solutionList with
     * 				showGraph values of true.
     */
    public int getShowPolarCount() {
        return this.showPolarCount;
    }

    /**
     * Returns the number of Cartesian equations in the solutionList with
     * showGraph values of true.
     * 
     * @return the number of Cartesian equations in the solutionList with
     * 				showGraph values of true.
     */
    public int getShowCartesianCount() {
        return this.showCartesianCount;
    }

    /**
     * Returns the number of polar equations in the solutionList with
     * sonifyGraph values of true.
     * 
     * @return the number of polar equations in the solutionList with
     * 				sonifyGraph values of true.
     */
    public int getSonifyPolarCount() {
        return this.sonifyPolarCount;
    }

    /**
     * Returns the number of Cartesian equations in the solutionList with
     * sonifyGraph values of true.
     * 
     * @return the number of Cartesian equations in the solutionList with
     * 				sonifyGraph values of true.
     */
    public int getSonifyCartesianCount() {
        return this.sonifyCartesianCount;
    }

    /**
     * Determines if there are any AnalyzedData objects in the solution List.
     * 
     * @return true if there are any AnalyzedData objects in the solution List.
     */
    public boolean anyAnalyzedData() {
        if (solutionList.isEmpty()) {
            return false;
        }
        Solution solution;
        int len = solutionList.size();
        for (int index = 0; index < len; index++) {
            solution = (Solution) solutionList.get(index);
            if (solution.getAnalyzedItem() instanceof AnalyzedData) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if there are any unsolvable equations in the solutionList.
     * Unsolvable equations have bad syntax or are outside the set of equations
     * handled by the MDE library.
     * 
     * @return true if there are any bad equations.
     */
    public boolean anyBadEquations() {
        if (solutionList.isEmpty()) {
            return true;
        }
        Solution solution;
        int len = solutionList.size();
        for (int index = 0; index < len; index++) {
            solution = (Solution) solutionList.get(index);
            if (solution.isBadEquation()) {
                return true;
            }
        }
        return false;
    } // end anyBadEquations

    /**
     * Determines if MDE can generate text descriptions for any items in the
     * solutionList. Solutions are not describable where syntax errors or
     * unsupported equations or data are input.
     * 
     * @return true if any solution is describable.
     */
    public boolean anyDescribable() {
        Solution solution;
        int len = solutionList.size();
        for (int index = 0; index < len; index++) {
            solution = (Solution) solutionList.get(index);
            if (solution.isDescribable()) {
                return true;
            }
        }
        return false;
    } // end anyDescribable

    /**
     * Determines if MDE can generate drawn graphs for any items in the
     * solutionList. Solutions are not graphable where syntax errors or
     * unsupported equations or data are input.
     * 
     * @return true if any solution is graphable.
     */
    public boolean anyGraphable() {
        Solution solution;
        int len = solutionList.size();
        for (int index = 0; index < len; index++) {
            solution = (Solution) solutionList.get(index);
            if (solution.isGraphable()) {
                return true;
            }
        }
        return false;
    } // end anyGraphable

    /**
     * Determines if MDE can generate sonifications for any items in the
     * solutionList. Solutions are not sonifiable where syntax errors or
     * unsupported equations or data are input.
     * 
     * @return true if any solution can be sonified.
     */
    public boolean anySonifiable() {
        Solution solution;
        int len = solutionList.size();
        for (int index = 0; index < len; index++) {
            solution = (Solution) solutionList.get(index);
            if (solution.isSonifiable()) {
                return true;
            }
        }
        return false;
    } // end anySonifiable

    /**
     * Return the left bound value.
     * 
     * @return the left bound value.
     */
    public double getLeft() {
        return bounds.left;
    } // end getLeft

    /**
     * Return the right bound value.
     * 
     * @return the right bound value.
     */
    public double getRight() {
        return bounds.right;
    } // end getRight

    /**
     * Return the top bound value.
     * 
     * @return the top bound value.
     */
    public double getTop() {
        return bounds.top;
    } // end getTop

    /**
     * Return the bottom bound value.
     * 
     * @return the bottom bound value.
     */
    public double getBottom() {
        return bounds.bottom;
    } // end getBottom

    /**
     * Return the Solver object's current solution bounds.
     * 
     * @return the Solver object's current solution bounds.
     */
    public Bounds getBounds() {
        return bounds;
    } // end getBounds

    /**
     * Set the solution bounds only, to the specified Bounds, b.
     * <em>NOTE: Does not solve the solutionList items over the 
     * input bounds.</em>
     * 
     * @param b the bounds.
     */
    public void setBounds(Bounds b) {
        bounds.setBounds(b);
    } // end setBounds

    /**
     * Set the solution bounds only, to the specified bounds left, right, top,
     * and bottom. <em>NOTE: Does not solve the solutionList items over the 
     * new bounds.</em>
     * 
     * @param left the left bound
     * @param right the right bound
     * @param top the top bound
     * @param bottom the bottom bound
     */
    public void setBounds(double left, double right, double top, double bottom) {
        bounds.setBounds(left, right, top, bottom);
    } // end setBounds

    /**
     * Return the preferred bounds.
     * 
     * @return the preferred bounds. 
     */
    public Bounds getPreferredBounds() {
        return preferredBounds;
    } // end getPreferredBounds

    /**
     * Set the solution preferred bounds only.
     * <em>NOTE: Does not solve the solutionList items over the 
     * new bounds.</em>
     * 
     * @param b the preferred bounds.
     */
    public void setPreferredBounds(Bounds b) {
        preferredBounds.setBounds(b.left, b.right, b.top, b.bottom);
    } // end setPreferredBounds

    /**
     * Set the solution preferred bounds only.
     * <em>NOTE: Does not solve the solutionList items over the 
     * new bounds.</em>
     * 
     * @param left the left preferred bound.
     * @param right the right preferred bound.
     * @param top the top preferred bound.
     * @param bottom the bottom preferred bound.
     */
    public void setPreferredBounds(double left, double right, double top,
            double bottom) {
        preferredBounds.setBounds(left, right, top, bottom);
    } // end setPreferredBounds

    /**
     * Register a component to listen for Solver state change events (adding a
     * new item to the solutionList or a new solve event).
     * 
     * @param l the change listener.
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    } // end addChangeListener

    /**
     * Remove the specified component from the Solver change event listener
     * list.
     * 
     * @param l the change listener.
     */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    } // end removeChangeListener

    /**
     * Remove all registered components from the Solver change event listener
     * list.
     */
    public void removeAllChangeListeners() {
        EventListener[] listeners = listenerList.getListeners(ChangeListener.class);
        ChangeListener cl;
        for (int i = 0; i < listeners.length; i++) {
            // Added a typecast to pass Xlint: ROS 2/3/05
            cl = (ChangeListener)listeners[i];
            listenerList.remove(ChangeListener.class, cl);
        }
    } // end removeAllChangeListeners

    /**
     * Notify all registered listener components that a Solver change event has
     * occurred.
     */
    protected void fireStateChanged() {
        // Create our cached change-event if it does not exist.
        if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
        }
        // notify all the listeners
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    } // end fireStateChanged

    private class SolutionFlagsChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            SolutionFlagsStateChange state = (SolutionFlagsStateChange)e.getSource();
            Solution solution = state.solution;
            int showOffset = state.showGraphChanged ? (solution.isShowGraph() ? 1 : -1) : 0;
            int sonifyOffset = state.sonifyGraphChanged ? (solution.isSonifyGraph() ? 1 : -1) : 0;
            updateShowSonifyCounts(solution, showOffset, sonifyOffset);
        }
    }

//    public static void main(String[] args) {
//        Solver s = new Solver();
//
//        s.add(GraphUtilities.combineArgs(args));
//
//        System.out.println("getLeft(): " + s.getLeft() + " getRight(): "
//                + s.getRight());
//        System.out.println("getTop(): " + s.getTop() + " getBottom(): "
//                + s.getBottom());
//
//        // Display information about each of the solutions.
//        Solution solution;
//        SolvedGraph features;
//        for (Iterator iter = s.getSolutionIterator(); iter.hasNext();) {
//            solution = (Solution) iter.next();
//
//            System.out.println("getInputEquation(): "
//                    + solution.getInputEquation());
//
//            features = solution.getFeatures();
//            if (features != null) {
//                System.out.println("Features: " + features);
//            }
//
//            MultiPointXY[] points = solution.getPoints();
//            if (points != null) {
//                for (int i = 0; i < points.length; i += 30) {
//                    System.out.println("points[" + i + "]: " + points[i]);
//                }
//            }
//
//            AnalyzedItem analyzedItem = solution.getAnalyzedItem();
//            if (analyzedItem instanceof AnalyzedEquation) {
//                AnalyzedEquation ae = (AnalyzedEquation) analyzedItem;
//                boolean isFunctionOverInterval = (ae != null)
//                        && ae.isFunctionOverInterval();
//
//                System.out.println("functionOverInterval?: "
//                        + isFunctionOverInterval);
//            }
//        }
//    } // end main

} // end class Solver
