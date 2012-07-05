/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Nov 3, 2004.
 */
package gov.nasa.ial.mde.math;

import gov.nasa.ial.mde.io.ExpressionFileParser;
import gov.nasa.ial.mde.solver.symbolic.Expression;

import java.io.IOException;
import java.util.Arrays;

/**
 * The <code>SymbolicMatrix</code> represents a symbolic matrix.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class SymbolicMatrix {
    
    private Expression[][] theMatrix;

    private int numRows;
    private int numCols;

    @SuppressWarnings("unused")
	private SymbolicMatrix() {
        throw new RuntimeException("Default constructor not allowed");
    } // end SymbolicMatrix

    /**
     * Constructs a <code>SymbolicMatrix</code> from the values located in the
     * specified file.
     * 
     * @param fileName the file to load the symbolic matric values from.
     * @throws IOException is thrown if the file could not be loaded.
     */
    public SymbolicMatrix(String fileName) throws IOException {
        ExpressionFileParser p = new ExpressionFileParser(fileName);

        theMatrix = p.parseAsExpressionMatrix();
        numRows = theMatrix.length;
        numCols = theMatrix[0].length;
    } // end SymbolicMatrix

    /**
     * Constructs a <code>SymbolicMatrix</code> with the specified number of
     * rows and columns.
     * 
     * @param numRows the number of rows in the matrix.
     * @param numCols the number of columns in the matrix.
     */
    public SymbolicMatrix(int numRows, int numCols) {
        theMatrix = new Expression[this.numRows = numRows][this.numCols = numCols];
    } // end SymbolicMatrix

    /**
     * Returns the string representation of the symbolic matrix.
     * 
     * @return the string representation of the symbolic matrix.
     */
    public String toString() {
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < numRows; i++) {
            if (i > 0)
                b.append("\n");

            for (int j = 0; j < numCols; j++) {
                if (j > 0)
                    b.append(" ");
                b.append(theMatrix[i][j].toString());
            } // end for j
        } // end for i

        return b.toString();
    } // end toString

    /**
     * Returns the submatrix of this SymbolicMatrix obtained by striking out
     * prescribed rows and columns.
     * 
     * @param rowDeletes the rows to delete.
     * @param colDeletes the columns to delete.
     * @return the sub-matrix.
     */
    public SymbolicMatrix submatrix(int[] rowDeletes, int[] colDeletes) {
        int cdi, rdi;
        int m, n;
        int cdSize = colDeletes.length, rdSize = rowDeletes.length;
        SymbolicMatrix r = new SymbolicMatrix(numRows - rdSize, numCols - cdSize);

        Arrays.sort(colDeletes);
        Arrays.sort(rowDeletes);

        m = rdi = 0;
        for (int i = 0; i < numRows; i++) {
            if (rdi < rdSize) {
                if (i == rowDeletes[rdi]) {
                    rdi++;
                    continue;
                } // end if
            } // end outer if

            n = cdi = 0;
            for (int j = 0; j < numCols; j++) {
                if (cdi < cdSize) {
                    if (j == colDeletes[cdi]) {
                        cdi++;
                        continue;
                    } // end if
                } // end outer if

                r.theMatrix[m][n++] = theMatrix[i][j];
            } // end for j
            m++;
        } // end for i

        return r;
    } // end submatrix

    /**
     * Returns the determinant of the symbolic matrix as an expression.
     * 
     * @param m the matrix.
     * @return the determinant of the symbolic matrix as an expression.
     */
    public static Expression determinant(SymbolicMatrix m) {
        // System.out.println ("Finding determinant of\n" + m);

        if (m.numRows != m.numCols)
            throw new IllegalArgumentException("Determinant only defined for square matrices");

        int n = m.numRows;

        if (n == 1)
            return m.theMatrix[0][0];

        Expression r = new Expression("0");

        for (int i = 0; i < n; i++) {
            Expression c = SymbolicMatrix.determinant(m.submatrix(new int[] { 0 }, new int[] { i }));
            Expression p = c.product(m.theMatrix[0][i]);

            switch (i & 1) {
            case 0:
                r = r.sum(p);
                break;

            case 1:
                r = r.difference(p);
            } // end switch
        } // end for i

        return r;
    } // end determinant


//    public static void main(String[] args) {
//        SymbolicMatrix sm = null;
//
//        try {
//            switch (args.length) {
//            case 0:
//                sm = new SymbolicMatrix("../data.txt");
//                break;
//
//            case 1:
//                sm = new SymbolicMatrix(args[0]);
//                break;
//
//            default:
//                System.err.println("Usage: java SymbolicMatrix [NAME_OF_FILE_CONTAINING_MATRIX]");
//            } // end switch
//
//            System.out.println(sm);
//            System.out.println("Determinant = " + 
//                new gov.nasa.ial.mde.solver.symbolic.RationalExpression(
//                                SymbolicMatrix.determinant(sm)));
//            
////            System.out.println ("Submatrix:"); System.out.println
////            (sm.submatrix(new int[]{1,0}, new int[0])); System.out.println
////            ("Submatrix:"); System.out.println (sm.submatrix (new int[]{2},
////            new int[0])); System.out.println ("Determinant:");
////            System.out.println (SymbolicMatrix.determinant(sm.submatrix(new
////            int[]{2}, new int[0]))); System.out.println ("Submatrix:");
////            System.out.println (sm.submatrix(new int[]{2}, new int[]{1}));
//             
//        } // end try
//        catch (Exception e) {
//            e.printStackTrace();
//        } // end catch
//    }

}
