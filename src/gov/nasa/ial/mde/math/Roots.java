/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The <code>Roots</code> represent of a polynomial.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Roots {
    
    private final static int MAXITER = 500;

    private static void deflateByQuad(double[] a, int n, double[] b, double[] quad, double[] err) {
        double r, s;
        int i;

        r = quad[1];
        s = quad[0];

        b[0] = 1.0;
        b[1] = a[1] - r;

        for (i = 2; i <= n; i++) {
            b[i] = a[i] - r * b[i - 1] - s * b[i - 2];
        }

        err[0] = Math.abs(b[n]) + Math.abs(b[n - 1]);
    } // end deflateByQuad

    private static void deflateByLinear(double[] a, int n, double[] b, double[] root, double[] err) {
        double r = root[0];

        b[0] = 1.0;
        for (int i = 1; i <= n; i++) {
            b[i] = a[i] - r * b[i - 1];
        }

        err[0] = Math.abs(b[n]);
    } // end deflateByLinear

    //
    // Find quadratic factor using Bairstow's method (quadratic Newton method).
    // A number of ad hoc safeguards are incorporated to prevent stalls due
    // to common difficulties, such as zero slope at iteration point, and
    // convergence problems.
    //
    private static void find_quad(double[] a, int n, double[] b, double[] quad, double[] err, int[] iter) {
        if (n < 3) {
            if (n == 2) {
                err[0] = 0.0;
                iter[0] = 0;
                quad[1] = a[1];
                quad[0] = a[2];
                return;
            } // end if

            throw new IllegalArgumentException("find_quad needs degree of at least 2");
        } // end if

        double[] c;
        double dn, dr, ds, drn, dsn, eps, r, s;
        int i;

        c = new double[n + 1];
        b[0] = c[0] = 1.0;
        r = quad[1];
        s = quad[0];
        dr = 1.0;
        ds = 0;
        eps = 1e-15;
        iter[0] = 1;

        while ((Math.abs(dr) + Math.abs(ds)) > eps) {
            if (iter[0] > MAXITER) {
                break;
            }
            if (((iter[0]) % 200) == 0) {
                eps *= 10.0;
            }
            b[1] = a[1] - r;
            c[1] = b[1] - r;

            for (i = 2; i <= n; i++) {
                b[i] = a[i] - r * b[i - 1] - s * b[i - 2];
                c[i] = b[i] - r * c[i - 1] - s * c[i - 2];
            }
            dn = c[n - 1] * c[n - 3] - c[n - 2] * c[n - 2];
            drn = b[n] * c[n - 3] - b[n - 1] * c[n - 2];
            dsn = b[n - 1] * c[n - 1] - b[n] * c[n - 2];

            if (Math.abs(dn) < 1e-10) {
                if (dn < 0.0) {
                    dn = -1e-8;
                } else {
                    dn = 1e-8;
                }
            }
            dr = drn / dn;
            ds = dsn / dn;
            r += dr;
            s += ds;
            iter[0]++;
        }
        quad[0] = s;
        quad[1] = r;
        err[0] = Math.abs(ds) + Math.abs(dr);
    }

    private static void find_linear(double[] a, int n, double[] root, double[] err, int[] iter) {
        double[] b = new double[n + 1], c = new double[n + 1];
        double dr = 1.0, r, s;
        double eps = 1.0e-15;

        iter[0] = 0;
        b[0] = c[0] = 1.0;
        while (Math.abs(dr) > eps) {
            for (int i = 1; i <= n; i++) {
                b[i] = a[i] - root[0] * b[i - 1];
                c[i] = b[i] - root[0] * c[i - 1];
            } // end for i

            if (Math.abs(err[0] = r = b[n]) < 1.0e-16) {
                break;
            }

            iter[0]++;
            s = c[n - 1];

            if (s == 0.0) {
                root[0] -= 1.0;
                continue;
            } // end if

            root[0] += (dr = r / s);
            if (iter[0] > MAXITER) {
                break;
            }
        } // end while
    } // end find_linear

    //
    // Differentiate polynomial 'a' returning result in 'b'.
    //
    private static void diff_poly(double[] a, int n, double[] b) {
        double coef;
        int i;

        coef = n;
        b[0] = 1.0;
        for (i = 1; i < n; i++) {
            b[i] = (a[i] * (n - i)) / coef;
        }
    }

    private static int quadraticMultiplicity(double[] a, int n, double[] q, double[] err, int[] iter) {
        int m = n - 1, rv = 1;

        if (n < 4) {
            return 1;
        }

        double[] d = new double[n]; // degree = m = n-1
        double tst; // root error to see if converge to same root
        double[] rs = { q[0], q[1] };
        double[] ws = new double[n]; // workspace for find_quad

        while (m > 1) {
            diff_poly(a, n, d);
            while (Math.abs(d[m]) < 1.0e-15) {
                m--;
            }

            if (m > 2) {
                iter[0] = 0;
                find_quad(d, m, ws, rs, err, iter);
            } // end if
            else {
                rs[0] = 0.0;

                switch (m) {
                case 2:
                    rs[0] = d[2];

                case 1:
                    rs[1] = d[1];
                    break;

                default:
                    rs[0] = rs[1] = 0.0;
                } // end switch
            } // end else

            tst = Math.abs(q[0] - rs[0]) + Math.abs(q[1] - rs[1]);

            if (tst > 1.0e-2) {
                return rv;
            }

            q[0] = rs[0];
            q[1] = rs[1];
            rv++;
            n = m;
            m = n - 1;
            a = d;
            d = new double[n];
        } // end while

        return rv;
    } // end quadraticMultiplicity

    private static int linearMultiplicity(double[] a, int n, double[] r, double[] err, int[] iter) {
        int m = n - 1, rv = 1;
        double[] d = new double[n]; // degree = m = n-1
        double tst; // root error to see if converge to same root
        double[] rs = { r[0] };

        while (true) {
            diff_poly(a, n, d);
            while (Math.abs(d[m]) < 1.0e-15) {
                m--;
            }

            switch (m) {
            case 1:
                rs[0] = d[1];
                break;

            case 0:
                return rv;

            default:
                iter[0] = 0;
                find_linear(d, m, rs, err, iter);
            } // end switch

            tst = Math.abs(rs[0] - r[0]);

            if (tst > 1.0e-3) {
                return rv;
            }

            r[0] = rs[0];
            rv++;
            n = m;
            m = n - 1;
            a = d;
            d = new double[n];
        } // end while
    } // end linearMultiplicity

    private static RootFactor[] getFactors(double[] a, int n) {
        if (n == 0) {
            return new RootFactor[0];
        }

        double[] b, z;
        double[] err = { 0.0 };
        double tmp;
        int[] iter = { 0 };
        int i, j, m;
        ArrayList<RootFactor> rootList = new ArrayList<RootFactor>();

        if ((tmp = a[0]) != 1.0) {
            a[0] = 1.0;
            for (i = 1; i <= n; i++) {
                a[i] /= tmp;
            }
        }
        for (m = n; m >= 0; m--) {
            if (Math.abs(a[m]) > 1.0e-15) {
                break;
            }
        }
        b = new double[m + 1];
        z = new double[m + 1];
        for (i = 0; i <= m; i++) {
            z[i] = a[i];
        }

        do {
            if (m < 5) {
                double[] tempC = new double[m + 1];

                for (i = 0; i <= m; i++) {
                    tempC[i] = z[i];
                }

                Formulas tempF = new Formulas(new PNom(tempC));

                RootFactor[] tempRF = tempF.getRoots();

                for (i = 0; i < tempRF.length; i++) {
                    rootList.add(tempRF[i]);
                }

                break;
            } // end if

            double[] lerr = { 0.0 };
            double[] root = { Math.PI / 10.0 };
            double[] quad = { Math.PI * 0.1, -Math.E * 0.1 };
            int[] liter = { 0 };

            find_linear(z, m, root, lerr, liter);
            if (lerr[0] < 1.0e-12) {
                int mult = linearMultiplicity(z, m, root, lerr, liter);
                RootFactor rf = new RootFactor(root);

                rf.multiplicity = mult;
                rootList.add(rf);

                for (i = 0; i < mult; i++) {
                    deflateByLinear(z, m, b, root, lerr);
                    m--;
                    for (j = 0; j <= m; j++)
                        z[j] = b[j];
                } // end for i

                continue;
            } // end if

            find_quad(z, m, b, quad, err, iter);

            if (err[0] < 1.0e-4) {
                int mult = quadraticMultiplicity(z, m, quad, err, iter);
                RootFactor rf = new RootFactor(quad);

                rf.multiplicity = mult;
                rootList.add(rf);

                for (i = 0; i < mult; i++) {
                    deflateByQuad(z, m, b, quad, err);
                    m -= 2;
                    for (j = 0; j <= m; j++) {
                        z[j] = b[j];
                    }
                } // end for i
            } // end if
            else {
                System.err.println("No convergence.");
                return new RootFactor[0];
            }
        } while (m > 0);

        return rootList.toArray(new RootFactor[rootList.size()]);
    }

    private static RootFactor[] getRealRootFactors(RootFactor[] rf, ArrayList<RootFactor> rl) {
        int n, numFactors = rf.length;

        for (n = 0; n < numFactors; n++) {
            if (rf[n].degree == 1) {
                rl.add(rf[n]);
                continue;
            } // end if

            if (rf[n].isReal) {
                /* first handle a case that probably never happens */
                if (rf[n].rootValues[0] == rf[n].rootValues[1]) {
                    RootFactor solo = new RootFactor(rf[n].rootValues[0]);

                    solo.multiplicity = 2 * rf[n].multiplicity;
                    rl.add(solo);
                    continue;
                } // end if

                RootFactor[] pair = { new RootFactor(rf[n].rootValues[0]),
                        new RootFactor(rf[n].rootValues[1]) };

                pair[0].multiplicity = pair[1].multiplicity = rf[n].multiplicity;
                rl.add(pair[0]);
                rl.add(pair[1]);
            } // end if
        } // end for n

        Collections.sort(rl, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                RootFactor r1 = (RootFactor) o1, r2 = (RootFactor) o2;

                if (r1.rootValues[0] < r2.rootValues[0]) {
                    return -1;
                }

                if (r1.rootValues[0] > r2.rootValues[0]) {
                    return 1;
                }

                return 0;
            } // end compare
        } // end Comparator
        ); // end sort

        return rl.toArray(new RootFactor[rl.size()]);
    } // end getRealRootFactors

    /**
     * Returns the roots given the polynomial coefficients.
     * 
     * @param a the polynomial coefficients.
     * @return the roots.
     */
    public static RootFactor[] extractRoots(double[] a) {
        int n = a.length - 1;

        return getFactors(a, n);
    } // end extractRoots

    /**
     * Gets an array containing all real roots including duplicates, sorted
     * smallest to largest
     * 
     * @param coeffs
     *            the coefficients of the polynomial in increasing order of
     *            powers of x
     * @param deg
     *            the degree of the polynomial
     * @return a sorted array of doubles containing all real roots of the
     *         polynomial
     */
    public static double[] getRealRoots(double[] coeffs, int deg) {
        RootFactor[] rf = getRealRootsWithMultiplicities(coeffs, deg);
        int i, j, k, numDistinct = rf.length, numTotal = 0;

        for (i = 0; i < numDistinct; i++) {
            numTotal += rf[i].multiplicity;
        }

        double[] r = new double[numTotal];

        for (i = j = 0; i < numDistinct; i++) {
            for (k = 0; k < rf[i].multiplicity; k++) {
                r[j++] = rf[i].rootValues[0];
            }
        }

        return r;
    } // end getRealRoots

    /**
     * Returns the real roots with multiplicities.
     * 
     * @return an array of linear RootFactors sorted smallest to largest
     * @param a
     *            is the polynomial in the form a[0]x^n + ... + a[n]
     * @param deg
     *            is the degree
     */
    public static RootFactor[] getRealRootsWithMultiplicities(double[] a, int deg) {
        int i, n = deg;
        ArrayList<RootFactor> rl = new ArrayList<RootFactor>();

        for (i = n; i >= 0; i--) {
            if (a[i] != 0.0) {
                break;
            }
        }

        if (i < n) {
            RootFactor rf = new RootFactor(0.0);
            rf.multiplicity = n - i;
            rl.add(rf);
        } // end if

        return getRealRootFactors(getFactors(a, i), rl);
    } // end getRealRootsWithMultiplicities

    
    /**
     * The <code>RootFactor</code> represents the root factors.
     * 
     * @author Dr. Robert Shelton
     * @version 1.0
     * @since 1.0
     */
    public static class RootFactor {
        /** The multiplicity. */
        public int      multiplicity = 1;

        /** The degree. */
        public int      degree = 1;

        /** The coefficients. */
        public double[] coefficients;

        /** The root values. */
        public double[] rootValues;

        /** Flag indicating the factor is real. */
        public boolean  isReal = true;

        /**
         * Constructs a <code>RootFactor</code> for the specified constant value.
         * 
         * @param r a real constatnt value.
         */
        public RootFactor(double r) {
            degree = 1;
            coefficients = new double[1];
            rootValues = new double[1];
            coefficients[0] = r;
            rootValues[0] = -r;
        } // end RootFactor

        /**
         * Constructs a <code>RootFactor</code> for the two specified
         * coefficients of a polynomial.
         * 
         * @param c0 the first coefficient.
         * @param c1 the second coefficient.
         */
        public RootFactor(double c0, double c1) {
            coefficients = new double[degree = 2];
            rootValues = new double[2];
            coefficients[0] = c0;
            coefficients[1] = c1;

            double b2 = -0.5 * c1;
            double d2 = b2 * b2 - c0;

            if (d2 >= 0.0) {
                double d = Math.sqrt(d2);

                rootValues[0] = b2 - d;
                rootValues[1] = b2 + d;
            } // end if
            else {
                isReal = false;
                rootValues[0] = b2;
                rootValues[1] = Math.sqrt(-d2);
            } // end else
        } // end RootFactor

        /**
         * Constructs a <code>RootFactor</code> for the specified polynomial
         * coefficients.
         * 
         * @param r the real polynomial coefficients.
         */
        public RootFactor(double[] r) {
            switch (degree = r.length) {
            case 1:
                copyFrom(new RootFactor(r[0]));
                break;

            case 2:
                copyFrom(new RootFactor(r[0], r[1]));
                break;

            default:
                throw new IllegalArgumentException("Number of coefficients for RootFactor must be 1 or 2");
            } // end switch
        } // end RootFactor

        private void copyFrom(RootFactor r) {
            multiplicity = r.multiplicity;
            coefficients = r.coefficients;
            isReal = r.isReal;
            rootValues = r.rootValues;
            degree = r.degree;
        } // end copyFrom
        
        /**
         * A string representation of the root factor.
         * 
         * @return string representation of the root factor.
         */
        public String toString() {
            StringBuffer b = new StringBuffer("Multiplicity = " + multiplicity + "\n");

            switch (degree) {
            case 1:
                if (coefficients[0] < 0.0)
                    b.append("x " + coefficients[0]);
                else if (coefficients[0] > 0.0)
                    b.append("x +" + coefficients[0]);
                else
                    b.append("x");
                break;

            case 2:
                b.append("x^2 +(" + coefficients[1] + ")*x +(" + coefficients[0] + ")");
                break;

            default:
                throw new IllegalStateException("Bad value for degree = " + degree);
            } // end switch

            return b.toString();
        } // end toString
    } // end classRootFactor


//    public static void main(String[] args) {
//        int i, n = args.length;
//        double[] a = null;
//
//        if (n > 2) {
//            a = new double[n];
//
//            for (i = 0; i < n; i++)
//                try {
//                    a[i] = new Double(args[i]).doubleValue();
//                } // end try
//                catch (NumberFormatException nfe) {
//                    System.out.println(args[i] + " is not a legal double.");
//                    System.exit(1);
//                } // end catch
//        } // end if
//        else
//            try {
//                double cases[][] = {
//                        { 1.0, 0.0, 8.0, 0.0, 16.0 },
//                        { 1.0, -8.0, 20.0, 0.0, -80.0, 128.0, -64.0 },
//                        { 1.0, 0.0, -14.0, -8.0, 89.0, 56.0, -264.0, -160.0, 400.0 },
//                        { 128.0, 256.0, -376.0, -828.0, 270.0, 675.0 },
//                        { 2.0, -9.0, 12.0, -36.0, 144.0, -192.0, 192.0, -576.0, 768.0, -256.0 },
//                        { 4.0, -24.0, 37.0, -55.0, 352.0, -604.0, 112.0, -1024.0, 2560.0, 576.0, -2048.0,
//                                -768.0 },
//                        { 1.0, 14.0, 72.0, 127.0, -181.0, -870.0, -214.0, 2023.0, 978.0, -2114.0, -856.0,
//                                -180.0, 2200.0, -1000.0 } };
//                int c = new Integer(args[0]).intValue();
//
//                if (c < 1 || c > cases.length) {
//                    System.err.println("No case " + c + " available");
//                    System.exit(0);
//                } // end if
//
//                a = cases[c - 1];
//            } // end try
//            catch (NumberFormatException nfe) {
//                PNom p = PNom.getPNom(
//                    new gov.nasa.ial.mde.solver.symbolic.Polynomial(
//                        new gov.nasa.ial.mde.solver.symbolic.Expression(args[0])), "x");
//                a = p.getCoefficients();
//            } // end catch
//
//        RootFactor[] rf = getRealRootsWithMultiplicities(a, a.length - 1);
//        // RootFactor[] rf=extractRoots(a);
//
//        for (i = 0; i < rf.length; i++)
//            System.out.println(rf[i].toString());
//    } // end main
    
} // end class Roots
