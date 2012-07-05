package gov.nasa.ial.mde.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessibleTTSConverter {

	/**
	 * @param args
	 */
	
	public static String convertCoefficients(String string){
		return string;
	}
	
	public static String convertDomainAndRange(String string){
		//TODO: get domain and range to the format "the domain|range is x|y from blank and to blanky
		
		string = replaceBrackets(string);
		string = replaceLesser(string);
		string = replaceGreater(string);
		string = replaceEquals(string);
		string = replaceMinusWithNegative(string);
		return string;
	}
	
	public static String getSets(String string){
		string = string.replaceAll("(\\{[^\\}\n]*\\})", "the set of $1 ");
		
		return string;
	}
	
	public static String replaceBrackets(String string){
		string = string.replaceAll("[{}]", "\n");
		return string;
	}
	
	public static String replacePlus(String string){
		string = string.replaceAll("\\+", " plus ");
		return string;
	}
	
	public static String replacePlusWithPositive(String string){
		string = string.replaceAll("\\+", " positive ");
		return string;
	}
	
	public static String replaceMinus(String string){
		string = string.replaceAll("-(?!\\w\\w+)", " minus ");
		string = string.replaceAll("-infinity" , "negative infinity ");
		return string;
	}
	
	public static String replaceMinusWithNegative(String string){
		string = string.replaceAll("-", "negative ");
		return string;
	}
	
	public static String replaceMultiply(String string){
		string = string.replaceAll("\\*", " times ");
		return string;
	}
	
	public static String replaceDivided(String string){
		string = string.replaceAll("/", " divided by ");
		return string;
	}
	
	public static String replaceDividedWithOver(String string){
		string = string.replaceAll("/", " over ");
		return string;
	}
	
	public static String replaceExponent(String string){
		//http://www.programmersheaven.com/2/RegexJAVA
		String caretReplacement = "";
		String numberReplacement = "";
		
		Pattern pattern = Pattern.compile("(\\^)(\\d+)");
		Matcher matcher = pattern.matcher(string);
		
		while(matcher.find()){
			if(1 == Double.valueOf(matcher.group(2))){
				caretReplacement = " to the ";
				numberReplacement = "first power ";
			}else if(2 == Double.valueOf(matcher.group(2))){
				caretReplacement = "";
				numberReplacement = " squared ";
			}else if (3 == Double.valueOf(matcher.group(2))){
				caretReplacement = "";
				numberReplacement = " cubed ";
			}else{
				caretReplacement = " to the ";
				numberReplacement = "power of " + matcher.group(2);
			}
			
			string =  string.replaceFirst("(\\^)(\\d+)", caretReplacement+ numberReplacement);
			
			/*
			System.out.println("The whole: " + matcher.group() + 
					"\nThe exponent: " + matcher.group(2));
			System.out.println("Matcher beginning: " + matcher.start()
					+ "\n Matcher ending: " + matcher.end());
			System.out.println(string.substring(matcher.start(1), matcher.end(1)));*/
		}
		
		return string;
	}
	
	public static String tokenizePlus(String string){
		string = string.replaceAll("\\+", " {plus} ");
		return string;
	}

	public static String tokenizeMinus(String string){
		string = string.replaceAll("-", " {minus} ");
		return string;
	}
	
	public static String tokenizeMultiply(String string){
		string = string.replaceAll("\\*", " {star} ");
		return string;
	}
	
	public static String tokenizeDivided(String string){
		string = string.replaceAll("/", " {slash} ");
		return string;
	}
	
	public static String tokenizeExponent(String string){
		string = string.replaceAll("\\^", " {carat} ");
		return string;
	}
	
	public static String tokenizeParentheses(String string){
		string = string.replaceAll("\\(", " {open parentheses} ");
		string = string.replaceAll("\\)", " {close parentheses} ");
		return string;
	}
	
	
	public static String replaceSqrt(String string){
		string = string.replaceAll("sqrt", " the square root of ");
		return string;
	}
	
	public static String replaceAbs(String string){
		string = string.replaceAll("abs(?\\()", "the absolute value of ");
		return string;	
	}
	
	public static String replaceGreater(String string){
		string = string.replaceAll(">=", "greater than or equal to");
		string = string.replaceAll(">", "greater than");
		return string;	
	}
	
	public static String replaceLesser(String string){
		string = string.replaceAll("<=", "less than or equal to");
		string = string.replaceAll("<", "less than");
		return string;	
	}
	
	public static String replaceEquals(String string){
		string = string.replaceAll("=", "equals");
		return string;	
	}
		
	public static void main(String[] args) {
		System.out.println(replaceExponent("x^2 = y^4"));
		
	}

}
