/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Apr 23, 2004
 */
package gov.nasa.ial.mde.io;

import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The <code>TextDataFileParser</code> will parse a text data file with an
 * optional text header describing each column of data and with the data
 * arranged rows and columns. The result of parsing the text data file is a
 * <code>List</code> of <code>AnalyzedData</code> items where each entry in
 * the <code>List</code> corresponds to one segment of data and where each
 * segment corresponds to the chunk of data between a gaps in the values in the
 * first column of data. The <code>TextDataFileParser</code> will analyze the
 * text data file to determine what character is consistently used as a
 * delimiter between columns of data. This allows the
 * <code>TextDataFileParser</code> to parse most any text data file provided
 * that the delimiter is consistently used throughout the file.
 * <p>
 * The <code>TextDataFileParser</code> can also read text data files in the
 * Comma Separated Values (CSV) format, which is compatible with the CSV file
 * format used by Microsoft Excel.
 * <p>
 * Here is how the <code>TextDataFileParser</code> works:
 * <ol>
 * <li>The text data file must have at least two columns.</li>
 * <li>If a text header is exists, the number of columns in the text headers
 * must match the number of columns for the data.</li>
 * <li>It will skip blank lines in the header and data fields.</li>
 * <li>If a data cell is empty (for a given row and column, nothing is
 * specified but there are valid delimiters) it will use a value of 0.0 for it.</li>
 * <li>The first column of data must be in ascending order.</li>
 * <li>Duplicate entries in the first column of data are allowed.</li>
 * <li>If the gap between first column data values is greater than 2 times the
 * average difference between the values then a new segment of data is created.</li>
 * </ol>
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class TextDataFileParser implements FileParser {

    /** A referene to the file */
	File file;

    /** The number of columns of data. */
	int columnCnt;
    
    /** The number of rows of test header. */
	int headerRowCnt;
    
    /** The number of rows of data. */
	int dataRowCnt;
    
    /** The type of the delimiter. */
	int delimType;

    /** The text headers. */
	String[] headers;

	private double[][] data;

	private NumberFormat numberFormat = NumberFormat.getInstance();

	private static final boolean ENABLE_DATA_SEGMENTATION = true;

	/**
	 * Default constructor not allowed.
	 */
	@SuppressWarnings("unused")
	private TextDataFileParser() {
		throw new RuntimeException("Default constructor not allowed.");
	}

	/**
	 * Creates an instance of <code>TextDataFileParser</code> that will parse
	 * the specified <code>filename</code> for the data.
	 * 
	 * @param filename
	 *            the specified name of the text file to parse the data from
	 */
	public TextDataFileParser(String filename) {
		this(new File(filename));
	}

	/**
	 * Creates an instance of <code>TextDataFileParser</code> that will parse
	 * the specified <code>File</code> object for the data.
	 * 
	 * @param file
	 *            the specified <code>File</code> object of the text file to
	 *            parse the data from
	 */
	public TextDataFileParser(File file) {
		if (file == null) {
			throw new NullPointerException("Null file.");
		}
		if (!file.exists() || !file.isFile()) {
			throw new IllegalArgumentException(
					"The specified file does not exist or is an invalid file.");
		}
		this.file = file;
		clear();
	}

	/**
	 * Parse the file and return a <code>List</code> of
	 * <code>AnalyzedData</code> item's, where each entry in the
	 * <code>List</code> corresponds to one segment of data.
	 * 
	 * @return a <code>List</code> of <code>AnalyzedData</code> item's
	 * @throws IOException
	 *             is thrown for file Input/Output errors
	 * @throws ParseException
	 *             is thrown for parse errors
	 * @see gov.nasa.ial.mde.solver.symbolic.AnalyzedData
	 */
	public List<AnalyzedData> parse() throws IOException, ParseException {
		analyzeFile();

		if (columnCnt < 2) {
			throw new IllegalArgumentException("Data file must have at least two columns of data.");
		}

		// Parse the file for the column header information.
		parseFileForHeader();

		// Use a default header name for the columns that do not have a header.
		checkHeaders();

		// Parse the file for the data.
		parseFileForData();

		// Make sure we have some data.
		if ((data == null) || (data.length <= 0) || (data[0].length <= 0)) {
			throw new IllegalArgumentException("Data file does not contain any data.");
		}

		// For now, we only support the first column values being sorted in
		// ascending order.
		if (!isSortedInAscendingOrder(data[0])) {
			throw new IllegalArgumentException(
					"Data values in the first column must be sorted in ascending order.");
		}

		return getAnalyzedDataList();
	}

	/* (non-Javadoc)
	 * @see gov.nasa.ial.mde.io.FileParser#clear()
	 */
	public void clear() {
		this.columnCnt = 0;
		this.headerRowCnt = 0;
		this.dataRowCnt = 0;
		this.delimType = UNKNOWN_DELIM;
		this.headers = null;
		if (data != null) {
			int len = data.length;
			for (int i = 0; i < len; i++) {
				data[i] = null;
			}
			this.data = null;
		}
	}

	/* (non-Javadoc)
	 * @see gov.nasa.ial.mde.io.FileParser#dispose()
	 */
	public void dispose() {
		clear();
		this.numberFormat = null;
		this.file = null;
	}

	/**
     * Analyze the file to determine how many columns and rows of header and
     * data there are. It also verifies that there is a consistant number of
     * columns of data.
     * 
	 * @throws IOException thrown if the file could not be read.
	 */
	protected void analyzeFile() throws IOException {
		String line;
		String[] columnValues;
		BufferedReader in = null;
		boolean findColCnt = true;
		boolean findHeader = true;
		int row = 0;
		columnCnt = 0;
		headerRowCnt = 0;
		dataRowCnt = 0;
		delimType = UNKNOWN_DELIM;
		char delimValue = ',';

		try {
			in = new BufferedReader(new FileReader(file));

			while ((line = in.readLine()) != null) {
				line = line.trim();

				// Skip blank lines.
				if (line.length() <= 0) {
					continue;
				}

				// Determine what deliminator is being used.
				if (delimType == UNKNOWN_DELIM) {
					delimType = lineUsesWhichDelim(line);
					delimValue = valueOf(delimType);
				}

				columnValues = splitLine(line, delimValue);

				// Set the column count if we are looking for it.
				if (findColCnt) {
					findColCnt = false;
					columnCnt = columnValues.length;
				}

				// Do a consistancy check against the expected number of
				// columns.
				if ((columnValues == null) || (columnValues.length != columnCnt)) {
					throw new IllegalArgumentException(
							"Inconsistent number of columns in the data file.");
				}

				// We have reached the end of the header once we find a row of
				// all numbers.
				if (findHeader) {
					if (isAllNumbers(columnValues)) {
						findHeader = false;
						headerRowCnt = row;
						dataRowCnt++;
					}
				} else {
					dataRowCnt++;
				}
				row++;
			}

			// just in case it's all headers
			if (findHeader) {
				headerRowCnt = row;
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

	private void parseFileForHeader() throws IOException {
		BufferedReader in = null;
		int row, col;
		String line;
		String[] columnValues;
		char delimValue = valueOf(delimType);

		headers = new String[columnCnt];
		for (int i = 0; i < columnCnt; i++) {
			headers[i] = "";
		}

		try {
			in = new BufferedReader(new FileReader(file));

			row = 0;
			while ((row < headerRowCnt) && ((line = in.readLine()) != null)) {
				line = line.trim();

				// Skip blank lines.
				if (line.length() <= 0) {
					continue;
				}

				columnValues = splitLine(line, delimValue);

				// Do a consistancy check against the expected number of
				// columns.
				if ((columnValues == null) || (columnValues.length != columnCnt)) {
					throw new IllegalArgumentException(
							"Inconsistent number of columns in the data file.");
				}

				for (col = 0; col < columnCnt; col++) {
					if (columnValues[col].length() > 0) {
						if ((headers[col] == null) || (headers[col].length() <= 0)) {
							headers[col] = columnValues[col];
						} else {
							// Just append the column value to the header text.
							headers[col] += " " + columnValues[col];
						}
					}
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

	private void parseFileForData() throws IOException, ParseException {
		BufferedReader in = null;
		int row, col;
		String line;
		String[] columnValues;
		char delimValue = valueOf(delimType);

		data = new double[columnCnt][dataRowCnt];

		try {
			in = new BufferedReader(new FileReader(file));

			// Burn through the header to get to the line with the data on it.
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

				// Do a consistancy check against the expected number of
				// columns.
				if ((columnValues == null) || (columnValues.length != columnCnt)) {
					throw new IllegalArgumentException(
							"Inconsistent number of columns in the data file.");
				}

				for (col = 0; col < columnCnt; col++) {
					// Use 0.0 for an empty/blank cell.
					data[col][row] = (columnValues[col].length() > 0) ? numberFormat.parse(
							columnValues[col]).doubleValue() : 0.0;
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

	private List<AnalyzedData> getAnalyzedDataList() {
		if (columnCnt < 2) {
			throw new IllegalArgumentException("Data file must have at least two columns of data.");
		}
		AnalyzedData analyzedData;

		int[] segmentIndexes = calcSegmentIndexes();
		int segCount = (segmentIndexes != null) ? segmentIndexes.length : 0;
		int initialCapacity = Math.max(1, (segCount * (columnCnt - 1)));
		ArrayList<AnalyzedData> analyzedDataList = new ArrayList<AnalyzedData>(initialCapacity);

		if (segCount <= 1) {
			// One data segment means that there were no breaks/holes in the
			// data.
			for (int col = 1; col < columnCnt; col++) {
				analyzedData = new AnalyzedData(headers[0], headers[col], data[0], data[col]);
				analyzedDataList.add(analyzedData);
			}
		} else {
			int s, segStartIndex, segLength;
			double[] dataSeg1, dataSeg2;

			// Create an analyzed data object for each segment and column of the
			// data
			// excluding the first column (independent variable).
			for (int col = 1; col < columnCnt; col++) {
				for (s = 0; s < segCount; s++) {
					segStartIndex = (s > 0) ? (segmentIndexes[s - 1] + 1) : 0;
					segLength = (segmentIndexes[s] - segStartIndex) + 1;

					if (segLength > 0) {
						dataSeg1 = new double[segLength];
						dataSeg2 = new double[segLength];

						System.arraycopy(data[0], segStartIndex, dataSeg1, 0, segLength);
						System.arraycopy(data[col], segStartIndex, dataSeg2, 0, segLength);

						analyzedData = new AnalyzedData(headers[0], headers[col], dataSeg1,
								dataSeg2);
						analyzedDataList.add(analyzedData);
					}
				}
			}
		}
		return analyzedDataList;
	}

	// Determine where the breaks in the data are if the data is segmented.
	private int[] calcSegmentIndexes() {
		int[] segmentIndexes = null;
		if (ENABLE_DATA_SEGMENTATION) {
			double[] dataArray = data[0];
			int len = (dataArray != null) ? dataArray.length : 0;

			// There is no data to analyzed so return a zero length array.
			if (len == 0) {
				return new int[0];
			}

			// Double the average distance between x-values
			double doubleAvgStepSize = 2.0 * Math.abs(dataArray[len - 1] - dataArray[0]) / (len - 1.0);

			// We declare a segment anytime we have a point that steps more
			// than two times the average step size away from the previous
			// point.
			ArrayList<Integer> segList = new ArrayList<Integer>(10);
			for (int i = 1; i < len; i++) {
				if (Math.abs(dataArray[i] - dataArray[i - 1]) > doubleAvgStepSize) {
					segList.add(new Integer(i - 1));
				}
			}

			// If the segment list is empty then just return the index to the
			// last point since we only have one segment.
			if (segList.isEmpty()) {
				return new int[] { (len - 1) };
			}

			// Make sure we include the index to the last data point.
			Integer lastSeg = segList.get(segList.size() - 1);
			if (lastSeg.intValue() != (len - 1)) {
				segList.add(new Integer(len - 1));
			}

			// Create the integer array of the segment indexes.
			segmentIndexes = new int[segList.size()];
			for (int i = 0; i < segmentIndexes.length; i++) {
				segmentIndexes[i] = segList.get(i).intValue();
			}

			// Done with the list.
			segList.clear();
		} else {
			// If data segmentation is disabled then just return the index
			// to the last item in the data array.
			if (data[0] != null) {
				segmentIndexes = new int[] { (data[0].length - 1) };
			}
		}
		return (segmentIndexes != null) ? segmentIndexes : (new int[0]);
	}

	private boolean isSortedInAscendingOrder(double[] d) {
		if ((d == null) || (d.length <= 1)) {
			return true;
		}
		double[] tmp = new double[d.length];
		System.arraycopy(d, 0, tmp, 0, d.length);
		Arrays.sort(tmp);
		return Arrays.equals(d, tmp);
	}
	
	/**
	 * Use a default header name for the columns that did not have a header.
	 */
	protected void checkHeaders() {
		if (headers == null) {
			headers = new String[columnCnt];
		}
		for (int i = 0; i < columnCnt; i++) {
			if ((headers[i] == null) || (headers[i].length() <= 0)) {
				headers[i] = "Column-" + (i + 1);
			}
		}
	}

	/**
     * Splits the line based on the delimiter.
     * 
	 * @param line the input line.
	 * @param delim the delimiter for splitting.
	 * @return the split up line.
	 */
	protected String[] splitLine(String line, char delim) {
		int len = line.length();
		int pos = 0;
		char ch;
		boolean processingQuotes;
		StringBuffer strBuff = new StringBuffer(32);
		ArrayList<String> results = new ArrayList<String>(10);

		// Handle the special case of a comma delim and the line starting with
		// an
		// empty cell, which means the line started with a comma.
		if ((delim == ',') && line.startsWith(",")) {
			results.add("");
		}

		while (pos < len) {
			// The start of a column.
			strBuff.setLength(0);

			if (delim == ',') {
				// Move past the delim character.
				if ((pos < len) && (line.charAt(pos) == delim)) {
					pos++;
				}
			} else {
				// Burn any leading delims while we are at the start of a
				// column.
				while ((pos < len) && (line.charAt(pos) == delim)) {
					pos++;
				}
			}

			// Burn any leading whitespace in the column that is not our delim.
			while ((pos < len) && ((ch = line.charAt(pos)) != delim) && Character.isWhitespace(ch)) {
				pos++;
			}

			// Extract the characters in the column until we reach the next
			// delim character or the end of the line.
			if (pos < len) {
				ch = line.charAt(pos);

				// If we find a " character at the start of this columns' data
				// then we
				// need to do special processing of the quotes including escaped
				// quotes.
				if (ch == '"') {
					processingQuotes = true;
					pos++; // Point to the next char after the first "
					// character

					while ((pos < len) && (processingQuotes || (line.charAt(pos) != delim))) {
						ch = line.charAt(pos);

						if (ch == '"') {
							// If the next character is a " then it was escaped.
							// (i.e. "")
							if ((pos + 1 < len) && (line.charAt(pos + 1) == '"')) {
								strBuff.append('"'); // escaped quote
								pos++;
							} else {
								// This is a quote by it's self, so we are
								// either done
								// processing quotes, or we need to start
								// processing again.
								processingQuotes = !processingQuotes;
							}
						} else {
							strBuff.append(ch);
						}
						pos++;
					}
				} else {
					// Column data is all the characters until the next delim or
					// end of line.
					while ((pos < len) && ((ch = line.charAt(pos)) != delim)) {
						strBuff.append(ch);
						pos++;
					}
				}
				results.add(strBuff.toString().trim());
			}
		}

		// Handle the special case of a comma delim and the line ending with an
		// empty cell, which means the line ended with a comma.
		if ((delim == ',') && line.endsWith(",")) {
			results.add("");
		}

		String[] returnVal = results.toArray(new String[results.size()]);
		results.clear();

		return returnVal;
	}

	/**
     * Returns the character for the specified delimiter type.
     * 
	 * @param delimiterType the delimiter type.
	 * @return the character for the delimiter type.
	 */
	protected char valueOf(int delimiterType) {
		switch (delimiterType) {
		case COMMA_DELIM:
			return ',';
		case TAB_DELIM:
			return '\t';
		case NEWLINE_DELIM:
			return '\n';
		case VERTICAL_TAB_DELIM:
			return VERTICAL_TAB;
		case FORM_FEED_DELIM:
			return '\f';
		case CARRIAGE_RETURN_DELIM:
			return '\r';
		case SPACE_DELIM:
			return ' ';
		}
		throw new IllegalArgumentException("Unknown Data Delimiter.");
	}

	private int lineUsesWhichDelim(String line) {
		if (characterCountFor(line, ',') > 0) {
			return COMMA_DELIM;
		}
		if (characterCountFor(line, '\t') > 0) {
			return TAB_DELIM;
		}
		if (characterCountFor(line, '\n') > 0) {
			return NEWLINE_DELIM;
		}
		if (characterCountFor(line, VERTICAL_TAB) > 0) {
			return VERTICAL_TAB_DELIM;
		}
		if (characterCountFor(line, '\f') > 0) {
			return FORM_FEED_DELIM;
		}
		if (characterCountFor(line, '\r') > 0) {
			return CARRIAGE_RETURN_DELIM;
		}
		if (characterCountFor(line, ' ') > 0) {
			return SPACE_DELIM;
		}
		return UNKNOWN_DELIM;
	}

	private int characterCountFor(String line, char ch) {
		int count = 0;
		int len = line.length();
		for (int i = 0; i < len; i++) {
			if (line.charAt(i) == ch) {
				count++;
			}
		}
		return count;
	}

	// @return true if all the strings are valid numbers, false otherwise.
	private boolean isAllNumbers(String[] values) {
		int len = values.length;
		try {
			for (int i = 0; i < len; i++) {
				if (values[i].length() > 0) {
					numberFormat.parse(values[i]).doubleValue();
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
