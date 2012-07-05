package gov.nasa.ial.mde.solver.classifier;

import gov.nasa.ial.mde.solver.SolvedCosineFunction;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.SolvedSineFunction;
import gov.nasa.ial.mde.solver.SolvedTangentFunction;
import gov.nasa.ial.mde.solver.SolvedTrigFunction;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

import java.util.ArrayList;

public class TrigClassifier extends MDEClassifier {
	
	private boolean hasSin = false,  
					hasCos = false, 
					hasTan = false,
					hasMultiples =false;
					//hasX = false;
	
	public ArrayList<String> arr;
	//private Polynomial lhs =null;
	
	
	/*public TrigClassifier() {
		super();
		
		
	}*/
	
	
	
	/*public TrigClassifier(Polynomial lhs) {
		this.lhs=lhs;
	}*/



	public SolvedGraph getFeatures(AnalyzedEquation analyzedEquation) {
		detectTrig(analyzedEquation);
		SolvedGraph features;
		
		if(hasMultiples)
		{
			
		}else if(hasSin)
		{
			features = new SolvedSineFunction(analyzedEquation);
			return features;
		}else if(hasCos)
		{
			features = new SolvedCosineFunction(analyzedEquation);
			return features;
		}else if(hasTan)
		{
			features = new SolvedTangentFunction(analyzedEquation);
			return features;
		}
		
		features = new SolvedTrigFunction(analyzedEquation);
		return features;
	}



	private void detectTrig(AnalyzedEquation analyzedEquation) {
		String equat = analyzedEquation.getInputEquation();
		
		hasSin = equat.contains("sin");
		hasCos = equat.contains("cos");
		hasTan = equat.contains("tan");
		
		if((hasSin && hasCos)||(hasSin && hasTan)||(hasCos && hasTan)){
			hasMultiples=true;
		}
		
		
	}
	
}
