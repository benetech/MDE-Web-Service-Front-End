/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Feb 22, 2004
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.solver.MdeFeatureNode;

import java.text.NumberFormat;

/**
 * The <code>AngleModel</code> class models angles in radians and degrees.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class AngleModel {

    private String      degreeRepresentation;

    private NumberModel piFraction;

    private boolean     degreesUnset   = true;

    private boolean     radsUnset      = true;

    private boolean     hasRadFraction = false;

    private double      degrees;

    private double      radians;

    /** 
     * Default constructor. 
     */
    public AngleModel() {
        super();
    }
    
    /**
     * Sets the angle in degrees.
     * 
     * @param deg the angle in degrees.
     */
    public void setAngleInDegrees(double deg) {
        if (degreesUnset) {
            NumberFormat nf = NumberFormat.getInstance();

            nf.setMaximumFractionDigits(3);
            nf.setMinimumFractionDigits(0);
            degreeRepresentation = nf.format(deg);
            degrees = deg;
            degreesUnset = false;
            if (radsUnset)
                setAngleInRads(Math.PI * deg / 180.0);
        } // end if
        else {
            throw new IllegalStateException("AngleModel is immutable");
        }
    } // end setAngleInDegrees

    /**
     * Sets the angle in Radians.
     * 
     * @param rads the angle in Radians.
     */
    public void setAngleInRads(double rads) {
        if (radsUnset) {
            piFraction = new NumberModel(rads / Math.PI);
            radians = rads;
            radsUnset = false;
            if (degreesUnset)
                setAngleInDegrees(180.0 * rads / Math.PI);
        } // end if
        else {
            throw new IllegalStateException("AngleModel is immutable");
        }
    } // end setAngleInRads

    /**
     * Returns a string representation of the angle in degrees.
     * 
     * @return string representation of the angle in degrees.
     */
    public String getRepresentationInDegrees() {
        return degreeRepresentation;
    } // end getRepresentationInDegrees

    /**
     * Returns a string representation of the angle in Radians.
     * 
     * @return string representation of the angle in Radians.
     */
    public String getRepresentationInRadians() {
        String s = piFraction.getRationalValue();

        if (s == null) {
            s = piFraction.getQuadraticValue();
        }

        if (s != null) {
            hasRadFraction = true;
            return "(" + s + ")PI";
        } // end if

        NumberFormat nf = NumberFormat.getInstance();

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(3);
        return nf.format(Math.PI * piFraction.getDecimalValue());
    } // end getRepresentationInRadians

    /**
     * Returns the angle in degrees.
     * 
     * @return the angle in degrees.
     */
    public double getDegrees() {
        return degrees;
    } // end getDegrees

    /**
     * Returns the angle in Radians.
     * 
     * @return the angle in Radians.
     */
    public double getRadians() {
        return radians;
    } // end getRadians

    
    /**
     * Returns the MDE featue node for the angle.
     * 
     * @return the MDE featue node for the angle.
     */
    public MdeFeatureNode getMFN() {
        MdeFeatureNode f = new MdeFeatureNode();

        f.addKey("degreeValue");
        f.addValue("degreeValue", getRepresentationInDegrees());

        f.addKey("radianValue");
        f.addValue("radianValue", getRepresentationInRadians());

        if (hasRadFraction) {
            f.addKey("fractionalRadians");
            f.addValue("fractionalRadians", getRepresentationInRadians());
        } // end if

        return f;
    } // end getMFN


//    public static void main(String[] args) {
//        AngleModel a = new AngleModel();
//        gov.nasa.ial.mde.solver.symbolic.Expression e = 
//            new gov.nasa.ial.mde.solver.symbolic.Expression(
//                gov.nasa.ial.mde.util.StringSplitter.combineArgs(args));
//
//        a.setAngleInRads(e.evaluate(new java.util.Hashtable()));
//        System.out.println(a.getMFN().getXMLString());
//    } // end main

    
} // end class AngleModel
