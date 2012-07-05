package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.solver.classifier.PolynomialClassifier;
import gov.nasa.ial.mde.solver.features.individual.VertexFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

public class SolvedAbsoluteValue extends SolvedXYGraph implements VertexFeature{
	
	protected String[] newFeatures = { 
            "vertex", "absDirection"};
	
	protected PolynomialClassifier PC;

	public SolvedAbsoluteValue(AnalyzedEquation ae) {
		super(ae, "absolute value");
		PC = (PolynomialClassifier) ae.getClassifier();
		
		
		double A = 1;
	    double B = Double.NaN;
	    double C = Double.NaN;
	    double D = 0;
	    
	    String getCoeff = "y=(-?\\d*[\\./]?\\d*)\\*abs\\([^)\\n]*\\)([\\+-]\\d*[\\./]?\\d*)?";
		String insideABS = "abs\\(([^)\\n]*)\\)";
		String getOffset = "abs\\([^)\\n]*\\)([\\+\\-]\\d*[\\./]?\\d*)";
		
		String equat = ae.getInputEquation();
		//System.out.println(equat);
		equat = equat.trim();
		equat = equat.replaceAll("-abs", "-1*abs");
		//System.out.println(equat);
		
		
		String innerEquat = equat.replaceAll(insideABS, "____$1____");
		//System.out.println("     Abs: " + innerEquat);
		innerEquat = "y= " + innerEquat.split("____")[1];
		//System.out.println("     Abs: " + innerEquat);
		
		Solver solver = new Solver();
		solver.add(innerEquat);
	    solver.solve();   
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
	    
	    if(features instanceof SolvedLine){		    
		    //for a linear equation 
		    //form of a*|bx+c| + d
		    // vertex is ( -ac/b, d)
		    // slope of ab
	    	
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
		//	System.out.println("  Offset: " + offsetString);
			if(offsetString.contains("____")){
				offsetString = offsetString.split("____")[1];
				if(offsetString.contains("/")){
					String[] fraction= offsetString.split("/");
					D = Double.valueOf(fraction[0])/Double.valueOf(fraction[1]);
				}else{
					D = Double.valueOf(offsetString);
				}
	    	}
		    
		    
	    	//double slope =A*B;
		    //double intercept= D;
		    double xVertice = -A*C/B;
		    double yVertice = D;
		    
	    	IntervalXY domain, range;
	    	PointXY vertex = new PointXY( new double[]{xVertice,yVertice});
	    	domain = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	    	
	    	String direction;
	    	if(A>0){
                range = new IntervalXY(analyzedEq.getActualVariables()[1], yVertice, Double.POSITIVE_INFINITY);
                direction="up";
	    	}else{
	    		range = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, yVertice);
                direction="down";
	    	}
	    	
	    	
	    	putNewFeatures(newFeatures);
			putFeature("vertex", vertex);
			putFeature("absDirection", direction);
			putFeature("domain", domain);
			putFeature("range", range);
	    	
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
