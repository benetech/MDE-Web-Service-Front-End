/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;

/**
 * A Polar model.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarModel {
    
    /** The fit of the polar model. */
    public double fit = Double.POSITIVE_INFINITY;
    
    /** The polar model vector. */
    public double[] modelVector;
    
    /** The Polar model signature. */
    public int[] modelSignature;
    
    /** The degree of the Polar. */
    public int degree;
    
    /** The complexity of the Polar. */
    public double complexity;
    
    /** Which signature of the Polar. */
    public int whichSignature = 0;
    
    /** The Polar model name. */
    public String name;
    
    /** The Polar indentity. */
    public int identity = PolarClassifier.POLAR_UNKNOWN;

    /**
     * Evaluates the model signatures using the given Polar model builder.
     * 
     * @param p the Polar model builder.
     * @param modelSignatures the model signatures.
     */
    public void evaluate(PolarModelBuilder p, int[][] modelSignatures) {
        int i, n = modelSignatures.length;

        for (i = 0; i < n; i++) {
            p.buildModel(modelSignatures[i]);
            if (p.getFit() <= fit) {
                fit = p.getFit();
                modelVector = p.getModel();
                modelSignature = modelSignatures[whichSignature = i];
                degree = modelSignature.length;
            } // end if
        } // end for i

        pruneModel();
        complexity = degree + (n - 1.0) / n;
    } // end evaluate

    /**
     * Returns the name as a String.
     * 
     * @return the name as a String.
     */
    public String toString() {
        return name;
    } // end toString

    /**
     * Returns the phase in Radians.
     * 
     * @param a one length of the phasor.
     * @param b the other length of the phasor.
     * @return the phase in Radians.
     */
    public static double phaseInRads(double a, double b) {
        return Math.atan2(b, a);
    } // end phaseInRads

    /**
     * Returns the phase in degrees.
     * 
     * @param a one length of the phasor.
     * @param b the other length of the phasor.
     * @return the phase in degrees.
     */
    public static double phaseInDeg(double a, double b) {
        return 180.0 * phaseInRads(a, b) / Math.PI;
    } // end phaseInDeg

    /**
     * Returns the amplitude.
     * 
     * @param a one length of the phasor.
     * @param b the other length of the phasor.
     * @return the amplitude.
     */
    public static double amplitude(double a, double b) {
        return Math.sqrt(a * a + b * b);
    } // end amplitude

    private void pruneModel() {
        int i, n = modelVector.length;
        double t, u = 0.0;

        for (i = 0; i < n; i++) {
            if ((t = Math.abs(modelVector[i])) > u) {
                u = t;
            }
        }

        t = u * 1.0e-8;

        for (i = 0; i < n; i++) {
            if (Math.abs(modelVector[i]) < t) {
                modelVector[i] = 0.0;
            }
        }
    } // end pruneModel
    
} // end class PolarModel
