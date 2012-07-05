package gov.nasa.ial.mde.solver.tests;

import gov.nasa.ial.mde.solver.SolvedAbsoluteValue;
import gov.nasa.ial.mde.solver.SolvedCubicPolynomial;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.SolvedHyperbola;
import gov.nasa.ial.mde.solver.SolvedLine;
import gov.nasa.ial.mde.solver.SolvedParabola;
import gov.nasa.ial.mde.solver.SolvedSineFunction;
import gov.nasa.ial.mde.solver.SolvedSquareRoot;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.solver.classifier.MDEClassifier;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

import org.junit.After;
import org.junit.Before;

import junit.framework.TestCase;

public class SolverTest extends TestCase {
	
	private String[][] linearFormulas = {
	{ // good ones
		"y=x",
		"y=x+3",
		"y=x+5",
		"y-x=0",
		"y/5-3x=0",
	}, 
	{ // bad ones
		"y=x*x",
		"y=x+3*x*x",
		"y=4*x+x*x",
	}
	};
	
	private String[][] parabolaFormulas = {
			{ // good ones
				"y=x*x",
				"y=x*x*3",
				"y=x*x*2.5",
			},
			{ // bad ones
				"y=x*0",
				"y=x*3",
			}
	};
	
	private String[][] sineFunctions = {
			{ // good ones
				"y=4/3*sin(x)",
				"y=sin(x-4)",
				"y=sin(pi*x-4)+3/5",
			},
			{ // bad ones
				"y=x",
				"y=x*x+3",
				"y=cos(x)"
			}
	};
	
	
	
	
	private String[][] hyperbolaFunctions = {
			{ // good ones
				"y=1/(-3x+2) + 5",
				"y-1/x=0",
				"1/y+x=0",
			},
			{ // bad ones
				"y=0",
				"y=x*x+3",
				"y=cos(x)",
			}
	};
	
	private String[][] sqrtFunctions= {
			{//good
				"y=sqrt(x)",
				"y=4*sqrt(x)",
				"y=4/3*sqrt(x)",
				//"3y=x^3-4x^2-3x",
				//"5y-x^3+3x=0",
				//"y/3-(x^3)/5=7",
				//"y=x^4/x"
			},
			{//bad
				//"y=x^3/x",
				"y=x^5",
				"y=0",
				//"y=4-x",
			}
	};
	
	
	private String[][] cubicFunctions = {
			{//good
				"y=x^3",
				"y=x^3+4",
				"y=x^3+x^2"
				//"3y=x^3-4x^2-3x",
				//"5y-x^3+3x=0",
				//"y/3-(x^3)/5=7",
				//"y=x^4/x"
			},
			{//bad
				//"y=x^3/x",
				"y=x^5",
				"y=0",
				//"y=4-x",
			}
	};
	
	private String[][] absoluteValueFunctions = {
			{//good
				"y=abs(x)+4",
				"y=4/3*abs(x/3+3)+7"
			},
			{//bad
				"y=34x",
				"y=x",
				"y+3=x"
			}
	};
	
		
	private String[][][] formulas = {
			linearFormulas,
			parabolaFormulas,
	};

	
	@Before
	public void setup() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	private SolvedGraph loadSolvedGraph(String equation) {
		//System.out.println(equation);
		Solver solver = new Solver();
		
		AnalyzedEquation analyzedEquation = solver.add(equation);
		//AnalyzedEquation analyzedEquation = new AnalyzedEquation(equation);
		
		//adding solve apparently changes everything
		solver.solve();
		
		
		MDEClassifier classifier = analyzedEquation.getClassifier();
		SolvedGraph solvedGraph = classifier.getFeatures(analyzedEquation);
		return solvedGraph;
	}
	
	
	private void equationMatches(boolean expected, String expectedClassName, String[] formulas) {
		String actualClassName;
		SolvedGraph solvedGraph;
		for(String formula: formulas) {
			solvedGraph = this.loadSolvedGraph(formula);
			actualClassName = solvedGraph.getClass().getCanonicalName();
			this.classMatch(expected, actualClassName, expectedClassName, formula);
		}
	}
	
	private void classMatch(boolean expected, String actualClassName, String expectedClassName, String formula) {
		//System.out.println( expectedClassName  + " || " + actualClassName);
		boolean actual = expectedClassName.equals(actualClassName);
		assertEquals(expectedClassName+" for "+formula, expected, actual);
	}
	
	public void testLinearMatch() {
		String[] goodLinearFormulas = this.linearFormulas[0];
		String[] badLinearFormulas = this.linearFormulas[1];
		String name = SolvedLine.class.getCanonicalName();
		this.equationMatches(true, name, goodLinearFormulas);
		this.equationMatches(false, name, badLinearFormulas);
	}
	
	public void testParabolaMatch() {
		String[] goodParabolaFormulas = this.parabolaFormulas[0];
		String[] badParabolaFormulas = this.parabolaFormulas[1];
		String name = SolvedParabola.class.getCanonicalName();
		this.equationMatches(true, name, goodParabolaFormulas);
		this.equationMatches(false, name, badParabolaFormulas);		
	}
	
	public void testSineMatch() {
		String[] goodSineFormulas = this.sineFunctions[0];
		String[] badSineFormulas = this.sineFunctions[1];
		String name = SolvedSineFunction.class.getCanonicalName();
		this.equationMatches(true, name, goodSineFormulas);
		this.equationMatches(false, name, badSineFormulas);				
	}
	
	public void testHyperbolaMatch() {
		String[] goodHyperbolaFormulas = this.hyperbolaFunctions[0];
		String[] badHyperbolaFormulas = this.hyperbolaFunctions[1];
		String name = SolvedHyperbola.class.getCanonicalName();
		this.equationMatches(true, name, goodHyperbolaFormulas);
		this.equationMatches(false, name, badHyperbolaFormulas);				
	}

	
	public void testSqrtMatch() {
		String[] goodSqrtFormulas = this.sqrtFunctions[0];
		String[] badSqrtFormulas = this.sqrtFunctions[1];
		String name = SolvedSquareRoot.class.getCanonicalName();
		this.equationMatches(true, name, goodSqrtFormulas);
		this.equationMatches(false, name, badSqrtFormulas);				
	}
	
	
	public void testCubicMatch() {
		String[] goodCubicFormulas = this.cubicFunctions[0];
		String[] badCubicFormulas = this.cubicFunctions[1];
		String name = SolvedCubicPolynomial.class.getCanonicalName();
		//System.out.println("--------" + name);
		this.equationMatches(true, name, goodCubicFormulas);
		this.equationMatches(false, name, badCubicFormulas);				
	}
	
	public void testAbsMatch(){
		String[] goodAbsFormulas = this.absoluteValueFunctions[0];
		String[]  badAbsFormulas = this.absoluteValueFunctions[1];
		String name  = SolvedAbsoluteValue.class.getCanonicalName();
		this.equationMatches(true, name, goodAbsFormulas);
		this.equationMatches(false, name, badAbsFormulas);
	}
	
	
	private void dump() {
		for(String[][] saa : this.formulas) {
			for(String[] sa : saa) {
				for(String formula : sa) {
					SolvedGraph graph = this.loadSolvedGraph(formula);
					String className = graph.getClass().getCanonicalName();
					System.out.println(formula + " has class "+className);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		SolverTest test = new SolverTest();
		test.dump();
	}
}
