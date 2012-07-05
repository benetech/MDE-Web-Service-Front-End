/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.MdeFeatureNode;
import gov.nasa.ial.mde.util.MathUtil;

import java.text.NumberFormat;

/**
 * A model of a number.
 * 
 * @version 1.0
 * @since 1.0
 */
public class NumberModel {
    
    //Might want to use BigDecimal instead or in addition
    private double decimalValue;
    private String approximateDecimalValue;
    private String rationalValue;
    private String quadraticValue;
    private boolean isAnApproximation;  
    private int fractionDigits = -1; // use default if not set
    
    private static final double EPSILON = 1.0e-10;
    private static final int    DEFAULT_FRACTION_DIGITS = 3;  

    @SuppressWarnings("unused")
	private NumberModel() {
        throw new RuntimeException("Default constructor not permitted");
    }

    /**
     * Constructs a NumberModel with the specified decimal and a default
     * search limit of 100.
     * 
     * @param decimal the decimal value.
     */
    public NumberModel(double decimal) {
        this(decimal, 100);
    } // end NumberModel(double decimal)

    /**
     * Constructs a NumberModel with the specified decimal and search limit.
     * 
     * @param decimal the decimal value.
     * @param searchLimit the search limit.
     */
    public NumberModel(double decimal, int searchLimit) {
        decimalValue = decimal;
        if(MdeSettings.DEBUG){
        	System.out.println(decimalValue);
        }
        if (Double.isInfinite(decimal))
            return;

        constructApproximateDecimal();
        rationalValue = MathUtil.getEquivalentRationalString(decimal, searchLimit);
        quadraticValue = MathUtil.getQuadraticRepresentationString(decimal, searchLimit);
    } // end NumberModel(double decimal, int searchLimit)

    private void constructApproximateDecimal() {
        NumberFormat n = NumberFormat.getInstance();
        

        n.setMinimumFractionDigits(0);
        n.setMaximumFractionDigits((fractionDigits > 0) ? fractionDigits : DEFAULT_FRACTION_DIGITS);
        approximateDecimalValue = n.format(decimalValue);
        
        if(MdeSettings.DEBUG){
        	System.out.println(approximateDecimalValue);
        }
        
        

        try {
        	approximateDecimalValue = approximateDecimalValue.replace(",","");
            double d = new Double(approximateDecimalValue).doubleValue();
            isAnApproximation = (Math.abs(d - decimalValue) > EPSILON
                    * (1.0 + Math.abs(d) + Math.abs(decimalValue)));
        } // end try
        catch (NumberFormatException nfe) {
            throw new RuntimeException("Internal number format error");
        } // end catch
    } // end constructApproximateDecimal

    /**
     * Returns the decimal value.
     * 
     * @return the decimal value.
     */
    public double getDecimalValue() {
        return decimalValue;
    }

    /**
     * Returns the approximate decimal value.
     * 
     * @return the approximate decimal value.
     */
    public String getApproximateDecimalValue() {
        return approximateDecimalValue;
    } // end getApproximateDecimalValue

    /**
     * Returns the rational value.
     * 
     * @return the rational value.
     */
    public String getRationalValue() {
        return rationalValue;
    }

    /**
     * Returns the quadratic value.
     * 
     * @return the quadratic value.
     */
    public String getQuadraticValue() {
        return quadraticValue;
    }

    /**
     * Returns the string representation of the number model.
     * 
     * @return the string representation of the number model.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer s = new StringBuffer("NumberModel:");
        s.append("\ndecimalValue: " + decimalValue);
        if (rationalValue != null) {
            s.append("\nrationalValue: " + rationalValue);
        } else {
            s.append("\nNo rational representation was found.");
        }
        if (quadraticValue != null) {
            s.append("\nquadraticValue: " + quadraticValue);
        } else {
            s.append("\nNo quadratic representation was found.");
        }
        return s.toString();
    }

    /**
     * Returns the MDE Fedature Node for the number model.
     * 
     * @return the MDE Fedature Node for the number model.
     */
    public MdeFeatureNode getMFN() {
        String temp;
        MdeFeatureNode r = new MdeFeatureNode();

        if (Double.isInfinite(decimalValue))
            temp = (decimalValue > 0.0) ? "infinity" : "-infinity";
        else if (Double.isNaN(decimalValue))
            temp = "undefined";
        else
            temp = "" + decimalValue;
        r.addKey("decimalValue");
        r.addValue("decimalValue", temp);

        if ((temp = getApproximateDecimalValue()) != null) {
            r.addKey("approximateDecimalValue");
            r.addValue("approximateDecimalValue", temp);
            r.addKey("isApproximation");
            r.addValue("isApproximation", "" + isAnApproximation);
        } // end if

        if ((temp = getRationalValue()) != null) {
            r.addKey("rationalValue");
            r.addValue("rationalValue", temp);
        } // end if

        if ((temp = getQuadraticValue()) != null) {
            r.addKey("quadraticValue");
            r.addValue("quadraticValue", temp);
        } // end if

        return r;
    } // end getMFN

    /**
     * Returns the XML representation of the number model.
     * 
     * @return the XML representation of the number model.
     */
    public String getXML() {
        StringBuffer s = new StringBuffer("");
        s.append("\n  <decimalValue>" + decimalValue + "</decimalValue>");
        if (rationalValue != null) {
            s.append("\n  <rationalValue>" + rationalValue + "</rationalValue>");
        }
        if (quadraticValue != null) {
            s.append("\n  <quadraticValue>" + quadraticValue + "</quadraticValue>");
        }
        return s.toString();
    }


//    public static void main(String[] args) {
//        try {
//
//            if (args.length == 0) {
//                java.io.InputStreamReader isr = new java.io.InputStreamReader(System.in);
//                java.io.BufferedReader u = new java.io.BufferedReader(isr);
//
//                while (true) {
//                    System.out.println("Enter a number (or CTRL-C to exit): ");
//                    Double d = new Double(u.readLine());
//                    double decimal = d.doubleValue();
//                    NumberModel nm = new NumberModel(decimal);
//                    System.out.println(nm);
//                    System.out.println("xml: " + nm.getXML());
//                }
//            }
//            
//            NumberModel nm = new NumberModel(new Double(args[0]).doubleValue());
//
//            System.out.println(nm);
//            System.out.println("xml: " + nm.getXML());
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

}