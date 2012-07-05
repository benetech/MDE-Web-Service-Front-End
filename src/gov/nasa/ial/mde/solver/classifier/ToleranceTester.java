package gov.nasa.ial.mde.solver.classifier;

public class ToleranceTester {
	

    /**
     * Check whether a value is within a tolerance range around a value.
     * This can be used to check whether a computed value is essentially another value.  For example, to fix rounding error.
     * @param test_value the value to test
     * @param tolerance the tolerance.  Multiplied by 1.0e-6.
     * @return whether the test_value is within the tolerance region.
     * @see #isWithinToleranceOfZero(double)
     */
    public static boolean isWithinTolerance(double test_value, double tolerance) {
        return (Math.abs(test_value) <= 1.0e-6 * tolerance);
    }

    /**
     * Check whether a value is within a tolerance of zero.
     * @param test_value the value to test.  Tolerance is within 1.0e-6 around zero.
     * @return whether the test_value is within the tolerance region.
     * @see #isWithinTolerance(double, double)
     */
    public static boolean isWithinToleranceOfZero(double test_value) {
        return isWithinTolerance(test_value, 0.0);
    }
}
