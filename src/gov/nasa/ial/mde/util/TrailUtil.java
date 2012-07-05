/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 30, 2004
 */
package gov.nasa.ial.mde.util;

import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.solver.GraphTrail;

import java.util.ArrayList;

/**
 * The <code>TrailUtil</code> class is a utility for handling point trails.
 * 
 * @author Dan Dexter
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class TrailUtil {

    /*
     * Splits the ragged array f into separate trails or branches. A branch is a
     * double[][2] containing a list of ordered pairs (x,y) to be drawn sequentially.
     * The input array f is a so-called "ragged" array which means that the length of
     * f[i] varies with i. The value of f[i][0] is the x value, and f[i][j] are any
     * corresponding y values, j = 1, ... An individual branch b is obtained by
     * taking a slice of f, b[j1-L][0] = f[j1][0] and b[j1-L][1] = f[j1][k] for some
     * k > 0 and j ranging from l to h, where L and H are indices for which
     * f[i].length changes.
     * 
     * @param p the array of multiple point arrays.
     * @param maxJump the maximum value jump between segment boundaries.
     * @return the branches from the multiple point arrays.
     */
    public static double[][][] getBranchesFrom(MultiPointXY[] p, double maxJump) {
        int i;
        int[] segmentBoundaries = getSegmentBoundariesFrom(p, maxJump);

        double[][] f = MathUtil.multiToRagged(p);

        // first count the branches
        int numBranches = 0, numSegments = segmentBoundaries.length - 1;

        for (i = 0; i < numSegments; i++) {
            numBranches += (f[segmentBoundaries[i]].length - 1);
        }

        // accounts for f[i][0] being the x value. we have a branch for each y value

        int k = 0; // counts over new branches
        double[][][] r = new double[numBranches][0][0]; // the array of branches to return
        
        for (i = 0; i < numSegments; i++) {
            int l = segmentBoundaries[i]; // low boundary of current segment
            int b = f[l].length - 1; // the number of branches in the ith segment
            int h = segmentBoundaries[i + 1]; // high boundary of current segment

            for (int j = 0; j < b; j++, k++) {
                r[k] = new double[h - l][2]; // allocate the kth branch

                for (int j1 = l; j1 < h; j1++) {
                    // fill in the values
                    r[k][j1 - l][0] = f[j1][0];
                    r[k][j1 - l][1] = f[j1][j + 1];
                } // end for j1
            } // end for j
        } // end for i

        return r;
    } // end getBranchesFrom

    /**
     * Returns the graph trails from the multiple point arrays and the specified
     * maximum jump between segment boundaries.
     * 
     * @param p the array of multiple point arrays.
     * @param maxJump the maximum value jump between segment boundaries.
     * @return the graph trail from the multiple point arrays.
     */
    public static GraphTrail[] getGraphTrailsFrom(MultiPointXY[] p, double maxJump) {
        double[][][] f = getBranchesFrom(p, maxJump);
        int n = f.length;
        ArrayList<GraphTrail> gt = new ArrayList<GraphTrail>();

        for (int i = 0; i < n; i++) {
            GraphTrail g = new GraphTrail(f[i]);

            if (g.getLength() > 1) {
                gt.add(g);
            }
        } // end for i

        return gt.toArray(new GraphTrail[gt.size()]);
    } // end getGraphTrailsFrom

    /**
     * Returns the segment boundaries from the multiple point arrays and the
     * specified maximum jump between segment boundaries. 
     * 
     * @param data the array of multiple point arrays.
     * @param maxJump the maximum value jump between segment boundaries.
     * @return an array containing the indexes to the segment boundaries.
     */
    public static int[] getSegmentBoundariesFrom(MultiPointXY[] data, double maxJump) {
        int i, n = data.length;
        ArrayList<Integer> segmentBoundaries = new ArrayList<Integer>(data.length);

        segmentBoundaries.add(new Integer(0));
        for (i = 1; i < n; i++) {
            // If number of y's changes 
            if (data[i] == null || data[i - 1] == null) {
                continue;
            }

            if (data[i].yArray.length != data[i - 1].yArray.length) {
                segmentBoundaries.add(new Integer(i));
                continue;
            } // end if

            int j, k = data[i].yArray.length;

            for (j = 0; j < k; j++) {
                if (Math.abs(data[i].yArray[j] - data[i - 1].yArray[j]) > maxJump) {
                    segmentBoundaries.add(new Integer(i));
                    break;
                } // end if
            }
        } // end for i

        segmentBoundaries.add(new Integer(n));

        int[] r = new int[n = segmentBoundaries.size()];

        for (i = 0; i < n; i++) {
            r[i] = segmentBoundaries.get(i).intValue();
        }

        return r;
    } // end getSegmentBoundariesFrom

}
