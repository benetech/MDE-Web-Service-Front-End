/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.io;

import gov.nasa.ial.mde.solver.symbolic.Expression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Parses a text file contining a matrix of expressions.
 *
 * @author Dr. Robert Shelton
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class ExpressionFileParser extends TextDataFileParser {

    private Expression[][] expressions = null;

    /**
     * Creates an instance of <code>ExpressionFileParser</code> that will
     * parse the specified <code>filename</code> for the expressions.
     * 
     * @param filename the specified name of the text file to parse the expressions
     *            from
     */
    public ExpressionFileParser(String filename) {
        super(filename);
    }

    /**
     * Creates an instance of <code>ExpressionFileParser</code> that will
     * parse the specified <code>File</code> object for the expressions.
     * 
     * @param file 
     *            the specified <code>File</code> object of the text file to
     *            parse the expressions from
     */
    public ExpressionFileParser(File file) {
        super(file);
    }

    /* (non-Javadoc)
     * @see gov.nasa.ial.mde.io.TextDataFileParser#clear()
     */
    public void clear() {
        super.clear();
        if (expressions != null) {
            int len = expressions.length;
            for (int i = 0; i < len; i++) {
                expressions[i] = null;
            }
            this.expressions = null;
        }
    }

    /**
     * Read the file as a matrix of Expressions Ignore headers for now because
     * they would look just like expressions
     * 
     * @return a two dimensional array of type <code>Expression</code>
     * @throws IOException
     *             is thrown for file Input/Output errors
     */
    public Expression[][] parseAsExpressionMatrix() throws IOException {
        analyzeFile();

        if (columnCnt < 1) {
            throw new IllegalArgumentException("File must have at least one column of expressions.");
        }

        dataRowCnt += headerRowCnt;
        headerRowCnt = 0;
        headers = null;

        // Use a default header name for the columns that do not have a header.
        checkHeaders();

        // Parse the file for the data.
        parseFileForExpressions();

        // Make sure we have some expressions.
        if ((expressions == null) || (expressions.length <= 0) || (expressions[0].length <= 0)) {
            throw new IllegalArgumentException("File does not contain any expressions.");
        }

        return expressions;
    } // end parseAsExpressionMatrix

    private void parseFileForExpressions() throws IOException {
        BufferedReader in = null;
        int row, col;
        String line;
        String[] columnValues;

        char delimValue = valueOf(delimType);

        expressions = new Expression[dataRowCnt][columnCnt];

        try {
            in = new BufferedReader(new FileReader(file));

            // Burn through the header to get to the line with the expressions on it.
            row = 0;
            while ((row < headerRowCnt) && ((line = in.readLine()) != null)) {
                line = line.trim();

                // Count only lines that are not empty/blank.
                if (line.length() > 0) {
                    row++;
                }
            }

            row = 0;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                // Skip blank lines.
                if (line.length() <= 0) {
                    continue;
                }

                columnValues = splitLine(line, delimValue);

                // Do a consistancy check against the expected number of columns.
                if ((columnValues == null) || (columnValues.length != columnCnt)) {
                    throw new IllegalArgumentException(
                            "Inconsistent number of columns in the expression file.");
                }

                for (col = 0; col < columnCnt; col++) {
                    // Use 0.0 for an empty/blank cell.
                    expressions[row][col] = (columnValues[col].length() > 0) ? new Expression(
                            columnValues[col]) : new Expression("0.0");
                }

                row++;
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

}
