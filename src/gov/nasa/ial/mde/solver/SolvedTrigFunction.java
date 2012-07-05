package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

public class SolvedTrigFunction extends SolvedXYGraph{
	
	
	
	public SolvedTrigFunction(AnalyzedEquation e) {
		super(e);
	}
	
	public SolvedTrigFunction(AnalyzedEquation analyzedEquation, String graphName) {
		
		super(analyzedEquation, graphName);
	}
	
	protected static boolean isMultipleOfPi(double coeff) {	
		
		//TODO: Make this detection better!
		return (coeff% 3.14<=0.01);
	}
	
	public static void main(String[] args){
		
		System.out.println(isMultipleOfPi(3.142));
		
		System.out.println(isMultipleOfPi(7));
		
		System.out.println(isMultipleOfPi(3.142));
		System.out.println(isMultipleOfPi(6.284));
		System.out.println(isMultipleOfPi(15.408));
		
	}
}
