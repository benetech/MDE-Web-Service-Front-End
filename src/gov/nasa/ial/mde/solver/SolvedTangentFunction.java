package gov.nasa.ial.mde.solver;

import java.util.ArrayList;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.solver.classifier.TrigClassifier;
import gov.nasa.ial.mde.solver.features.individual.AsymptoteFeature;
import gov.nasa.ial.mde.solver.features.individual.FrequencyFeature;
import gov.nasa.ial.mde.solver.features.individual.OffsetFeature;
import gov.nasa.ial.mde.solver.features.individual.PeriodFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.util.MathUtil;

@SuppressWarnings("all")
public class SolvedTangentFunction extends SolvedTrigFunction implements PeriodFeature, OffsetFeature, AsymptoteFeature{

	protected String[] newFeatures = {"frequency", "phase", "offset", "shift", "period", 
			"orientation", "asymptotes", "rate"};

	protected TrigClassifier TC;
	
	public SolvedTangentFunction(AnalyzedEquation analyzedEquation) {
		super(analyzedEquation, "tangent function");
		

		double A = 1;
	    double B = Double.NaN;
	    double C = Double.NaN;
	    double D = 0;
		double phase = Double.NaN;
		double offset = Double.NaN;
		String baseAsymptote = "";
		String period = "";
		String frequency = "";
		double amplitude = Double.NaN;
		String orientation = "WARRRGL!";
		String interval= "";
		    
		String domain = ""; // domain
		IntervalXY range = null; // Range
		
		// TODO improve the spliting 
		
		String getCoeff = "y=(-?\\d*[\\./]?\\d*)\\*tan\\([^)\\n]*\\)([\\+-]\\d*[\\./]?\\d*)?";
		String insideTAN = "tan\\(([^)\\n]*)\\)";
		String getOffset = "tan\\([^)\\n]*\\)([\\+\\-]\\d*[\\./]?\\d*)";
		
		TC  = (TrigClassifier) analyzedEquation.getClassifier();
		
		String equat = analyzedEquation.getInputEquation();
		//System.out.println(equat);
		equat = equat.trim();
		equat = equat.replaceAll("-tan", "-1*tan");
		//System.out.println(equat);
		
		String innerEquat = equat.replaceAll(insideTAN, "____$1____");
		//System.out.println("     Tan: " + innerEquat);
		innerEquat = "y= " + innerEquat.split("____")[1];
		//System.out.println("     Tan: " + innerEquat);
		
		Solver solver = new Solver();
		solver.add(innerEquat);
	    solver.solve();   
	    Solution solution = solver.get(0);
	    SolvedGraph features = solution.getFeatures();
	    
	    if(features instanceof SolvedLine)
		{
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
			
			
			//B affects the period
		    //B = 1 creates asymptotes at -pi/2 and pi/2 
		    //B = pi creates asymptotes at -.5 and .5
			
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
			
			/*
			System.out.println("       A: " + A);
			System.out.println("       B: " + B);
			System.out.println("       C: " + C);
			System.out.println("       D: " + D);
			*/
			
			//System.out.println("  Offset: " + offsetString);
			
			
			if(Math.signum(A*B)==1){
		    	orientation = "ascending";
		    }
		    else if(Math.signum(A*B)==-1){
		    	orientation = "decending";
		    }
		    else{
		    	orientation = "0";
		    }
			offset = D;
			
			 //pi/B
	 	    //TODO: need some better formatting for PI in B
	 	    
	 	    period = calculatePeriod(B);
	 	    //period = (Math.round((Math.abs(1.0/B)*100))/100.0) + "pi";
		    //frequency = (Math.round((Math.abs((B)*100)))/100.0) +"/pi";
		    
			//String asmptoteEquation= Math.PI*B;
			baseAsymptote = MathUtil.trimDouble(((2*B) - C/B), -1) +"";
		    
			
		    domain = "x for all real numbers except for where we encounter vertical asymptotes";
	 	    range = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		    
		    putNewFeatures(newFeatures);
	    	putFeature("offset", offset + "");
	    	putFeature("orientation", orientation);
	    	putFeature("period", period);
	    	putFeature("asymptotes", baseAsymptote);
	    	putFeature("rate",A+"");
	    	putFeature("domain", domain);
	    	putFeature("range", range);
		}  
	    
	}

	private String calculatePeriod(double coeff) {
		if(this.isMultipleOfPi(coeff)){
			double ret = coeff/3.142;
			//TODO formating
			if(ret==1.0){
				return "1";
			}
			MathUtil.trimDouble(ret, -1);
			return "1/"+ret;
			
		}else{
			if(coeff==1.0){
				return "pi";
			}
			return "pi/"+ coeff;
		}
	}

	public String getPeriod() {
		Object value = this.getValue(PeriodFeature.PATH, PeriodFeature.KEY);
		String periodString = (String)value;
		System.out.println("Getting period.\nPeriod is : " + periodString);
		return periodString;
	}
	
	public double getOffset() {
		Object value = this.getValue(OffsetFeature.PATH, OffsetFeature.KEY);
		Double doubleValue = new Double((String)value);	
		System.out.println("Getting Offset.\nOffset is : " + doubleValue);
		return doubleValue;
	}

	public String[] getAsymptotes() {
		Object values = this.getValues(AsymptoteFeature.PATH, AsymptoteFeature.KEY);
		ArrayList list = (ArrayList)values;
		System.out.println("The size of the returned array is"+list.size());
		String[] asymptotes = new String[list.size()];
		for(int i=0;i<list.size();i++)
		{
			System.out.println(list.get(i));
			asymptotes[i]=(String) list.get(i);
		}
		
		return asymptotes;
	}

	

}
