/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import java.util.ArrayList;

/**
 * The class manages the MDE feature nodes.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MdeFeatureNodeManager {
    
	public static String ROOT_PATH = "";
	public static String MDE_NAME = "MDE";
	public static String MDE_PATH = "/"+MDE_NAME+"/";
	public static String GRAPH_DATA_NAME = "GraphData";
	public static String GRAPH_DATA_PATH = MDE_PATH + GRAPH_DATA_NAME + "/";
	
    private MdeFeatureNode root;
    private MdeFeatureNode current;
    private String[] pathSegments;
    private int numSegments;
    private ArrayList<MdeFeatureNode> nodeList = new ArrayList<MdeFeatureNode>();
    
    /** Position to add a node. */
    protected final static int ADD_LAST = -2, ADD_ALL = -1;

    /**
     * Default constructor.
     */
    public MdeFeatureNodeManager() {
        this(new MdeFeatureNode());
    } // end MdeFeatureNodeManager
    
    /**
     * Constructs an MDE feature node manager with the specified root node.
     * 
     * @param root the root node.
     */
    public MdeFeatureNodeManager(MdeFeatureNode root) {
        this.root = root;
        this.current = root;
    } // end MdeFeatureNodeManager

    /**
     * Returns the current MDE feature node.
     * 
     * @return the current MDE feature node.
     */
    public MdeFeatureNode getCurrent() {
        return current;
    } // end getCurrent

    /**
     * Sets the current MDE feature node based on the specified path.
     * 
     * @param path the path to the current node.
     */
    public void setCurrent(String path) {
        MdeFeatureNode[] n = getNodes(path);

        switch (n.length) {
        case 1:
            current = n[0];
            return;

        case 0:
            throw new IllegalArgumentException("Path: \"" + path + "\" not found");

        default:
            throw new IllegalArgumentException("Path \"" + path + "\" resulted in multiple nodes");
        } // end switch
    } // end setCurrent

    /**
     * Adds a key to the current node.
     * 
     * @param key the key to add.
     */
    public void addKey(String key) {
        current.addKey(key);
    } // end addKey

    /**
     * Adds a value to the current node.
     * 
     * @param key the key for the value.
     * @param value the value to add.
     */
    public void addValue(String key, Object value) {
        current.addValue(key, value);
    } // end addValue

    /**
     * Adds a key to the node specified by the path.
     * 
     * @param path the path to the node.
     * @param key the key to add.
     */
    public void addKey(String path, String key) {
        addKey(path, key, MdeFeatureNodeManager.ADD_LAST);
    } // end addKey

    /**
     * Adds a key to the node specified by the path.
     * 
     * @param path the path to the node.
     * @param key the key to add.
     * @param whichNode which node to add to and is one of ADD_ALL or ADD_LAST.
     */
    public void addKey(String path, String key, int whichNode) {
        MdeFeatureNode[] n = getNodes(path);
        int i, l = n.length;

        switch (whichNode) {
        case MdeFeatureNodeManager.ADD_ALL:
            // add to all
            for (i = 0; i < l; i++)
                n[i].addKey(key);
            return;

        case MdeFeatureNodeManager.ADD_LAST:
            // add to last
            n[l - 1].addKey(key);
            return;

        default:
            // add to a specific node
            n[whichNode].addKey(key);
        } // end switch
    } // end addKey

    /**
     * Adds the value for the specified key and path to the node.
     * 
     * @param path the path to the node.
     * @param key the key to the value.
     * @param value the value to add.
     * @param whichNode which node to add to and is one of ADD_ALL or ADD_LAST.
     */
    public void addValue(String path, String key, Object value, int whichNode) {
        MdeFeatureNode[] n = getNodes(path);
        int i, l = n.length;

        switch (whichNode) {
        case MdeFeatureNodeManager.ADD_ALL:
            // add to all
            for (i = 0; i < l; i++)
                n[i].addValue(key, value);
            return;

        case MdeFeatureNodeManager.ADD_LAST:
            // add to last
            n[l - 1].addValue(key, value);
            return;

        default:
            // add to a specific node
            n[whichNode].addValue(key, value);
        } // end switch
    } // end addValue

    /**
     * Adds a node for the specified path and key.
     * 
     * @param path the path to the node.
     * @param key the key to add.
     */
    public void addNode(String path, String key) {
        MdeFeatureNode[] n = getNodes(path);
        int i, l = n.length;

        for (i = 0; i < l; i++) {
            if (!n[i].containsKey(key))
                n[i].addKey(key);

            n[i].addValue(key, new MdeFeatureNode());
        } // end for i
    } // end addNode

    /**
     * Resets the current node to the root node.
     */
    public void resetCurrent() {
        current = root;
    } // end resetCurrent

    /**
     * Returns the nodes associated with the specified path.
     * 
     * @param path the path to the nodes.
     * @return the MDE feature nodes belonging to the path.
     */
    public MdeFeatureNode[] getNodes(String path) {
        parsePath(path);
        nodeList.clear();
        if (path.trim().startsWith("/"))
            collectMFNs(root, 0);
        else
            collectMFNs(current, 0);
        return nodeList.toArray(new MdeFeatureNode[nodeList.size()]);
    } // end getNodes

    /**
     * Returns the XML from the root node.
     * 
     * @return the XML from the root node.
     */
    public String toString() {
        return root.getXMLString();
    } // end toString

    private void collectMFNs(MdeFeatureNode r, int segmentNumber) {
        if (segmentNumber == numSegments) {
            nodeList.add(r);
            return;
        } // end if

        MdeFeatureNode[] nodes = r.getChildNodes(pathSegments[segmentNumber]);
        int i, n = nodes.length;

        for (i = 0; i < n; i++)
            collectMFNs(nodes[i], segmentNumber + 1);
    } // end collectMFNs

    private void parsePath(String path) {
        String[] p = path.split("/");
        ArrayList<String> t = new ArrayList<String>();
        int i, n = p.length;

        for (i = 0; i < n; i++) {
            String u = p[i].trim();

            if (u.length() == 0)
                continue;

            t.add(u);
        } // end for i

        numSegments = t.size();
        pathSegments = t.toArray(new String[numSegments]);
    } // end parsePath

} // end class MdeFeatureNodeManager
