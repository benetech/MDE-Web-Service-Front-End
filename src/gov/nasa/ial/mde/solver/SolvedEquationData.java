/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 *
 * Created on Apr 30, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.classifier.MDEClassifier;
import gov.nasa.ial.mde.solver.classifier.PolynomialClassifier;
import gov.nasa.ial.mde.solver.numeric.PolynomialModel;
import gov.nasa.ial.mde.solver.numeric.QuadraticModel;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * The class for solved equation data.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedEquationData extends SolvedXYGraph {
    
    private AnalyzedItem analyzedItem;
    private MDEClassifier classifier;
    private PolynomialModel polyModel = null;
    private int numSegments;
    private GraphTrail[] segments;
    private String[] newFeatures = { "ComputedFunctionData", "DataID" };

    /**
     * Constructs a solved equation for the data given the specified analyzed item.
     * 
     * @param analyzedItem the item to solve.
     */
    public SolvedEquationData(AnalyzedItem analyzedItem) {
        super();
        this.analyzedItem = analyzedItem;
        doInit();
    } // end SolvedEquationData

    /**
     * Constructs a solved equation for the data given the specified analyzed item.
     * 
     * @param analyzedEquation the equation to solve.
     */
    public SolvedEquationData(AnalyzedEquation analyzedEquation) {
        super(analyzedEquation);
        this.analyzedItem = analyzedEquation;
        doInit();
        //System.out.println("DEBUG: " + this.getXMLString());
    } // end SolvedEquationData

    private void doInit() {
        segments = analyzedItem.getGraphTrails();

      //  System.out.println("In SolvedEquationData");
        
        if ((classifier = analyzedItem.getClassifier()) != null)
            if (classifier instanceof PolynomialClassifier)
                polyModel = ((PolynomialClassifier)classifier).getBestGuess();

        numSegments = segments.length;
        //System.out.println("In SolvedEquationData applying features");
        putNewFeatures(newFeatures);

        //TODO: Consider relocating DataID in XML rework.
        putFeature("DataID", analyzedItem.getName());
        putFeature("graphName", "FunctionOverInterval");
        

        MdeFeatureNode node = new MdeFeatureNode();

        node.addKey("NumSegments");
        node.addValue("NumSegments", "" + numSegments);

        if (polyModel instanceof QuadraticModel) {
            QuadraticModel qm = (QuadraticModel)polyModel;

            node.addKey("AlternateEquation");
            node.addValue("AlternateEquation", qm.toString() + " = 0");
        } // end if

        node.addKey("FunctionAnalysisData");

        for (int which = 0; which < numSegments; which++)
            node.addValue("FunctionAnalysisData", getMFN(findEndpoints(which)));

        putFeature("ComputedFunctionData", node);
    } // end doInit

    private IntervalEndpoint[] findEndpoints(int which) {
        ArrayList<IntervalEndpoint> e = new ArrayList<IntervalEndpoint>();
        PointXY[] p = segments[which].getPoints();
        int lastP = p.length - 1;

        e.add(new IntervalEndpoint(p[0], IntervalEndpoint.BOUNDARY_POINT));
        e.add(new IntervalEndpoint(p[lastP], IntervalEndpoint.BOUNDARY_POINT));

        if (lastP > 1) {
            int i;
            IntervalEndpoint e0 = new IntervalEndpoint(p[0]);
            IntervalEndpoint e1 = new IntervalEndpoint(p[1]);
            int intervalSense = IntervalDescription.getDirection(e0, e1);

            for (i = 2; i <= lastP; i++) {
                e0 = e1;
                e1 = new IntervalEndpoint(p[i]);
                int newSense = IntervalDescription.getDirection(e0, e1);

                if (newSense != intervalSense) {
                    e.add(e0);
                    intervalSense = newSense;
                } // end if
            } // end for i

            Collections.sort(e);

            double x = 0.0, y = 0.0;
            int count = 1, i1;
            IntervalEndpoint[] eps = (IntervalEndpoint[])e.toArray(new IntervalEndpoint[e.size()]);

            lastP = eps.length - 1;
            e.clear();

            for (i = 0; i <= lastP; i = i1) {
                x = eps[i].xValue;
                y = eps[i].leftYValue;

                for (i1 = i + 1; i1 <= lastP; i1++) {
                    e0 = eps[i1 - 1];
                    e1 = eps[i1];

                    if (e1.xValue - e0.xValue > IntervalDescription.MIN_INTERVAL_LENGTH)
                        break;

                    if (IntervalDescription.getDirection(e0, e1) != IntervalDescription.REMAINS_CONSTANT)
                        break;

                    x += e1.xValue;
                    y += e1.leftYValue;
                    count++;
                } // end for i1

                e.add(new IntervalEndpoint(new PointXY(x / count, y / count)));
                count = 1;
            } // end for i

            eps = (IntervalEndpoint[])e.toArray(new IntervalEndpoint[e.size()]);

            if (eps.length > 2) {
                lastP = eps.length - 1;

                for (i = 1; i < lastP; i++) {
                    e0 = eps[i - 1];
                    e1 = eps[i];
                    IntervalEndpoint e2 = eps[i + 1];

                    if (IntervalDescription.getDirection(e0, e1) == IntervalDescription.INCREASES
                            && IntervalDescription.getDirection(e1, e2) == IntervalDescription.DECREASES)
                        eps[i] = new IntervalEndpoint(new PointXY(e1.xValue, e1.leftYValue),
                                IntervalEndpoint.LOCAL_MAX);

                    if (IntervalDescription.getDirection(e0, e1) == IntervalDescription.DECREASES
                            && IntervalDescription.getDirection(e1, e2) == IntervalDescription.INCREASES)
                        eps[i] = new IntervalEndpoint(new PointXY(e1.xValue, e1.leftYValue),
                                IntervalEndpoint.LOCAL_MIN);

                    if (IntervalDescription.getDirection(e0, e1) == IntervalDescription.INCREASES
                            && IntervalDescription.getDirection(e1, e2) == IntervalDescription.INCREASES)
                        eps[i] = new IntervalEndpoint(new PointXY(e1.xValue, e1.leftYValue),
                                IntervalEndpoint.INFLECTION_POINT);

                    if (IntervalDescription.getDirection(e0, e1) == IntervalDescription.DECREASES
                            && IntervalDescription.getDirection(e1, e2) == IntervalDescription.DECREASES)
                        eps[i] = new IntervalEndpoint(new PointXY(e1.xValue, e1.leftYValue),
                                IntervalEndpoint.INFLECTION_POINT);

                    if (IntervalDescription.getDirection(e0, e1) == IntervalDescription.REMAINS_CONSTANT
                            || IntervalDescription.getDirection(e1, e2) == IntervalDescription.REMAINS_CONSTANT)
                        eps[i] = new IntervalEndpoint(new PointXY(e1.xValue, e1.leftYValue),
                                IntervalEndpoint.UNDEFINED);
                } // end for i

                return eps;
            } // end if
        } // end if

        return (IntervalEndpoint[])e.toArray(new IntervalEndpoint[e.size()]);
    } // end getEndpoints

    private MdeFeatureNode getMFN(IntervalEndpoint[] eps) {
        MdeFeatureNode r = new MdeFeatureNode();
        int i, n = eps.length;

        r.addKey("EndPoint");
        r.addKey("intervalDescription");

        for (i = 0; i < n; i++)
            r.addValue("EndPoint", eps[i].getMFN());

        for (i = 1; i < n; i++)
            r.addValue("intervalDescription", new IntervalDescription(eps[i - 1], eps[i]).getMFN());

        return r;
    } // end getMFN


//    public static void main(String[] args) {
//        gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation e = 
//            new gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation(
//                gov.nasa.ial.mde.util.StringSplitter.combineArgs(args));
//
//        e.computePoints(-10.0, 10.0, -10.0, 10.0);
//
//        SolvedEquationData s = new SolvedEquationData(e);
//        System.out.println(s.getXMLString());
//    }

}
