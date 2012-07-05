/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.math.MultiPointXY;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A Polar model builder.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarModelBuilder extends DataModelBuilder {
    
    private final static int MAX_GENERATORS = 12;
    private final static int NUM_RS = 4, NUM_THETAS = 4;

    /**
     * Default constructor.
     */
    public PolarModelBuilder() {
        super();
    } // end PolarModelBuilder

    /**
     * Adds a new Polar point.
     * 
     * @param r the radial coordinate.
     * @param theta the angular coordinate.
     */
    public void addNewPoint(double r, double theta) {
        int i, j;
        double[] tempData = new double[MAX_GENERATORS];

        tempData[0] = 1.0;
        tempData[1] = r;
        tempData[2] = 1.0 / r;
        tempData[3] = r * r;
        for (i = 1, j = NUM_RS; i <= NUM_THETAS; i++) {
            double phi = i * theta;

            tempData[j++] = Math.cos(phi);
            tempData[j++] = Math.sin(phi);
        } // end for i

        data.add(tempData);
    } // end addNewPoint

    /**
     * Adds a new multiple point.
     * 
     * @param rt a multiple point in r and theta.
     */
    public void addNewPoint(MultiPointXY rt) {
        double[] r = rt.yArray;
        int i, n = r.length;

        for (i = 0; i < n; i++)
            addNewPoint(r[i], rt.x);
    } // end addNewPoint

    /**
     * Returns the ranked models.
     * 
     * @return the ranked models.
     */
    public PolarModel[] getRankedModels() {
        PolarModel[] rpm = {
                new PolarEnchiladaModel(this),
                new PolarTrochoidModel(this),
                new PolarConicModel(this),
                new PolarRoseModel(this),
                new PolarLineModel(this),
                new PolarLemniscateModel(this) };

        Arrays.sort(rpm, new Comparator<PolarModel>() {
            public int compare(PolarModel o1, PolarModel o2) {
                PolarModel p1 = (PolarModel)o1, p2 = (PolarModel)o2;

                if (p1.fit > p2.fit) {
                    return 1;
                }

                if (p1.fit < p2.fit) {
                    return -1;
                }

                return 0;
            } // end compare
        } // end new Comparator
        ); // end sort
        return rpm;
    } // end getRankedModels
    
} // end class PolarModelBuilder
