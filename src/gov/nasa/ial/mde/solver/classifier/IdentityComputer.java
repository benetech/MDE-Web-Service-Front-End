package gov.nasa.ial.mde.solver.classifier;

import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier.QuadraticType;

public class IdentityComputer {

    public static QuadraticType computeIdentity(double a, double b, double c, double d, double e) {
    	QuadraticType type = null;
    	
    	boolean has_a_coefficient = !ToleranceTester.isWithinToleranceOfZero(a);
    	boolean has_b_coefficient = !ToleranceTester.isWithinToleranceOfZero(b);
    	boolean has_c_coefficient = !ToleranceTester.isWithinToleranceOfZero(c);
    	boolean has_d_coefficient = !ToleranceTester.isWithinToleranceOfZero(d);
    	boolean has_e_coefficient = !ToleranceTester.isWithinToleranceOfZero(e);
    	
    	// Note! Order matters in this large conditional.
    	if(BooleanTester.areAllFalse(has_a_coefficient, has_b_coefficient, has_c_coefficient, has_d_coefficient)) {
    		type = computeTrivialIdentity(has_e_coefficient);
    	} else if(BooleanTester.areAllFalse(has_a_coefficient, has_b_coefficient)) {
    		type = computeLinearIdentity(has_c_coefficient, has_d_coefficient);
    	} else if(BooleanTester.areAllFalse(has_b_coefficient, has_d_coefficient)) {
    		type = computeVerticalLineIdentity(a, c, e);
    	} else if(BooleanTester.areAllFalse(has_a_coefficient, has_c_coefficient)) {
    		type = computeHorizontalLineIdentity(b, d, e);
    	} else if(BooleanTester.areAnyFalse(has_a_coefficient, has_b_coefficient)) {
    		type = QuadraticType.Parabola;
    	} else if(a*b < 0.0) {
    		type = computeHyperbola(has_e_coefficient);
    	} else {
    		type = computeEllipseIdentity(has_e_coefficient, a, e);
    	}
    	
    	return type;
    }
    
    public static QuadraticType computeEllipseIdentity(boolean has_e_coefficient, double a, double e) {
    	QuadraticType type = null;

        if (has_e_coefficient)
            type = QuadraticType.SinglePoint;
        else if (a * e > 0.0)
            type = QuadraticType.NullSet;
        else
            type = QuadraticType.Ellipse;
        
		return type;
	}

	public static QuadraticType computeHyperbola(boolean has_e_coefficient) {
    	QuadraticType type = null;
    	
    	if(has_e_coefficient) {
    		type = QuadraticType.Hyperbola;
    	} else {
    		type = QuadraticType.Cross;
    	}
    	
    	return type;
	}

	public static QuadraticType computeHorizontalLineIdentity(double b, double d, double e) {
    	QuadraticType type = null;
    	
        double discriminant = computeDiscriminant(b, d, e);

        if (ToleranceTester.isWithinToleranceOfZero(discriminant)) {
            type = QuadraticType.HorizontalLine;
        } else if (discriminant > 0.0) {
            type = QuadraticType.TwoHorizontalLines;
        } else {
        	type = QuadraticType.NullSet;
        }
        
        return type;
	}

	public static QuadraticType computeVerticalLineIdentity(double a, double c, double e) {
        QuadraticType type = null;
        
    	double discriminant = computeDiscriminant(a, c, e);

        if(ToleranceTester.isWithinToleranceOfZero(discriminant)) {
        	type = QuadraticType.VerticalLine;
        } else if(discriminant > 0.0) {
        	type = QuadraticType.TwoVerticalLines;
        } else {
        	type = QuadraticType.NullSet;
        }

        return type;
	}

	public static QuadraticType computeLinearIdentity(boolean has_c_coefficient, boolean has_d_coefficient) {
    	QuadraticType type = null;
    	
    	if(!has_c_coefficient) {
    		type = QuadraticType.HorizontalLine;
    	} else if(!has_d_coefficient) {
    		type = QuadraticType.VerticalLine;
    	} else {
    		type = QuadraticType.SlopingLine;
    	}

    	return type;
	}

	public static QuadraticType computeTrivialIdentity(boolean has_e_coefficient) {
		QuadraticType type = has_e_coefficient ? QuadraticType.NullSet : QuadraticType.AllPoints;
		return type;
	}
    
    /**
     * Compute the coefficient.  b^2 - 4ac, from ax^2 + bx + c.
     * @param square_coefficient the value of a in the formula.
     * @param linear_coefficient the value of b in the formula.
     * @param constant the value of c in the formula.
     * @return double the calculated discriminant.  b^2-4ac.
     */
    public static double computeDiscriminant(double square_coefficient, double linear_coefficient, double constant) {
    	return square_coefficient * square_coefficient - 4.0 * linear_coefficient * constant;
    }
}
