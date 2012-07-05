/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.math.Bounds;
import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.math.Roots;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.GraphTrail;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.classifier.MDEClassifier;
import gov.nasa.ial.mde.solver.classifier.PolarClassifier;
import gov.nasa.ial.mde.solver.classifier.PolynomialClassifier;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.classifier.TrigClassifier;
import gov.nasa.ial.mde.solver.numeric.PolynomialModel;
import gov.nasa.ial.mde.solver.numeric.QuadraticModel;
import gov.nasa.ial.mde.util.MathUtil;
import gov.nasa.ial.mde.util.PointsUtil;
import gov.nasa.ial.mde.util.TrailUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Analyzes an input string to extract features associated with equation(s).
 * Determines variables, coordinate system, and type of equation e.g. Cartesian,
 * Polar or Parametric. Acts as a screener to determine which classifier to apply.
 *
 * @author Dan Dexter
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class AnalyzedEquation implements AnalyzedItem {

	/** Constant corresponds to an equation that did not parse correctly. */
    public static final int UNKNOWN = 0;
    
    /** Constant corresponds to a Cartesian equation. */
    public static final int CARTESIAN = 1;
    
    /** Constant corresponds to a Polar equation. */
    public static final int POLAR = 2;
    
    /** Constant corresponds to a Parametric equation. */
    public static final int PARAMETRIC = 3;
    
    /** Property of the equation. */
    public static final int GENERIC = 0, CONSTANT = 1, FUNCTION = 2, POLYNOMIAL = 4,
            QUADRATIC = 8, MORE_THAN_TWO_VARIABLES = 16, UNDEFINED = 32, NO_SOLUTION = 64;

    private Hashtable<String, Double> parameterHash = new Hashtable<String, Double>();
    private int equationType = UNKNOWN;
    private int equationProperties = GENERIC;
    private Equation theEquation;
    private String inputEquation;
    private Polynomial lhs;
    private Polynomial dvp; // dependent variable polynomial
    private int dvpDegree; // degree of dvp
    private Expression[] dvpCoefficients; // coefficients of dvp
    private double[] doubleCoefficients; // dvp coefficients as a double array
    private double[] reducedDVPCoefficients; //Workspace for Roots solution methods
    private double et, et2; // tolerances for EZ and EZ2
    private String[] actualVariables = { "x", "y" };
    private String independentVariable, dependentVariable;

	private Bounds preferredBounds = new Bounds(-DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, -DEFAULT_BOUND_VALUE);
	private MultiPointXY[] points;
	private GraphTrail[] graphTrails;

	private SolvedGraph features = null;

    private double maxJump = 0.0; //tolerance for breaking a GraphTrail

    // Are the computed points a function.
	private boolean functionOverInterval = false;

    // Use this in case the equation defines a constant; leave null otherwise
    private MultiPointXY constantSolution = null;
    private MultiPointXY[] savedR = null;
    private double savedRXLow = 0.0;
    private double savedRXHigh = 0.0;
    private boolean bad = false;

	private int degree;
    
    public int getDegree() {
		return degree;
	}

	@SuppressWarnings("unused")
	private AnalyzedEquation() {
        throw new RuntimeException("Default constructor not allowed.");
    }

    /**
     * Creates an instance of <code>AnalyzedEquation</code> using the
	 * specified equation string.
	 * 
     * @param eqString the String representation of the input equation.
     */
    public AnalyzedEquation(String eqString) {
        super();
        if ((theEquation = new Equation(eqString)).bad) {
            bad = true;
            return;
        }
        this.inputEquation = eqString;
        doInit();
    } // end AnalyzedEquation

    public Polynomial getLhs() {
		return lhs;
	}

	// Do initialization of the class.
    private void doInit() {
        lhs = theEquation.getPolynomial();
        
        checkVariables();

        /* If there are more than 2 variables, quit */
        if ((equationProperties & MORE_THAN_TWO_VARIABLES) != 0) {
            return;
        }
        

        switch (equationType) {
        case CARTESIAN:
            /* if it's a polynomial */
            if (lhs.hasConstantCoefficients()) {
                equationProperties |= POLYNOMIAL;
                if (lhs.getDegree() <= 2)
                    equationProperties |= QUADRATIC;
            } // end if polynomial
            independentVariable = actualVariables[0];
            dependentVariable = actualVariables[1];
            dvp = theEquation.getOneVariablePolynomial(dependentVariable);
            
            degree = lhs.getDegree();
            
            //System.out.println(degree);

            if (checkForSolvable())
                switch (dvpDegree) {
                case 0:
                    equationProperties |= UNDEFINED;
                    break;

                case 1:
                    equationProperties |= FUNCTION;

                default:
                    checkForConstant();
                    break;
                } // end switch
            break;

        case POLAR:
            dependentVariable = "r";
            independentVariable = "theta";
            dvp = theEquation.getOneVariablePolynomial(dependentVariable);
            if (checkForSolvable())
                checkForConstant();
            break;

        default:
            throw new IllegalStateException("Unimplemented equation type: " + equationType);
        } // end switch
    } // end doInit
    
	/**
	 * Returns the String representation of the input equation.
	 * 
	 * @return returns the input equation.
	 */
	public String getInputEquation() {
		return inputEquation;
	}
	
    /**
     * Returns the dependent variable of the equation.
     * 
     * @return the dependent variable of the equation.
     */
    public String getDependentVariable() {
        return dependentVariable;
    } // end getDependentVariable

    /**
     * Returns the independent variable of the equation.
     * 
     * @return the independent variable of the equation.
     */
    public String getIndependentVariable() {
        return independentVariable;
    } // end getIndependentVariable
    
    /**
     * Returns a <code>Hashtable</code> of the parameters in the equation.
     * <p>
     * The <code>Hashtable</code> key is the parameter as it shows up in the
     * equation and the <code>Hashtable</code> value is the value of the parameter.
     * 
     * @return a <code>Hashtable</code> of the parameters in the equation.
     * @see #getParameters()
     * @see #getParameterValue(String)
     */
    public Hashtable<String, Object> getParameterHash() {
        Hashtable<String, Object> r = new Hashtable<String, Object>();
        Enumeration<String> k = parameterHash.keys();
        String key;
        Object value;

        while (k.hasMoreElements()) {
            value = parameterHash.get(key = k.nextElement());
            r.put(key, value);
        } // end while

        return r;
    } // end getParameterHash
    
    /**
     * Returns an array of <code>String</code>s of all the parameters that exist
     * in the input equation.
     * 
     * @return an array of all the parameters that exist in the input equation.
     * @see #getAllPossibleParameters()
     */
    public String[] getParameters() {
        ArrayList<String> pList = new ArrayList<String>();
        Enumeration<String> k = parameterHash.keys();

        while (k.hasMoreElements()) {
            pList.add(k.nextElement());
        }

        String[] r = pList.toArray(new String[pList.size()]);
        Arrays.sort(r);
        return r;
    } // end getParameters

    /**
     * Returns an array of <code>String</code>s of all the supported parameter
     * names.
     * 
     * @return an array of <code>String</code>s of all the supported parameter
     * 		names.
     */
    public static String[] getAllPossibleParameters() {
        int n = MdeSettings.PARAMETER_STRINGS.length;
        String[] r = new String[n];

        for (int i = 0; i < n; i++) {
            r[i] = MdeSettings.PARAMETER_STRINGS[i][0];
        }
        return r;
    } // end getAllPossibleParameters

    /**
     * Returns true if the specified parameter name exists in the input
     * equation, otherwise false.
     * 
     * @param paramName the parameter name.
     * @return true if the specified parameter name exists in the input
     * 		equation, otherwise false.
     */
    public boolean containsParameter(String paramName) {
        return parameterHash.containsKey(paramName);
    } // end containsParameter

    /**
     * Returns the parameter value for the specified parameter name.
     * 
     * @param paramName the parameter name.
     * @return the parameter value.
     * @exception IllegalArgumentException is thrown if the specified parameter
     * 		name does not exist in the input equation.
     * @see #setParameterValue(String, double)
     */
    public double getParameterValue(String paramName) {
        Double paramValue = (Double)parameterHash.get(paramName);

        if (paramValue == null) {
            throw new IllegalArgumentException("Attempt to access undefined parameter.");
        }

        return paramValue.doubleValue();
    } // end getParameterValue

    /**
     * Sets the value for the specified parameter.
     * 
     * @param name the parameter name.
     * @param value the parameter value.
     * @exception IllegalArgumentException is thrown if the specified parameter
     * 		name does not exist in the input equation.
     * @see #getParameterValue(String)
     */
    public void setParameterValue(String name, double value) {
        if (!parameterHash.containsKey(name)) {
            throw new IllegalArgumentException("Attempt to change nonexistent parameter");
        }
        parameterHash.put(name, new Double(value));
    } // end setParameterValue

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#dispose()
     */
    public void dispose() {
        parameterHash.clear();
        parameterHash = null;
        theEquation = null;
        lhs = null;
        dvp = null; // dependent variable polynomial
        dvpCoefficients = null; // coefficients of dvp
        doubleCoefficients = null; // dvp coefficients as a double array
        reducedDVPCoefficients = null; //Workspace for Roots solution methods
        actualVariables = null;
        independentVariable = null;
        dependentVariable = null;

        preferredBounds = null;
        disposePoints();
        disposeGraphTrails();

        features = null;
        constantSolution = null;
        savedR = null;
        bad = true;
    }
    
    // Dispose of the graph trails.
    private void disposeGraphTrails() {
        if (graphTrails != null) {
            int len = graphTrails.length;
            for (int i = 0; i < len; i++) {
                if (graphTrails[i] != null) {
                    graphTrails[i].dispose();
                    graphTrails[i] = null;
                }
            }
            graphTrails = null;
        }
    }
    
    // Dispose of the points.
    private void disposePoints() {
        if (points != null) {
            int len = points.length;
            for (int i = 0; i < len; i++) {
                if (points[i] != null) {
                    points[i].dispose();
                    points[i] = null;
                }
            }
            points = null;
        }
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getName()
     */
    public String getName() {
        //return printOriginalEquation();
    	return getInputEquation();
    }

    /**
     * Returns true if the input equation is bad, otherwise false.
     * 
     * @return true if the input equation is bad, otherwise false.
     */
    public boolean isBad() {
        return bad;
    }

    /**
     * Returns the input equation as a <code>String</code> that was passed to
     * the constructor of this class.
     * 
     * @return the input equation that was passed to the constructor of this class.
     */
    public String printOriginalEquation() {
        return theEquation.toString();
    }

    /**
     * Returns the analyzed equation as a <code>String</code>.
     * 
     * @return the analyzed equation as a <code>String</code>.
     */
    public String printEquation() {
        Expression L = new Expression(theEquation.left.root);
        Expression R = new Expression(theEquation.right.root);

        L.setParameterHash(parameterHash);
        R.setParameterHash(parameterHash);
        return L.toString() + " = " + R.toString();
    } // end printEquation

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPreferredBounds()
     */
    public Bounds getPreferredBounds() {
        return preferredBounds;
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getClassifier()
     */
    public MDEClassifier getClassifier() {
        if (isQuadratic()) {
        	
        	//System.out.println("I got a quadratic formula!");
            return new QuadraticClassifier(lhs);
        }
        

        if (isPolar()) {
        	
        	//System.out.println("I got a polar formula!");
        	return new PolarClassifier(solveForPoints(0.0, 2.0 * Math.PI));
            
        }
        
        
        //TODO: put trig here
        //isTrig()-->return new TrigClassfier

     
        if((lhs.toString().contains("sin"))||(lhs.toString().contains("tan"))||(lhs.toString().contains("cos"))){
        	//System.out.println("DEBUG FOR TRIGCLASSIFIER: LHS IS AT THIS POINT: " +lhs);
        	return new TrigClassifier();
        }
        

        // If we have a non-null savedR of function points with a good Low and
        // High bounds for X then we will use those bounds.
        PolynomialClassifier pc;
        //System.out.println("I got a polynomial... maybe!");
        
        if ((savedR != null) && (savedRXLow < savedRXHigh)) {
            pc = new PolynomialClassifier(solveForPoints(savedRXLow, savedRXHigh));
        } else {
            // Use the default bounds.
            pc = new PolynomialClassifier(solveForPoints(-DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE));
        }
        
        
        PolynomialModel pm = pc.getBestGuess();
        //System.out.println("Best Guess entered!");

        
        if (pm == null) {
        	//System.out.println("polynmial model is null!");
            return new MDEClassifier();
        }

        if (pm instanceof QuadraticModel) {
        	//System.out.println("pm is instanceof of quadratic model!");
            QuadraticModel q = (QuadraticModel)pm;

            if (q.getAnalyzedEquation().isFunction()) {
            	//System.out.println("quadractic classifier!");
                return new QuadraticClassifier(q.getPolynomial());
            }
        } // end if
        
        //System.out.println("I'm a polynomial!");
        return pc;
    } // end getClassifier

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#updateFeatures()
     */
    public void updateFeatures() {
        MDEClassifier c = getClassifier();
        this.features = (c != null) ? c.getFeatures(this) : null;
    }

    // Get the cached features.
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getFeatures()
     */
    public SolvedGraph getFeatures() {
        return features;
    }

    /**
     * Returns the type of the equation as a coded integer which can be compared to
     * the constants <code>UNKNOWN</code>, <code>CARTESIAN</code>, <code>POLAR</code>
     * or <code>PARAMETRIC</code> defined in this class.
     * 
     * @return an integer corresponding to UNKNOWN, CARTESIAN, POLAR or PARAMETRIC.
     *         Might be useful as a variable in a case statement. The value UNKNOWN
     *         corresponds to an equation that did not parse correctly. The alternate
     *         isUnknown|Cartesian|Polar|Parametric methods are provided. Currently,
     *         parametric equations are not supported.
     * @see #UNKNOWN
     * @see #CARTESIAN
     * @see #POLAR
     * @see #PARAMETRIC
     */
    public int getEquationType() {
        return equationType;
    } // end getEquationType

    /**
     * Returns an integer that codes boolean properties of the equation.
     * 
     * @return an integer code for the various equation properties to check against
     *         the enumeration constants CONSTANT|FUNCTION|POLYNOMIAL of this class.
     *         Might be useful as a variable in a case statement.
     * @see #GENERIC
     * @see #CONSTANT
     * @see #FUNCTION
     * @see #POLYNOMIAL
     * @see #QUADRATIC
     * @see #MORE_THAN_TWO_VARIABLES
     * @see #UNDEFINED
     * @see #NO_SOLUTION
     */
    public int getEquationProperties() {
        return equationProperties;
    } // end getEquationProperties

    /**
     * Returns true if the equation string did not parse, otherwise false.
     * 
     * @return true if the equation string did not parse, otherwise false.
     * @see #UNKNOWN
     * @see #getEquationType()
     */
    public boolean isUnknown() {
        return (equationType == UNKNOWN);
    } // end isUnknown

    /**
     * Returns true if the equation is Cartesian, otherwise false.
     * 
     * @return true if the equation is understood to be described by Cartesian
     *         coordinates, false otherwise.
     * @see #CARTESIAN
     * @see #getEquationType()
     */
    public boolean isCartesian() {
        return (equationType == CARTESIAN);
    } // end isCartesian

    /**
     * Returns true if the equation is Polar, otherwise false.
     * 
     * @return true if the equation is understood to be described by Polar
     *         coordinates; false otherwise.
     * @see #POLAR
     * @see #getEquationType()
     */
    public boolean isPolar() {
        return (equationType == POLAR);
    } // end isPolar

    /**
     * Determine if the input string describe a set of parametric equations.
     * <p>
     * <i>NOTE:</i> Currently we do not recognize parametric equations, so this
     * will return false until this feature is implemented.
     * 
     * @return true if the input string describes a pair of parametric equations.
     * @see #PARAMETRIC
     * @see #getEquationType()
     */
    public boolean isParametric() {
        return (equationType == PARAMETRIC);
    } // end isParametric

    /**
     * Does the input string describe an equation with no reference to an independent
     * variable, does it define a constant.
     * 
     * @return true if the value(s) of the dependent variable do not depend on the
     *		independent variable. Does not strictly mean constant, in the sense
     *		that the input string "y^3 = y" would evoke a return value of true,
     *		even though this relation does not define a function. The meaning of
     *		"constant" in this context is that we need only compute the dependent
     *		variable once.
     * @see #CONSTANT
     * @see #getEquationProperties()
     */
    public boolean isConstant() {
        return ((equationProperties & CONSTANT) != 0);
    } // end isConstant

    /**
     * Determines syntactically if the input string defines a function. It may return
     * false in situations for which the input string is sufficiently obfuscated that
     * the syntactic tests fail, for example "exp(x^2 - y) = 1" which is
     * algebraically equivalent to y = x^2 -- the code will not discover such an
     * equivalence.
     * 
     * @return true if and only if the equation is guaranteed to define a function,
     * 		independent of the value of the independent variable. A return value
     * 		of "false" should suggest testing points over the indicated domain.
     * @see #FUNCTION
     * @see #getEquationProperties()
     */
    public boolean isFunction() {
        return ((equationProperties & FUNCTION) != 0);
    } // end isFunction

    /**
     * Does the input string define a polynomial equation?
     * 
     * @return true if and only if the input equation is a polynomial.
     * @see #POLYNOMIAL
     * @see #getEquationProperties()
     */
    public boolean isPolynomial() {
        return ((equationProperties & POLYNOMIAL) != 0);
    } // end isPolynomial

    /**
     * Indicates that the equation is a quadratic equation.
     * 
     * @return true if and only if the equation is a quadratic equation.
     * @see #QUADRATIC
     * @see #getEquationProperties()
     */
    public boolean isQuadratic() {
        return ((equationProperties & QUADRATIC) != 0);
    } // end isQuadratic

    /**
     * Indicates that the equation should not be considered for further processing.
     * 
     * @return true if and only if the equation contains more than two variables.
     * @see #MORE_THAN_TWO_VARIABLES
     * @see #getEquationProperties()
     */
    public boolean hasMoreThanTwoVariables() {
        return ((equationProperties & MORE_THAN_TWO_VARIABLES) != 0);
    } // end hasMoreThanTwoVariables

    /**
     * Indicates no explicit dependent variable.
     * 
     * @return true if and only if the dependent variable does not appear in the
     * 		equation. The resulting graph will always be zero or more vertical lines.
     * @see #UNDEFINED
     * @see #getEquationProperties()
     */
    public boolean isUndefined() {
        return ((equationProperties & UNDEFINED) != 0);
    } // end isUndefined

    /**
     * Indicates whether or not the solution algorithm will succeed on the input
     * equation.
     * 
     * @return true if and only if the solution algorithm will fail on this equation.
     * @see #NO_SOLUTION
     * @see #getEquationProperties()
     */
    public boolean cannotBeSolved() {
        return ((equationProperties & NO_SOLUTION) != 0);
    } // end cannotBeSolved

    /**
     * Convenience method to determine if the equation is a solvable function.
     * 
     * @return true if and only if <code>!cannotBeSolved() && isFunction()</code>.
     */
    public boolean isSolvableFunction() {
        return isFunction() && (!cannotBeSolved());
    } // end isSolvableFunction

    /**
     * Gets the function f if the equation defines y = f(x).
     * 
     * @return an expression representing the function f, if this equation
     * 		defines a function; null otherwise.
     */
    public Expression getFunction() {
        return isSolvableFunction() ? Expression.negate(dvpCoefficients[1].quotient(dvpCoefficients[0])) : null;
    } // end getFunction

    /**
     * Fills in the double values for the coefficients of the polynomial we will
     * solve for the dependent variable.
     * 
     * @param x is the value of the independent variable for which we will want
     *		corresponding values of the dependent variable.
     * @return the coefficients as an array of doubles.
     */
    public double[] getCoefficients(double x) {
        Polynomial.evaluateCoefficients(dvpCoefficients, independentVariable, x, doubleCoefficients);
        
        // The EZ et al methods are sensitive to the current value of x, so the
        // tolerances must be re-computed with every new value of x
        setTolerances();

        return doubleCoefficients;
    } // end getCoefficients

    /**
     * Finds all real values of the dependent variable which correspond to a given
     * value of the independent variable.
     * 
     * @param x the given value of the independent variable.
     * @return A <code>MultiPointXY</code> in which the x field contains values of
     * 		the independent variable, and the yArray field contains all corresponding
     * 		values of the dependent variable.
     */
    public MultiPointXY findRealSolutions(double x) {
        if (constantSolution != null) {
            return new MultiPointXY(x, constantSolution.yArray);
        } // end if

        if (isConstant()) {
            constantSolution = getSolution(x);
            return constantSolution;
        }

        return getSolution(x);
    } // end findRealSolutions

    /**
     * Returns the two variables used in the equation if the equation only contains
     * one variable, makes a reasonable guess as to the other implied variable.
     * 
     * @return two-element string array containing the equation variables.
     */
    public String[] getActualVariables() {
        return new String[] { actualVariables[0], actualVariables[1] };
    } // end getActualVariables

    /**
     * Returns an array of expressions corresponding to the coefficients of the
     * polynomial for the dependent variable.
     * <p>
     * Note that this <code>AnalyzedEquation</code> is solvable if and only if
     * each DVP coefficient contains at most one variable, and that variable is
     * identical to the independent variable.
     * 
     * @return an array of expressions c[k], where c[k] is the coefficient
     * 		of y^k in the polynomial for y.
     */
    public Expression[] getDVPCoefficients() {
        int i, n = dvpCoefficients.length;
        Expression[] r = new Expression[n];

        for (i = 0; i < n; i++)
            r[i] = dvpCoefficients[i];

        return r;
    } // end getDVPCoefficients
    
    // Set the default tolerances.
    private void setTolerances () {
    	setTolerances(dvpDegree, doubleCoefficients);
    } // end setTolerances

    // Use the specified tolerances.
    private void setTolerances(int dvpd, double[] dpc) {
        et = et2 = 0.0;
        for (int i = 0; i <= dvpd; i++) {
            et += Math.abs(dpc[i]);
            et2 += (dpc[i] * dpc[i]);
        } // end for i

        et = et * 1.0e-8 + Double.MIN_VALUE;
        et2 = et2 * 1.0e-16 + Double.MIN_VALUE;
    } // end setTolerances

    private boolean EZ(double x) {
        return (Math.abs(x) < et);
    } // end EZ

    private boolean EZ2(double x) {
        return (Math.abs(x) < et2);
    } // end EZ2

    private boolean LZ2(double x) {
        if (EZ2(x))
            return false;

        if (x > 0.0)
            return false;

        return true;
    } // end LZ2

    private MultiPointXY getSolution(double x) {
        if (cannotBeSolved() || isUndefined()) {
            return null;
        }

        // No need to capture return value because the coefficients are stored
        getCoefficients(x);
        
        return actuallySolve(dvpDegree, x);
    } // end getSolution

    private MultiPointXY actuallySolve(int deg, double[] coeffs, double x) {
        int d, i, n;
        MultiPointXY r = new MultiPointXY(x);
        
        /* check for NaNs */
        for (i = 0; i <= deg; i++) {
            if (Double.isNaN(coeffs[i])) {
                r.yArray = new double[0];
                return r;
            } // end if
        }
        
        // Find effective degree of dependent variable polynomial
        setTolerances (deg, coeffs); //ROS: 1/25/05
        for (n = 0; n <= deg; n++)
            if (!EZ(coeffs[n]))
                break;

        switch (d = deg - n) {
        case -1:
            return new MultiPointXY(x, new double[0]);

        case 0:
        case 1:
            if (!EZ(coeffs[deg - 1])) {
                double[] y = { -coeffs[deg] / coeffs[deg - 1] };
                r.yArray = y;
            } // end if
            else
                r.yArray = new double[0];

            return r;

        case 2:
            { // start of block
                double t0 = -0.5 * coeffs[deg - 1] / coeffs[deg - 2];
                double t1 = coeffs[deg] / coeffs[deg - 2];
                double d2 = t0 * t0 - t1;

                if (LZ2(d2))
                    r.yArray = new double[0];
                else if (EZ2(d2)) {
                    double[] y = { t0, t0 };

                    r.yArray = y;
                } // end if
                else {
                    double disc = Math.sqrt(d2);
                    double[] y = { t0 - disc, t0 + disc };

                    r.yArray = y;
                } // end else

                return r;
            } // end block

        default:
            if (reducedDVPCoefficients.length <= d)
                reducedDVPCoefficients = new double[d + 1];

            for (i = n; i <= deg; i++)
                reducedDVPCoefficients[i - n] = coeffs[i];

            r.yArray = Roots.getRealRoots(reducedDVPCoefficients, d);
            return r;
        } // end switch
    } // end actuallySolve

    private MultiPointXY actuallySolve(int deg, double x) {
        return actuallySolve(deg, doubleCoefficients, x);
    } // end actuallySolve

    private void determineParameters(String[] allVariables) {
        Double tempObj;
        Hashtable<String, Double> temp = new Hashtable<String, Double>();
        int i, n = MdeSettings.PARAMETER_STRINGS.length;

        for (i = 0; i < n; i++)
            temp.put(MdeSettings.PARAMETER_STRINGS[i][0], new Double(MdeSettings.PARAMETER_STRINGS[i][1]));

        n = allVariables.length;

        for (i = 0; i < n; i++) {
            String v = allVariables[i].toLowerCase();
            //System.out.println("Variables:" +v);

            if ((tempObj = temp.get(v)) != null)
                parameterHash.put(v, tempObj);
        } // end for i
    } // end determineParameters

    private void checkVariables() {
       // System.out.println("DEBUG: in checkVaribles.");
    	
    	String[] temp;
        ArrayList<String> realVariables = new ArrayList<String>();

        temp = lhs.toExpression().varStrings;
        determineParameters(temp);
       
        
        
        lhs.setParameterHash(parameterHash);

        for (int i = 0; i < temp.length; i++)
            if (parameterHash.get(temp[i].toLowerCase()) == null)
                realVariables.add(temp[i]);

        temp = realVariables.toArray(new String[realVariables.size()]);
        
       

        switch (temp.length) {
        case 0:
            break;

        case 1:
            /* handle the polar case */
            if (temp[0].equals("r") || temp[0].equals("theta")) {
                actualVariables[0] = "r";
                actualVariables[1] = "theta";
                break;
            } // end if

            // if there's just one variable in the equation entered by the user, and
            // that variable is not ``y'', then the variable in the equation is the
            // abscissa, and ``y'' is the ordinate.
            if (!temp[0].equals(actualVariables[1]))
                actualVariables[0] = temp[0];
            break;

        case 2:
            actualVariables = temp; // adopt variables entered by user
            break;

        default:
            actualVariables = temp;
            equationProperties |= MORE_THAN_TWO_VARIABLES;
            return;
        } // end switch

        if (actualVariables[0].equals("r") && actualVariables[1].equals("theta"))
            equationType = POLAR;
        else
            equationType = CARTESIAN;
    } // end checkVariables

    private boolean checkForSolvable() {
        computeCoefficients();

        for (int i = 0; i <= dvpDegree; i++)
            switch (dvpCoefficients[i].varStrings.length) {
            case 0:
                break;

            case 1:
                if (dvpCoefficients[i].varStrings[0].equals(independentVariable))
                    break;

            default:
                equationProperties |= NO_SOLUTION;
                return false;
            } // end switch

        return true;
    } // end checkForSolvable

    private void checkForConstant() {
        int i;
        
       // System.out.println("DEBUG: in check for constant.");

        for (i = 0; i <= dvpDegree; i++)
            if (dvpCoefficients[i].varStrings.length != 0)
                break;

        if (i > dvpDegree)
            equationProperties |= CONSTANT;
    } // end checkForConstant

    private void computeCoefficients() {
        dvpDegree = dvp.getDegree();
        dvpCoefficients = Polynomial.getCoefficientsAsExpressions(dvp, dependentVariable);
        for (int i = 0; i <= dvpDegree; i++)
            dvpCoefficients[i].setParameterHash(parameterHash);

        doubleCoefficients = new double[dvpDegree + 1];
        reducedDVPCoefficients = new double[dvpDegree + 1];
    } // end computeCoefficients

    private MultiPointXY findBoundary(MultiPointXY mp0, MultiPointXY mp1) {
        double[] y0 = mp0.yArray, y1 = mp1.yArray;
        double x0 = mp0.x, x1 = mp1.x;
        int n0 = y0.length, n1 = y1.length;

        if (x1 - x0 < 1.0e-8) {
            return (n0 > n1) ? mp0 : mp1;
        }

        double x = 0.5 * (x0 + x1);

        if (n1 > n0) {
            if (hasMultiples(y1)) {
                return mp1;
            }
            MultiPointXY mp = findRealSolutions(x);
            return (mp.yArray.length != n1) ? findBoundary(mp, mp1) : findBoundary(mp0, mp);
        } // end if

        if (n1 < n0) {
            if (hasMultiples(y0)) {
                return mp0;
            }
            MultiPointXY mp = findRealSolutions(x);
            return (mp.yArray.length != n0) ? findBoundary(mp0, mp) : findBoundary(mp, mp1);
        } // end if

        return mp0;
    } // end findBoundary

    private boolean hasMultiples(double[] y) {
        int i, n = y.length;

        if (n < 2) {
            return false;
        }

        for (i = 1; i < n; i++) {
            if (y[i] == y[i - 1]) {
                return true;
            }
        }

        return false;
    } // end hasMultiples

    private MultiPointXY[] generateVerticalLine(double low, double high, double top, double bottom) {
        int i, j, k;
        int len = NUM_POINTS;
        double delta = (top - bottom) / (len - 1.0);
        double y = bottom;
        MultiPointXY[] r = new MultiPointXY[len];

        double[] x = getXIntercepts();

        for (i = 0; i < len; i++, y += delta) {
            k = 0;
            for (j = 0; j < x.length; j++) {
                if (x[j] >= low && x[j] <= high) {
                    double[] Y = new double[1];
                    Y[0] = y;
                    r[i++] = new MultiPointXY(x[j], Y);
                    k++;
                }
            }
            if (k > 0) {
                y = y + (delta * (k));
                i--;
            }
        }
        return r;
    }

    private GraphTrail[] getVerticalGraphTrails(MultiPointXY[] p) {
        double[] xIntercepts = getXIntercepts();
        int[] foundX = new int[xIntercepts.length];

        int i, j;

        for (i = 0; i < xIntercepts.length; i++)
            foundX[i] = 0;
        for (i = 0; i < xIntercepts.length; i++)
            for (j = 0; j < p.length; j++)
                if (p[j] != null && p[j].x == xIntercepts[i])
                    foundX[i] = foundX[i] + 1;

        int cnt = 0;
        for (i = 0; i < xIntercepts.length; i++) {
            if (MdeSettings.DEBUG) {
                System.out.println("x intercept =" + xIntercepts[i]);
            }
            if (foundX[i] > 0) {
                cnt++;
            }
        }

        int gcnt = 0;
        GraphTrail[] g = new GraphTrail[cnt];
        for (i = 0; i < xIntercepts.length; i++) {
            int pcnt = 0;
            if (foundX[i] > 0) {
                double[][] pts = new double[foundX[i]][2];
                for (j = 0; j < p.length; j++)
                    if (p[j] != null && p[j].x == xIntercepts[i]) {
                        pts[pcnt][0] = xIntercepts[i];
                        pts[pcnt++][1] = p[j].yArray[0];
                    }
                g[gcnt++] = new GraphTrail(pts);
            }
        }

        return g;

    } // end getVerticalGraphTrails

    private MultiPointXY[] solveForPoints(double low, double high) {
        int i;
        int n = NUM_POINTS;
        double delta = (high - low) / (n - 1.0);
        double deltaError = 0.9 * delta; // account for roundoff error in step size
        double x = low;
        MultiPointXY[] r = new MultiPointXY[n];
        boolean found = false;
        int l = 0;
        
        // Determine if the low and high range has changes which means we can
        // not reuse the savedR array.
        if (low != savedRXLow) {
            savedRXLow = low;
            
            // New lower range on X, so clear the cached real points.
            savedR = null;
        }
        if (high != savedRXHigh) {
            savedRXHigh = high;
            
            // New upper range on X, so clear the cached real points.
            savedR = null;
        }
        
        // Find the real values and used cached values if we have one.
        for (i = 0; i < n; i++, x += delta) {
            found = false;
            if ((savedR != null) && (savedR[i] != null)) {
                if ((x >= savedR[0].x) && (x <= savedR[savedR.length - 1].x)) {
                    for (; l < n; l++) {
                        if (Math.abs(savedR[l].x - x) < deltaError) {
                            found = true;
                            r[i] = savedR[l];
                            break;
                        }
                    }
                }
            }
            if (!found) {
                r[i] = findRealSolutions(x);
            }
        }
        
        int[] sb = TrailUtil.getSegmentBoundariesFrom(r, maxJump);

        n = sb.length - 1;

        for (i = 1; i < n; i++) {
            MultiPointXY newR = findBoundary(r[sb[i] - 1], r[sb[i]]);

            if (newR.yArray.length == r[sb[i] - 1].yArray.length) {
                r[sb[i] - 1] = newR;
            }

            if (r[sb[i]].yArray.length == newR.yArray.length) {
                r[sb[i]] = newR;
            }
        } // end for i

        // Create the cached array of real values.
        savedR = new MultiPointXY[r.length];
        for (i = 0; i < r.length; i++) {
            if (r[i] != null) {
                savedR[i] = new MultiPointXY(r[i].x, r[i].yArray);
            }
        }
        
        return r;
    } // end solveForPoints

    /**
     * Returns the Y-intercept values.
     * 
     * @return the Y-intercept values.
     */
    public double[] getYIntercepts() {
        if (cannotBeSolved())
            return new double[0];

        MultiPointXY p = findRealSolutions(0.0);
        if (p == null)
            return new double[0];
        return p.yArray;
    }

    /**
     * Returns the X-intercept values.
     * 
     * @return the X-intercept values.
     */
    public double[] getXIntercepts() {
        Polynomial p = theEquation.getPolynomial();
        Expression[] ec = p.getCoefficientsAsExpressions(independentVariable);
        int deg = ec.length - 1;

        if (deg == 0)
            return new double[0];

        for (int i = 0; i <= deg; i++)
            ec[i].setParameterHash(parameterHash);

        double[] c = new double[deg + 1];

        try {
            Polynomial.evaluateCoefficients(ec, dependentVariable, 0.0, c);
            return actuallySolve(deg, c, 0.0).yArray;
        } // end try
        catch (Exception e) {
            return new double[0];
        } // end catch
    } // end getXIntercepts

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPoint(double)
     */
    public MultiPointXY getPoint(double position) {
        if ((points == null) || (points.length <= 0) || (position < 0.0) || (position > 1.0)) {
            return null;
        }
        int index = (int)Math.floor(position * (points.length - 1));
        return points[index];
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPoint(int)
     */
    public MultiPointXY getPoint(int index) {
        return ((points != null) && (index >= 0) && (index < points.length)) ? points[index] : null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getPoints()
     */
    public MultiPointXY[] getPoints() {
        return points;
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#getGraphTrails()
     */
    public GraphTrail[] getGraphTrails() {
        return graphTrails;
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#computePoints(gov.nasa.ial.mde.math.Bounds)
     */
    public void computePoints(Bounds b) {
        computePoints(b.left, b.right, b.top, b.bottom);
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedItem#computePoints(double, double, double, double)
     */
    public void computePoints(double left, double right, double top, double bottom) {
        maxJump = Math.abs(top - bottom);
        if (isPolar()) {
            /* get the polar points */
            points = solveForPoints(0.0, 2.0 * Math.PI);
            
            /* account for possible multiple values of r */
            graphTrails = TrailUtil.getGraphTrailsFrom(points, maxJump);

            int i, j, n = graphTrails.length;
            PointXY[][] p = new PointXY[n][0];
            ArrayList<MultiPointXY> newPoints = new ArrayList<MultiPointXY>();

            left = Double.POSITIVE_INFINITY;
            right = Double.NEGATIVE_INFINITY;
            top = Double.NEGATIVE_INFINITY;
            bottom = Double.POSITIVE_INFINITY;

            for (i = 0; i < n; i++) {
                p[i] = PointsUtil.toCartesian(graphTrails[i].getPoints());
                for (j = 0; j < graphTrails[i].getLength(); j++) {
                    double[] y = { p[i][j].y };

                    right = Math.max(right, Math.abs(p[i][j].x));
                    top = Math.max(top, Math.abs(p[i][j].y));
                    newPoints.add(new MultiPointXY(p[i][j].x, y));
                } // end for j
            } // end for i
            right = Math.max(right, top);

            // Limit the bounds to the maximum value of the DEFAULT_LIMIT
            right = Math.min(right, DEFAULT_BOUND_VALUE);

            top = right;
            left = -right;
            bottom = -top;
            
            // Create the new points
            points = newPoints.toArray(new MultiPointXY[newPoints.size()]);
            
            graphTrails = new GraphTrail[n];
            for (i = 0; i < n; i++) {
                graphTrails[i] = new GraphTrail(p[i]);
            }
        } // end if polar
        else {
            if (isUndefined()) {
                points = generateVerticalLine(left, right, top, bottom);
                graphTrails = getVerticalGraphTrails(points);
            } else {
                points = solveForPoints(left, right);
                graphTrails = TrailUtil.getGraphTrailsFrom(points, maxJump);
            }
        }

        // Update the bounds we keep for this analyzed equation.
        preferredBounds.setBounds(left, right, top, bottom);

        // Now do the fuction test.
        functionTest();
    } //end computePoints

    /**
     * Returns true if the equation is a function over the interval, otherwise false.
     * 
     * @return true if the equation is a function over the interval, otherwise false.
     */
    public boolean isFunctionOverInterval() {
        return functionOverInterval;
    } // end isFunctionOverInterval
    
    // The computePoints() method calls this method to determine of the
    // equation is a function over the computed points.
    private void functionTest() {
        int i, j, m;
        int len = points.length;
        boolean foundPoints = false;

        functionOverInterval = true;
        for (i = 0; i < len; i++) {
            if (points[i] != null) {
                if ((m = points[i].yArray.length) > 0) {
                    foundPoints = true;
                    if (m == 1) {
                        continue;
                    }

                    for (j = 1; j < m; j++) {
                        if (Math.abs(points[i].yArray[j] - points[i].yArray[j - 1]) > 1.0e-3) {
                            functionOverInterval = false;
                            return;
                        } // end if
                    }
                } // end outer if

                if (!foundPoints) {
                    functionOverInterval = false;
                }
            }
        }
    } // end functionTest

    /**
	 * Checks whether two <code>AnalyzedEquation</code> objects have equal values.
	 * 
	 * @return true if the specified object and this <code>AnalyzedEquation</code> object are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AnalyzedEquation) {
            AnalyzedEquation ae = (AnalyzedEquation)obj;
            return this.printOriginalEquation().equals(ae.printOriginalEquation());
        }
        return false;
    }
    
    /**
	 * Returns a string representation of this <code>AnalyzedEquation</code> object.
	 * Intended primarily for test/debug.
	 * 
	 * @return a string representation of this <code>AnalyzedEquation</code> object.
	 * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        switch (equationType) {
        case CARTESIAN:
            b.append("Cartesian equation");
            b.append("\nX intercepts: ");
            double[] X = getXIntercepts();
            if (X != null) {
                if (X.length > 0)
                    for (int i = 0; i < X.length; i++)
                        b.append(" " + MathUtil.trimDouble(X[i], 4));
            } // end if

            break;

        case POLAR:
            b.append("Polar equation");
            break;

        default:
            b.append("Unknown equation format.");
            return b.toString();
        } // end switch

        if (isConstant())
            b.append("\nConstant");

        if (isFunction())
            b.append("\nIt's a function");

        if (isPolynomial())
            b.append("\nIt's a polynomial");

        if (isQuadratic())
            b.append("\nIt's a quadratic");

        if (hasMoreThanTwoVariables())
            b.append("\nIt has more than two variables");

        if (isUndefined())
            b.append("\nDependent variable is undefined.");

        if (cannotBeSolved())
            b.append("\nIt can't be solved");

        b.append("\nIndependent variable = " + independentVariable);
        b.append("\nDependent variable = " + dependentVariable);

        return b.toString();
    } // end toString
    
//    public static void main(String[] args) {
//        AnalyzedEquation iea = new AnalyzedEquation(MathUtil.combineArgs(args));
//        System.out.println("" + iea);
//
//        /*
//         * long d1=System.currentTimeMillis(); for (int i = 0; i < 600; i++)
//         * iea.findRealSolutions(Math.random()); long milli=System.currentTimeMillis() -
//         * d1.getTime(); System.out.println ("Elapsed time = " + milli + " milli
//         * seconds"); // doTest (iea); System.out.println ("Solutions:\n" +
//         * iea.findRealSolutions(0.0)); MDEClassifier c; c = iea.getClassifier();
//         * System.out.println (c.toString()); c = iea.getClassifier();
//         * System.out.println (c.toString());
//         */
//    } // end main
//
//    private static void doTest(AnalyzedEquation e) {
//        byte[] b = new byte[80];
//
//        while (true)
//            try {
//                System.in.read(b);
//                System.out.println("Solutions:\n" + e.findRealSolutions(new Double(new String(b)).doubleValue()));
//            } // end try
//            catch (java.io.IOException ioe) {
//                break;
//            } // end catch
//            catch (NumberFormatException nfe) {
//                break;
//            } // end catch
//    } // end doTest
    
} // end class AnalyzedEquation
