package gov.nasa.ial.mde.solver.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RegexTest {

	private static final String REGEX = "(-?\\d*\\.?\\d*)";
	
	public static void main(String[] args){

		try {
			
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(isr);
			
			while(true){
				System.out.println("\n\nEnter equation (or CTRL-C to exit): ");
				String equation = reader.readLine();
				
				System.out.println(equation.matches(REGEX));
			}
		}
		catch(Exception e){
			System.out.println("Oh noes.");
		}
		
	}
}
