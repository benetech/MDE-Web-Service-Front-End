package gov.nasa.ial.mde.solver.classifier;

public class BooleanTester {

	public static boolean areAllTrue(boolean ... values) {
    	return areAllWhatever(true, values);
    }
    
    public static boolean areAllFalse(boolean ... values) {
    	return areAllWhatever(false, values);
    }
    
    public static boolean areAnyTrue(boolean ... values) {
    	return areAnyWhatever(true, values);
    }
    
    public static boolean areAnyFalse(boolean ... values) {
    	return areAnyWhatever(false, values);
    }
    
    public static boolean areAllWhatever(boolean target, boolean ... values) {
    	return !areAnyWhatever(!target, values);
    }
    
    public static boolean areAnyWhatever(boolean target, boolean ... values) {
    	Boolean check = null;
    	for(boolean value : values) {
    		check = (target == value);
    		if(check) break;
    	}
    	return check;
    }
}
