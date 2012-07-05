/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.math.Matrix;
import gov.nasa.ial.mde.util.MathUtil;

import java.util.ArrayList;

/**
 * The <code>DataModelBuilder</code> class builds a model based on the data.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class DataModelBuilder {
    
    /** The degree of the data model. */
    protected int       degree;
    
    /** The fit of the data model. */
    protected double    fit;
    
    /** The model data. */
    protected ArrayList<double[]> data = new ArrayList<double[]>();
    
    /** The model values as a vector. */
    private double[]    modelVector = new double[0];
    
    private final static double MAX_DATA = 200.0;

    /**
     * Builds a model based on the indices.
     * 
     * @param indices the model indices.
     */
    public void buildModel(int[] indices) {
        int i, n = data.size();
        degree = indices.length;
        ArrayList<double[]> temp = new ArrayList<double[]>();

        for (i = 0; i < n; i++) {
            double[] td = new double[degree];

            for (int j = 0; j < degree; j++)
                td[j] = ((double[])data.get(i))[indices[j]];

            if (qualifies(td))
                temp.add(td);
        } // end for i

        if (temp.size() < 10 * degree) {
            fit = Double.POSITIVE_INFINITY;
            return;
        } // end if

        Matrix a = new Matrix(temp.toArray(new double[0][0]));

        double[] s = a.getSingularValues();
        double f = s[degree - 1] / s[0];

        modelVector = Matrix.transpose(a.getRightSingularVectors()).getArray()[degree - 1];
        fit = (f == 0.0) ? Double.NEGATIVE_INFINITY : MathUtil.log10(f);
    } // end buildModel

    /**
     * Returns the degree of the model.
     * 
     * @return the degree of the model.
     */
    public int getDegree() {
        return degree;
    } // end getDegree

    /**
     * Returns the fit of the model.
     * 
     * @return the fit of the model.
     */
    public double getFit() {
        return fit;
    } // end getFit

    /**
     * Returns the model.
     * 
     * @return the model.
     */
    public double[] getModel() {
        return modelVector;
    } // end getModel

    private boolean qualifies(double[] dataArray) {
        double m = 0.0, t;

        for (int i = 0; i < degree; i++) {
            if ((t = Math.abs(dataArray[i])) > m)
                m = t;

            if (t != t)
                return false;
        } // end for i

        if (m > MAX_DATA)
            return false;

        return true;
    } // end qualifies
    
} // end class DataModelBuilder
