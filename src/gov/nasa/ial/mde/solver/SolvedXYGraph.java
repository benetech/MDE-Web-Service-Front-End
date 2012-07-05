/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.solver.features.individual.XInterceptFeature;
import gov.nasa.ial.mde.solver.features.individual.YInterceptFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.util.MathUtil;

/**
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedXYGraph extends SolvedGraph {
    
    /** The analyzed equation for the graph. */
    protected AnalyzedEquation analyzedEq;
    
    /** The x intercepts. */
    protected double[] xInts;
    
    /** The y intercepts. */
    protected double[] yInts;
    
    private boolean nullOrAll = false;

    /**
     * Constructs a solved XY graph with a null analyzed equation and intercepts.
     */
    public SolvedXYGraph() {
        this.analyzedEq = null;
        this.xInts = null;
        this.yInts = null;
    }

    /**
     * Constructs a solved XY graph for the specified analyzed equation.
     * 
     * @param equation the analyzed equation.
     * @param graphName the name to use with the graph.
     */
    public SolvedXYGraph(AnalyzedEquation equation, String graphName) {
        this(equation);
        putFeature("graphName", graphName);
        if (graphName.equals("null set") || graphName.equals("all points")) {
            nullOrAll = true;
        }
    }

    /**
     * Constructs a solved XY graph for the specified analyzed equation.
     * 
     * @param equation the analyzed equation.
     */
    public SolvedXYGraph(AnalyzedEquation equation) {
        super();
        this.analyzedEq = equation;
        
        putFeature("coordinateSystem", "Cartesian");
        putFeature("equationPrint", analyzedEq.printEquation());
        if (analyzedEq.getParameters().length > 0)
            putFeature("originalEquationPrint", analyzedEq.printOriginalEquation());
        //putFeature("graphName", "unclassified");

        String[] xy = analyzedEq.getActualVariables();

        putFeature("abscissaSymbol", xy[0]);
        putFeature("ordinateSymbol", xy[1]);

        if (nullOrAll)
            return;

        xInts = analyzedEq.getXIntercepts();
        yInts = analyzedEq.getYIntercepts();

        if (xInts != null) {
            for (int i = 0; i < xInts.length; i++) {
                if (i == 0) {
                    addToFeature(XInterceptFeature.KEY, MathUtil.trimDouble(xInts[i], 6));
                    continue;
                } // end if
                if (xInts[i] != xInts[i - 1])
                    addToFeature(XInterceptFeature.KEY, MathUtil.trimDouble(xInts[i], 6));
            } // end for i
        }

        if (yInts != null) {
            for (int i = 0; i < yInts.length; i++) {
                if (i == 0) {
                    addToFeature(YInterceptFeature.KEY, MathUtil.trimDouble(yInts[i], 6));
                    continue;
                } // end if

                if (yInts[i] != yInts[i - 1])
                    addToFeature(YInterceptFeature.KEY, MathUtil.trimDouble(yInts[i], 6));
            }
        }
    } // end SolvedXYGraph

//    public static void main(String[] args) {
//        AnalyzedEquation e = new AnalyzedEquation(
//            gov.nasa.ial.mde.util.StringSplitter.combineArgs(args));
//
//        try {
//            e.setParameterValue("a", -1.5);
//            e.setParameterValue("b", 2.3);
//            e.setParameterValue("c", -10);
//        } // end try
//        catch (Exception ex) {}
//
//        System.out.println(new SolvedXYGraph(e).getXMLString());
//    } // end main

} // end class SolvedXYGraph
