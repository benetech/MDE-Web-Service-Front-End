/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.util;

import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.math.PointRT;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.properties.MdeSettings;

/**
 * <code>PointsUtil</code> is utility class for handling the PointXY and 
 * MultiPointXY classes.
 *
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class PointsUtil {

    /**
     * Converts the Polar points to Cartesian points.
     * 
     * @param polars the points in Polar form.
     * @return the points in Cartesian form.
     */
    public static PointXY[] toCartesian(PointXY[] polars) {
        int n = polars.length;
        PointXY[] r = new PointXY[n];
        if (MdeSettings.DEBUG) {
            System.out.println("polars.length=" + n);
        }
        for (int i = 0; i < n; i++) {
            r[i] = new PointRT(polars[i].y, polars[i].x).toCartesian();
        }
        return r;
    } // end toCartesian

    /**
     * Creates a multi-point array from the X and Y data arrays and interpolates
     * as necessary to ensure <code>length</code> number of points.
     * 
     * @param leftIndex left starting array index.
     * @param rightIndex right ending array index.
     * @param length the length of the multi-point array to create.
     * @param xData X-data value array.
     * @param yData Y-data value array.
     * @return a multi-point array.
     */
    public static MultiPointXY[] interpolatePoints(int leftIndex, 
                                                   int rightIndex, 
                                                   int length, 
                                                   double[] xData,
                                                   double[] yData) {
        // If we only have one point then just repeat it.
        if (leftIndex == rightIndex) {
            // Just use the one point.
            return fillPoints(leftIndex, 1, xData, yData);
        }

        int pointBudget = length - ((rightIndex - leftIndex) + 1);
        double stepSize = (double) (rightIndex - leftIndex) / (double) (pointBudget + 1);
        MultiPointXY[] r = new MultiPointXY[length];
        int lastPointPos = length - 1;

        int i, k, index, position, interpolateCnt;
        double x, y, xStep, yStep;
        double sum = 0.0;

        // Handle the first index as a special case to ensure we pick up this end point.
        r[0] = new MultiPointXY(xData[leftIndex], yData[leftIndex]);

        for (i = 1, index = leftIndex + 1; (i < lastPointPos) && (index <= rightIndex); i++, index++) {
            interpolateCnt = 0;
            position = index - leftIndex;
            while ((sum + stepSize) < position) {
                sum += stepSize;
                interpolateCnt++;
            }

            // Do linear interpolation of the X and Y real-data values between
            // the current index and the previous index.
            if (interpolateCnt > 0) {
                k = index - 1;
                x = xData[k];
                y = yData[k];

                k = interpolateCnt + 1;
                xStep = (xData[index] - x) / k;
                yStep = (yData[index] - y) / k;

                do {
                    x += xStep;
                    y += yStep;
                    r[i] = new MultiPointXY(x, y);
                    interpolateCnt--;
                    i++;
                } while ((interpolateCnt > 0) && (i < lastPointPos));
            }

            if (i < lastPointPos) {
                r[i] = new MultiPointXY(xData[index], yData[index]);
            }
        }

        // Handle the last index as a special case to ensure we pick up this end point.
        r[lastPointPos] = new MultiPointXY(xData[rightIndex], yData[rightIndex]);

        return r;
    }

    /**
     * Creates a multi-point array from the X and Y data arrays and decimates
     * as necessary to ensure <code>length</code> number of points.
     * 
     * @param leftIndex left starting array index.
     * @param rightIndex right ending array index.
     * @param length the length of the multi-point array to create.
     * @param xData X-data value array.
     * @param yData Y-data value array.
     * @return a multi-point array.
     */
    public static MultiPointXY[] decimatePoints(int leftIndex, 
                                                int rightIndex, 
                                                int length, 
                                                double[] xData,
                                                double[] yData) {
        double stepSize = (double) ((rightIndex - leftIndex) + 1) / (double) length;
        MultiPointXY[] r = new MultiPointXY[length];
        int lastIndex = length - 1;

        int i, index;
        double offset;

        // Handle the first index as a special case to ensure we pick up this end point.
        r[0] = new MultiPointXY(xData[leftIndex], yData[leftIndex]);

        // Decimate by using every dataStep value from the real-data.
        for (i = 1, offset = leftIndex + stepSize; i < lastIndex; i++, offset += stepSize) {
            index = (int) Math.round(offset);
            if (index > rightIndex) {
                index = rightIndex;
            }
            r[i] = new MultiPointXY(xData[index], yData[index]);
        }

        // Handle the last index as a special case to ensure we pick up this end point.
        r[lastIndex] = new MultiPointXY(xData[rightIndex], yData[rightIndex]);

        return r;
    }

    /**
     * Creates a multi-point array from the X and Y data arrays.
     * 
     * @param leftIndex left starting array index.
     * @param rightIndex right ending array index.
     * @param xData X-data value array.
     * @param yData Y-data value array.
     * @return a multi-point array.
     */
    public static MultiPointXY[] copyPoints(int leftIndex, 
                                            int rightIndex, 
                                            double[] xData, 
                                            double[] yData) {
        int len = (rightIndex - leftIndex) + 1;
        MultiPointXY[] r = new MultiPointXY[len];
        int i, dataIndex;
        for (i = 0, dataIndex = leftIndex; i < len; i++, dataIndex++) {
            r[i] = new MultiPointXY(xData[dataIndex], yData[dataIndex]);
        }
        return r;
    }

    /**
     * Creates a multi-point array from the X and Y data array value at the
     * specified index.
     * 
     * @param index the array index in the data to fill the multi-point array with.
     * @param length the length of the multi-point array to create.
     * @param xData X-data value array.
     * @param yData Y-data value array.
     * @return a multi-point array.
     */
    public static MultiPointXY[] fillPoints(int index, 
                                            int length, 
                                            double[] xData,
                                            double[] yData) {
        MultiPointXY point = new MultiPointXY(xData[index], yData[index]);
        MultiPointXY[] r = new MultiPointXY[length];
        for (int i = 0; i < length; i++) {
            r[i] = point;
        }
        return r;
    }

}
