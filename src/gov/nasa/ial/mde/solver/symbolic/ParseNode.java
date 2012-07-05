/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver.symbolic;

import gov.nasa.ial.mde.util.Comparison;
import gov.nasa.ial.mde.util.StringSplitter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A parse node.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class ParseNode implements Comparison {
    
    /** The operator. */
    int operator;
    
    /** The value. */
    Double value = null;
    
    /** The string associated string. */
    String theString = null;
    
    /** The parent node. */
    ParseNode parent = null;
    
    /** The children of this node. */
    ParseNode[] children = null;
    
    /** The associated action. */
    static Action theAction = new Action();
    
    /** Flag to indicate the node is bad. */
    public boolean badFlag = false;

    /**
     * Constructs a <code>ParseNode</code> with no operator and no children.
     */
    public ParseNode() {
        operator = Action.NO_OP;
        children = null;
    } // end ParseNode (empty leaf)

    /**
     * Constructs a <code>ParseNode</code> with the specified number of children
     * and operator.
     * 
     * @param nChildren number of children to this node.
     * @param op the operator.
     */
    public ParseNode(int nChildren, int op) {
        children = new ParseNode[nChildren];
        operator = op;
        theString = null;
    } // end ParseNode (internal node)

    /**
     * Constructs a <code>ParseNode</code> with the specified quantity as a
     * String.
     * 
     * @param s the string Quantity.
     */
    public ParseNode(String s) {
        ParseNode p = buildTree(new Quantity(s));
        if (p == null) {
            badFlag = true;
            theString = s;
            children = null;
            value = null;
            operator = Action.CORRUPTED;
            return;
        } // end if
        theString = p.theString;
        children = p.children;
        operator = p.operator;
        value = p.value;
        badFlag = p.badFlag;
    } // end ParseNode (general case)

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.util.Comparison#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object d1, Object d2) {
        StringSplitter s1 = (StringSplitter)d1;
        StringSplitter s2 = (StringSplitter)d2;

        return s1.pieces[0].length() - s2.pieces[0].length();
    } // end compare

    /**
     * Evaluate the node.
     * 
     * @return the value of the evaluation.
     */
    double eval() {
        if (value != null)
            return value.doubleValue();

        if (operator == Action.NO_OP || operator == Action.CORRUPTED)
            throw new RuntimeException(theString + " is undefined");

        double t = Action.EVALUATOR[operator].eval(children);
        for (int i = 0; i < children.length; i++)
            if (children[i].badFlag) {
                badFlag = true;
                return 0;
            } // end if

        return t;
    } // end eval

    /**
     * Creates a leaf node.
     * 
     * @param s the string for the leaf node.
     * @return the leaf parse node.
     */
    ParseNode leaf(String s) {
        ParseNode p = new ParseNode();
        p.theString = s;
        return p;
    } // end leaf

    /**
     * Builds the tree given the specified quantity.
     * 
     * @param q the quantity.
     * @return the root node of the tree.
     */
    public ParseNode buildTree(Quantity q) {
        if (q == null)
            return null;
        if (q.children == null)
            return null;
        int i, n = q.children.length;

        if ((n & 1) == 0)
            return null;

        if (n == 1)
            return doSum((String)q.children[0]);

        String s = "", t;
        Hashtable<String, Object> ht = new Hashtable<String, Object>();

        for (i = 0; i < n; i++) {
            if (q.children[i] == null)
                return null;

            if (q.children[i] instanceof Quantity) {
                s = s + (t = "Quantity" + i);
                ht.put(t, q.children[i]);
            } // end if
            else if (q.children[i] instanceof String)
                s = s + ((String)q.children[i]).trim();
            else
                return null;
        } // end for i

        return replaceSubExpressions(ht, doSum(s));
    } // end buildTree

    /**
     * Replaces the sub-expression for the given node.
     * 
     * @param h the hashtable of quantities.
     * @param p the parse node.
     * @return the root node with the sub-expression replaced.
     */
    public ParseNode replaceSubExpressions(Hashtable<String, Object> h, ParseNode p) {
        if (p.children == null) {
            if (h.isEmpty())
                return p;

            Quantity q = (Quantity)h.get(p.theString);
            if (q != null)
                return buildTree(q);

            String r[] = new StringSplitter(this).multiSplit(h.keys(), p.theString);

            if (r.length == 1)
                return p;

            Vector<String> v = new Vector<String>();
            for (int i = 0; i < r.length; i++) {
                String s = r[i].trim();

                if (s.length() == 0)
                    continue;

                v.addElement(s);
            } // end for i

            ParseNode prod = new ParseNode(v.size(), Action.PRODUCT);
            Enumeration<String> e = v.elements();

            for (int i = 0; e.hasMoreElements(); i++) {
                String s = e.nextElement();

                if ((q = (Quantity)h.get(s)) == null)
                    prod.children[i] = leaf(s);
                else
                    prod.children[i] = buildTree(q);
            } // end for i
            return prod;
        } // end if

        for (int i = 0; i < p.children.length; i++)
            if ((p.children[i] = replaceSubExpressions(h, p.children[i])) == null)
                return null;

        return p;
    } // end replaceSubExpressions

    /**
     * Returns the sum of this node with the specified expression.
     * 
     * @param theLocalString the string expression.
     * @return the sum of this node with the specified expression.
     */
    public ParseNode doSum(String theLocalString) {
        int i, n;
        String[] operators;
        String[] operands;
        ParseNode p;
        StringTokenizer st;

        theLocalString = theLocalString.trim();
        // System.out.println ("doSum: "+theLocalString);
        if ((theLocalString.indexOf('+') >= 0) || (theLocalString.indexOf('-') >= 0)) {
            if (!((theLocalString.startsWith("-")) || (theLocalString.startsWith("+"))))
                theLocalString = "+" + theLocalString;
            st = new StringTokenizer(theLocalString, "+-", true);

            if (((i = st.countTokens()) & 1) == 1)
                return null;

            operators = new String[n = (i >> 1)];
            operands = new String[n];

            for (i = 0; i < n; i++) {
                operators[i] = st.nextToken().trim();
                operands[i] = st.nextToken().trim();
            } // end for i

            for (i = 0; i < n; i++) {
                if (!((operators[i].equals("+")) || (operators[i].equals("-"))))
                    return null;

                if ((operands[i].indexOf('+') >= 0) || (operands[i].indexOf('-') >= 0))
                    return null;
            } // end for i

            p = new ParseNode(n, Action.SUM); // sum node
            for (i = 0; i < n; i++)
                if (operators[i].equals("-")) {
                    p.children[i] = new ParseNode(1, Action.U_MINUS); // unary minus
                    p.children[i].children[0] = leaf(operands[i]);
                } // end if
            else
                p.children[i] = leaf(operands[i]);

            for (i = 0; i < n; i++)
                if (p.children[i].operator == Action.U_MINUS) {
                    if ((p.children[i].children[0] = doProd(p.children[i].children[0].theString)) == null)
                        return null;
                } // end if
            else if (p.children[i].operator == Action.NO_OP) {
                if ((p.children[i] = doProd(p.children[i].theString)) == null)
                    return null;
            } // end if
            else
                return null;
        } // end if
        else
            p = doProd(theLocalString);

        return p;
    } // end doSum

    /**
     * Returns the product of this node and the specified expression.
     * 
     * @param theLocalString the string expression.
     * @return the product of this node and the specified expression.
     */
    public ParseNode doProd(String theLocalString) {
        if (theLocalString == null)
            return null;
        int i, n;
        String[] operators;
        String[] operands;
        ParseNode p;
        StringTokenizer st;

        // System.out.println ("doProd: "+theLocalString);
        if ((theLocalString.indexOf('*') >= 0) || (theLocalString.indexOf('/') >= 0)) {
            theLocalString = "*" + theLocalString.trim();
            st = new StringTokenizer(theLocalString, "*/", true);

            if (((i = st.countTokens()) & 1) != 0)
                return null;

            operators = new String[n = (i >> 1)];
            operands = new String[n];

            for (i = 0; i < n; i++) {
                operators[i] = st.nextToken().trim();
                operands[i] = st.nextToken().trim();
            } // end for i

            for (i = 0; i < n; i++) {
                if (!((operators[i].equals("*")) || (operators[i].equals("/"))))
                    return null;
                if ((operands[i].indexOf('*') >= 0) || (operands[i].indexOf('/') >= 0))
                    return null;
            } // end for i

            p = new ParseNode(n, Action.PRODUCT); // product node
            for (i = 0; i < n; i++)
                if (operators[i].equals("/")) {
                    p.children[i] = new ParseNode(1, Action.RECIPROCAL); // reciprocal
                    if ((p.children[i].children[0] = doPowers(operands[i])) == null)
                        return null;
                } // end if
            else {
                if ((p.children[i] = doPowers(operands[i])) == null)
                    return null;
            } // end else
        } // end if
        else {
            if ((p = doPowers(theLocalString)) == null)
                return null;
        } // end else

        return p;
    } // end doProd

    private ParseNode doPowers(String theLocalString) {
        theLocalString = theLocalString.trim();
        // System.out.println ("doPowers: "+theLocalString);
        int c = theLocalString.indexOf("^");
        ParseNode p;

        if (c >= 0) {
            int n;
            char[] chars = new char[n = theLocalString.length()];

            theLocalString.getChars(0, n, chars, 0);
            p = new ParseNode(2, Action.POWER);
            if ((p.children[0] = doFunctions(new String(chars, 0, c))) == null)
                return null;
            c++;
            if ((p.children[1] = doFunctions(new String(chars, c, n - c))) == null)
                return null;
        } // end if
        else if ((p = doFunctions(theLocalString)) == null)
            return null;

        return p;
    } // end doPowers

    private ParseNode doFunctions(String theLocalString) {
        theLocalString = theLocalString.trim();
        // System.out.println ("doFunctions: "+theLocalString);
        String[] fNames = Action.FNAMES;
        int i = theAction.findFirst(theLocalString, fNames);
        // if (i >= 0) System.out.println ("i = "+i+" String = "+FNAMES[i]);
        ParseNode p;

        if (i == Action.NO_OP)
            return leaf(theLocalString);

        if (i < Action.FIRST_FUNCTION)
            return null;

        int c = fNames[i].length();
        int m = theLocalString.length() - c;

        char[] chars = new char[m];
        theLocalString.getChars(c, c + m, chars, 0);

        p = new ParseNode(1, i);
        if ((p.children[0] = doFunctions(new String(chars, 0, m))) == null)
            return null;
        return p;
    } // end doFunctions

    /**
     * Process the parent node (this node).
     */
    void doParent() {
        doParent(this);
    } // end doParent

    private void doParent(ParseNode p) {
        if (p.children == null)
            return;

        int i, n = p.children.length;

        for (i = 0; i < n; i++) {
            p.children[i].parent = p;
            doParent(p.children[i]);
        } // end for i
    } // end doParent
    
    /**
     * Returns a string representation of the node.
     * 
     * @return a string representation of the node.
     */
    public String toString() {
        if (operator == Action.NO_OP)
            return theString;

        String r = "<" + operator;
        for (int i = 0; i < children.length; i++)
            r = r + " " + children[i].toString();
        r = r + ">";

        return r;
    } // end doString
    
} // end class ParseNode
