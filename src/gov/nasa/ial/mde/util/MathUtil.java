/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.util;

import gov.nasa.ial.mde.math.MdeNumberFormat;
import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.math.Roots;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.symbolic.RationalExpression;

/**
 * <code>MathUtil</code> is a math utility class.
 * 
 * @author Dan Dexter
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MathUtil {
	
    private final static double[] inverseFactorials = {
                                            1.0,
                                            1.0,
                                            0.5,
                                            0.16666666666666666,
                                            0.041666666666666664,
                                            0.008333333333333333,
                                            0.001388888888888889,
                                            1.984126984126984E-4,
                                            2.48015873015873E-5,
                                            2.7557319223985893E-6,
                                            2.755731922398589E-7,
                                            2.505210838544172E-8,
                                            2.08767569878681E-9,
                                            1.6059043836821613E-10,
                                            1.1470745597729725E-11,
                                            7.647163731819816E-13,
                                            4.779477332387385E-14,
                                            2.8114572543455206E-15,
                                            1.5619206968586225E-16,
                                            8.22063524662433E-18 };
    private final static int MAX_TERMS = inverseFactorials.length;
    private final static double[] terms = new double[MAX_TERMS];
    
    private static MdeNumberFormat MNF = MdeNumberFormat.getInstance();
    private static int defaultNumDigits = -1;

	private static final double INV_LOG2 = 1.0 / Math.log(2.0);
	private static final double INV_LOG10 = 1.0 / Math.log(10.0);
	
	/**
     * Calculates Log base 2 of a given number.
     * 
	 * @param a the number to calculate the Log base 2 of.
	 * @return the Log base 2 of the number <code>a</code>
	 */
	public static final double log2(double a) {
        return INV_LOG2 * Math.log(a);
    } // end log2
	
    /**
     * Calculates Log base 10 of a given number.
     * 
     * @param a the number to calculate the Log base 10 of.
     * @return the Log base 10 of the number <code>a</code>
     */
    public static final double log10(double a) {
        return INV_LOG10 * Math.log(a);
    } // end log10
	
    /**
     * Calculates Log of a given number for the specified base.
     * 
     * @param a the number to calculate the Log of.
     * @param base the base for calculationg the log.
     * @return the Log of the number for the given <code>base</code>
     */
	public static final double logBaseX(double a, double base) {
		return Math.log(a) / Math.log(base);
	}
    
    /**
     * Calculates <code>log( 1 + x )</code>.
     * 
     * @param x the number to calculate the function over.
     * @return the value of <code>log( 1 + x )</code>.
     */
    public static double logOnePlusX(double x) {
        if (Math.abs(x) > 0.1) {
            return Math.log(1.0 + x);
        }
        if (x == 0.0) {
            return 0.0;
        }

        double r = 0.0, t = x;
        int i, l;

        for (l = 0; l < MAX_TERMS; l++, t *= (-x)) {
            if (Math.abs(terms[l] = t / (l + 1)) < 1.0e-16 * Math.abs(x)) {
                break;
            }
        }

        if (l >= MAX_TERMS) {
            throw new IllegalStateException("logOnePlusX needed too many terms");
        }
        
        for (i = l; i >= 0; i--) {
            r += terms[i];
        }

        return r;
    } // end logOnePlusX
    
    /**
     * Determines the factor of a number for a given order.
     * 
     * @param x the number to stump
     * @param n the order
     * @return the stump of the number.
     */
    public static double stump(double x, int n) {
        if (x == 0.0) {
            return (n <= 0) ? 1.0 : 0.0;
        }

        int i, l;
        double r = 0.0, t = 1.0;

        if (Math.abs(x) < 0.1) {
            for (i = 0; i < n; i++, t *= x) {
                terms[i] = t * inverseFactorials[i];
            }

            for (l = n; l < MAX_TERMS; l++, t *= x) {
                if (Math.abs(terms[l] = t * inverseFactorials[l]) < 1.0e-16 * Math.abs(terms[n])) {
                    break;
                }
            }
            if (l >= MAX_TERMS) {
                throw new IllegalArgumentException("Stump index limit exceeded.");
            }

            for (i = l; i >= n; i--) {
                r += terms[i];
            }
            
            return r;
        } // end if

        for (i = 0; i < n; i++, t *= x) {
            terms[i] = inverseFactorials[i] * t;
        }

        for (i = n - 1; i >= 0; i--) {
            r -= terms[i];
        }

        return r + Math.exp(x);
    } // end stump
    
    /**
     * Returns a delta floating point number that represents a whole number 
     * division of the specified number.
     * 
     * @param d the number to find the delta for.
     * @return a small delta (range) that represents a while number division of
     * the specified number.
     */
    public static final double findDelta(double d) {
        double z = Math.pow(10.0, Math.ceil(log10(d)));
		double d_div_7 = d / 7.0;
        
		while (d_div_7 < z) {
            z *= 0.5;
			if (d_div_7 > z) {
                return z;
            }
            z *= 0.2;
        } // end for
        
		double d_div_15 = d / 15.0;
		while (d_div_15 > z) {
            z *= 2.0;
        }
        
        return z;
    } // end findDelta
	
    /**
     * Trims the double to the specified number of digits in the number format.
     * 
     * @param x the number to trim the string representation.
     * @param numDigits number of digits to limits the string representation to.
     * @return the string representation of the double limited to the specified
     * number of digits in the number format.
     */
    public static String trimDouble(double x, int numDigits) {
        if (defaultNumDigits != numDigits) {
            MNF.setMaximumFractionDigits(defaultNumDigits = numDigits);
            MNF.setMinimumFractionDigits(0);
            MNF.setGroupingUsed(false);
        } // end if

        return MNF.format(x);
    } // end trimDouble

    /**
     * Returns the equivalent rational string of a number.
     * 
     * @param x the number.
     * @param lim the limit.
     * @return the equivalent rational string, or null if one does not exist.
     */
    public static String getEquivalentRationalString(double x, int lim) {
        long startTime = MdeSettings.DEBUG ? System.currentTimeMillis() : 0;
        
        RationalExpression r = new RationalExpression.ContinuedFraction(x, 20).iterate();
        
        if (MdeSettings.DEBUG) {
            long endTime = System.currentTimeMillis();
            System.out.println("Call to \"RationalExpression.ContinuedFraction.iterate\" on x = " +
                    x + " took " + (endTime - startTime) + " milliseconds");
        } // end if

        if (r == null) {
            return null;
        }

        double d = r.getDenominator().toExpression().theValue.doubleValue();
        double n = r.getNumerator().toExpression().theValue.doubleValue();

        if (Math.abs(n) > lim || Math.abs(d) > lim) {
            return null;
        }

        if (d == 1.0) {
            return "" + (int)n;
        }
        
        return (int)n + "/" + (int)d;
    } // end getEquivalentRationalString

    /**
     * Returns the quadratic representation of a number.
     * 
     * @param x the number.
     * @param lim the limit.
     * @return the quadratic representation of the number.
     */
    public static String getQuadraticRepresentationString(double x, int lim) {
        Object s = getRationalObject(x, lim);

        if (s == null) {
            return null;
        }
        
        if (s instanceof String) {
            return (String)s;
        }

        double[] r = (double[])s;

        if (r.length != 3) {
            return null;
        }

        int d = GCD(r);

        for (int i = 0; i < r.length; i++) {
            r[i] /= d;
        }

        if (Math.abs(r[0]) > lim || Math.abs(r[1]) > lim || Math.abs(r[2]) > lim) {
            return null;
        }

        Roots.RootFactor rf = new Roots.RootFactor(r[2] / r[0], r[1] / r[0]);

        if (Math.abs(x - rf.rootValues[0]) < Math.abs(x - rf.rootValues[1])) {
            return prettyPrintQRoot((int)r[0], (int)r[1], (int)r[2], 0, lim);
        }
        return prettyPrintQRoot((int)r[0], (int)r[1], (int)r[2], 1, lim);
    } // end getQuadraticRepresentationString

    /**
     * Returns the rational equivalent of a number.
     * 
     * @param x the number.
     * @param lim the limit.
     * @return the rational equivalent of a number.
     */
    public static String getRationalEquivalent(double x, int lim) {
        String s = getEquivalentRationalString(x, lim);

        if (s == null) {
            return "approx. " + trimDouble(x, 4);
        }

        return s;
    } // end getRationalEquivalent

    
    /**
     * A printable version of the Quadratic root.
     * 
     * @param a the <code>a</code> value of the quadratic.
     * @param b the <code>b</code> value of the quadratic.
     * @param c the <code>c</code> value of the quadratic.
     * @param whichRoot either the <code>0</code> or <code>1</code> root.
     * @param lim the limit.
     * @return printable version of the quadratic root, or null if one does not 
     * exist.
     */
    public static String prettyPrintQRoot(double a, double b, double c, int whichRoot, int lim) {
        String[] sign = { " - ", " + " };

        if (a == 0.0) {
            return getEquivalentRationalString(-b / c, lim);
        }

        if (b == 0.0) {
            switch (whichRoot) {
            case 1:
                return "sqrt(" + getEquivalentRationalString(-c / a, lim) + ")";
            case 0:
                return "-sqrt(" + getEquivalentRationalString(-c / a, lim) + ")";
            default:
                throw new IllegalArgumentException("whichRoot must bbe 0 or 1");
            } // end switch
        }
        
        double h = -0.5 * b / a;
        double d = h * h - c / a;
        String hString = getEquivalentRationalString(h, lim);
        String dString = getEquivalentRationalString(d, lim);

        if (hString == null || dString == null) {
            return null;
        }
        
        return hString + sign[whichRoot] + "sqrt(" + dString + ")";
    } // end prettyPrintQRoot

    /**
     * Returns the greatest common divisor.
     * 
     * @param c the first integer.
     * @param d the second integer.
     * @return the greatest common divisor of the two integer numbers.
     */
    public static int GCD(int c, int d) {
        int c1 = Math.abs(c);
        int d1 = Math.abs(d);
        int big = Math.max(c1, d1);
        int little = Math.min(c1, d1);

        if (little == 0) {
            return big;
        }

        return getGCD(big, little);
    } // end GCD

    /**
     * Returns the greatest common divisor of an array of numbers.
     * 
     * @param r array of numbers.
     * @return the the greatest common divisor of an array of numbers.
     */
    public static int GCD(double[] r) {
        int i, n = r.length, z;

        for (i = 0; i < n; i++) {
            if (Math.abs(r[i]) > Integer.MAX_VALUE) {
                return 1;
            }
        }

        switch (n) {
        case 1:
            return (int)r[0];

        case 0:
            throw new IllegalArgumentException("Can't find GCD of empty array.");

        default:
            z = (int)r[0];
            for (i = 1; i < n; i++) {
                z = GCD(z, (int)r[i]);
            }

            return z;
        } // end switch
    } // end GCD

    private static int getGCD(int big, int little) {
        int r = big % little;

        if (r == 0) {
            return little;
        }

        return getGCD(little, r);
    } // end getGCD
    
//    // A utility method to translate between ragged arrays and MultiPoints --
//    // probably should eventually be private to discourage use of ragged arrays
//    static MultiPointXY[] raggedToMulti(double[][] r) {
//        int d, i, j, n = r.length;
//        MultiPointXY[] m = new MultiPointXY[n];
//
//        for (i = 0; i < n; i++) {
//            m[i] = new MultiPointXY(r[i][0]);
//            m[i].yArray = new double[d = (r[i].length - 1)];
//
//            for (j = 0; j < d; j++) {
//                m[i].yArray[j] = r[i][j + 1];
//            }
//        } // end for i
//
//        return m;
//    } // end raggedToMulti

    /**
     * Utility to translate from MultiPoints to a ragged array.
     * 
     * @param m the multipoint array.
     * @return the ragged array.
     */
    static double[][] multiToRagged(MultiPointXY[] m) {
        int d, i, j, n = m.length;
        double[][] r = new double[n][1];

        for (i = 0; i < n; i++) {
            if (m[i] != null) {
                if ((d = 1 + m[i].yArray.length) > 1) {
                    r[i] = new double[d];
                }

                r[i][0] = m[i].x;

                for (j = 1; j < d; j++) {
                    r[i][j] = m[i].yArray[j - 1];
                }
            }
        } // end for i

        return r;
    } // end multiToRagged

    private static Object getRationalObject(double x, int lim) {
        if (x == 0)
            return "0";

        double[] r = new RationalExpression.ContinuedFraction(x, 20).getPolynomialCoefficients();

        if (r.length != 2) {
            return r;
        }

        if (Math.abs(r[0]) > lim || Math.abs(r[1]) > lim) {
            return null;
        }

        r[1] = -r[1];

        if (r[0] < 0) {
            r[0] = -r[0];
            r[1] = -r[1];
        } // end if

        if (r[0] == 1.0) {
            return "" + (int)r[1];
        }

        return "" + (int)r[1] + "/" + (int)r[0];
    } // end getRationalObject
    
//    public static void main(String[] args) {
//        gov.nasa.ial.mde.solver.symbolic.Expression e = 
//            new gov.nasa.ial.mde.solver.symbolic.Expression(MathUtil.combineArgs(args));
//
//        if (e.theValue == null)
//            return;
//
//        String s = MathUtil.getQuadraticRepresentationString(e.theValue.doubleValue(), 1000);
//
//        System.out.println("The value = " + s);
//    } // end main

}
