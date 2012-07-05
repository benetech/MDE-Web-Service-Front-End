package gov.nasa.ial.mde;
import gov.nasa.ial.mde.describer.Describer;


import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.SolvedCubicPolynomial;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;


public class NewClassTest {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
		Solver solver = new Solver();
		Describer describer = new Describer(solver, currentSettings);
		describer.setOutputFormat(Describer.TEXT_OUTPUT);
		
        String inputEquation = "y =x^3";
     // Give Solver equation and solve
        
        AnalyzedEquation analyzedEquation = solver.add(inputEquation);        
        
        SolvedCubicPolynomial solved = new SolvedCubicPolynomial(analyzedEquation);  
        solved.getD();
        
        System.out.println(analyzedEquation);
        
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
        
	}
}
