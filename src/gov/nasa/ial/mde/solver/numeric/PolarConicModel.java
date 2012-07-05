/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.numeric;

import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier.QuadraticType;

/**
 * The polar equation of a conic section is given by 
 * <code>r = a/(1 - e*cos(\theta-\phi))</code> where <code>a</code> is the 
 * semi-major axis, <code>e</code> is the eccentricity, and <code>\phi</code> 
 * is the inclination of the semi-major axis. The origin will always be a focus 
 * of a conic given in polar form. Orbits in the gravitational field of a single 
 * massive primary (located at the origin) are described by polar conic sections. 
 * The curve will be an ellipse (parabola) (hyperbola) in case e is strictly 
 * between 0 and 1 (equal to 1) (greater than 1) respectively.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class PolarConicModel extends PolarModel {
    
    /** The eccentricity. */
    public double E;

    /** One of QuadraticClassifier.PARABOLA|ELLIPSE|HYPERBOLA */
    public QuadraticType conicIdentity;

    private int[][] conic = { { 0, 2, 4, 5 }, { 0, 1 } };

    /**
     * Constructs a polor conic model for the given polar model builder.
     * 
     * @param p the polar model builder.
     */
    public PolarConicModel(PolarModelBuilder p) {
        evaluate(p, conic);
        name = "conic";
        identity = PolarClassifier.POLAR_CONIC;

        if (whichSignature == 0) {
            double e = PolarModel.amplitude(modelVector[2], modelVector[3]);
            double a = Math.abs(modelVector[0]);

            E = e / a;

            if (Math.abs(E - 1.0) < 1.0e-6) {
                modelVector[0] /= E;
                E = 1.0;
                conicIdentity = QuadraticType.Parabola;
            } // end if
            else if (E < 1.0)
                conicIdentity = QuadraticType.Ellipse;
            else
                conicIdentity = QuadraticType.Hyperbola;
        } // end nominal case
        else // circular case
            conicIdentity = QuadraticType.Ellipse;
    } // end PolarConicModel

    /**
     * Returns a Cartesian equation of the Polar Conic.
     * 
     * @return Cartesian equation of the Polar Conic.
     */
    public String getCartesianEquation() {
        if (whichSignature == 0) {
            return "(" + modelVector[0] + ")^2*(x^2+y^2) = ((" + modelVector[1] + ")+(" + modelVector[2] + ")*x+(" + modelVector[3] + ")*y)^2";
        }
        return modelVector[0] + " = " + -modelVector[1] + "*(x^2+y^2)";
    } // end getCartesianEquation
    
} // end class PolarConicModel
