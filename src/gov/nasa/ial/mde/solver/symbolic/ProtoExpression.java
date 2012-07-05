/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The proto-expression.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class ProtoExpression {
    
    /** The variable strings. */
    public String[] varStrings = null;
    
    /** The root node. */
    public ParseNode root = null;
    
    /** The known values. */
    public Hashtable<String, Double> knowns = null;
    
    /** The parameters. */
    public Hashtable<String, Double> parameters = null;
    
    /** The variables. */
    public Hashtable<Object, Hashtable> variables = null;
    
    /** The legal variables. 
     * <String?, Integer?>
     * */
    public Hashtable<String, Integer> legalVariables = null;
    
    /** The value. */
    public Double theValue = null;

    /** The value as a string. */
    public String valueString = null;

    /**
     * Default constructor.
     */
    public ProtoExpression() {
        super();
    }
    
    /**
     * Sets the parameter hashtable.
     * 
     * @param h the parameter hashtable.
     */
    public void setParameterHash(Hashtable<String, Double> h) {
        int n = varStrings.length;
        ArrayList<String> t = new ArrayList<String>();

        parameters = h;
        for (int i = 0; i < n; i++)
            if (parameters.get(varStrings[i].toLowerCase()) == null)
                t.add(varStrings[i]);

        varStrings = t.toArray(new String[t.size()]);
    } // end setParameterHash

    /**
     * Evaluates the expression given the specified inputs.
     * 
     * @param inputs the input variable values.
     * @return the value of the expression.
     */
    public double evaluate(Hashtable inputs) {
        Hashtable nodeList = (variables.get(root));
        Enumeration k = nodeList.keys();

        while (k.hasMoreElements()) {
            Object key;
            Double v = parameters.get(((String) (key = k.nextElement())).toLowerCase());
            if (v == null)
                v = (Double)inputs.get(key);

            Vector nodes = (Vector)nodeList.get(key);
            Enumeration e = nodes.elements();

            while (e.hasMoreElements())
                 ((ParseNode)e.nextElement()).value = v;
        } // end while

        return root.eval();
    } // end evaluate
    
} // end ProtoExpression
