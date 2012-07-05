/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.properties;

import gov.nasa.ial.mde.util.ArrayUtil;

import java.awt.Color;
import java.util.Properties;

/**
 * MdeSettings manages initialization and storage of MDE description, sound,
 * and graphing properties (options), such as: line colors and thicknesses for
 * CartesianGraph, the description mode for Describer, and sonification
 * properties for Sounder. Properties files created by MdeSettings can be used
 * to store application or end-user preferences between application runs.
 * <p>
 * CartesianGraph properties and initial defaults are:
 * <blockquote>axisColor = Color.white; <br>
 * backgroundColor = Color.black; <br>
 * gridColor = Color.magenta; <br>
 * lineColor = Color.yellow; <br>
 * lineSize = 2; // medium <br>
 * dataPointColor = Color.red; <br>
 * dataPointsShown = true; <br>
 * autoscaleGraph = true; <br>
 * traceOn = true; <br>
 * </blockquote> Describer properties and initial defaults are:
 * <blockquote>descriptionMode = "standards"; </blockquote> Sounder properties and
 * initial defaults are: <blockquote>negativeYValuesIndicator = HISS <br>
 * sonificationWaveform = TRIANGLE <br>
 * traceSweepSpeed = MEDIUM <br>
 * xAxisIndicator = NO_INDICATION <br>
 * xAxisIndicatorDuration = MEDIUM <br>
 * xAxisIndicatorFrequency = MEDIUM <br>
 * yAxisIndicator = DING <br>
 * yAxisIndicatorDuration = MEDIUM <br>
 * yAxisIndicatorFrequency = MEDIUM</blockquote>
 * 
 * Properties files created by MdeSettings will be stored in the application
 * end-user's home directory, for example, within the Documents and Settings
 * folder on Windows Operating Systems. MdeSettings uses Java's
 * System.getProperty method to retrieve the path to the user's home directory:
 * 
 * <blockquote>String folder = System.getProperty("user.home"); </blockquote>
 * 
 * If you do not specify a filename on construction, <blockquote>MdeSettings
 * myMdeSettings = new MdeSettings(); </blockquote> MdeSettings will write
 * the properties to a file named MDE_Properties.properties (if the
 * application/user changes one of the defaults).
 * <p>
 * If you do specify a filename on construction, <blockquote>MdeSettings
 * myMdeSettings = new MdeSettings("myMDEProperties.txt"); </blockquote> then
 * MdeSettings will attempt to initialize properties from this file. If the
 * file doesn't exist, MDE uses the default properties ("factory settings").
 * <p>
 * MDE will write to the properties file each time the
 * application/user changes a property value. This occurs automatically. The
 * application does not need to explicitly invoke a save with the
 * saveParameters() method. <blockquote>
 * 
 * @version 1.0
 */
public class MdeSettings extends Settings {

    private Color axisColor;

    private Color backgroundColor;

    private Color gridColor;

    private Color lineColor;

    private Color dataPointColor;

    private boolean dataPointsShown;

    private boolean autoscaleGraph;

    private int lineSize;

    private boolean traceOn;

    private String descriptionMode;

    // One of the values from the BASIC_WAVEFORMS array
    private int sonificationWaveform;

    // One of the values fom the Y_AXIS_INDICATORS array
    private int yAxisIndicator;

    // One of the values fom the INDICATOR_FREQUENCIES and INDICATOR_DURATIONS arrays
    private int yAxisIndicatorFreq;

    private int yAxisIndicatorDuration;

    // Any combination of values from the NEG_Y_VALUES_INDICATORS array
    private int negYValuesIndicator;

    // One of the values fom the X_AXIS_INDICATORS array
    private int xAxisIndicator;

    // One of the values fom the INDICATOR_FREQUENCIES and INDICATOR_DURATIONS arrays
    private int xAxisIndicatorFreq;

    private int xAxisIndicatorDuration;

    // One of the values fom the TRACE_SWEEP_SPEEDS array
    private int traceSweepSpeed;
    
    /** Turn Debug comments on or off by setting the value to true or false respectively. */
    public static final boolean DEBUG = false;
    
    /** Turns <= and the like into their verbal equivalents */
    public static boolean ACCESSIBLE_TTS = false;
    
    /** Default setting for ACCESSIBLE_TTS*/
    public static final boolean ACCESSIBLE_TTS_DEFAULT = false;
    
    /** The MDE application version number. */
    public static final String  VERSION = "2.3.7";
    
    /** The build number which is updated by the Ant build script. */
    public static final String  BUILD_NUMBER = "201204111440";
    
    /** true if this is a Beta release, false if it is not */
    public static final boolean BETA_RELEASE = false;
    
    /** The Beta version i.e. 2 for Beta-2 */
    public static final String  BETA_VERSION = "1";

    /** The application name */
    public static final String  APP_NAME = "MDE" + 
                                (BETA_RELEASE ? " (Beta-"+BETA_VERSION+")" : "");

    /** The path to the MDE resources such as images, XSL, and HTML files. */
    //public static final String  RESOURCES_PATH = "/resources/";
    public static final String  RESOURCES_PATH = "gov/nasa/ial/mde/util/res/resources/";
    
    /** Array of parameters and default values represented as strings */
    public static final String[][] PARAMETER_STRINGS = {
            { "a", "1" },
            { "b", "1" },
            { "c", "1" },
            { "d", "1" },
            { "e", "2.718281828459045" },
            { "f", "1" },
            { "g", "1" },
            { "h", "0" },
            { "k", "0" },
            { "m", "1" }
    };
    
    /** The integer value representing "No Indication". */
    public static final int NO_INDICATION = 1;

    /** The integer value representing a "Ding". */
    public static final int DING = (NO_INDICATION << 1);

    /** The integer value representing a "Chirp". */
    public static final int CHIRP = (DING << 1);

    /** The integer value representing "Hiss". */
    public static final int HISS = (CHIRP << 1);

    /** The integer value representing a "Tambor Change". */
    public static final int TAMBOR_CHANGE = (HISS << 1);

    /** The integer value representing a "Sine" waveform. */
    public static final int SINE = (TAMBOR_CHANGE << 1);

    /** The integer value representing a "Triangle" waveform. */
    public static final int TRIANGLE = (SINE << 1);

    /** The integer value representing a "Saw" waveform. */
    public static final int SAW = (TRIANGLE << 1);

    /** The integer value representing a "Square" waveform. */
    public static final int SQUARE = (SAW << 1);

    /** The integer value representing "Variable". */
    public static final int VARIABLE = (SQUARE << 1);

    /** The integer value representing "Low". */
    public static final int LOW = (VARIABLE << 1);

    /** The integer value representing "Medium". */
    public static final int MEDIUM = (LOW << 1);

    /** The integer value representing "High". */
    public static final int HIGH = (MEDIUM << 1);

    /** The integer value representing "Short". */
    public static final int SHORT = (HIGH << 1);

    /** The integer value representing "Long". */
    public static final int LONG = (SHORT << 1);

    /** The integer value representing "Slow". */
    public static final int SLOW = (LONG << 1);

    /** The integer value representing "Fast". */
    public static final int FAST = (SLOW << 1);

    /** The string representation of the <code>NO_INDICATION</code> integer value. */
    public static final String NO_INDICATION_STRING = "No indication";

    /** The string representation of the <code>DING</code> integer value. */
    public static final String DING_STRING = "Ding";

    /** The string representation of the <code>CHIRP</code> integer value. */
    public static final String CHIRP_STRING = "Chirp";

    /** The string representation of the <code>HISS</code> integer value. */
    public static final String HISS_STRING = "Hiss";

    /** The string representation of the <code>TAMBOR_CHANGE</code> integer value. */
    public static final String TAMBOR_CHANGE_STRING = "Buzz";

    /** The string representation of the <code>SINE</code> integer value. */
    public static final String SINE_STRING = "Sine";

    /** The string representation of the <code>TRIANGLE</code> integer value. */
    public static final String TRIANGLE_STRING = "Triangle";

    /** The string representation of the <code>SAW</code> integer value. */
    public static final String SAW_STRING = "Saw";

    /** The string representation of the <code>SQUARE</code> integer value. */
    public static final String SQUARE_STRING = "Square";

    /** The string representation of the <code>VARIABLE</code> integer value. */
    public static final String VARIABLE_STRING = "Variable";

    /** The string representation of the <code>LOW</code> integer value. */
    public static final String LOW_STRING = "Low";

    /** The string representation of the <code>MEDIUM</code> integer value. */
    public static final String MEDIUM_STRING = "Medium";

    /** The string representation of the <code>HIGH</code> integer value. */
    public static final String HIGH_STRING = "High";

    /** The string representation of the <code>SHORT</code> integer value. */
    public static final String SHORT_STRING = "Short";

    /** The string representation of the <code>LONG</code> integer value. */
    public static final String LONG_STRING = "Long";

    /** The string representation of the <code>SLOW</code> integer value. */
    public static final String SLOW_STRING = "Slow";

    /** The string representation of the <code>FAST</code> integer value. */
    public static final String FAST_STRING = "Fast";

    /**
     * The integer values representing the basic waveforms which are
     * <code>SINE, TRIANGLE, SAW, SQUARE, VARIABLE</code>.
     * @see #SINE
     * @see #TRIANGLE
     * @see #SAW
     * @see #SQUARE
     * @see #VARIABLE
     */
    public static final int[] BASIC_WAVEFORMS = { SINE, TRIANGLE, SAW, SQUARE, VARIABLE };

    /**
     * The string values representing the basic waveforms which are
     * <code>SINE_STRING, TRIANGLE_STRING, SAW_STRING, SQUARE_STRING,
     * VARIABLE_STRING</code>.
     * @see #SINE_STRING
     * @see #TRIANGLE_STRING
     * @see #SAW_STRING
     * @see #SQUARE_STRING
     * @see #VARIABLE_STRING
     */
    public static final String[] BASIC_WAVEFORM_STRINGS = { SINE_STRING,
                TRIANGLE_STRING, SAW_STRING, SQUARE_STRING, VARIABLE_STRING };

    /**
     * The integer values representing the negative y values indicators which are
     * <code>HISS, TAMBOR_CHANGE</code>.
     * @see #HISS
     * @see #TAMBOR_CHANGE
     */
    public static final int[] NEG_Y_VALUES_INDICATORS = { HISS, TAMBOR_CHANGE };

    /**
     * The string values representing the negative y values indicators which are
     * <code>HISS_STRING, TAMBOR_CHANGE_STRING</code>.
     * @see #HISS_STRING
     * @see #TAMBOR_CHANGE_STRING
     */
    public static final String[] NEG_Y_VALUES_INDICATOR_STRINGS = {
                HISS_STRING, TAMBOR_CHANGE_STRING };

    /**
     * The integer values representing the y-axis indicators which are
     * <code>DING, NO_INDICATION</code>.
     * @see #DING
     * @see #NO_INDICATION
     */
    public static final int[] Y_AXIS_INDICATORS = { DING, NO_INDICATION };

    /**
     * The string values representing the y-axis indicators which are
     * <code>DING_STRING, NO_INDICATION_STRING</code>.
     * @see #DING_STRING
     * @see #NO_INDICATION_STRING
     */
    public static final String[] Y_AXIS_INDICATOR_STRINGS = {
                DING_STRING, NO_INDICATION_STRING };

    /**
     * The integer values representing the x-axis indicators which are
     * <code>CHIRP, NO_INDICATION</code>.
     * @see #CHIRP
     * @see #NO_INDICATION
     */
    public static final int[] X_AXIS_INDICATORS = { CHIRP, NO_INDICATION };

    /**
     * The string values representing the x-axis indicators which are
     * <code>CHIRP_STRING, NO_INDICATION_STRING</code>.
     * @see #CHIRP_STRING
     * @see #NO_INDICATION_STRING
     */
    public static final String[] X_AXIS_INDICATOR_STRINGS = {
                CHIRP_STRING, NO_INDICATION_STRING };

    /**
     * The integer values representing the indicator frequencies which are
     * <code>LOW, MEDIUM, HIGH</code>.
     * @see #LOW
     * @see #MEDIUM
     * @see #HIGH
     */
    public static final int[] INDICATOR_FREQUENCIES = { LOW, MEDIUM, HIGH };

    /**
     * The string values representing the indicator frequencies which are
     * <code>LOW_STRING, MEDIUM_STRING, HIGH_STRING</code>.
     * @see #LOW_STRING
     * @see #MEDIUM_STRING
     * @see #HIGH_STRING
     */
    public static final String[] INDICATOR_FREQUENCY_STRINGS = {
                LOW_STRING, MEDIUM_STRING, HIGH_STRING };

    /**
     * The integer values representing the indicator durations which are
     * <code>SHORT, MEDIUM, LONG</code>.
     * @see #SHORT
     * @see #MEDIUM
     * @see #LONG
     */
    public static final int[] INDICATOR_DURATIONS = { SHORT, MEDIUM, LONG };

    /**
     * The string values representing the indicator durations which are
     * <code>SHORT_STRING, MEDIUM_STRING, LONG_STRING</code>.
     * @see #SHORT_STRING
     * @see #MEDIUM_STRING
     * @see #LONG_STRING
     */
    public static final String[] INDICATOR_DURATION_STRINGS = {
                SHORT_STRING, MEDIUM_STRING, LONG_STRING };

    /**
     * The integer values representing the trace sweep speeds which are
     * <code>SLOW, MEDIUM, FAST</code>.
     * @see #SLOW
     * @see #MEDIUM
     * @see #FAST
     */
    public static final int[] TRACE_SWEEP_SPEEDS = { SLOW, MEDIUM, FAST };

    /**
     * The string values representing the trace sweep speeds which are
     * <code>SLOW_STRING, MEDIUM_STRING, FAST_STRING</code>.
     * @see #SLOW_STRING
     * @see #MEDIUM_STRING
     * @see #FAST_STRING
     */
    public static final String[] TRACE_SWEEP_SPEED_STRINGS = {
                SLOW_STRING, MEDIUM_STRING, FAST_STRING };

    /** The axis color property file key. */
    private static final String AXIS_COLOR_KEY = "mde.axis.color";

    /** The background color property file key. */
    private static final String BACKGROUND_COLOR_KEY = "mde.background.color";

    /** The grid color property file key. */
    private static final String GRID_COLOR_KEY = "mde.grid.line.color";

    /** The line color property file key. */
    private static final String LINE_COLOR_KEY = "mde.line.color";

    /** The line size property file key. */
    private static final String LINE_SIZE_KEY = "mde.line.size";

    /** The trace-on property file key. */
    private static final String TRACE_ON_KEY = "mde.line.traceOn";

    /** The autoscale graph property file key. */
    private static final String AUTOSCALE_GRAPH_KEY = "mde.autoscale.graph";

    /** The data point color property file key. */
    private static final String DATA_POINT_COLOR_KEY = "mde.data.point.color";

    /** The show data points property file key. */
    private static final String DATA_POINTS_SHOWN_KEY = "mde.data.points.shown";

    /** The description mode property file key. */
    private static final String DESCRIPTION_MODE_KEY = "mde.description.mode";

    /** The sonification waveform property file key. */
    private static final String SONIFICATION_WAVEFORM_KEY = "mde.sonification.waveform";

    /** The y-axis indicator property file key. */
    private static final String Y_AXIS_INDICATOR_KEY = "mde.yaxis.indicator";

    /** The y-axis indicator frequency property file key. */
    private static final String Y_AXIS_INDICATOR_FREQ_KEY = "mde.yaxis.indicator.frequency";

    /** The y-axis indicator duration property file key. */
    private static final String Y_AXIS_INDICATOR_DURATION_KEY = "mde.yaxis.indicator.duration";

    /** The negative y values indicators property file key. */
    private static final String NEG_Y_VALUES_INDICATORS_KEY = "mde.negative.yvalues.indicator";

    /** The x-axis indicators property file key. */
    private static final String X_AXIS_INDICATOR_KEY = "mde.xaxis.indicator";

    /** The x-axis indicator frequency property file key. */
    private static final String X_AXIS_INDICATOR_FREQ_KEY = "mde.xaxis.indicator.frequency";

    /** The x-axis indicator duration property file key. */
    private static final String X_AXIS_INDICATOR_DURATION_KEY = "mde.xaxis.indicator.duration";

    /** The trace sweep speed property file key. */
    private static final String TRACE_SWEEP_SPEED_KEY = "mde.trace.sweep.speed";

    /**
     * Default constructor, which loads the properties from the default
     * <code>MDE_Properties.properties</code> file. 
     */
    public MdeSettings() {
        this("MDE_Properties.properties");
    }

    /**
     * Creates an instance of <code>MdeSettings</code> using the specified
     * properties file.
     * 
     * @param propertiesFilename the properties filename to load the settings from.
     */
    public MdeSettings(String propertiesFilename) {
        super(propertiesFilename, "MathTrax Properties");
        setDefaults();
        loadSettings();
    }

    /**
     * Sets the hard-coded application default settings.
     * <p>
     * <blockquote>axisColor = Color.white; <br>
     * backgroundColor = Color.black; <br>
     * gridColor = Color.magenta; <br>
     * lineColor = Color.yellow; <br>
     * lineSize = 2; // medium <br>
     * dataPointColor = Color.red; <br>
     * dataPointsShown = true; <br>
     * autoscaleGraph = true; <br>
     * traceOn = true; <br>
     * </blockquote> Describer properties and initial defaults are:
     * <blockquote>descriptionMode = "standards"; </blockquote> Sounder properties and
     * initial defaults are: <blockquote>negativeYValuesIndicator = HISS <br>
     * sonificationWaveform = TRIANGLE <br>
     * traceSweepSpeed = MEDIUM <br>
     * xAxisIndicator = NO_INDICATION <br>
     * xAxisIndicatorDuration = MEDIUM <br>
     * xAxisIndicatorFrequency = MEDIUM <br>
     * yAxisIndicator = DING <br>
     * yAxisIndicatorDuration = MEDIUM <br>
     * yAxisIndicatorFrequency = MEDIUM</blockquote>
     */
    public void setDefaults() {
        axisColor = Color.white;
        backgroundColor = Color.black;
        gridColor = Color.magenta;
        lineColor = Color.yellow;
        lineSize = 2; // medium
        dataPointColor = Color.red;
        dataPointsShown = true;
        autoscaleGraph = true;
        traceOn = true;
        descriptionMode = "standards";

        sonificationWaveform = TRIANGLE;

        yAxisIndicator = DING;
        yAxisIndicatorFreq = MEDIUM;
        yAxisIndicatorDuration = MEDIUM;

        negYValuesIndicator = HISS;

        xAxisIndicator = NO_INDICATION;
        xAxisIndicatorFreq = MEDIUM;
        xAxisIndicatorDuration = MEDIUM;

        traceSweepSpeed = MEDIUM;
    }

    // Inherit javadoc from Settings.java
    protected void setDefaults(Properties defaults) {
        defaults.put(AXIS_COLOR_KEY, Integer.toString(axisColor.getRGB()));
        defaults.put(BACKGROUND_COLOR_KEY, Integer.toString(backgroundColor.getRGB()));
        defaults.put(GRID_COLOR_KEY, Integer.toString(gridColor.getRGB()));
        defaults.put(LINE_COLOR_KEY, Integer.toString(lineColor.getRGB()));
        defaults.put(LINE_SIZE_KEY, Integer.toString(lineSize));
        defaults.put(DATA_POINT_COLOR_KEY, Integer.toString(dataPointColor.getRGB()));
        defaults.put(DATA_POINTS_SHOWN_KEY, Boolean.toString(dataPointsShown));
        defaults.put(AUTOSCALE_GRAPH_KEY, Boolean.toString(autoscaleGraph));
        defaults.put(TRACE_ON_KEY, Boolean.toString(traceOn));
        defaults.put(DESCRIPTION_MODE_KEY, descriptionMode);

        defaults.put(SONIFICATION_WAVEFORM_KEY, Integer.toString(sonificationWaveform));

        defaults.put(Y_AXIS_INDICATOR_KEY, Integer.toString(yAxisIndicator));
        defaults.put(Y_AXIS_INDICATOR_FREQ_KEY, Integer.toString(yAxisIndicatorFreq));
        defaults.put(Y_AXIS_INDICATOR_DURATION_KEY, Integer.toString(yAxisIndicatorDuration));

        defaults.put(NEG_Y_VALUES_INDICATORS_KEY, Integer.toString(negYValuesIndicator));

        defaults.put(X_AXIS_INDICATOR_KEY, Integer.toString(xAxisIndicator));
        defaults.put(X_AXIS_INDICATOR_FREQ_KEY, Integer.toString(xAxisIndicatorFreq));
        defaults.put(X_AXIS_INDICATOR_DURATION_KEY, Integer.toString(xAxisIndicatorDuration));

        defaults.put(TRACE_SWEEP_SPEED_KEY, Integer.toString(traceSweepSpeed));
    }

    // Inherit javadoc from Settings.java
    protected void updateSettingsFromCachedProperties() {
        try {
            String tmp;
            tmp = properties.getProperty(AXIS_COLOR_KEY);
            axisColor = new Color(Integer.parseInt(tmp));

            tmp = properties.getProperty(BACKGROUND_COLOR_KEY);
            backgroundColor = new Color(Integer.parseInt(tmp));

            tmp = properties.getProperty(GRID_COLOR_KEY);
            gridColor = new Color(Integer.parseInt(tmp));

            tmp = properties.getProperty(LINE_COLOR_KEY);
            lineColor = new Color(Integer.parseInt(tmp));

            tmp = properties.getProperty(LINE_SIZE_KEY);
            lineSize = Integer.parseInt(tmp);

            tmp = properties.getProperty(DATA_POINT_COLOR_KEY);
            dataPointColor = new Color(Integer.parseInt(tmp));

            tmp = properties.getProperty(DATA_POINTS_SHOWN_KEY);
            dataPointsShown = Boolean.valueOf(tmp).booleanValue();

            tmp = properties.getProperty(AUTOSCALE_GRAPH_KEY);
            autoscaleGraph = Boolean.valueOf(tmp).booleanValue();

            tmp = properties.getProperty(TRACE_ON_KEY);
            traceOn = Boolean.valueOf(tmp).booleanValue();

            descriptionMode = properties.getProperty(DESCRIPTION_MODE_KEY);

            tmp = properties.getProperty(SONIFICATION_WAVEFORM_KEY);
            sonificationWaveform = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(sonificationWaveform, BASIC_WAVEFORMS) < 0) {
                // Bad value it could be corrupted, so use the default value.
                sonificationWaveform = TRIANGLE;
            }

            tmp = properties.getProperty(Y_AXIS_INDICATOR_KEY);
            yAxisIndicator = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(yAxisIndicator, Y_AXIS_INDICATORS) < 0) {
                // Bad value it could be corrupted, so use the default value.
                yAxisIndicator = DING;
            }

            tmp = properties.getProperty(Y_AXIS_INDICATOR_FREQ_KEY);
            yAxisIndicatorFreq = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(yAxisIndicatorFreq,
                    INDICATOR_FREQUENCIES) < 0) {
                // Bad value it could be corrupted, so use the default value.
                yAxisIndicatorFreq = MEDIUM;
            }

            tmp = properties.getProperty(Y_AXIS_INDICATOR_DURATION_KEY);
            yAxisIndicatorDuration = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(yAxisIndicatorDuration,
                    INDICATOR_DURATIONS) < 0) {
                // Bad value it could be corrupted, so use the default value.
                yAxisIndicatorDuration = MEDIUM;
            }

            tmp = properties.getProperty(NEG_Y_VALUES_INDICATORS_KEY);
            negYValuesIndicator = Integer.parseInt(tmp);
            if (!ArrayUtil.isCodeValid(negYValuesIndicator,
                    NEG_Y_VALUES_INDICATORS)) {
                // Bad value it could be corrupted, so use the default value.
                negYValuesIndicator = HISS;
            }

            tmp = properties.getProperty(X_AXIS_INDICATOR_KEY);
            xAxisIndicator = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(xAxisIndicator, X_AXIS_INDICATORS) < 0) {
                // Bad value it could be corrupted, so use the default value.
                xAxisIndicator = NO_INDICATION;
            }

            tmp = properties.getProperty(X_AXIS_INDICATOR_FREQ_KEY);
            xAxisIndicatorFreq = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(xAxisIndicatorFreq,
                    INDICATOR_FREQUENCIES) < 0) {
                // Bad value it could be corrupted, so use the default value.
                xAxisIndicatorFreq = MEDIUM;
            }

            tmp = properties.getProperty(X_AXIS_INDICATOR_DURATION_KEY);
            xAxisIndicatorDuration = Integer.parseInt(tmp);
            if (ArrayUtil.indexOfFirstMatch(xAxisIndicatorDuration,
                    INDICATOR_DURATIONS) < 0) {
                // Bad value it could be corrupted, so use the default value.
                xAxisIndicatorDuration = MEDIUM;
            }

            tmp = properties.getProperty(TRACE_SWEEP_SPEED_KEY);
            traceSweepSpeed = Integer.parseInt(tmp);
            if (ArrayUtil
                    .indexOfFirstMatch(traceSweepSpeed, TRACE_SWEEP_SPEEDS) < 0) {
                // Bad value it could be corrupted, so use the default value.
                traceSweepSpeed = MEDIUM;
            }
        } catch (NumberFormatException e) {
            // We don't care if the property was of the wrong format,
            // they've all got default values. So catch the exception
            // and keep going.
        }
    }

    /**
     * Generates a string representation of the current settings.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "[" + axisColor + "," + backgroundColor + "," + lineColor + ","
                + gridColor + " " + lineSize + " " + dataPointColor + " "
                + dataPointsShown + " " + autoscaleGraph + " " + traceOn + " "
                + sonificationWaveform + " " + traceSweepSpeed + " "
                + negYValuesIndicator + " " + xAxisIndicator + " "
                + xAxisIndicatorFreq + " " + xAxisIndicatorDuration + " "
                + yAxisIndicator + " " + yAxisIndicatorFreq + " "
                + yAxisIndicatorDuration + "]";
    }

    /**
     * Sets the graph axis color and saves the settings.
     * 
     * @param color the graph axis color.
     */
    public void setAxisColor(Color color) {
        this.axisColor = color;
        save();
    }

    /**
     * Sets the graph background color and saves the settings.
     * 
     * @param color the graph background color
     */
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        save();
    }

    /**
     * Sets the graph grid color and saves the settings.
     * 
     * @param color the graph grid color.
     */
    public void setGridColor(Color color) {
        this.gridColor = color;
        save();
    }

    /**
     * Sets the graph line color and saves the settings.
     * 
     * @param color the graph line color.
     */
    public void setLineColor(Color color) {
        this.lineColor = color;
        save();
    }

    /**
     * Sets the graph line size and saves the settings.
     * 
     * @param size the graph line size.
     */
    public void setLineSize(int size) {
        this.lineSize = size;
        save();
    }

    /**
     * Sets the graph data point color and saves the settings.
     * 
     * @param color the graph data point color.
     */
    public void setDataPointColor(Color color) {
        this.dataPointColor = color;
        save();
    }

    /**
     * Sets the graph show data points flag and saves the settings.
     * 
     * @param showPoints true to show show data points on the graph, false to
     * not draw them.
     */
    public void setDataPointsShown(boolean showPoints) {
        this.dataPointsShown = showPoints;
        save();
    }

    /**
     * Sets the graph autoscale flag and saves the settings.
     * 
     * @param enableAutoscale true to automatically scale the graph, false for 
     * no graph scaling.
     */
    public void setAutoscaleGraph(boolean enableAutoscale) {
        this.autoscaleGraph = enableAutoscale;
        save();
    }

    /**
     * Sets the graph trace flag and saves the settings.
     * 
     * @param flag true to enable the trace, false to not show it.
     */
    public void setTraceOn(boolean flag) {
        this.traceOn = flag;
        save();
    }

    /**
     * Sets the description mode and saves the settings.
     * 
     * @param mode the description mode is one of "visual" or "math" or "standards".
     */
    public void setDescriptionMode(String mode) {
        this.descriptionMode = mode;
        save();
    }

    /**
     * Sets the sonification waveform and saves the settings.
     * 
     * @param waveform the sonification waveform, which is one of the values
     * in <code>BASIC_WAVEFORMS</code>.
     * @see #BASIC_WAVEFORMS
     */
    public void setSonificationWaveform(int waveform) {
        if (ArrayUtil.indexOfFirstMatch(waveform, BASIC_WAVEFORMS) < 0) {
            throw new IllegalArgumentException("Sonification waveform must be one of the values in BASIC_WAVEFORMS");
        }
        this.sonificationWaveform = waveform;
        save();
    }

    /**
     * Sets the Y-axis indicator and saves the settings.
     * 
     * @param indicator the Y-axis indicator, which is one of the values
     * in <code>Y_AXIS_INDICATORS</code>.
     * @see #Y_AXIS_INDICATORS
     */
    public void setYAxisIndicator(int indicator) {
        if (ArrayUtil.indexOfFirstMatch(indicator, Y_AXIS_INDICATORS) < 0) {
            throw new IllegalArgumentException("Y-axis indicator must be one of the values in Y_AXIS_INDICATORS");
        }
        this.yAxisIndicator = indicator;
        save();
    }

    /**
     * Sets the Y-axis indicator duration and saves the settings.
     * 
     * @param duration the Y-axis indicator duration, which is one of the values
     * in <code>INDICATOR_DURATIONS</code>.
     * @see #INDICATOR_DURATIONS
     */
    public void setYAxisIndicatorDuration(int duration) {
        if (ArrayUtil.indexOfFirstMatch(duration, INDICATOR_DURATIONS) < 0) {
            throw new IllegalArgumentException("Y-axis indicator duration must be one of the values in INDICATOR_DURATIONS");
        }
        this.yAxisIndicatorDuration = duration;
        save();
    }

    /**
     * Sets the negative Y values indicator and saves the settings.
     * 
     * @param indicator the negative Y values indicator, which is one of the values
     * in <code>NEG_Y_VALUES_INDICATORS</code>.
     * @see #NEG_Y_VALUES_INDICATORS
     */
    public void setNegativeYValuesIndicator(int indicator) {
        if (!ArrayUtil.isCodeValid(indicator, NEG_Y_VALUES_INDICATORS)) {
            throw new IllegalArgumentException("Negative Y-values indicator duration must be a combination of the values in NEG_Y_VALUES_INDICATORS");
        }
        this.negYValuesIndicator = indicator;
        save();
    }

    /**
     * Sets the Y-axis indicator frequency and saves the settings.
     * 
     * @param frequency the Y-axis indicator frequency, which is one of the values
     * in <code>INDICATOR_FREQUENCIES</code>.
     * @see #INDICATOR_FREQUENCIES
     */
    public void setYAxisIndicatorFrequency(int frequency) {
        if (ArrayUtil.indexOfFirstMatch(frequency, INDICATOR_FREQUENCIES) < 0) {
            throw new IllegalArgumentException("Y-axis indicator frequency must be one of the values in INDICATOR_FREQUENCIES");
        }
        this.yAxisIndicatorFreq = frequency;
        save();
    }

    /**
     * Sets the X-axis indicator and saves the settings.
     * 
     * @param indicator the X-axis indicator, which is one of the values
     * in <code>X_AXIS_INDICATORS</code>.
     * @see #X_AXIS_INDICATORS
     */
    public void setXAxisIndicator(int indicator) {
        if (ArrayUtil.indexOfFirstMatch(indicator, X_AXIS_INDICATORS) < 0) {
            throw new IllegalArgumentException("X-axis indicator must be one of the values in X_AXIS_INDICATORS");
        }
        this.xAxisIndicator = indicator;
        save();
    }

    /**
     * Sets the X-axis indicator duration and saves the settings.
     * 
     * @param duration the X-axis indicator duration, which is one of the values
     * in <code>INDICATOR_DURATIONS</code>.
     * @see #INDICATOR_DURATIONS
     */
    public void setXAxisIndicatorDuration(int duration) {
        if (ArrayUtil.indexOfFirstMatch(duration, INDICATOR_DURATIONS) < 0) {
            throw new IllegalArgumentException("X-axis indicator duration must be one of the values in INDICATOR_DURATIONS");
        }
        this.xAxisIndicatorDuration = duration;
        save();
    }

    /**
     * Sets the X-axis indicator frequency and saves the settings.
     * 
     * @param frequency the X-axis indicator frequency, which is one of the values
     * in <code>INDICATOR_FREQUENCIES</code>.
     * @see #INDICATOR_FREQUENCIES
     */
    public void setXAxisIndicatorFrequency(int frequency) {
        if (ArrayUtil.indexOfFirstMatch(frequency, INDICATOR_FREQUENCIES) < 0) {
            throw new IllegalArgumentException("X-axis indicator frequency must be one of the values in INDICATOR_FREQUENCIES");
        }
        this.xAxisIndicatorFreq = frequency;
        save();
    }

    /**
     * Sets the trace sweep speed and saves the settings.
     * 
     * @param speed the trace sweep speed, which is one of the values
     * in <code>TRACE_SWEEP_SPEEDS</code>.
     * @see #TRACE_SWEEP_SPEEDS
     */
    public void setTraceSweepSpeed(int speed) {
        if (ArrayUtil.indexOfFirstMatch(speed, TRACE_SWEEP_SPEEDS) < 0) {
            throw new IllegalArgumentException("Trace sweep speed must be one of the values in TRACE_SWEEP_SPEEDS");
        }
        this.traceSweepSpeed = speed;
        save();
    }

    /**
     * Returns the axis color used for the graph.
     * 
     * @return the axis color used for the graph.
     */
    public Color getAxisColor() {
        return axisColor;
    }

    /**
     * Returns the background color used for the graph.
     * 
     * @return the background color used for the graph.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the grid color used for the graph.
     * 
     * @return the grid color used for the graph.
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Returns the line color used for the graph.
     * 
     * @return the line color used for the graph.
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Returns the data point color used for the graph.
     * 
     * @return the data point color used for the graph.
     */
    public Color getDataPointColor() {
        return dataPointColor;
    }

    /**
     * Returns true if the data points should be shown in the graph.
     * 
     * @return true if the data points should be shown in the graph.
     */
    public boolean isDataPointsShown() {
        return dataPointsShown;
    }

    /**
     * Returns true if the graph will automatically scale.
     * 
     * @return true if the graph will automatically scale.
     */
    public boolean isAutoscaleGraph() {
        return autoscaleGraph;
    }

    /**
     * Returns the size of the line used for the graph.
     * 
     * @return the size of the line used for the graph.
     */
    public int getLineSize() {
        return lineSize;
    }

    /**
     * Returns the description mode.
     * 
     * @return the description mode.
     */
    public String getDescriptionMode() {
        return descriptionMode;
    }

    /**
     * Returns true if the sonification trace will be shown.
     * 
     * @return true if the sonification trace will be shown.
     */
    public boolean showTrace() {
        return traceOn;
    }

    /**
     * Returns the sonification waveform.
     * 
     * @return the sonification waveform.
     */
    public int getSonificationWaveform() {
        return sonificationWaveform;
    }

    /**
     * Returns the Y-axis indicator.
     * 
     * @return the Y-axis indicator.
     */
    public int getYAxisIndicator() {
        return yAxisIndicator;
    }

    /**
     * Returns the Y-axis indicator duration.
     * 
     * @return the Y-axis indicator duration.
     */
    public int getYAxisIndicatorDuration() {
        return yAxisIndicatorDuration;
    }

    /**
     * Returns the Y-axis indicator frequency.
     * 
     * @return the Y-axis indicator frequency.
     */
    public int getYAxisIndicatorFrequency() {
        return yAxisIndicatorFreq;
    }

    /**
     * Returns the negative y-values indicator flag value.
     * 
     * @return the negative y-values indicator flag value.
     */
    public int getNegativeYValuesIndicator() {
        return negYValuesIndicator;
    }

    /**
     * Returns the X-axis indicator.
     * 
     * @return the X-axis indicator.
     */
    public int getXAxisIndicator() {
        return xAxisIndicator;
    }

    /**
     * Returns the X-axis indicator duration.
     * 
     * @return the X-axis indicator duration.
     */
    public int getXAxisIndicatorDuration() {
        return xAxisIndicatorDuration;
    }

    /**
     * Returns the X-axis indicator frequency.
     * 
     * @return the X-axis indicator frequency.
     */
    public int getXAxisIndicatorFrequency() {
        return xAxisIndicatorFreq;
    }

    /**
     * Returns the sonification trace sweep speed.
     * 
     * @return the sonification trace sweep speed.
     */
    public int getTraceSweepSpeed() {
        return traceSweepSpeed;
    }

}
