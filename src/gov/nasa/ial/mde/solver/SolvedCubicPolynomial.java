package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

public class SolvedCubicPolynomial extends SolvedRationalFunction {


	protected String[] newFeatures = {};

	private IntervalXY D, R;

	public SolvedCubicPolynomial(AnalyzedEquation equation) {
		super(equation);
		//double[] coeffs = QC.getNormalizedCoefficients();
		
		IntervalXY D, R; // domain and range
		//This works to set the domain and range
		
		D = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
        R = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        R.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
        putFeature("domain", D);
        putFeature("range", R);
        
	}
	
	public IntervalXY getD() {
		return D;
	}

	public IntervalXY getR() {
		return R;
	}
	
	public String getClassName(){
		return this.getClassName();
	}
	
	
	
	/*
	public static void main(String[] args){
		MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);
        
        String inputEquation = "y = x^3 + 2x^2 + x - 2";
     // Give Solver equation and solve
        
        AnalyzedEquation analyzedEquation = solver.add(inputEquation);
        SolvedCubicPolynomial solved = new SolvedCubicPolynomial(analyzedEquation);
        
        solved.getD();
        
        System.out.println(solved.getD().toString());
        
        solver.solve();
     
        
        
     //   
        
        if (solver.anyDescribable()) {
            String description = describer.getDescriptions("standards");
            System.out.println("Description: " + description);
        } else {
            System.out.println("MDE could not generate a description for "
                            + inputEquation + ".");
        }
        
        
        
        
        
        
        
        
        solver.removeAll();
        
	}*/
}
