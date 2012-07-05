/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.math;

import java.math.BigDecimal;

/**
 * The <code>Matrix</code> class represents a matrix.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Matrix {
    
    private int numRows = 0;
    private int numCols = 0;
    private double[][] theArray;
    private SVDUtilities svd = null;
    private static int numDigits = 6;

    /**
     * Default constructor, which creates a 0x0 matrix.
     */
    public Matrix() {
        this(0,0);
    } // end Matrix

    /**
     * Constructs a matrix with the specified number of rows and columns and
     * sets all values to zero.
     * 
     * @param r number of rows in matrix.
     * @param c number of columns in matrix.
     */
    public Matrix(int r, int c) {
        this.numRows = r;
        this.numCols = c;
        theArray = new double[numRows][numCols];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                theArray[i][j] = 0.0;
            }
        }
    } // end Matrix

    /**
     * Constructs a matrix from the specified two-dimensional array.
     * 
     * @param a two-dimensional array.
     */
    public Matrix(double[][] a) {
        int n = a.length;

        for (int i = 0; i < n; i++) {
            if (a[i] == null) {
                continue;
            }

            numRows++;
            numCols = Math.max(numCols, a[i].length);
        } // end for i

        theArray = new double[numRows][numCols];

        for (int i = 0; i < n; i++) {
            int r = 0;

            if (a[i] != null) {
                r = a[i].length;
            }

            for (int j = 0; j < r; j++) {
                theArray[i][j] = a[i][j];
            }
            for (int j = r; j < numCols; j++) {
                theArray[i][j] = 0.0;
            }
        } // end for i
    } // end Matrix

    /**
     * Returns the number of rows in the matrix.
     * 
     * @return the number of rows in the matrix.
     */
    public int getNumRows() {
        return numRows;
    } // end getNumRows

    /**
     * Returns the number of columns in the matrix.
     * 
     * @return the number of columns in the matrix.
     */
    public int getNumCols() {
        return numCols;
    } // end getNumCols

    
    /**
     * Returns the matrix as a two-dimensional array.
     * 
     * @return the matrix as a two-dimensional array.
     */
    public double[][] getArray() {
        return theArray;
    } // end getArray

    /**
     * Copies the specified matrix to this matrix.
     * 
     * @param m the matrix to copy into this matrix.
     */
    public void copyMatrix(Matrix m) {
        numRows = m.getNumRows();
        numCols = m.getNumCols();
        theArray = m.getArray();
    } // end copyMatrix

    
    /**
     * Sets the matrix internal array to the specified array with the number
     * of rows and columns being set to the specified size.
     * 
     * @param newArray the new matrix array.
     * @param size the matrix size.
     */
    protected void copyArray(double[][] newArray, int size) {
        theArray = newArray;
        numRows = numCols = size;
    } // end copyArray

    /**
     * Returns a string represenation of the matrix.
     * 
     * @return a string represenation of the matrix.
     */
    public String toString() {
        int i, j;
        StringBuffer b = new StringBuffer(numRows + " rows by " + numCols + " columns");

        for (i = 0; i < numRows; i++)
            for (j = 0; j < numCols; j++)
                if ((j % 5) == 0)
                    b.append("\n" + trimDouble(theArray[i][j], numDigits));
                else
                    b.append(" " + trimDouble(theArray[i][j], numDigits));

        return b.toString();
    } // end toString

    /**
     * Returns a submatrix starting at the specified column and row.
     * 
     * @param m matrix to pull data from.
     * @param c column index at the start of the submatrix.
     * @param r row index at the start of the submatrix.
     * @return a submatrix starting at the specified column and row.
     */
    public static Matrix submatrix(Matrix m, int c, int r) {
        // was if (c < 0 | c > m.getNumCols() | r < 0 || r > m.getNumRows()) {
        if (c < 0 || c > m.getNumCols() || r < 0 || r > m.getNumRows()) {
            throw new IllegalArgumentException("Size params out of range.");
        }
        
        double[][] a = new double[r][c];
        double[][] b = m.getArray();

        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                a[i][j] = b[i][j];

        return new Matrix(a);
    } // end subMatrix

    /**
     * Returns the transpose of the specified matrix.
     * 
     * @param m the matrix to transpose.
     * @return the transposed matrix.
     */
    public static Matrix transpose(Matrix m) {
        int c = m.getNumCols(), r = m.getNumRows();
        double[][] result = new double[c][r];
        double[][] a = m.getArray();

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                result[j][i] = a[i][j];
            }
        }

        return new Matrix(result);
    } // end transpose

    /**
     * Returns the product of two matrix's.
     * 
     * @param left the left matrix in the product.
     * @param right the right matrix in the product.
     * @return the product of two matrix's.
     */
    public static Matrix product(Matrix left, Matrix right) {
        int cLeft = left.getNumCols(), cRight = right.getNumCols();
        int rLeft = left.getNumRows(), rRight = right.getNumRows();
        double[][] aLeft = left.getArray(), aRight = right.getArray();

        if (cLeft != rRight) {
            throw new IllegalArgumentException(
                    "Attempt to multiply matrices with incompatible dimensions.");
        }

        double[][] result = new double[rLeft][cRight];

        for (int i = 0; i < rLeft; i++) {
            for (int j = 0; j < cRight; j++) {
                result[i][j] = 0.0;
                for (int k = 0; k < cLeft; k++) {
                    result[i][j] += (aLeft[i][k] * aRight[k][j]);
                }
            } // end for j
        }
        return new Matrix(result);
    } // end product

    /**
     * Returns the product of a scalar and a matrix.
     * 
     * @param f the scaler.
     * @param m the matrix.
     * @return the product of a scalar and a matrix.
     */
    public static Matrix product(double f, Matrix m) {
        int i, j, r = m.getNumRows(), c = m.getNumCols();
        double[][] a = m.getArray();
        double[][] result = new double[r][c];

        for (i = 0; i < r; i++) {
            for (j = 0; j < c; j++) {
                result[i][j] = f * a[i][j];
            }
        }

        return new Matrix(result);
    } // end product

    
    /**
     * Returns the sum of two matrix's.
     * 
     * @param m1 the first matrix.
     * @param m2 the second matrix.
     * @return the sum of two matrix's.
     */
    public static Matrix sum(Matrix m1, Matrix m2) {
        int r1 = m1.getNumRows();
        int c1 = m1.getNumCols();
        int r2 = m2.getNumRows();
        int c2 = m2.getNumCols();

        if ((r1 != r2) || (c1 != c2)) {
            throw new IllegalArgumentException(
                    "Attempt to add matrixes of incompatible sizes.");
        }

        int i, j;
        double[][] a1 = m1.getArray(), a2 = m2.getArray();

        double[][] result = new double[r1][c1];

        for (i = 0; i < r1; i++) {
            for (j = 0; j < c1; j++) {
                result[i][j] = a1[i][j] + a2[i][j];
            }
        }
        return new Matrix(result);
    } // end sum

    /**
     * Returns the difference between two matrix's.
     * 
     * @param m1 the first matrix.
     * @param m2 the second matrix.
     * @return the difference between the two matrix's.
     */
    public static Matrix difference(Matrix m1, Matrix m2) {
        return sum(m1, product(-1.0, m2));
    } // end difference

    /**
     * Returns the L2 Norm.
     * @return the L2 Norm.
     */
    public double l2norm() {
        int i, j;
        double r = 0.0;

        if ((numRows == 0) || (numCols == 0)) {
            return 0.0;
        }

        for (i = 0; i < numRows; i++) {
            for (j = 0; j < numCols; j++) {
                r += (theArray[i][j] * theArray[i][j]);
            }
        }

        return Math.sqrt(r / ((double)numRows * (double)numCols));
    } // end l2norm

    /**
     * Do a Singular Value Decomposition (SVD) on the matrix.
     */
    public void doSVD() {
        svd = new SVDUtilities();
        svd.compute_svd();
    } // end doSVD

    
    /**
     * Returns the left singular vectors.
     * 
     * @return the left singular vectors.
     */
    public Matrix getLeftSingularVectors() {
        if (svd == null)
            doSVD();
        return new Matrix(svd.leftVectors);
    } // end getLeftSingularVectors

    /**
     * Returns the sigular values.
     * 
     * @return the sigular values.
     */
    public double[] getSingularValues() {
        if (svd == null)
            doSVD();

        double[] s = new double[svd.nc];

        for (int i = 0; i < svd.nc; i++)
            s[i] = svd.singularValues[i];

        return s;
    } // end getSingularValues

    /**
     * Returns the right singular vectors.
     * 
     * @return the right singular vectors.
     */
    public Matrix getRightSingularVectors() {
        if (svd == null)
            doSVD();

        return new Matrix(svd.rightVectors);
    } // end getRightSingularVectors

    /**
     * Returns the Pseudo inverse.
     * 
     * @param fractionOfLargestSV a fracton of the largest sigular value.
     * @return the Pseudo inverse.
     */
    public Matrix getPseudoInverse(double fractionOfLargestSV) {
        if (svd == null)
            doSVD();
        if (svd.nc == 0)
            return null;
        if (svd.singularValues[0] == 0.0)
            return null;

        int i, j, k;
        double t = svd.singularValues[0] * fractionOfLargestSV, u;
        double[] s = new double[svd.nc];
        double[][] a = new double[numCols][numRows];

        for (i = 0; i < svd.nc; i++) {
            u = svd.singularValues[i] + t;
            s[i] = svd.singularValues[i] / (u * u);
        } // end for i

        for (i = 0; i < numCols; i++)
            for (j = 0; j < numRows; j++) {
                a[i][j] = 0.0;
                for (k = 0; k < svd.nc; k++)
                    a[i][j] += (s[k] * svd.rightVectors[i][k] * svd.leftVectors[j][k]);
            } // end for j

        return new Matrix(a);
    } // end getPseudoInverse

    private static String trimDouble(double x, int digits) {
        if (Math.abs(x) == Double.POSITIVE_INFINITY) {
            return "" + x;
        }

        String s = new BigDecimal(x).setScale(digits, BigDecimal.ROUND_HALF_UP).toString();
        int i, l = s.indexOf("."), n = s.length();

        if (l < 0) {
            return s;
        }

        for (i = n - 1; i > l + 1; i--) {
            if (s.charAt(i) != '0') {
                break;
            }
        }

        return s.substring(0, i + 1);
    } // end trimDouble

    private class SVDUtilities {
        private boolean transposeMode;
        private int nr, nc;
        private double[][] leftVectors;
        private double[] singularValues;
        private double[][] rightVectors;
        private double[] tempDouble;

        private SVDUtilities() {
            int i, j;

            nr = Math.max(numRows, numCols);
            nc = Math.min(numRows, numCols);
            leftVectors = new double[nr][nc];
            singularValues = new double[nc];
            rightVectors = new double[nc][nc];
            tempDouble = new double[nc];

            if (numRows < numCols) {
                for (i = 0; i < numRows; i++)
                    for (j = 0; j < numCols; j++)
                        leftVectors[j][i] = theArray[i][j];
                transposeMode = true;
            } // end if
            else {
                for (i = 0; i < numRows; i++)
                    for (j = 0; j < numCols; j++)
                        leftVectors[i][j] = theArray[i][j];
                transposeMode = false;
            } // end else

            // checkSVD();
        } // end SVDUtilities

        private double pythag(double a, double b) {
            double at, bt, ct;
            if ((at = Math.abs(a)) > (bt = Math.abs(b))) {
                ct = bt / at;
                return at * Math.sqrt(1 + ct * ct);
            } // end if
            else if (bt != 0.0) {
                ct = at / bt;
                return bt * Math.sqrt(1 + ct * ct);
            } // end if
            else
                return 0.0;
        } // end pythag

        private double sign(double a, double b) {
            return (b >= 0.0) ? Math.abs(a) : -Math.abs(a);
        } // end sign

        private void bubble() {
            int i, j, k;
            double t;

            for (i = 1; i < nc; i++)
                for (j = (nc - 1); j >= i; j--)
                    if (Math.abs(singularValues[j - 1]) < Math.abs(singularValues[j])) {
                        t = singularValues[j - 1];
                        singularValues[j - 1] = singularValues[j];
                        singularValues[j] = t;

                        for (k = 0; k < nc; k++) {
                            t = rightVectors[k][j - 1];
                            rightVectors[k][j - 1] = rightVectors[k][j];
                            rightVectors[k][j] = t;
                        } /* end for k */

                        for (k = 0; k < nr; k++) {
                            t = leftVectors[k][j - 1];
                            leftVectors[k][j - 1] = leftVectors[k][j];
                            leftVectors[k][j] = t;
                        } /* end for k */
                    } /* end if */
        } /* end bubble */

        private void compute_svd() {
            int flag, i, its, j, jj, k, l = 0, nm = 0;
            double c, f, h, s, x, y, z;
            double anorm = 0.0, g = 0.0, scale = 0.0;

            for (i = 0; i < nc; i++) {
                l = i + 1;
                tempDouble[i] = scale * g;
                g = s = scale = 0.0;
                if (i < nr) {
                    for (k = i; k < nr; k++)
                        scale += Math.abs(leftVectors[k][i]);
                    if (scale != 0.0) {
                        for (k = i; k < nr; k++) {
                            leftVectors[k][i] /= scale;
                            s += leftVectors[k][i] * leftVectors[k][i];
                        } // end for k
                        f = leftVectors[i][i];
                        g = -sign(Math.sqrt(s), f);
                        h = f * g - s;
                        leftVectors[i][i] = f - g;
                        if (i != nc - 1) {
                            for (j = l; j < nc; j++) {
                                for (s = 0.0, k = i; k < nr; k++)
                                    s += leftVectors[k][i] * leftVectors[k][j];
                                f = s / h;
                                for (k = i; k < nr; k++)
                                    leftVectors[k][j] += f * leftVectors[k][i];
                            } // end for j
                        } // end if
                        for (k = i; k < nr; k++)
                            leftVectors[k][i] *= scale;
                    } // end if
                } // end if
                singularValues[i] = scale * g;
                g = s = scale = 0.0;
                if (i < nr && i != nc - 1) {
                    for (k = l; k < nc; k++)
                        scale += Math.abs(leftVectors[i][k]);
                    if (scale != 0.0) {
                        for (k = l; k < nc; k++) {
                            leftVectors[i][k] /= scale;
                            s += leftVectors[i][k] * leftVectors[i][k];
                        } // end for k
                        f = leftVectors[i][l];
                        g = -sign(Math.sqrt(s), f);
                        h = f * g - s;
                        leftVectors[i][l] = f - g;
                        for (k = l; k < nc; k++)
                            tempDouble[k] = leftVectors[i][k] / h;
                        if (i != nr - 1) {
                            for (j = l; j < nr; j++) {
                                for (s = 0.0, k = l; k < nc; k++)
                                    s += leftVectors[j][k] * leftVectors[i][k];
                                for (k = l; k < nc; k++)
                                    leftVectors[j][k] += s * tempDouble[k];
                            } // end for j
                        } // end if
                        for (k = l; k < nc; k++)
                            leftVectors[i][k] *= scale;
                    } // end if (scale)
                } // end if (i < nr...
                anorm = Math.max(anorm, (Math.abs(singularValues[i]) + Math.abs(tempDouble[i])));
            } // end for i
            for (i = nc - 1; i >= 0; i--) {
                if (i < nc - 1) {
                    if (g != 0.0) {
                        for (j = l; j < nc; j++)
                            rightVectors[j][i] = (leftVectors[i][j] / leftVectors[i][l]) / g;
                        for (j = l; j < nc; j++) {
                            for (s = 0.0, k = l; k < nc; k++)
                                s += leftVectors[i][k] * rightVectors[k][j];
                            for (k = l; k < nc; k++)
                                rightVectors[k][j] += s * rightVectors[k][i];
                        } // end for j
                    } // end if g
                    for (j = l; j < nc; j++)
                        rightVectors[i][j] = rightVectors[j][i] = 0.0;
                } // end if i < nc-1
                rightVectors[i][i] = 1.0;
                g = tempDouble[i];
                l = i;
            } // end for i
            for (i = nc - 1; i >= 0; i--) {
                l = i + 1;
                g = singularValues[i];
                if (i < nc - 1)
                    for (j = l; j < nc; j++)
                        leftVectors[i][j] = 0.0;
                if (g != 0.0) {
                    g = 1.0 / g;
                    if (i != nc - 1) {
                        for (j = l; j < nc; j++) {
                            for (s = 0.0, k = l; k < nr; k++)
                                s += leftVectors[k][i] * leftVectors[k][j];
                            f = (s / leftVectors[i][i]) * g;
                            for (k = i; k < nr; k++)
                                leftVectors[k][j] += f * leftVectors[k][i];
                        } // end for j
                    } // end if i != nc-1
                    for (j = i; j < nr; j++)
                        leftVectors[j][i] *= g;
                } // end if g != 0
                else {
                    for (j = i; j < nr; j++)
                        leftVectors[j][i] = 0.0;
                } // end else
                ++leftVectors[i][i];
            } // end for i
            for (k = nc - 1; k >= 0; k--) {
                for (its = 1; its <= 30; its++) {
                    flag = 1;
                    for (l = k; l >= 0; l--) {
                        nm = l - 1;
                        if (Math.abs(tempDouble[l]) + anorm == anorm) {
                            flag = 0;
                            break;
                        } // end if
                        if (Math.abs(singularValues[nm]) + anorm == anorm)
                            break;
                    } // end for l
                    if (flag != 0) {
                        c = 0.0;
                        s = 1.0;
                        for (i = l; i <= k; i++) {
                            f = s * tempDouble[i];
                            if (Math.abs(f) + anorm != anorm) {
                                g = singularValues[i];
                                h = pythag(f, g);
                                singularValues[i] = h;
                                h = 1.0 / h;
                                c = g * h;
                                s = (-f * h);
                                for (j = 0; j < nr; j++) {
                                    y = leftVectors[j][nm];
                                    z = leftVectors[j][i];
                                    leftVectors[j][nm] = y * c + z * s;
                                    leftVectors[j][i] = z * c - y * s;
                                } // end for j
                            } // end if f
                        } // end for i
                    } // end if flag != 0
                    z = singularValues[k];
                    if (l == k) {
                        if (z < 0.0) {
                            singularValues[k] = -z;
                            for (j = 0; j < nc; j++)
                                rightVectors[j][k] = (-rightVectors[j][k]);
                        } // end if z is negative
                        break;
                    } // end if l == k
                    if (its == 30)
                        System.err.println(" sing_val:  no comvergence after 30 iterations!");
                    x = singularValues[l];
                    nm = k - 1;
                    y = singularValues[nm];
                    g = tempDouble[nm];
                    h = tempDouble[k];
                    f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
                    g = pythag(f, 1.0);
                    f = ((x - z) * (x + z) + h * ((y / (f + sign(g, f))) - h)) / x;
                    c = s = 1.0;
                    for (j = l; j <= nm; j++) {
                        i = j + 1;
                        g = tempDouble[i];
                        y = singularValues[i];
                        h = s * g;
                        g = c * g;
                        z = pythag(f, h);
                        tempDouble[j] = z;
                        c = f / z;
                        s = h / z;
                        f = x * c + g * s;
                        g = g * c - x * s;
                        h = y * s;
                        y = y * c;
                        for (jj = 0; jj < nc; jj++) {
                            x = rightVectors[jj][j];
                            z = rightVectors[jj][i];
                            rightVectors[jj][j] = x * c + z * s;
                            rightVectors[jj][i] = z * c - x * s;
                        } // end for jj
                        z = pythag(f, h);
                        singularValues[j] = z;
                        if (z != 0.0) {
                            z = 1.0 / z;
                            c = f * z;
                            s = h * z;
                        } // end if z != 0.0
                        f = (c * g) + (s * y);
                        x = (c * y) - (s * g);
                        for (jj = 0; jj < nr; jj++) {
                            y = leftVectors[jj][j];
                            z = leftVectors[jj][i];
                            leftVectors[jj][j] = y * c + z * s;
                            leftVectors[jj][i] = z * c - y * s;
                        } // end for jj
                    } // end for j
                    tempDouble[l] = 0.0;
                    tempDouble[k] = f;
                    singularValues[k] = x;
                } // end for its
            } // end for k

            bubble();
            if (transposeMode) {
                double[][] t = leftVectors;

                leftVectors = rightVectors;
                rightVectors = t;
            } // end if
        } /* end compute_svd */

//        private void checkSVD() {
//            long n = System.currentTimeMillis();
//
//            compute_svd();
//            n = System.currentTimeMillis() - n;
//
//            Matrix l = new Matrix(leftVectors);
//            double[][] s = new double[nc][nc];
//
//            for (int i = 0; i < nc; i++) {
//                for (int j = 0; j < i; j++)
//                    s[i][j] = s[j][i] = 0.0;
//                s[i][i] = singularValues[i];
//            } // end for i
//
//            Matrix m = new Matrix(s);
//            Matrix r = new Matrix(rightVectors);
//            Matrix a = new Matrix(theArray);
//
//            if (nc * nr < 31) {
//                System.out.println("Left singular vectors:\n" + l);
//                System.out.println("Diagonal singular value matrix:\n" + m);
//                System.out.println("Right singular vectors:\n" + r);
//            } // end if
//
//            Matrix p = Matrix.product(l, m);
//
//            p = Matrix.product(p, Matrix.transpose(r));
//
//            if (nr * nc < 100)
//                System.out.println("Decomposition product:\n" + p);
//
//            System.out.println("Error = " + Matrix.difference(a, p).l2norm());
//            System.out.println("Total time = " + n + "Milliseconds");
//        } // end checkSVD
        
    } // end class SVDUtilities
    
} // end class Matrix
