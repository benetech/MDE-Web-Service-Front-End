package session;
import gov.nasa.ial.mde.describer.Describer;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;

import java.util.Vector;

public class MathEquationBean {
	    Vector<String> v = new Vector<String>();
	    String submit = null;
	    String item = null;

	    private void addItem(String name) {
	        v.addElement(name);
	    }

	    private void removeItem(String name) {
	        v.removeElement(name);
	    }

	    public void setItem(String name) {
	        item = name;
	    }

	    public void setSubmit(String s) {
	        submit = s;
	    }

	    public String[] getItems() {
	        String[] s = new String[v.size()];
	        v.copyInto(s);
	        return s;
	    }
	    public String getMathDescription(String equation) {
	    	String description = null;
	    	MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
	        Solver solver = new Solver();
	        Describer describer = new Describer(solver, currentSettings);
	        describer.setOutputFormat(Describer.TEXT_OUTPUT);
	        
	        solver.add(equation);
            solver.solve();
            //solver.get(0).getAnalyzedItem().getFeatures();
            if (solver.anyDescribable())
                description = describer.getDescriptions("standards");
            else 
            	description = "Equation `" + equation + " ` is not supported by MDE.";
            solver.removeAll(); 
	    	return description;
	    }

	    public void processRequest() {
	        // null value for submit - user hit enter instead of clicking on
	        // "add" or "remove"
	        if (submit == null || submit.equals("add"))
	            addItem(item);
	        else if (submit.equals("remove"))
	            removeItem(item);

	        // reset at the end of the request
	        reset();
	    }

	    // reset
	    private void reset() {
	        submit = null;
	        item = null;
	    }
}
