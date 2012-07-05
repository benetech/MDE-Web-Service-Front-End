/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.describer;

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solution;
import gov.nasa.ial.mde.solver.SolvedGraph;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.util.LocalResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.BreakIterator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * <code>Describer</code> works with a MDE <code>Solver</code> object to
 * create natural-language text descriptions of graphs. Describer provides
 * methods for setting different <em>description modes</em> and for specifying
 * the <em>output format</em>.
 * <p>
 * <code>Describer</code> supports two default description modes - VISUAL or
 * MATH. The VISUAL mode provides qualitative descriptions without much
 * mathematical jargon. The MATH mode provides a description of the mathematical
 * solution.  A third has be added, called STANDARD, which incorperates descriptions 
 * suited for the Georgia Performance Standards.
 * <p>
 * <code>Describer</code> also lets you define new description modes by
 * specifying a new mode name and an XSLT file for transforming the MDE (XML)
 * solution to a textual description.
 * <p>
 * Two output formats are available for textual descriptions - TEXT_OUTPUT or
 * HTML_OUTPUT. Select the HTML format if you will be displaying MDE
 * descriptions to a browser or other display component that supports HTML.
 * <p>
 * <code>Describer</code> uses a <code>MdeSettings</code> object to get
 * and set description mode changes.
 * <p>
 * <a href="http://prime.jsc.nasa.gov/MDE">Math Description Engine Programmers
 * Guide </a>
 * 
 * @author Dat Truong
 * @author Terry Hodgson
 * @version 1.0
 */
public class Describer {

    /**
     * HTML output indicator
     */
    public static final String HTML_OUTPUT  = "html";

    /**
     * text output indicator
     */
    public static final String TEXT_OUTPUT  = "text";

    /**
     * XML output format is not implemented yet.
     */
    public static final String XML_OUTPUT   = "xml";

    /**
     * visual description mode indicator
     */
    public static final String VISUAL       = "visual";

    /**
     * math description mode indicator
     */
    public static final String MATH         = "math";
    
    /**
     * educational standards description mode indicator
     */
    public static final String STANDARDS         = "standards";

    /**
     * MDE <code>Solver</code> object which provides the graph data to be
     * described textually.
     */
    private Solver             solver;

    /**
     * Comment for <code>tFactory</code>
     */
    private TransformerFactory tFactory;

    /**
     * Table of initialized XSLT transformers. One per description mode.
     */
    
	private Hashtable<String, Transformer>          transformers;

    /**
     * Comment for <code>currentDescriptionMode</code>
     */
    private String             currentDescriptionMode;

    /**
     * Comment for <code>currentTransformer</code>
     */
    private Transformer        currentTransformer = null;

    /**
     * Comment for <code>currentOutputFormat</code>
     */
    private String             currentOutputFormat;

    /**
     * Number of words per line for text format
     */
    private int                wordsPerLine;

    // Default constructor not allowed.
    @SuppressWarnings("unused")
	private Describer() {
        throw new RuntimeException("Default constructor not allowed.");
    }

    /**
     * Construct a Describer object with the given <code>Solver</code> object.
     * An instance of MdeSettings will be created internally. Use default XSLT
     * template files.
     * 
     * @param solver the solver to use with this describer.
     */
    public Describer(Solver solver) {
        this(solver, new MdeSettings());
    }

    /**
     * Construct a Describer object with the given <code>Solver</code> object
     * and <code>MdeSettings</code> object. Use default XSLT template files.
     * 
     * @param solver the solver to use with this desriber.
     * @param settings the MDE settings.
     */
    public Describer(Solver solver, MdeSettings settings) {
        //set defaults
        this.solver = solver;
        this.currentOutputFormat = TEXT_OUTPUT;
        this.wordsPerLine = 15;

        configureTranformerFactory();

        //TODO: The template/mode defaults should probably be set in MdeSettings

        transformers = new Hashtable<String, Transformer>();
        addDescriptionMode("visual", "mdeApplyVisual1.xsl");
        addDescriptionMode("math", "mdeApplyMath1.xsl");
        addDescriptionMode("standards", "mdeApplyStandards1.xsl");

        this.currentDescriptionMode = settings.getDescriptionMode();
        this.currentTransformer = transformers.get(currentDescriptionMode);
    }

    /**
     * Return the MDE description for this equation using the current
     * description mode.
     * 
     * @param equation equation to be described
     * @return the MDE description for this equation using the current
     * 			  description mode.
     */
    public String getDescription(String equation) {
        // Use current description mode
        String result = null;

        Solution[] sol = solver.get(equation);
        if ((sol != null) && (sol.length > 0)) {
            if (MdeSettings.DEBUG) {
                System.out.println(getClass().getName() + ".getDescription() solution is not null");
            }

            StringBuffer sb = new StringBuffer(128);
            sb.append("\n<MDE>");
            SolvedGraph sg = sol[0].getFeatures();
            if (sg != null) {
                String sgxml = sg.getXMLString();
                sb.append(sgxml);
            }
            sb.append("\n</MDE>");
            result = transformXML(sb.toString());
        }
        return result;
    }

    /**
     * Return the MDE description for this equation using the given description
     * mode.
     * 
     * @param equation the equation as a string.
     * @param mode either "visual" or "math".
     * @return the MDE description for this equation using the given description mode.
     */
    public String getDescription(String equation, String mode) {

        //If mode has changed, get appropriate Transformer
        if (!currentDescriptionMode.equals(mode)) {
            setCurrentDescriptionMode(mode);
        }
        return getDescription(equation);
    }

    /**
     * Return MDE descriptions for all items in the Solver object's solution
     * list. Use the given description mode.
     * 
     * @param mode either "visual" or "math".
     * @return MDE descriptions for all items in the Solver object's solution list.
     */
    public String getDescriptions(String mode) {

        //If mode has changed, get appropriate Transformer
        if (!currentDescriptionMode.equals(mode)) {
            setCurrentDescriptionMode(mode);
        }

        return getDescriptions();
    }

    /**
     * Return MDE descriptions for all items in the Solver object's solution
     * list. Use the current description mode.
     * 
     * @return MDE descriptions for all items in the Solver object's solution list.
     */
    public String getDescriptions() {
        String xmlToTransform;
        String result;

        xmlToTransform = getFeatureXML(); // magic
        //  	System.out.println(xmlToTransform);
        result = transformXML(xmlToTransform);
        return result;
    }

    /**
     * Change the output format to the requested type for each description mode
     * transformer.
     * 
     * @param outputFormat
     *            the desired output format - text or html
     */
    public void setOutputFormat(String outputFormat) {
        //TODO: handle invalid outputFormat
        boolean notXML = (outputFormat.equals(TEXT_OUTPUT) || outputFormat.equals(HTML_OUTPUT));
        this.currentOutputFormat = outputFormat;

        if (MdeSettings.DEBUG) {
            System.out.println(getClass().getName() + ".setOutputFormat() " + outputFormat);
        }
        Transformer tf;
        Enumeration<Transformer> en = transformers.elements();
        while (en.hasMoreElements()) {
            tf = en.nextElement();
            if (notXML) {
                omitXMLDeclaration(tf, true);
            } else {
                omitXMLDeclaration(tf, false);
            }
            tf.setOutputProperty(OutputKeys.METHOD, outputFormat);
        }
    }

    /**
     * Return the MDE XML String for all items in the Solver object's solution
     * list which have a showGraph=true.
     * 
     * @return The feature in XML form.
     */
    private String getFeatureXML() {
        //Build the XML stream:
        StringBuffer b = new StringBuffer(128);
        b.append("\n<MDE>");

        // Get the XML block from the features.
        Solution solution;
        SolvedGraph features;
        for (@SuppressWarnings("rawtypes")
		Iterator iter = solver.getSolutionIterator(); iter.hasNext();) {
            solution = (Solution) iter.next();

            // Only describe the solutions that are graphed.
            if (solution.isShowGraph()) {
                features = solution.getFeatures();
                if (features != null) {
                    b.append(features.getXMLString());
                }
            }
        }

        b.append("\n</MDE>");

//        if (MdeConstants.DEBUG) {
//    	      System.out.println(getClass().getName()+".getFeatureXML()"+b.toString());
//    	  }

        return b.toString();
    }

    /**
     * Not yet supported. Lets you specify your own text description mode with
     * corresponding XSLT description templates file.
     * 
     * @param modeName either "visual" or "math".
     * @param xslFilename the name of the XSL file.
     */
    public void addDescriptionMode(String modeName, String xslFilename) {
        //TODO: handle other than default path to xsl
        //TODO: check for/handle duplicate mode names...
        //Map transformer to description mode
        Transformer tf = getTransformer(xslFilename);
        transformers.put(modeName, tf);
    }

    /**
     * Change the description mode to the specified value.
     * 
     * @param mode either "visual" or "math" or "standards".
     */
    public void setCurrentDescriptionMode(String mode) {
        //TODO: We could handle an invalid mode condition better than we do.
        Transformer tf = transformers.get(mode);
        if (tf == null) {
            if (MdeSettings.DEBUG) {
                System.out.println("Invalid description mode. Previously set mode will be used.");
            }
        } else {
            currentTransformer = tf;
            currentDescriptionMode = mode;
        }
    }

    /**
     * Return the current description mode value.
     * 
     * @return the current description mode value.
     */
    public String getCurrentDescriptionMode() {
        return currentDescriptionMode;
    }

    private void configureTranformerFactory() throws TransformerFactoryConfigurationError {
        tFactory = TransformerFactory.newInstance();

        // Use a resource resolver to find the resources in the resources 
        // "/resources/" path of the Jar file. DDexter 1/19/2004
        tFactory.setURIResolver(new LocalResourceResolver(MdeSettings.RESOURCES_PATH));
    }

    private Transformer getTransformer(String xslFilename) {
        Transformer tf = null;
        try {
            if (MdeSettings.DEBUG) {
                System.out.println(getClass().getName() + ".getTransformer() Get resource \"" + xslFilename
                        + "\"");
            }

            LocalResourceResolver resolver = (LocalResourceResolver) tFactory.getURIResolver();
            tf = tFactory.newTransformer(resolver.resolve(xslFilename, null));
        } catch (Exception e) {
            if (MdeSettings.DEBUG) {
                System.out.println("Failed to initialize Transformer, styleSheet is !" + xslFilename + "!");
            }
            System.out.println(e);
            e.printStackTrace(System.out);
        }
        return tf;
    }

    /**
     * Specify whether the (XSLT-transformed XML) text description output should
     * include an XML declaration.
     * 
     * @param tf - the Transformer of
     * @param flag true to omit XML declaration, flase to include it.
     */
    private void omitXMLDeclaration(Transformer tf, boolean flag) {
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, flag ? "yes" : "no");
        if (MdeSettings.DEBUG) {
            System.out.println(getClass().getName() + ".omitXMLDeclaration() OMIT_XML_DEC: "
                    + tf.getOutputProperty(OutputKeys.OMIT_XML_DECLARATION));
        }
    }

    private String transformXML(String xmlData) {
        String finalResult = "";

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            currentTransformer.transform(new StreamSource(new ByteArrayInputStream(xmlData.getBytes())),
                    new StreamResult(result));
            String resultStr = result.toString();
            if(MdeSettings.DEBUG)
            {

                System.out.println("transformed Str = "+resultStr);
            }

            if (currentOutputFormat.equals(TEXT_OUTPUT)) {
                finalResult = cleanUpText(resultStr, 40);
            } else {
                finalResult = resultStr;
            }

        } catch (Exception e) {
            System.out.println("Failed to transform XML string: !" + xmlData + "!");
            System.out.println(e);
            e.printStackTrace();
        }
        return finalResult;
    }

    private String cleanUpText(String result1, int textLineLength) {
        int i = 0;

        //If it's HTML, we don't want the tags escaped. HTML viewers *should*
        // properly convert &lt; and &gt; (less than and greater than math
        // symbols)
        while ((i = result1.indexOf("&lt;")) > -1) {
            result1 = result1.substring(0, i) + "<" + result1.substring(i + 4);
        }
        while ((i = result1.indexOf("&gt;")) > -1) {
            result1 = result1.substring(0, i) + ">" + result1.substring(i + 4);
        }

        StringTokenizer bt = new StringTokenizer(result1, "\n");
        StringBuffer bb = new StringBuffer(result1.length());
        while (bt.hasMoreTokens()) {
            //bb.append(" ").append(bt.nextToken().trim());
            bb.append(bt.nextToken().trim()).append(' ');
        }
        String result2 = bb.toString();

        //This doesn't seem to work like it should! It's breaking a line after
        // every word.
        BreakIterator lines = BreakIterator.getLineInstance(Locale.US);
        lines.setText(result2);
        StringBuffer result3 = new StringBuffer(result2.length());

        //So let's kludge it:
        //int wordCount = 0;
        int start = lines.first();
        for (int end = lines.next(); end != BreakIterator.DONE; start = end, end = lines.next()) {
            //wordCount++;
            result3.append(result2.substring(start, end));
           /* if (wordCount == wordsPerLine) {
                result3.append("\n");
                wordCount = 0;
            }*/
        }

        return result3.toString();
    }

    /**
     * Return the number of words per line for TEXT descriptions. The default
     * value is 15 words per line.
     * 
     * @return the words per line in text description outputs.
     */
    public int getWordsPerLine() {
        return wordsPerLine;
   }

    /**
     * Set the number of words per line for TEXT descriptions. The default value
     * is 15 words per line.
     * 
     * @param wordsPerLine
     *            the wordsPerLine to set for text output
     */
    public void setWordsPerLine(int wordsPerLine) {
        this.wordsPerLine = wordsPerLine;
    }

} // end class Describer
