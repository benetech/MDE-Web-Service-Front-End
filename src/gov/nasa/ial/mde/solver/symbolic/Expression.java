/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.util.Comparison;
import gov.nasa.ial.mde.util.MathUtil;
import gov.nasa.ial.mde.util.SortedKeyStrings;
import gov.nasa.ial.mde.util.StringSplitter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The <code>Expression</code> represents an equation expression.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Expression extends ProtoExpression implements Comparison {

    /**
     * Constructs an Expression given an expression as a String.
     * 
     * @param s expression as a String.
     */
    public Expression(String s) {
        elaborate(new ParseNode(s));
    } // end Expression

    /**
     * Constructs an Expression given a parse node.
     * 
     * @param r a parse node.
     */
    public Expression(ParseNode r) {
        switch (r.operator) {
            case Action.U_MINUS :
                {
                    ParseNode p = new ParseNode(1, Action.SUM);

                    p.children[0] = r;
                    elaborate(p);
                    return;
                } // end case

            case Action.RECIPROCAL :
                {
                    ParseNode p = new ParseNode(2, Action.PRODUCT);

                    p.children[0] = new ParseNode("1");
                    p.children[1] = r;
                    elaborate(p);
                    return;
                } // end case

            default :
                elaborate(r);
        } // end switch
    } // end Expression

    private void elaborate(ParseNode newRoot) {
        (root = newRoot).doParent();
        if (root.badFlag)
            return;

        variables = new Hashtable<Object, Hashtable>();
        legalVariables = new Hashtable<String, Integer>();
        knowns = new Hashtable<String, Double>();
        knowns.put("pi", new Double(Math.PI));
        knowns.put("Pi", new Double(Math.PI));
        knowns.put("PI", new Double(Math.PI));
        parameters = new Hashtable<String, Double>();
        make_legalVariables();
        fixImpliedMultiplication(root);
        if (find_variables(root) == null) {
            theValue = null;
            variables = null;
            legalVariables = null;
            knowns = null;
            parameters = null;
            root = null;
            return;
        } // end if

        Hashtable h = (Hashtable) (variables.get(root));
        Enumeration k = h.keys();
        while (k.hasMoreElements()) {
            String var = (String)k.nextElement();
            //System.out.println(var);
            Double t = (Double)knowns.get(var);
            //System.out.println(t);
            
            if (t == null)
                continue;

            Vector v = (Vector)h.get(var);
            Enumeration e = v.elements();
            while (e.hasMoreElements()) {
                ParseNode pn = (ParseNode)e.nextElement();
                pn.value = t;
            } // end while
        } // end while

        for (Enumeration e = variables.keys(); e.hasMoreElements();) {
            Object r;
            h = (Hashtable) (variables.get(r = e.nextElement()));

            for (k = knowns.keys(); k.hasMoreElements();)
                h.remove(k.nextElement());

            variables.put(r, h);
        } // end for

        condenseConstants(root);

        if ((theValue = root.value) != null) {
            valueString = MathUtil.trimDouble(theValue.doubleValue(), 12);
        } // end if
        else
            valueString = null;

        varStrings = new SortedKeyStrings((Hashtable) (variables.get(root))).theKeys;
    } // end elaborate

    private void make_legalVariables() {
    	int i = 0;
        Enumeration<String> k = knowns.keys();

        while (k.hasMoreElements()){
            legalVariables.put(k.nextElement(), new Integer(i++));
        }

        legalVariables.put("alpha", new Integer(i++));
        legalVariables.put("beta", new Integer(i++));
        legalVariables.put("gamma", new Integer(i++));
        legalVariables.put("delta", new Integer(i++));
        legalVariables.put("phi", new Integer(i++));
        legalVariables.put("lambda", new Integer(i++));
        legalVariables.put("theta", new Integer(i++));
        

        char[] varString = new char[1];
        int c;

        for (c = 'A'; c <= 'Z'; c++) {
            varString[0] = (char)c;
            legalVariables.put(new String(varString), new Integer(i++));
        } // end for c
        for (c = 'a'; c <= 'z'; c++) {
            varString[0] = (char)c;
            legalVariables.put(new String(varString), new Integer(i++));
        } // end for c
    } // end make_legalVariables

    private void fixImpliedMultiplication(ParseNode r) {
        int i;

        if (r.operator == Action.NO_OP) {
            if (legalVariables.get(r.theString) != null)
                return;

            Vector<String> v = splitIM(r.theString);

            if (v.size() == 1)
                return;

            r.operator = Action.PRODUCT;
            r.children = new ParseNode[v.size()];
            Enumeration<String> e = v.elements();
            for (i = 0; e.hasMoreElements(); i++)
                r.children[i] = new ParseNode((String)e.nextElement());
        } // end if
        else if (r.operator == Action.POWER) {
            int n0 = 0, n1 = 1;
            ParseNode r0 = r.children[0], r1 = r.children[1];
            StringBuffer b0 = new StringBuffer(), b1 = new StringBuffer();

            if (r0.operator == Action.NO_OP) {
                Vector<String> v = splitIM(r0.theString);

                if ((n0 = v.size() - 1) < 0) {
                    root.badFlag = true;
                    return;
                } // end if

                for (i = 0; i < n0; i++)
                    b0.append((String)v.elementAt(i) + "*");

                b0.append((String)v.elementAt(n0) + "^");
            } // end if
            else
                b0.append(new Expression(r0).toString() + "^");

            switch (r1.operator) {
                case Action.NO_OP :
                    {
                        Vector<String> v = splitIM(r1.theString);

                        if ((n1 = v.size()) < 1) {
                            root.badFlag = true;
                            return;
                        } // end if

                        b1.append((String)v.elementAt(0));

                        if (n1 > 1)
                            b1.append("*");

                        for (i = 1; i < n1; i++)
                            b1.append((String)v.elementAt(i));
                        break;
                    } // end block

                case Action.PRODUCT :
                case Action.SUM :
                    b1.append("(" + new Expression(r1).toString() + ")");
                    break;

                default :
                    b1.append(new Expression(r1).toString());
            } // end switch

            if (n0 == 0 && n1 == 1)
                return;

            ParseNode t = new Expression(b0.toString() + b1.toString()).root;

            if (t == null) {
                root.badFlag = true;
                return;
            } // end if

            r.children = t.children;
            r.operator = t.operator;
        } // end if
        else
            for (i = 0; i < r.children.length; i++)
                fixImpliedMultiplication(r.children[i]);
    } // end fixImpliedMultiplication

    private Vector<String> splitIM(String u) {
        Enumeration<String> k = legalVariables.keys();
        String[] vars = new StringSplitter(this).multiSplit(k, u);
        Vector<String> v = new Vector<String>();

        for (int i = 0; i < vars.length; i++) {
            if (vars[i].trim().length() == 0)
                continue;
            v.addElement(vars[i]);
        } // end for i
        

        return v;
    } // end splitIM

    private static void combine(Hashtable h1, Hashtable h2) {
        Enumeration k1 = h1.keys();
        Vector v1, v2;
        Object r;

        while (k1.hasMoreElements()) {
            v1 = (Vector)h1.get(r = k1.nextElement());

            if ((v2 = (Vector)h2.get(r)) != null) {
                Enumeration e = v1.elements();

                while (e.hasMoreElements())
                    v2.addElement(e.nextElement());
            } // end if
            else
                v2 = v1;

            h2.put(r, v2);
        } // end while
    } // end combine

    private Hashtable<String,Vector> find_variables(ParseNode r) {
        Hashtable<String, Vector> leavesOfR = new Hashtable<String, Vector>();
        //System.out.println("in find_varibles");

        if (r.operator == Action.NO_OP) { // no children
            if (legalVariables.get(r.theString) != null) {
                Vector<ParseNode> v = new Vector<ParseNode>();
                v.addElement(r);
                leavesOfR.put(r.theString, v);
            } // end if
            else
                try {
                    r.value = new Double(r.theString);
                } // end try
            catch (NumberFormatException NFE) {
                return null;
            } //end catch
        } // end if operator
        else {
            Hashtable childLeaves;
            for (int i = 0; i < r.children.length; i++) {
                if ((childLeaves = find_variables(r.children[i])) == null)
                    return null;
                combine(childLeaves, leavesOfR);
            } // end for i
        } // end else
        variables.put(r, leavesOfR);
        return leavesOfR;
    } // end find_variables

    private void condenseConstants(ParseNode r) {
        if (r.value != null)
            return;

        Hashtable h = (Hashtable)variables.get(r);

        if (h.isEmpty()) {
            r.value = new Double(r.eval());
            return;
        } // end if

        if (r.children != null)
            for (int i = 0; i < r.children.length; i++)
                condenseConstants(r.children[i]);
    } // end condenseConstants

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.util.Comparison#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object a, Object b) {
        StringSplitter s1 = (StringSplitter)a;
        StringSplitter s2 = (StringSplitter)b;
        int i = ((Integer)legalVariables.get(s1.pieces[1])).intValue();
        int j = ((Integer)legalVariables.get(s2.pieces[1])).intValue();

        return i - j;
    } // end compare

    /**
     * Returns true if the expression is valid, or false if it is not.
     * 
     * @return true if the expression is valid, or false if it is not.
     */
    public boolean isValid() {
        if (root == null)
            return false;

        if (root.badFlag)
            return false;

        return true;
    } // end isValid

    private String showSum(ParseNode r) {
        if (r.children == null)
            return null;
        if (r.children.length == 0)
            return "";
        String s = "";

        for (int i = 0; i < r.children.length; i++) {
            ParseNode t = r.children[i];
            if (t.operator == Action.U_MINUS) {
                s = s + " -";
                t = t.children[0];
            } // end if
            else
                s = s + " +";
            if (t.operator == Action.SUM)
                s = s + "(" + showTerm(t) + ")";
            else
                s = s + showTerm(t);
        } // end for i
        s = s.trim();
        if (s.indexOf("+") == 0)
            s = s.substring(1).trim();
        return s;
    } // end showSum

    private String showProd(ParseNode r) {
    	boolean needsParens=false;
    	
        if (r.children == null)
            return null;
        if (r.children.length == 0)
            return "";
        String s = "";
        for (int i = 0; i < r.children.length; i++) {
            ParseNode t = r.children[i];
            if (t.operator == Action.RECIPROCAL) {
                s = s + "/";
                t = t.children[0];
                needsParens = true;
            } // end if
            else
                s = s + "*";
            if (t.operator == Action.SUM)
                s = s + "(" + showSum(t) + ")";
            else
                if (needsParens)
                    s = s + "(" + showTerm(t) + ")";
                else
                    s = s+showTerm(t);
        } // end for i
        s = s.trim();
        if (s.indexOf("*") == 0)
            s = s.substring(1).trim();
        return s;
    } // end showProd

    private String showPower(ParseNode r) {
        if (r.children == null)
            return null;
        if (r.children.length != 2)
            return null;

        String base, power;
        if (r.children[0].operator != Action.NO_OP)
            base = "(" + showTerm(r.children[0]) + ")";
        else
            base = showTerm(r.children[0]);
        if (r.children[1].operator != Action.NO_OP)
            power = "(" + showTerm(r.children[1]) + ")";
        else
            power = showTerm(r.children[1]);
        return base + "^" + power;
    } // end showPower

    private String showFunction(ParseNode r) {
        if (r.children == null)
            return null;
        if (r.children.length != 1)
            return null;
        return (Action.FNAMES[r.operator] + "(" + showTerm(r.children[0]) + ")");
    } // end showFunction

    private String showTerm(ParseNode r) {
        if (r == null)
            return "";

        String s;
        Double D;

        switch (r.operator) {
            case Action.NO_OP :
                s = r.theString.trim();
                if ((D = (Double)parameters.get(s.toLowerCase())) != null) {
                    if (D.doubleValue() < 0.0)
                        return "(" + D.toString() + ")";
                    return D.toString();
                } // end if

                return s;

            case Action.U_MINUS :
                return null;

            case Action.SUM :
                return showSum(r);

            case Action.RECIPROCAL :
                return null;

            case Action.PRODUCT :
                return showProd(r);

            case Action.POWER :
                return showPower(r);

            case Action.SQRT :
            case Action.EXPONENTIAL :
            case Action.LOG :
            case Action.SINE :
            case Action.COSINE :
            case Action.TANGENT :
            case Action.ABS :
                return showFunction(r);

            default :
                return null;
        } // end switch
    } // end showTerm

    /**
     * Returns true if the expression is simple, false otherwise.
     * 
     * @return true if the expression is simple, false otherwise.
     */
    public boolean isSimple() {
        return ((root.operator == Action.NO_OP) && (varStrings.length == 1));
    } // end isSimple

    /**
     * Computes the product of this expression with the specified expression.
     * 
     * @param other the other expression.
     * @return the product of this expression with the specified expression.
     */
    public Expression product(Expression other) {
        ParseNode p = new ParseNode(2, Action.PRODUCT);

        p.children[0] = root;
        p.children[1] = other.root;
        return new Expression(p);
    } // end product

    /**
     * Computes the sum of this expression with the specified expression.
     * 
     * @param other the other expression.
     * @return the sum of this expression with the specified expression.
     */
    public Expression sum(Expression other) {
        ParseNode p = new ParseNode(2, Action.SUM);

        p.children[0] = root;
        p.children[1] = other.root;
        return new Expression(p);
    } // end sum

    /**
     * Returns the negated expression.
     * 
     * @param other the expression to negate.
     * @return the negated expression.
     */
    public static Expression negate(Expression other) {
        /*
         * remove unnecessary minus signs if the expression is already negated, just remove the
         * existing minus sign
         */
        if (other.root.operator == Action.SUM)
            if (other.root.children.length == 1)
                if (other.root.children[0].operator == Action.U_MINUS) {
                    ParseNode p = new ParseNode(1, Action.SUM);

                    p.children[0] = other.root.children[0].children[0];
                    return new Expression(p);
                } // end if

        ParseNode p = new ParseNode(1, Action.SUM);

        p.children[0] = new ParseNode(1, Action.U_MINUS);
        p.children[0].children[0] = other.root;
        return new Expression(p);
    } // end negate

    /**
     * Returns the reciprocal of the specified expression.
     * 
     * @param other the expression to process.
     * @return the reciprocal of the expression.
     */
    public static Expression reciprocal(Expression other) {
        if (other.root.operator == Action.PRODUCT)
            if (other.root.children.length == 1)
                if (other.root.children[0].operator == Action.RECIPROCAL) {
                    ParseNode p = new ParseNode(1, Action.PRODUCT);

                    p.children[0] = other.root.children[0].children[0];
                    return new Expression(p);
                } // end if

        ParseNode p = new ParseNode(2, Action.PRODUCT);

        p.children[0] = new ParseNode("1");
        p.children[1] = new ParseNode(1, Action.RECIPROCAL);
        p.children[1].children[0] = other.root;
        return new Expression(p);
    } // end reciprocal

    /**
     * Returns the difference between this expression and the specified expression.
     * 
     * @param other the other expression.
     * @return the difference between this expression and the specified expression.
     */
    public Expression difference(Expression other) {
        return sum(negate(other));
    } // end difference

    /**
     * Returns the quotient of this expression and the specified expression.
     * 
     * @param other the expression to process.
     * @return the quotient of this expression and the specified expression.
     */
    public Expression quotient(Expression other) {
        ParseNode p = new ParseNode(2, Action.PRODUCT);

        p.children[0] = root;
        p.children[1] = new ParseNode(1, Action.RECIPROCAL);
        p.children[1].children[0] = other.root;
        return new Expression(p);
    } // end quotient

    /**
     * Returns a string representation of the expression.
     * 
     * @return a string representation of the expression.
     */
    public String toString() {
        String r = showTerm(root);

        /*
         * r = r+"\nVariables:\n"; Enumeration k = ((Hashtable)(variables.get(root))).keys();
         * 
         * while (k.hasMoreElements()) r = r + (String)k.nextElement() +"\n";
         */

        return r;
    } // end toString


    public static void main(String[] args) {
        //Expression e = new Expression(StringSplitter.combineArgs(args));
    	Expression e= new Expression("sin(x)+2");

        if (e.isValid()) {
            Hashtable h = new Hashtable();

            h.put("x", new Double(7.0));
            e.parameters.put("a", new Double(2.0));
            e.parameters.put("m", new Double(-3.0));
            System.out.println("E = " + e.toString());
            System.out.println("Value = " + e.evaluate(h));
        } // end if
        else
            System.out.println("No dice");
    } // end main

} // end class Expression
