/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import java.util.ArrayList;

import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.features.individual.AsymptoteFeature;
import gov.nasa.ial.mde.solver.features.individual.VertexFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * Subclass of SolvedGraph responsible for recording features unique to hyperbolas.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SolvedHyperbola extends SolvedConic implements VertexFeature, AsymptoteFeature{
    
    /** Identify new features so we can access them with SolvedGraph.putFeature */
    protected String[] newFeatures = { 
                "center", 
                "focus", 
                "focalLength", 
                "eccentricity", 
                "transverseAxis", 
                "conjugateAxis", // axis described as lines
                "transverseAxisInclination", 
                "conjugateAxisInclination",
                "semiTransverseAxis", 
                "semiConjugateAxis", // lengths -- A and B
                "vertex", 
                "asymptotes" };

    /* enums for transverse axis direction */
    private final static int NO_D = 0, HORIZONTAL = 1, VERTICAL = 2;
    private int transverseAxisD = NO_D;

    /**
     * Constructs a solved hyperbola from the specifed analyzed equation.
     * 
     * @param equation the analyzed equation.
     */
    public SolvedHyperbola(AnalyzedEquation equation) {
        super(equation);

        /* QC is the QuadraticClassifier field in SolvedConic */
        double alpha = QC.getRotation(); // rotation angle in degrees
        
        /*
		 * coeffs={a, b, c, d, e} where a(u-h)^2 + b(v-k)^2 + cu + dv + e = 0
		 */
        double[] coeffs = QC.getNormalizedCoefficients();
        PointXY center = new PointXY(QC.UV2XY(QC.getTranslation()));
        String[] vars = analyzedEq.getActualVariables(); // saves a lot of typing
        double transverseAxisInclination, 
               conjugateAxisInclination, 
               asymptoteInclination, 
               A, // semi-transverse axis
               B, // semi-conjugate axis
               C, // focal length
               E; // eccentricity

        putNewFeatures(newFeatures); // enable use of new features
        putFeature("graphName", "hyperbola"); // self-explanatory
        putFeature("equationType", "conic section"); // ditto
        putFeature("center", center);
        putFeature("graphClosure", "false"); // might be hard to determine in general

        /* rule out the degenerate case */
        if (coeffs[4] == 0.0)
            throw new IllegalArgumentException("SolvedHyperbola.init found degenerate equation.");

        /* normalize so that coeffs[4] = -1 in order to solve for A and B */
        for (int i = 0; i < 5; i++)
            coeffs[i] /= (-coeffs[4]);
        
        /* Satisfy the java compiler's obsessive need for initializations */
        A = B = -1.0;

        /* coeffs[0] and coeffs[1] better have oppositve signs */
        if (coeffs[0] * coeffs[1] >= 0.0 || coeffs[2] != 0.0 || coeffs[3] != 0.0)
            throw new IllegalArgumentException("Attempt to initialize SolvedHyperbola on wrong equation.");

        /*
		 * determine horizontal/vertical case and define A and B horizontal
		 * case : (u-h)^2)/A^2 - (v-k)^2/B^2 = 1 vertical case: (v-k)^2/A^2 -
		 * (u-h)^2/B^2 = 1
		 */
        if (coeffs[0] > 0) {
            transverseAxisD = HORIZONTAL;
            A = 1.0 / Math.sqrt(coeffs[0]);
            B = 1.0 / Math.sqrt(-coeffs[1]);
        } // end if

        if (coeffs[1] > 0.0) {
            transverseAxisD = VERTICAL;
            A = 1.0 / Math.sqrt(coeffs[1]);
            B = 1.0 / Math.sqrt(-coeffs[0]);
        } // end if
        
        

        /* might as well take care of the obvious */
        putFeature("semiTransverseAxis", new NumberModel(A));
        putFeature("semiConjugateAxis", new NumberModel(B));
        

        /*
		 * Determine axis inclinations which depend on horizontal/vertical
		 * orientation
		 */
        switch (transverseAxisD) {
            case HORIZONTAL :
                transverseAxisInclination = alpha;
                asymptoteInclination = 180.0 * Math.atan(B / A) / Math.PI;
                break;

            case VERTICAL :
                transverseAxisInclination = alpha + 90.0;
                asymptoteInclination = 180.0 * Math.atan(A / B) / Math.PI;
                break;

            default :
                throw new IllegalStateException("Internal error in init");
        } // end switch

        conjugateAxisInclination = transverseAxisInclination + 90.0;

        /* normalize angles between -179.9999 and 180.0 */
        transverseAxisInclination = QuadraticClassifier.normalizeAngleInDegrees(transverseAxisInclination);
        conjugateAxisInclination = QuadraticClassifier.normalizeAngleInDegrees(conjugateAxisInclination);

        putFeature("transverseAxisInclination", new NumberModel(transverseAxisInclination));
        putFeature("conjugateAxisInclination", new NumberModel(conjugateAxisInclination));


        /* on with the rest of the calculations -- focalLength and vertices */
        C = Math.sqrt(A * A + B * B);
        E = C / A;

        double[] vertexDisplacement = { A * Math.cos(Math.PI * transverseAxisInclination / 180.0), A * Math.sin(Math.PI * transverseAxisInclination / 180.0)};
        double[] focalDisplacement = { E * vertexDisplacement[0], E * vertexDisplacement[1] };
        PointXY F1 = center.sum(new PointXY(focalDisplacement));
        PointXY F2 = center.difference(new PointXY(focalDisplacement));
        PointXY V1 = center.sum(new PointXY(vertexDisplacement));
        PointXY V2 = center.difference(new PointXY(vertexDisplacement));        

        putFeature("focalLength", new NumberModel(C));
        putFeature("eccentricity", new NumberModel(E));
        putFeature("focus", F1);
        addToFeature("focus", F2);
        putFeature("vertex", V1);
        addToFeature("vertex", V2);
        
        putFeature("transverseAxis", QuadraticClassifier.getEquationOfALine(center, transverseAxisInclination, vars));
        putFeature("conjugateAxis", QuadraticClassifier.getEquationOfALine(center, conjugateAxisInclination, vars));
        putFeature("asymptotes", QuadraticClassifier.getEquationOfALine(center, alpha + asymptoteInclination, vars));
        addToFeature("asymptotes", QuadraticClassifier.getEquationOfALine(center, alpha - asymptoteInclination, vars));
        addDomainAndRange();
        
     
        //System.out.println(getXMLString());
        //getAsymptotes();
    } // end SolvedHyperbola
    
    
    private void addDomainAndRange() {
    	String[] asymptotes  = getAsymptotes();
    	if(asymptotes[0].contains("y")){
    		String D = getDomain(asymptotes[1]);
        	String R = getRange(asymptotes[0]);
        	putFeature("domain", D);
        	putFeature("range", R);
    	}else{
    		String D = getDomain(asymptotes[0]);
        	String R = getRange(asymptotes[1]);
        	putFeature("domain", D);
        	putFeature("range", R);
    	}
    	
    	
    	
	}


    private String getDomain(String string) {
		Solver solver = new Solver();
		solver.add(string);
	    solver.solve();   
	    
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
	    return ("{x such that x is all real numbers except where x = "+ ((SolvedLine) features).getXIntercept() + "}");
	}
    
	private String getRange(String string) {
		Solver solver = new Solver();
		solver.add(string);
	    solver.solve();   
	    	    
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
	    return ("{y such that y is all real numbers except where y = "+ ((SolvedLine) features).getYIntercept() + "}");
	}

	@SuppressWarnings("unused")
	private double getSlope(String string)
    {
    	Solver solver = new Solver();
        solver.add(string);
    	solver.solve();   
    	Solution solution = solver.get(0);
    	SolvedGraph features = solution.getFeatures();
    	double y= ((SolvedLine)features).getYIntercept();
    	solver.removeAll();
    	return y;
    }
    
    public PointXY getVertex() {
		Object value = this.getValue(VertexFeature.PATH, VertexFeature.KEY);
		String vertexString = (String)value;
		//System.out.println("Getting vertex.\nVertex is : " + vertexString);
		String[] split = vertexString.split(",");
		split[0] = split[0].replace("(", "");
		split[1] = split[1].replace(")", "");
		double xPos = Double.valueOf(split[0]);
		double yPos = Double.valueOf(split[1]);
		
		return (new PointXY(xPos,yPos));
	}

	public String[] getAsymptotes() {
		Object values = this.getValues(AsymptoteFeature.PATH, AsymptoteFeature.KEY);
		ArrayList<?> list = (ArrayList<?>)values;
		//System.out.println("The size of the returned array is"+list.size());
		String[] asymptotes = new String[list.size()];
		for(int i=0;i<list.size();i++)
		{
			//System.out.println(list.get(i));
			asymptotes[i]=(String) list.get(i);
		}
		
		return asymptotes;
	}
    
} // end class SolvedHyperbola
