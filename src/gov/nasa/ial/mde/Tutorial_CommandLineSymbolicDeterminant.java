package gov.nasa.ial.mde;
/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Jan 10, 2005
 *
 * @author Dr. Robert Shelton
 */

import gov.nasa.ial.mde.math.SymbolicMatrix;
import gov.nasa.ial.mde.solver.symbolic.Polynomial;

public class Tutorial_CommandLineSymbolicDeterminant {

    public static void main(String[] args) {
        SymbolicMatrix sm = null;

        try {
            switch (args.length) {
            case 0:
                sm = new SymbolicMatrix("MatrixData.txt");
                break;

            case 1:
                sm = new SymbolicMatrix(args[0]);
                break;

            default:
                System.err.println("Usage: java SymbolicMatrix [NAME_OF_FILE_CONTAINING_MATRIX]");
            } // end switch

            System.out.println(sm);
            System.out.println("Determinant = " + new Polynomial(SymbolicMatrix.determinant(sm)));
        } // end try
        catch (Exception e) {
            e.printStackTrace();
        } // end catch
    }
}
