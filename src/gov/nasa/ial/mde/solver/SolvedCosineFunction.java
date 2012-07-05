package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.solver.classifier.TrigClassifier;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.features.individual.AmplitudeFeature;
import gov.nasa.ial.mde.solver.features.individual.FrequencyFeature;
import gov.nasa.ial.mde.solver.features.individual.OffsetFeature;


import gov.nasa.ial.mde.solver.features.individual.PhaseFeature;
import gov.nasa.ial.mde.util.MathUtil;


public class SolvedCosineFunction extends SolvedTrigFunction implements FrequencyFeature, AmplitudeFeature, PhaseFeature, OffsetFeature{
	
	protected String[] newFeatures = {"frequency" , "amplitude", "phase", "offset", "period"};
	
	protected TrigClassifier TC;
	
	
	
	public SolvedCosineFunction(AnalyzedEquation analyzedEquation) {
		super(analyzedEquation, "cosine function");
		// for a basic sinusoid y=A*cos(Bx+C)+D
	    // amplitude = A
	    // period = 2*pi / |B|
	    // frequency = |B| / 2*pi
	    // phase shift = C/B ???
	    // offset = D
		
		
		double A = 1;
	    double B = Double.NaN;
	    double C = Double.NaN;
	    double D = 0;
	    
	    double amplitude = Double.NaN;
		String period= null;
		String frequency = null;
		double phase = Double.NaN;
		double offset = Double.NaN;
		IntervalXY domain = null; // domain
		IntervalXY range = null; // Range
		
		
		
		
		
		
		String getCoeff = "y=(-?\\d*[\\./]?\\d*)\\*cos\\([^)\\n]*\\)([\\+-]\\d*[\\./]?\\d*)?";
		String insideCOS = "cos\\(([^)\\n]*)\\)";
		String getOffset = "cos\\([^)\\n]*\\)([\\+\\-]\\d*[\\./]?\\d*)";
		
		TC  = (TrigClassifier) analyzedEquation.getClassifier();
		
		String equat = analyzedEquation.getInputEquation();
		//System.out.println(equat);
		equat = equat.trim();
		equat = equat.replaceAll("-cos", "-1*cos");
		//System.out.println(equat);
		
		String innerEquat = equat.replaceAll(insideCOS, "____$1____");
		//System.out.println("     Cos: " + innerEquat);
		innerEquat = "y= " + innerEquat.split("____")[1];
		//System.out.println("     Cos: " + innerEquat);
		
		Solver solver = new Solver();
		solver.add(innerEquat);
	    solver.solve();   
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
		
		
	    
	    if(features instanceof SolvedLine){
	    	String coeff = equat.replaceAll(getCoeff, "____$1____");
		//	System.out.println("   Coeff: " + coeff);
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
		//	System.out.println("  Offset: " + offsetString);
			
			
		//	System.out.println("       A: " + A);
		//	System.out.println("       B: " + B);
		//	System.out.println("       C: " + C);
		//	System.out.println("       D: " + D);
			
			
			
			//TODO:  Create a method to give a more well define value, such as 2/3 pi or 5/6 pi or 1/4
			phase = -C/B;  	    	
			amplitude = A;
			
			
			//TODO: Formatting
	    	//period = 2.0 /(Math.round((Math.abs(B*4)))/4.0) +"pi"; //2*pi/b
	    	//frequency =(((Math.round((Math.abs(B)) *4))/ 4.0))/2.0 + "/pi"; //b/2pi
	    	
			if(isMultipleOfPi(B)){
				period = MathUtil.trimDouble((2*3.142)/Math.abs(B), 3)+"";
				frequency = MathUtil.trimDouble(Math.abs(B)/(2*3.142) ,3)+"";
			}
			else{
				period = MathUtil.trimDouble(2/Math.abs(B), 3)+"pi";
				frequency = MathUtil.trimDouble(Math.abs(B)/2, 3) + "/pi";
			}
			
	    	
			offset = D;
	    	
			
	    	
	 	    domain = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	 	    range = new IntervalXY(analyzedEq.getActualVariables()[1], - Math.abs(amplitude) + offset, Math.abs(amplitude)+offset);
	    	
	    	putNewFeatures(newFeatures);    	
	    	putFeature("amplitude", MathUtil.trimDouble(amplitude, -1));
	    	putFeature("phase", MathUtil.trimDouble(phase, -1));
	    	putFeature("offset", MathUtil.trimDouble(offset, -1));
	    	putFeature("frequency", frequency);
	    	putFeature("period", period);
	    	putFeature("domain", domain);
	    	putFeature("range", range);
	    }
		
        
	}




	public String getFrequency() {
		Object value = this.getValue(FrequencyFeature.PATH, FrequencyFeature.KEY);
		String frequencyString = (String)value;
		System.out.println("Getting frequency.\nFrequency is : " + frequencyString);
		return frequencyString;
	}
	

	public double getAmplitude() {
		Object value = this.getValue(AmplitudeFeature.PATH, AmplitudeFeature.KEY);
		Double doubleValue = new Double((String)value);	
		System.out.println("Getting Amplitude.\nAmplitude is : " + doubleValue);
		return doubleValue;
	}


	public double getOffset() {
		Object value = this.getValue(OffsetFeature.PATH, OffsetFeature.KEY);
		Double doubleValue = new Double((String)value);	
		System.out.println("Getting Offset.\nOffset is : " + doubleValue);
		return doubleValue;
	}


	public double getPhase() {
		Object value = this.getValue(PhaseFeature.PATH, PhaseFeature.KEY);
		Double doubleValue = new Double((String)value);
		System.out.println("Getting Phase.\nPhase is : " + doubleValue);
		return doubleValue;
	}
}
