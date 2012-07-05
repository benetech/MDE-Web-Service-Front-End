/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.properties.MdeSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * The represents nodes of MDE features.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MdeFeatureNode {
    
    private LinkedHashMap<String, ArrayList<Object>> values = new LinkedHashMap<String, ArrayList<Object>>();

    /**
     * Default constructor.
     */
    public MdeFeatureNode() {
        super();
    } // end MdeFeatureNode

    /**
     * Constructs an MDE feature node for the specified object.
     * 
     * @param o the object to create a feature node for.
     */
    public MdeFeatureNode(Object o) {
        Class<? extends Object> c = o.getClass();
        Field[] f = c.getFields();
        int i, n = f.length;

        if (MdeSettings.DEBUG) {
            System.out.println("MdeFeatureNode.init called on \"" + c.getName() + "\"");
        }
        
        for (i = 0; i < n; i++)
            try {
                String k = f[i].getName();
                Object v = f[i].get(o);

                addKey(k);
                if (f[i].getType().isPrimitive() || v instanceof String)
                    addValue(k, v.toString());
                else
                    addValue(k, new MdeFeatureNode(v));
            } // end try
            catch (IllegalAccessException iae) {
            }
    } // end MdeFeatureNode

    /**
     * Returns the values for the specified key.
     * 
     * @param key the key to the values.
     * @return a list of values.
     */
    public ArrayList<Object> getList(String key) {
        return values.get(key);
    } // end getList

    /**
     * Returns the child MDE feature nodes for the specified key.
     * 
     * @param key the key to the child nodes.
     * @return the child MDE feature nodes.
     */
    public MdeFeatureNode[] getChildNodes(String key) {
        ArrayList<Object> l = getChildList(key, MdeFeatureNode.class);

        return l.toArray(new MdeFeatureNode[l.size()]);
    } // end getChildNodes

    /**
     * Returns the child strings for the specified key.
     * 
     * @param key the key to the child nodes.
     * @return the child strings.
     */
    public String[] getChildStrings(String key) {
        ArrayList<Object> l = getChildList(key, String.class);

        return l.toArray(new String[l.size()]);
    } // end getChildStrings

    /**
     * Returns true if the MDE feature node contains the key.
     * 
     * @param key a key to test.
     * @return true if the MDE feature node contains the key, false otherwise.
     */
    public boolean containsKey(String key) {
        return values.containsKey(key);
    } // end containsKey

    /**
     * Adds the specified key to the MDE feature node.
     * 
     * @param key the key to add.
     */
    public void addKey(String key) {
        ArrayList<Object> l = new ArrayList<Object>();

        //l.add (new MdeFeatureNode());
        values.put(key, l);
    } // end addKey

    /**
     * Adds a value for the specified key to the MDE feature node.
     * 
     * @param key the key for the value.
     * @param value the value to add.
     */
    public void addValue(String key, Object value) {
        ArrayList<Object> l = getList(key);

        if (l == null)
            throw new IllegalArgumentException("Key \"" + key + "\" not found.");

        l.add(value);
        values.put(key, l);
    } // end addValue

    /**
     * Returns the number of keys.
     * 
     * @return the number of keys.
     */
    public int numKeys() {
        return values.size();
    } // end numKeys

    /**
     * Returns an array of all the keys.
     * 
     * @return an array of all the keys.
     */
    public String[] keys() {
        int i = 0, n = values.size();
        String[] r = new String[n];
        Iterator<String> it = values.keySet().iterator();

        while (it.hasNext())
            r[i++] = it.next();

        return r;
    } // end keys

    /**
     * Returns an XML string representation of the MDE feature node.
     * 
     * @return an XML string representation of the MDE feature node.
     */
    public String getXMLString() {
        return MdeFeatureNode.toXML(this);
    } // end getXMLString
    


    /**
     * Converts the specifed MDE feature node into its XML representation.
     * 
     * @param r the MDE feature node.
     * @return the XML representation of the node.
     */
    private static String toXML(MdeFeatureNode r) {
        int i, n = r.numKeys();
        String[] keys = r.keys();
        StringBuffer b = new StringBuffer();

        for (i = 0; i < n; i++) {
            ArrayList<Object> l = r.values.get(keys[i]);
            String k = MdeFeatureNode.massageKeyString(keys[i]);
            Iterator<Object> it = l.iterator();

            while (it.hasNext()) {
                Object o = it.next();
                String v = (o instanceof MdeFeatureNode) ? MdeFeatureNode.toXML((MdeFeatureNode)o) + "\n"
                        : MdeFeatureNode.massageValueString((String)o);

                if (v.trim().length() > 0)
                    b.append("\n<" + k + ">" + v + "</" + k + ">");
            } // end while
        } // end for

        return b.toString();
    } // end toXML

    private ArrayList<Object> getChildList(String key, Class<?> c) {
        ArrayList<Object> l = getList(key);
        ArrayList<Object> n = new ArrayList<Object>();

        if (l == null)
            throw new IllegalArgumentException("Key \"" + key + "\" not available");

        Iterator<Object> it = l.iterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o.getClass() == c)
                n.add(o);
        } // end while

        return n;
    } // end getChildList

    private static String massageKeyString(String k) {
        return k.replaceAll("[^a-zA-Z0-9]", "");
    } // end massageKeyString

    private static String massageValueString(String theString) {
        String outString = theString.replaceAll(">", "&gt;");
        outString = outString.replaceAll("<", "&lt;");
        //int i;

        /*
         * while((i = outString.indexOf("{"))>-1) { outString =
         * outString.substring(0,i) + "_open_curly_bracket_"+
         * outString.substring(i+1); } while((i = outString.indexOf("}"))>-1) {
         * outString = outString.substring(0,i) + "_close_curly_bracket_"+
         * outString.substring(i+1); }
         */
        return outString;
    } // end massageValueString

    public ArrayList<Object> getValues(String key) {
    	return this.values.get(key);
    }
    
    public Object getValue(String key) throws NullPointerException {
    	Object value = null;
    	ArrayList<Object> valueArray = null;
    	if(this.values.containsKey(key)) {
    		valueArray = this.values.get(key);
    		value = valueArray.get(0);
    	}
    	if(value == null) throw new NullPointerException();
    	return value;
    }

//    public static void main(String[] args) {
//        MdeFeatureNode f = new MdeFeatureNode();
//
//        f.addKey("foo");
//        f.addValue("foo", "bar");
//        f.addValue("foo", new MdeFeatureNode());
//
//        String[] v = f.getChildStrings("foo");
//        MdeFeatureNode[] n = f.getChildNodes("foo");
//
//        n[0].addKey("hello");
//        n[0].addValue("hello", "goodbye");
//
//        System.out.println("Values:");
//
//        for (int i = 0; i < v.length; i++)
//            System.out.println("\n" + v[i]);
//
//        System.out.println("\nNumber of child nodes = " + n.length);
//
//        System.out.println("XML string:");
//        System.out.println(f.getXMLString());
//
//        MdeFeatureNodeManager mf = new MdeFeatureNodeManager(f);
//        byte[] b = new byte[256];
//        String s;
//        MdeFeatureNode[] nodes;
//
//        while (true)
//            try {
//                int l = System.in.read(b);
//
//                if (l > 255)
//                    break;
//
//                s = new String(b, 0, l).trim();
//
//                nodes = mf.getNodes(s);
//
//                if (nodes.length > 0)
//                    for (int i = 0; i < nodes.length; i++)
//                        System.out.println(nodes[i].getXMLString());
//                else
//                    System.out.println("Nothing found.");
//            } // end try
//            catch (java.io.IOException ioe) {
//                System.err.println(ioe);
//                System.exit(1);
//            } // end catch
//    } // end main

} // end classMdeFeatureNode
