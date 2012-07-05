/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 31, 2004
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.math.RealZero;
import gov.nasa.ial.mde.util.MathUtil;

/**
 * The class represents an interval endpoint.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class IntervalEndpoint implements Comparable<Object> {
    
    /** The state of the interval endpoint. */
    protected final static int UNDEFINED = -1, 
                               LOCAL_MIN = 0, 
                               LOCAL_MAX = 1, 
                               INFLECTION_POINT = 2,
                               VERTICAL_ASYMPTOTE = 3, 
                               HORIZONTAL_ASYMPTOTE = 4, 
                               BOUNDARY_POINT = 5;
    
    // The strings match the array index value above.
    private static final String[] TYPE_STRINGS = { 
                                "local minimum", 
                                "local maximum", 
                                "inflection point",
                                "vertical asymptote", 
                                "horizontal asymptote", 
                                "boundary point" };
    
    /** The x value of the endpoint. */
    protected double xValue;
    
    /** The left Y value of the endpoint. */
    protected double leftYValue;

    /** The right Y value of the endpoint. */
    protected double rightYValue;
    
    /** Flag indicating if the endpoint is critical. */
    protected boolean isCritical = false;
    
    /** Flag indicating if the endpoint is Singular. */
    protected boolean isSingular = false;
    
    private int typeCode = IntervalEndpoint.UNDEFINED;
    private String typeString = "undefined";

    /**
     * Constructs an interval endpoint given the specified real-zereo value and
     * type.
     * 
     * @param rz the real-zero value.
     * @param tc the type code, which is one of LOCAL_MIN, LOCAL_MAX, 
     * INFLECTION_POINT, VERTICAL_ASYMPTOTE, HORIZONTAL_ASYMPTOTE, or 
     * BOUNDARY_POINT.
     */
    public IntervalEndpoint(RealZero rz, int tc) {
        xValue = rz.getX();

        switch (this.typeCode = tc) {
        case IntervalEndpoint.LOCAL_MAX:
        case IntervalEndpoint.LOCAL_MIN:
        case IntervalEndpoint.INFLECTION_POINT:
            isCritical = true;
            break;

        case IntervalEndpoint.VERTICAL_ASYMPTOTE:
            isSingular = true;
            break;
        } // end switch

        typeString = TYPE_STRINGS[tc];
    } // end IntervalEndpoint

    /**
     * Constructs an interval endpoint given the specified point.
     * 
     * @param p the XY point.
     */
    public IntervalEndpoint(PointXY p) {
        xValue = p.x;
        leftYValue = rightYValue = p.y;
        isCritical = false;
        isSingular = false;
        if (!Double.isInfinite(leftYValue)) {
            if (Double.isInfinite(xValue))
                typeString = TYPE_STRINGS[typeCode = IntervalEndpoint.HORIZONTAL_ASYMPTOTE];
            else
                typeString = TYPE_STRINGS[typeCode = IntervalEndpoint.BOUNDARY_POINT];
        } // end if
    } // end IntervalEndpoint

    /**
     * Constructs an interval endpoint given the specified point and type.
     * 
     * @param p the XY point.
     * @param tc the type code, which is one of LOCAL_MIN, LOCAL_MAX, 
     * INFLECTION_POINT, VERTICAL_ASYMPTOTE, HORIZONTAL_ASYMPTOTE, or 
     * BOUNDARY_POINT.
     */
    public IntervalEndpoint(PointXY p, int tc) {
        this(p);
        if (0 > tc || tc >= TYPE_STRINGS.length) {
            typeCode = IntervalEndpoint.UNDEFINED;
            typeString = "undefined";
        } // end if
        else
            typeString = TYPE_STRINGS[typeCode = tc];
    } // end IntervalEndpoint

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(Object o) {
        IntervalEndpoint e = (IntervalEndpoint)o;

        if (xValue < e.xValue)
            return -1;

        if (xValue > e.xValue)
            return 1;

        return 0;
    } // end compareTo

    /**
     * Returns the interval type as a string.
     * 
     * @return the interval type as a string.
     */
    public String getTypeString() {
        return typeString;
    }

    /**
     * Returns the interval type.
     * 
     * @return the interval type.
     */
    public int getTypeCode() {
        return typeCode;
    }

    /**
     * Returns the MDE feature node for the interval endpoint.
     * 
     * @return the MDE feature node for the interval endpoint.
     */
    public MdeFeatureNode getMFN() {
        MdeFeatureNode r = new MdeFeatureNode();

        r.addKey("X");
        //		r.addValue ("X", new NumberModel(xValue).getMFN());
        r.addValue("X", MathUtil.trimDouble(xValue, 3));
        if (leftYValue != rightYValue) {
            r.addKey("discontinuity");
            r.addValue("discontinuity", "true");
            r.addKey("leftY");
            r.addKey("rightY");
//		  	  r.addValue("leftY", new NumberModel(leftYValue).getMFN());
//			  r.addValue ("rightY", new NumberModel(rightYValue).getMFN());
            r.addValue("leftY", MathUtil.trimDouble(leftYValue, 3));
            r.addValue("rightY", MathUtil.trimDouble(rightYValue, 3));
        } // end if
        else {
            r.addKey("Y");
//		      r.addValue ("Y", new NumberModel(leftYValue).getMFN());
            r.addValue("Y", MathUtil.trimDouble(leftYValue, 3));
        } // end else

        if (typeCode != IntervalEndpoint.UNDEFINED) {
            r.addKey("Type");
            r.addValue("Type", getTypeString());
        } // end if

        return r;
    } // end getMFN

} // end class IntervalEndpoint
