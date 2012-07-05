package gov.nasa.ial.mde;

import gov.nasa.ial.mde.describer.Describer;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;

public class Tutorial_MultiTest {
	
	private static String[] tests = {"x=y", "y=x^2+x", "y+x^2-4x-4=0",  "y=4*x^3 - 3*x^2", "y=-2x^3+x^2-x+1" , "x^4-5=y", 
		"y=sin(x)","y=1/x", "r=sin(theta)" , "r=sin(theta)+cos(theta)", "r=-2a/(1+cos(theta))", "r=5", "r = sin (a*theta)"};
	
	
	public static void main(String[] args) {
        // MDE Init:
        MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);
        
        for(int i=0; i<tests.length ;i++)
        {
        	solver.add(tests[i]);
        }
        
        solver.solve();
        if (solver.anyDescribable()) {
            String description = describer.getDescriptions("standards");
            System.out.println("Description: " + description);
        }
	}
}
