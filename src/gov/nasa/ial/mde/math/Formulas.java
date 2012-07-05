/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Apr 9, 2004
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.math.Roots.RootFactor;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class for representing <code>Formulas</code>.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Formulas {
    
    private final static boolean CHECK_RESULTS = true;

    private final static double  ONE_THIRD = 1.0 / 3.0;

    private final static double  EPSILON = 1.0e-10;

    private final static double  DOUBLE_ROOT_TOLERANCE = 1.0e-8;

    private double      error = 0.0;

    private double[]    reals;

    private double[]    coefficients;

    private Roots.RootFactor[]  roots;

    /**
     * Constructs a formula for the given Polynomial.
     * 
     * @param p a Polynomial.
     */
    public Formulas(PNom p) {
        switch (p.getDegree()) {
        case 0:
            throw new IllegalArgumentException("Polynomial is trivial.");

        case 1:
            roots = new Roots.RootFactor[1];
            coefficients = p.getCoefficients();
            roots[0] = new Roots.RootFactor(coefficients[1] / coefficients[0]);
            break;

        case 2:
            coefficients = p.getCoefficients();
            coefficients[1] = coefficients[1] / coefficients[0];
            coefficients[2] = coefficients[2] / coefficients[0];
            coefficients[0] = 1.0;
            roots = new Roots.RootFactor[1];
            roots[0] = new Roots.RootFactor(coefficients[2], coefficients[1]);
            break;

        case 3:
            doCubic(p);
            break;

        case 4:
            doQuartic(p);
            break;

        default:
            throw new IllegalArgumentException("Formulas only work on equations of degree less than 5");
        } // end switch

        collectRoots();

        if (CHECK_RESULTS) {
            PNom u = new PNom(1.0);

            for (int i = 0; i < roots.length; i++) {
                u = u.product(Formulas.getPNom(roots[i]));
            }

            double[] newC = u.getCoefficients();
            for (int i = 0; i < newC.length; i++) {
                error += Math.abs(newC[i] - coefficients[i]);
            }
        } // end if
    } // end Formulas

    /**
     * Returns the roots of the formula.
     * 
     * @return the roots of the formula.
     */
    public Roots.RootFactor[] getRoots() {
        return roots;
    } // end getRoots

    /**
     * Returns the Reals of the formula.
     * 
     * @return the Reals of the formula.
     */
    public double[] getReals() {
        return reals;
    } // end getReals

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer b = new StringBuffer("Factors:");

        for (int i = 0; i < roots.length; i++) {
            b.append("\n" + roots[i]);
        }
        if (CHECK_RESULTS) {
            b.append("\nError = " + error);
        }

        return b.toString();
    } // end toString

    /**
     * Returns the cube-root of x.
     * 
     * @param x the value to calculate the cube-root of.
     * @return the cube-root of x.
     */
    private static double cubeRoot(double x) {
        return (x >= 0.0) ? Math.pow(x, Formulas.ONE_THIRD) : -Math.pow(-x, Formulas.ONE_THIRD);
    } // end cubeRoot

    private void doCubic(PNom p) {
        coefficients = p.getCoefficients();
        for (int i = 1; i <= 3; i++) {
            coefficients[i] /= coefficients[0];
        }
        coefficients[0] = 1.0;

        double h = Formulas.ONE_THIRD * coefficients[1];
        double a = coefficients[2] + 3.0 * h * h - 2.0 * coefficients[1] * h;
        double b = h * h * h - h * h * coefficients[1] + h * coefficients[2] - coefficients[3];
        double c1 = Formulas.ONE_THIRD * a;
        double c = -c1 * c1 * c1;
        double b2 = b * b;
        double c2 = 4.0 * c;
        double d2 = b2 - c2;
        double d3 = b2 + Math.abs(c2);

        if (d3 <= EPSILON) {
            roots = new Roots.RootFactor[1];
            roots[0] = new Roots.RootFactor(h);
            roots[0].multiplicity = 3;
            return;
        } // end if

        if (Math.abs(d2) <= EPSILON * d3) {
            double r = Formulas.cubeRoot(4.0 * b);

            roots = new Roots.RootFactor[2];
            roots[0] = new Roots.RootFactor(h - r);
            roots[1] = new Roots.RootFactor(h + 0.5 * r);
            roots[1].multiplicity = 2;
            return;
        } // end if

        if (d2 > 0) {
            double d = Math.sqrt(d2);
            double t3 = 0.5 * (d - b);
            double s3 = 0.5 * (d + b);
            double r = Formulas.cubeRoot(s3) - Formulas.cubeRoot(t3) - h;
            double[] fc1 = { 1.0, -r };
            PNom[] q = p.quotient(new PNom(fc1));
            double[] factorCoeffs = q[0].getCoefficients();

            roots = new Roots.RootFactor[2];
            roots[0] = new Roots.RootFactor(-r);
            roots[1] = new Roots.RootFactor(factorCoeffs[2] / factorCoeffs[0], factorCoeffs[1]
                    / factorCoeffs[0]);
        } // end if
        else {
            double k = 2.0 * Math.sqrt(-c1);
            double theta = Formulas.ONE_THIRD * Math.acos(4.0 * b / (k * k * k));

            roots = new Roots.RootFactor[3];
            for (int i = 0; i < 3; i++) {
                roots[i] = new Roots.RootFactor(h - k
                        * Math.cos(theta + Formulas.ONE_THIRD * i * 2.0 * Math.PI));
            }
        } // end else
    } // end doCubic

    private void doQuartic(PNom p) {
        coefficients = p.getCoefficients();
        for (int i = 1; i <= 4; i++) {
            coefficients[i] /= coefficients[0];
        }
        coefficients[0] = 1.0;

        double h = 0.25 * coefficients[1];
        double h2 = h * h;
        double h3 = h * h2;
        double h4 = h2 * h2;
        double e = coefficients[2] + 6.0 * h2 - 3.0 * coefficients[1] * h;
        double f = coefficients[3] - 4.0 * h3 + 3.0 * coefficients[1] * h2 - 2.0 * coefficients[2] * h;
        double g = coefficients[4] + h4 - coefficients[1] * h3 + coefficients[2] * h2 - coefficients[3] * h;

        if (Math.abs(g) <= EPSILON) {
            double[] c = { 1.0, 0.0, e, f };
            Formulas newF = new Formulas(new PNom(c));

            roots = new Roots.RootFactor[1 + newF.roots.length];
            for (int i = 0; i < newF.roots.length; i++) {
                roots[i] = Formulas.translate(newF.roots[i], -h);
            }

            roots[newF.roots.length] = new Roots.RootFactor(h);
            return;
        } // end if

        double[] c = { 1.0, 2.0 * e, e * e - 4.0 * g, -f * f };

        if (Math.abs(c[3]) <= EPSILON * EPSILON) {
            if (Math.abs(c[2]) <= EPSILON) {
                roots = new Roots.RootFactor[1];
                roots[0] = Formulas.translate(new Roots.RootFactor(0.5 * e, 0.0), -h);
                roots[0].multiplicity = 2;
                return;
            } // end if

            if (c[2] >= 0.0) {
                Roots.RootFactor[] rf = {
                        Formulas.translate(new Roots.RootFactor(0.5 * (e - Math.sqrt(c[2])), 0.0), -h),
                        Formulas.translate(new Roots.RootFactor(0.5 * (e + Math.sqrt(c[2])), 0.0), -h) };
                roots = rf;
                return;
            } // end if
        } // end outer if

        Formulas newF = new Formulas(new PNom(c));
        double k2 = newF.reals[newF.reals.length - 1];

        if (k2 < 0) {
            throw new IllegalStateException("k2 must be positive");
        }

        double k = Math.sqrt(k2);
        double j = 0.5 * (e + k2 - f / k);
        Roots.RootFactor r1 = new Roots.RootFactor(j, k), r2 = new Roots.RootFactor(g / j, -k);
        //PNom temp=Formulas.getPNom(r1).product(Formulas.getPNom(r2));
        Roots.RootFactor[] rf = { Formulas.translate(r1, -h), Formulas.translate(r2, -h) };
        roots = rf;
    } // end doQuartic

    private static Roots.RootFactor translate(Roots.RootFactor r, double h) {
        Roots.RootFactor r1;

        if (r.isReal) {
            if (r.degree == 1) {
                r1 = new Roots.RootFactor(-h - r.rootValues[0]);
            } else {
                r1 = new Roots.RootFactor((h + r.rootValues[0]) * (h + r.rootValues[1]), r.coefficients[1]
                        - 2.0 * h);
            }
            r1.multiplicity = r.multiplicity;
            return r1;
        } // end if

        double re = r.rootValues[0] + h, im = r.rootValues[1];

        r1 = new Roots.RootFactor(re * re + im * im, r.coefficients[1] - 2.0 * h);
        r1.multiplicity = r.multiplicity;
        return r1;
    } // endTranslate

    private static PNom getPNom(Roots.RootFactor r) {
        double[] c = null;

        if (r.degree == 1) {
            c = new double[2];
            c[0] = 1.0;
            c[1] = r.coefficients[0];
        } // end if
        else {
            c = new double[3];
            c[0] = 1.0;
            c[1] = r.coefficients[1];
            c[2] = r.coefficients[0];
        } // end else

        PNom p = new PNom(c);

        if (r.multiplicity == 1) {
            return p;
        }

        PNom f = new PNom(c);

        for (int i = 1; i < r.multiplicity; i++) {
            p = p.product(f);
        }

        return p;
    } // end getPNom

    private void collectRoots() {
        int i, n = roots.length;
        ArrayList<RootFactor> realFactors = new ArrayList<RootFactor>();
        ArrayList<RootFactor> complexFactors = new ArrayList<RootFactor>();

        for (i = 0; i < n; i++) {
            Roots.RootFactor[] r = getRealFactors(roots[i]);
            int j, numRoots = r.length;

            if (numRoots > 0) {
                for (j = 0; j < numRoots; j++) {
                    realFactors.add(r[j]);
                }
            } else {
                complexFactors.add(roots[i]);
            }
        } // end for i

        reals = new double[n = realFactors.size()];

        for (i = 0; i < n; i++) {
            reals[i] = realFactors.get(i).rootValues[0];
        }
        
        Arrays.sort(reals);
        realFactors.clear();

        if (n > 0) {
            double c = 0.0;
            int mult;
            int last = 0;

            for (i = 1; i < n; i++) {
                if (reals[i] > reals[i - 1] + DOUBLE_ROOT_TOLERANCE) {
                    mult = i - last;

                    for (int j = last; j < i; j++) {
                        c += reals[j];
                    }
                    c /= mult;

                    Roots.RootFactor newRoot = new Roots.RootFactor(-c);
                    newRoot.multiplicity = mult;

                    realFactors.add(newRoot);
                    last = i;
                    c = 0.0;
                } // end if
            } // end for i

            mult = i - last;

            for (int j = last; j < i; j++) {
                c += reals[j];
            }
            c /= mult;

            Roots.RootFactor newRoot = new Roots.RootFactor(-c);
            newRoot.multiplicity = mult;

            realFactors.add(newRoot);
        } // end if

        Roots.RootFactor[] cf = complexFactors.toArray(
                                    new Roots.RootFactor[complexFactors.size()]);

        if (cf.length == 2) {
            double dx = cf[0].rootValues[0] - cf[1].rootValues[0];
            double dy = cf[0].rootValues[1] - cf[1].rootValues[1];

            if (Math.sqrt(dx * dx + dy * dy) < DOUBLE_ROOT_TOLERANCE) {
                double r1 = 0.5 * (cf[0].rootValues[0] + cf[1].rootValues[0]);
                double r2 = 0.5 * (cf[0].rootValues[1] + cf[1].rootValues[1]);
                Roots.RootFactor newRoot = new Roots.RootFactor(r1 * r1 + r2 * r2, -2.0 * r1);

                newRoot.multiplicity = cf[0].multiplicity + cf[1].multiplicity;
                complexFactors.clear();
                complexFactors.add(newRoot);
            } // end if
        } // end if
        roots = new Roots.RootFactor[n = (i = realFactors.size()) + complexFactors.size()];

        for (int j = 0; j < i; j++) {
            roots[j] = realFactors.get(j);
        }
        for (int j = i; j < n; j++) {
            roots[j] = complexFactors.get(j - i);
        }
    } // end collectRoots

    private static Roots.RootFactor[] getRealFactors(Roots.RootFactor r) {
        if (r.isReal) {
            int i, j, k, n = r.degree * r.multiplicity;
            Roots.RootFactor[] rv = new Roots.RootFactor[n];

            for (i = k = 0; i < r.degree; i++) {
                for (j = 0; j < r.multiplicity; j++) {
                    rv[k++] = new Roots.RootFactor(-r.rootValues[i]);
                }
            }
            return rv;
        } // end if

        if (Math.abs(r.rootValues[1]) < DOUBLE_ROOT_TOLERANCE) {
            int i, n = (r.multiplicity << 1);
            Roots.RootFactor[] rv = new Roots.RootFactor[n];

            for (i = 0; i < n; i++) {
                rv[i] = new Roots.RootFactor(-r.rootValues[0]);
            }
            return rv;
        } // end if

        return new Roots.RootFactor[0];
    } // end getRealFactorss


//     public static void main(String[] args) {
//         double[] c = {2.0, -30.0, 162.0, -350.0};
//         System.out.println(new Formulas(new PNom(c)));
//         if (args.length == 1) {
//             System.out.println(args[0]);
//        
//             double[] c1 = {1.0, Math.PI};
//             double[] c2 = {1.0, -Math.E};
//             PNom p1 = new PNom(c1);
//             PNom p2 = new PNom(c2);
//        
//             System.out.println(new Formulas(p1.product(p2).product(p1).product(
//             p2)));
//        
//             System.out.println(new Formulas(PNom.getPNom(
//             new gov.nasa.ial.mde.solver.symbolic.Polynomial(
//             new gov.nasa.ial.mde.solver.symbolic.Expression(args[0])), "x")));
//         } // end if
//     }

}
