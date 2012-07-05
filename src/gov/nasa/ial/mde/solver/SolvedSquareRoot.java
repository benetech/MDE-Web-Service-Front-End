package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.classifier.PolynomialClassifier;
import gov.nasa.ial.mde.solver.features.individual.VertexFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

public class SolvedSquareRoot extends SolvedXYGraph implements VertexFeature {

	protected String[] newFeatures = {"vertex" , "orientation"};
	
	protected PolynomialClassifier PC;

	public SolvedSquareRoot(AnalyzedEquation ae) {
		super(ae, "square root");
		
		
		//can be solved by making some assumptions	
		PC = (PolynomialClassifier) ae.getClassifier();
		double A = 1;
	    double B = Double.NaN;
	    double C = Double.NaN;
	    double D = 0;
		
	    
	    
	    String getCoeff = "y=(-?\\d*[\\./]?\\d*)\\*sqrt\\([^)\\n]*\\)([\\+-]\\d*[\\./]?\\d*)?";
		String insideSQRT = "sqrt\\(([^)\\n]*)\\)";
		String getOffset = "sqrt\\([^)\\n]*\\)([\\+\\-]\\d*[\\./]?\\d*)";
	    
		String equat = ae.getInputEquation();
		//System.out.println(equat);
		equat = equat.trim();
		equat = equat.replaceAll("-sqrt", "-1*sqrt");
		//System.out.println(equat);
		
		
		
		
		//Get the inner part of the SQRT( _______)
		String innerEquat = equat.replaceAll(insideSQRT, "____$1____");
		//System.out.println("    SQRT: " + innerEquat);
		innerEquat = "y= " + innerEquat.split("____")[1];
		//System.out.println("    SQRT: " + innerEquat);
		
		
		//Send that part thru the MDE
		Solver solver = new Solver();
		solver.add(innerEquat);
	    solver.solve();   
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
	    
		
		
		
	    
		
	    if(features instanceof SolvedLine){
	    	//get Slope and "yIntercept"
		    
		    String coeff = equat.replaceAll(getCoeff, "____$1____");
			//System.out.println("   Coeff: " + coeff);
			if(coeff.contains("____")){
				coeff= coeff.split("____")[1];
				if(coeff.contains("/")){
					String[] fraction= coeff.split("/");
					A = Double.valueOf(fraction[0])/Double.valueOf(fraction[1]);
				}else{
					A = Double.valueOf(coeff);
				}
				
	    	}
			
			B = ((SolvedLine) features).getSlope();	
		    C = ((SolvedLine) features).getYIntercept();
			
		    String offsetString = equat.replaceAll(getOffset, "____$1____");
			//System.out.println("  Offset: " + offsetString);
			if(offsetString.contains("____")){
				offsetString = offsetString.split("____")[1];
				if(offsetString.contains("/")){
					String[] fraction= offsetString.split("/");
					D = Double.valueOf(fraction[0])/Double.valueOf(fraction[1]);
				}else{
					D = Double.valueOf(offsetString);
				}
	    	}
		    
			double slope=B;
		   // double intercept =C;
		    double xVertice = -C/A;
		    double yVertice = D;
	    		    	
	    	IntervalXY domain, range;
	    	
	    	String orientation;
	    	if(A>0 && slope>0){
	    		orientation="quadrant I";
	    		domain = new IntervalXY(analyzedEq.getActualVariables()[0], xVertice, Double.POSITIVE_INFINITY);
	    		//D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
                range = new IntervalXY(analyzedEq.getActualVariables()[1], yVertice, Double.POSITIVE_INFINITY);
	    	}else if(A>0 && slope<0){
	    		orientation="quadrant II";
	    		domain = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, xVertice);
	    		//D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
	    		range = new IntervalXY(analyzedEq.getActualVariables()[1], yVertice, Double.POSITIVE_INFINITY);
	    	}else if(A<0 && slope>0){
	    		orientation="quadrant IV";
	    		domain = new IntervalXY(analyzedEq.getActualVariables()[0], xVertice, Double.POSITIVE_INFINITY);
	   
                range = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, yVertice);
	    	}else if(A<0 && slope<0){
	    		orientation="quadrant III";
	    		domain = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, xVertice);
                range = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, yVertice);
	    	}
	    	else{
	    		orientation="you should never be here.";
	    		domain = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NaN, Double.NaN);
                range = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NaN , Double.NaN);
	    	}
	    	
	    	
	    	PointXY vertex = new PointXY( new double[]{xVertice,yVertice});
			//System.out.println(vertex.toString());
			
	    	putNewFeatures(newFeatures);
			putFeature("vertex", vertex);
			putFeature("orientation", orientation);
			putFeature("domain", domain);
			putFeature("range", range);
			//System.out.println(getVertex());
	    }else
	    {
	    	
	    	System.out.println("sqrt does not have a linear function inside it");
	    	//TODO: figure out a way to get a more general description instead.
	    }	
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
}
