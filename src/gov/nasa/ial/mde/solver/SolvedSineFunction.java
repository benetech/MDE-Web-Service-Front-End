package gov.nasa.ial.mde.solver;


import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.solver.classifier.TrigClassifier;
import gov.nasa.ial.mde.solver.features.individual.AmplitudeFeature;
import gov.nasa.ial.mde.solver.features.individual.FrequencyFeature;
import gov.nasa.ial.mde.solver.features.individual.OffsetFeature;
import gov.nasa.ial.mde.solver.features.individual.PeriodFeature;
import gov.nasa.ial.mde.solver.features.individual.PhaseFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.util.MathUtil;

public class SolvedSineFunction extends SolvedTrigFunction implements FrequencyFeature, AmplitudeFeature, PhaseFeature, OffsetFeature, PeriodFeature{

	protected String[] newFeatures = {"frequency" , "amplitude", "phase", "offset", "period"};
	
	protected TrigClassifier TC;
	
		
	public SolvedSineFunction(AnalyzedEquation analyzedEquation) {
		super(analyzedEquation, "sine function");
		// for a basic sinusoid y=A*sin(Bx+C)+D
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
		
	
		
		String getCoeff = "y=(-?\\d*[\\./]?\\d*)\\*sin\\([^)\\n]*\\)([\\+-]\\d*[\\./]?\\d*)?";
		String insideSIN = "sin\\(([^)\\n]*)\\)";
		String getOffset = "sin\\([^)\\n]*\\)([\\+\\-]\\d*[\\./]?\\d*)";
		
		TC  = (TrigClassifier) analyzedEquation.getClassifier();
		
		String equat = analyzedEquation.getInputEquation();
		//System.out.println(equat);
		equat = equat.trim();
		equat = equat.replaceAll("-sin", "-1*sin");
		//System.out.println(equat);
		
		String innerEquat = equat.replaceAll(insideSIN, "____$1____");
		//System.out.println("    Sine: " + innerEquat);
		innerEquat = "y= " + innerEquat.split("____")[1];
		//System.out.println("    Sine: " + innerEquat);
		
		Solver solver = new Solver();
		solver.add(innerEquat);
	    solver.solve();   
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
		
		
	    
	    if(features instanceof SolvedLine){
	    	String coeff = equat.replaceAll(getCoeff, "____$1____");
	//		System.out.println("   Coeff: " + coeff);
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
//			System.out.println("  Offset: " + offsetString);
			if(offsetString.contains("____")){
				offsetString = offsetString.split("____")[1];
				if(offsetString.contains("/")){
					String[] fraction= offsetString.split("/");
					D = Double.valueOf(fraction[0])/Double.valueOf(fraction[1]);
				}else{
					D = Double.valueOf(offsetString);
				}
	    	}
//			System.out.println("  Offset: " + offsetString);
			
			
//			System.out.println("       A: " + A);
//			System.out.println("       B: " + B);
	//		System.out.println("       C: " + C);
	//		System.out.println("       D: " + D);
			
			
			
			//TODO:  Create a method to give a more well define value, such as 2/3 pi or 5/6 pi or 1/4
			phase = -C/B;  	    	
			amplitude = A;
			
	
			
			
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
	
	public String getPeriod() {
		Object value = this.getValue(PeriodFeature.PATH, PeriodFeature.KEY);
		String periodString = (String)value;
		System.out.println("Getting period.\nPeriod is : " + periodString);
		return periodString;
	}

	public double getAmplitude() {
		Object value = this.getValue(AmplitudeFeature.PATH, AmplitudeFeature.KEY);
		Double doubleValue = new Double((String)value);	
		System.out.println("Getting Amplitude.\nAmplitude is : " + doubleValue);
		return doubleValue;
	}

	public double getPhase() {
		Object value = this.getValue(PhaseFeature.PATH, PhaseFeature.KEY);
		Double doubleValue = new Double((String)value);
		System.out.println("Getting Phase.\nPhase is : " + doubleValue);
		return doubleValue;
	}

	public double getOffset() {
		Object value = this.getValue(OffsetFeature.PATH, OffsetFeature.KEY);
		Double doubleValue = new Double((String)value);	
		System.out.println("Getting Offset.\nOffset is : " + doubleValue);
		return doubleValue;
	}

	public static void main(String[] args){
		//TODO USE EXPRESSION THE EVALUATE A AND D
		
		String getCoeff = "y=(-?\\d*[\\./]?\\d*)\\*sin\\([^)\\n]*\\)([\\+-]\\d*[\\./]?\\d*)?";
		//String insideSIN = "sin\\(([^)\\n]*)\\)";
		//String getOffset = "sin\\([^)\\n]*\\)([\\+\\-]\\d*[\\./]?\\d*)";
		//String all = "(-?\\d*[\\./]?\\d*)\\*sin\\(([^)\\n]*)\\)";
		String[] test = {"y= sin( x)","y=-sin(4 * x)", "y= sin (x)", "y=-3 * sin(4*x+20)", "y=-4.3*sin(4*x+432)+9"," y=5 /3 * sin( x/3)-4"};
		
		for(int i= 0; i<test.length; i++){
			test[i] = test[i].replaceAll(" ", "");
			test[i] = test[i].replaceAll("-sin", "-1*sin");
			System.out.println("\nTest case "+i);
			System.out.println("Equation: " + test[i]);
			System.out.println("   Coeff: " + test[i].replaceAll(getCoeff, "____ $1 ____"));
			//System.out.println("    Sine: " + test[i].replaceAll(insideSIN, "____ $1 ____"));
			//System.out.println("  Offset: " + test[i].replaceAll(getOffset, "____ $1 ____"));
			//System.out.println("     All: " + test[i].replaceAll(all, "____ $1 ____ $2 ____"));
		}
	}	
}
