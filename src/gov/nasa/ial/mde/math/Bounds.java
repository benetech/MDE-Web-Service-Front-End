/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Feb 16, 2004
 */
package gov.nasa.ial.mde.math;

/**
 * The <code>Bounds</code> class is an encapsulation of the left, right, top,
 * and bottom bound (double) values.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class Bounds {

    /**
     * The <code>left</code> bound.
     */
    public double left;

    /**
     * The <code>right</code> bound.
     */
    public double right;

    /**
     * The <code>top</code> bound.
     */
    public double top;

    /**
     * The <code>bottom</code> bound.
     */
    public double bottom;

    /**
     * Creates an instance of <code>Bounds</code> with the default value of
     * zero for the left, right, top, and bottom fields.
     */
    public Bounds() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Creates an instance of <code>Bounds</code> using the values from the
     * specified <code>Bounds</code> object.
     * 
     * @param b
     *            the specified <code>Bounds</code> object to use for the new
     *            <code>Bounds</code> instance.
     */
    public Bounds(Bounds b) {
        super();
        setBounds(b);
    }

    /**
     * Creates an instance of <code>Bounds</code> with the specified left,
     * right, top, and bottom bound values.
     * 
     * @param left
     *            the specified <code>left</code> bound
     * @param right
     *            the specified <code>right</code> bound
     * @param top
     *            the specified <code>top</code> bound
     * @param bottom
     *            the specified <code>bottom</code> bound
     */
    public Bounds(double left, double right, double top, double bottom) {
        super();
        setBounds(left, right, top, bottom);
    }

    /**
     * Sets the bounds of this <code>Bounds</code> object to the specified
     * bounds.
     * 
     * @param b
     *            the bounds to set this <code>Bounds</code> object to
     * @see #setBounds(double,double,double,double)
     */
    public void setBounds(Bounds b) {
        setBounds(b.left, b.right, b.top, b.bottom);
    }

    /**
     * Sets the bounds of this <code>Bounds</code> object to the specified
     * left, right top, and bottom bounds.
     * 
     * @param left
     *            the specified <code>left</code> bound
     * @param right
     *            the specified <code>right</code> bound
     * @param top
     *            the specified <code>top</code> bound
     * @param bottom
     *            the specified <code>bottom</code> bound
     * @see #setBounds(Bounds)
     */
    public void setBounds(double left, double right, double top, double bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    /**
     * Maximize the bounds to be the maximum of the union between the specified
     * bounds and the instance of this class.
     * 
     * @param b
     *            the bounds to maximize this <code>Bounds</code> object
     *            against.
     * @return true if any of the <code>left</code>, <code>right</code>,
     *         <code>top</code> or <code>bottom</code> fields of this
     *         <code>Bounds</code> object has changed, false otherwise.
     */
    public boolean maximize(Bounds b) {
        boolean boundsChanged = false;
        if (b.left < left) {
            left = b.left;
            boundsChanged = true;
        }
        if (b.right > right) {
            right = b.right;
            boundsChanged = true;
        }
        if (b.top > top) {
            top = b.top;
            boundsChanged = true;
        }
        if (b.bottom < bottom) {
            bottom = b.bottom;
            boundsChanged = true;
        }
        return boundsChanged;
    }

    /**
     * Returns the hash code value for the <code>Bounds</code> object.
     * 
     * @return the hash code value.
     */
    public int hashCode() {
        long bits = Double.doubleToLongBits(left);
        bits += Double.doubleToLongBits(right) * 37;
        bits += Double.doubleToLongBits(top) * 43;
        bits += Double.doubleToLongBits(bottom) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Checks whether two bounds objects have equal values.
     * 
     * @return true if the specified object and this <code>Bounds</code>
     *         object are equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Bounds) {
            Bounds b = (Bounds) obj;
            return ((b.left == this.left) && (b.right == this.right) && 
                    (b.top == this.top) && (b.bottom == this.bottom));
        }
        return false;
    }

    /**
     * Returns a string representation of this <code>Bounds</code> object.
     * 
     * @return a string representation of this <code>Bounds</code> object
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer strBuff = new StringBuffer(32);
        strBuff.append(getClass().getName()).append("[left=").append(left).append(",right=").append(right)
                .append(",top=").append(top).append(",bottom=").append(bottom).append(']');
        return strBuff.toString();
    }

}
