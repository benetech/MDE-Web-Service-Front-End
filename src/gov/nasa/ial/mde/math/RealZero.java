/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

/**
 * The <code>RealZero</code> class represents a real zero.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class RealZero {
    
    /** End-point sign signature. */
    public final static int MINUS_MINUS = 0, 
                            MINUS_PLUS = 1, 
                            PLUS_MINUS = 2, 
                            PLUS_PLUS = 3,
                            UNDEFINED = 4;

    private double  x;

    private int     signature = UNDEFINED;

    /**
     * Constructs a <code>RealZero</code> for the given value.
     * 
     * @param x the value.
     */
    public RealZero(double x) {
        this.x = x;
    } // end RealZero

    /**
     * Constructs a <code>RealZero</code> for the given value and signature.
     * 
     * @param x the value.
     * @param signature the sign signature.
     */
    public RealZero(double x, int signature) {
        this.x = x;
        this.signature = signature;
    } // end RealZero

    /**
     * Returns the end-point sign signature.
     * 
     * @return the end-point sign signature.
     */
    public int getSignature() {
        return signature;
    }

    /**
     * Returns the end-point sign signature for the given polynomial.
     * 
     * @param factor the polynomial factor.
     * @return the end-point sign signature.
     */
    public int getSignature(PNom factor) {
        double v = factor.eval(x);

        if (v == 0.0) {
            return UNDEFINED;
        }

        if (v < 0.0) {
            return (-1 - signature) & 3;
        }

        return signature;
    } // end getSignature

    /**
     * Returns the real zero value.
     * 
     * @return the real zero value.
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the signature to one of MINUS_MINUS, MINUS_PLUS, PLUS_MINUS,
     * PLUS_PLUS, or UNDEFINED.
     * 
     * @param i the signature.
     */
    public void setSignature(int i) {
        signature = i;
    }

} // end class RealZero