/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * class to handle "sonification" of numerical data Gets data and window spec from
 * Solver Must not cache anything from Solver because there's no way to know if
 * cached info is fresh. Sets up necessary javax.sound.sampled machinery. Starts a
 * thread which continually replenishes a sound buffer. Contains methods to "render"
 * a single slice of the data through a given point x
 */

package gov.nasa.ial.mde.sound;

import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solution;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;

/**
 * Class to generate the audio used to sonify data or equations in the form of
 * AnalyzedItems in a Solver. Currently, we support a maximum of
 * MAX_FREQUENCIES=10 simultaneous tones, and if the Solver happens to generate
 * more than Sounder.MAX_FREQUENCIES simultaneous tones, the behavior is not
 * defined.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Sounder extends MultiWavePlayer {

    /**
     * <code>Solver</code> contains information on all graphs to be sonified.
     */
    private Solver solver;

    /**
     * <code>lastX</code> is used internally to track when the sweep crosses
     * the X-axis
     */
    private double lastX = Double.POSITIVE_INFINITY;

    /**
     * <code>lastNumNegative</code> is the number of traces which were below
     * the X-axis at the previous value of x. We maintain this quantity in order
     * to identify axis crossings which are indicated when the value of
     * <code>lastNumNegative</code> changes.
     */
    private int lastNumNegative = -1;

    /**
     * <code>settings</code> encapsulates user settings for the application,
     * including choices for how the graph should be sonified.
     */
    private MdeSettings settings;

    /**
     * <code>MAX_FREQUENCIES</code> is the maximum number of tones which can
     * be generated simultaneously by this <code>Sounder</code>. largest
     * number of y's for a given x
     */
    private final static int MAX_FREQUENCIES = 10;

    private boolean usingHiss = true;

    private boolean usingChirp = true;

    private boolean usingAlteredWaveform = true;

    private boolean usingDing = true;

    private float alteredWaveformAmplitude = 0.2f;

    private int whichWaveform = MultiWave.SINE;

    private float dingFreq = 4000f; //Hz

    private int dingTime = 150; // milliseconds

    @SuppressWarnings("unused")
	private Sounder() {
        throw new RuntimeException("Default constructor not allowed.");
    }

    /**
     * Instantiates a Sounder for a particular Solver and MDE configuration.
     * 
     * @param solver
     *            The Solver containing the AnalyzedItems to be sonified
     * @param settings
     *            Controls how the AnalyzedItems in the Solver will be rendered
     */
    public Sounder(Solver solver, MdeSettings settings) {
        super(Sounder.MAX_FREQUENCIES);

        if (solver == null) {
            throw new NullPointerException("Null solver.");
        }
        if (settings == null) {
            throw new NullPointerException("Null settings.");
        }
        this.solver = solver;
        this.settings = settings;
        initLine();
    } // end Sounder

    /**
     * Performs a sonification sweep for all sonifiable data represented in
     * <code>this.solver</code>
     * 
     * @param sweepTimeInSeconds
     *            is the desired duration of the sweep.
     */
    public void sweep(double sweepTimeInSeconds) {
        if (!isPlaying()) {
            return;
        }
        
        double sweepTimeInMiliseconds = 1000.0 * sweepTimeInSeconds;
        // makes dx just big enough to go 0 to one in the proper number of steps
        // where we take one step in each latency interval
        double dx = Sounder.LATENCY_IN_MILLISECONDS / sweepTimeInMiliseconds;
        for (double x = 0.0; x <= 1.0; x += dx) {
            render(x);
            try {
                Thread.sleep(Sounder.LATENCY_IN_MILLISECONDS);
            } // end try
            catch (InterruptedException ie) {
                // ignore exception
            }
        } // end for x
    } // end sweep

    /**
     * Updates all sonification settings including waveform and indicators for
     * axis crossing and negative values.
     * 
     * @param mdeSettings the sonification settings to be used.
     */
    public void updateSettings(MdeSettings mdeSettings) {
        if (mdeSettings == null) {
            throw new NullPointerException("Null settings.");
        }
        
        this.settings = mdeSettings;
        setWhichWaveform(settings.getSonificationWaveform());

        int negativeYIndicator = settings.getNegativeYValuesIndicator();
        int xAxisIndicator = settings.getXAxisIndicator();
        int yAxisIndicator = settings.getYAxisIndicator();

        setUsingHiss((negativeYIndicator & MdeSettings.HISS) != 0);
        setUsingAlteredWaveform((negativeYIndicator & MdeSettings.TAMBOR_CHANGE) != 0);
        setUsingChirp((xAxisIndicator & MdeSettings.CHIRP) != 0);
        setUsingDing((yAxisIndicator & MdeSettings.DING) != 0);
        setXAxisIndicatorDuration(settings.getXAxisIndicatorDuration());
        setXAxisIndicatorFrequency(settings.getXAxisIndicatorFrequency());
        setYAxisIndicatorDuration(settings.getYAxisIndicatorDuration());
        setYAxisIndicatorFrequency(settings.getYAxisIndicatorFrequency());
    }

    /**
     * Sonify the specified single point.
     * 
     * @param x the x value
     * @param y the y value
     */
    public void render(double x, double y) {
        boolean noiseOn = false;
        int numNegative = 0;
        int freqNumber = 0;

        double left = solver.getLeft();
        double right = solver.getRight();
        double top = solver.getTop();
        double bottom = solver.getBottom();
        double midY = 0.5 * (top + bottom);
        double invHalfHeight = 2.0 / (top - bottom);

        // Set pan between -1 and 1, representing the spatial x position of the
        // slice we will render, and only update the pan once.
        double midX = 0.5 * (right + left);
        double invHalfWidth = 2.0 / (right - left);
        setPan((float) ((x - midX) * invHalfWidth));
        
        if (usingDing) {
            dingAtYAxis(x);
        }
        if (y < 0.0) {
            numNegative++;
        }

        double octave = 2.0 * (y - midY) * invHalfHeight;

        // Toss stuff more than two octaves off fundamental frequency.
        if ((octave >= -2.0) && (octave <= 2.0)) {
            // The basic conversion from octave = log_2(f) to Hz
            float f = (float) (MultiWavePlayer.FUNDAMENTAL * Math.pow(2.0, octave));

//            source.activate(freqNumber, f, (y >= 0.0) ? MultiWave.SINE : MultiWave.SAW);
            source.activate(freqNumber, f, whichWaveform);
            if (y < 0.0) {
                if (usingAlteredWaveform)
                    source.activate(alteredWaveformAmplitude, ++freqNumber, f, MultiWave.SAW);

                noiseOn = true;
            } // end if

            freqNumber++;
        }

        // Kill off unnecessary/unused frequencies
        int na = source.getNumActive();
        if (freqNumber < na) {
            for (int i = freqNumber; i < na; i++) {
                source.deactivate(i);
            }
        }
        if (usingHiss) {
            if (noiseOn) {
                source.setNoise(0.15);
            } else {
                source.setNoise(0.0);
            }
        } // end if
        else {
            source.setNoise(0.0);
        }
        
        if (lastNumNegative >= 0) {
            if (numNegative > lastNumNegative) {
                if (usingChirp) {
                    source.chirpDown();
                }
            }

            if (numNegative < lastNumNegative) {
                if (usingChirp) {
                    source.chirpUp();
                }
            }
        } // end if

        lastNumNegative = numNegative;
    }

    /**
     * Sonify the relative position [0,1] for all the solutions in the solver.
     * 
     * @param position the relative position [0,1] in the array of points to render.
     *      Note that this assumes that the data points are sorted and equally spaced in x.
     */
    public void render(double position) {
        int numNegative = 0;
        boolean noiseOn = false;
        MultiPointXY point, modelPoint;
        Solution solution;
        AnalyzedData analyzedData;
        PointXY realDataPoint;
        int i, n;
        float f;
        double y, octave;

        int freqNumber = 0;

        double left = solver.getLeft();
        double right = solver.getRight();
        double top = solver.getTop();
        double bottom = solver.getBottom();
        double midY = 0.5 * (top + bottom);
        double invHalfHeight = 2.0 / (top - bottom);

        // For the given bounds and position, calculate the relative x-value.
        double x = left + position * (right - left);

        // Set pan between -1 and 1, representing the spatial x position of the
        // slice we will render, and only update the pan once.
        double midX = 0.5 * (right + left);
        double invHalfWidth = 2.0 / (right - left);

        // Set the pan value here only if we are not sonifiying anything Polar.
        if (solver.getSonifyPolarCount() == 0) {
            setPan((float) ((x - midX) * invHalfWidth));
            if (usingDing) {
                dingAtYAxis(x);
            }
        }

        int numSolutions = solver.size();
        for (int solutionIndex = 0; solutionIndex < numSolutions; solutionIndex++) {
            solution = solver.get(solutionIndex);

            // Just continue if this solution should not be sonified, or if
            // there is no point.
            if (!solution.isSonifyGraph() || ((point = solution.getPointNear(x)) == null)) {
                continue;
            }

            // If the solution is polar then adjust the pan as needed.
            if (solution.isPolar()) {
                // Update the pan value if needed.
                float p = (float) ((point.x - midX) * invHalfWidth);

                if (usingDing) {
                    dingAtYAxis(point.x);
                }
                if (p != getPan().getValue()) {
                    setPan(p);
                }
            }

            // The number of frequences needed to represent this solution.
            n = point.yArray.length;

            // Sonify the real data point based on the current position and
            // sonify the corresponding real data model point based on the
            // x-value of the real data point. Therefore sonification of the
            // real data points is relative to how many are viewable for the
            // current bounds.
            if (settings.isDataPointsShown() && (freqNumber < MAX_FREQUENCIES) &&
                    (solution.getAnalyzedItem() instanceof AnalyzedData)) {

                analyzedData = (AnalyzedData) solution.getAnalyzedItem();
                realDataPoint = analyzedData.getRealDataPoint(x);

                if (realDataPoint != null) {
                    // Based on the x-value for the real data, search for and
                    // use the model point that is close to the corresponding
                    // real data x-value.
                    modelPoint = solution.getPointNear(realDataPoint.x);
                    if (modelPoint != null) {
                        point = modelPoint;
                        n = point.yArray.length;
                    }

                    // Sonify the real data point if there is no corresponding
                    // model data point or it the real data point is not the
                    // same as the model data.
                    if ((n == 0) || (realDataPoint.y != point.yArray[0])) {

                        y = realDataPoint.y;
                        octave = 2.0 * (y - midY) * invHalfHeight;

                        if (y < 0.0) {
                            numNegative++;
                        }
                        
                        // Toss stuff more than two octaves off fundamental
                        // frequency.
                        if ((octave >= -2.0) && (octave <= 2.0)) {
                            // The basic conversion from octave = log_2(f) to Hz
                            f = (float) (MultiWavePlayer.FUNDAMENTAL * Math
                                    .pow(2.0, octave));

//                            source.activate(freqNumber, f, (y >= 0.0) ? MultiWave.SINE : MultiWave.SAW);
                            source.activate(freqNumber, f, whichWaveform);
                            if (y < 0.0) {
                                if (usingAlteredWaveform) {
                                    source.activate(alteredWaveformAmplitude, ++freqNumber, f, MultiWave.SAW);
                                }
                                noiseOn = true;
                            } // end if

                            freqNumber++;
                        }
                    }
                }
            }

            // Calculate the frequencies midY to top = one octave up;
            // midY to bottom = one octave down
            for (i = 0; (i < n) && (freqNumber < MAX_FREQUENCIES); i++) {
                y = point.yArray[i];
                if (y < 0.0) {
                    numNegative++;
                }
                octave = 2.0 * (y - midY) * invHalfHeight;

                // Toss stuff more than two octaves off fundamental frequency.
                if ((octave >= -2.0) && (octave <= 2.0)) {
                    // The basic conversion from octave = log_2(f) to Hz
                    f = (float) (MultiWavePlayer.FUNDAMENTAL * Math.pow(2.0, octave));

                    //                    source.activate(freqNumber, f, (y >= 0.0) ?
                    // MultiWave.SINE : MultiWave.SAW);
                    source.activate(freqNumber, f, whichWaveform);
                    if (y < 0.0) {
                        if (usingAlteredWaveform) {
                            source.activate(alteredWaveformAmplitude, ++freqNumber, f, MultiWave.SAW);
                        }
                        noiseOn = true;
                    } // end if

                    freqNumber++;
                }
            } // end for i
        }

        // Kill off unnecessary/unused frequencies
        int na = source.getNumActive();
        if (freqNumber < na) {
            for (i = freqNumber; i < na; i++) {
                source.deactivate(i);
            }
        }

        if (usingHiss) {
            if (noiseOn) {
                source.setNoise(0.15);
            } else {
                source.setNoise(0.0);
            }
        } // end if
        else {
            source.setNoise(0.0);
        }
        
        if (lastNumNegative >= 0) {
            if (numNegative > lastNumNegative) {
                if (usingChirp) {
                    source.chirpDown();
                }
            }
            if (numNegative < lastNumNegative) {
                if (usingChirp) {
                    source.chirpUp();
                }
            }
        } // end if

        lastNumNegative = numNegative;

    } // end render

    /**
     * Manages identification and time at which the sonification sweep crosses
     * the Y-axis. Updates <code>lastX</code> and declares a crossing if (1)
     * <code>x</code> is zero, or (2) the product of <code>x</code> and
     * <code>lastX</code> is negative.
     * 
     * @param x current value of the independent variable.
     */
    private void dingAtYAxis(double x) {
        if (!Double.isInfinite(lastX)) {
            if (x == 0.0) {
                source.ding(dingFreq, dingTime);
            } else if (x * lastX < 0.0) {
                source.ding(dingFreq, dingTime);
            }
        }
        lastX = x;
    }

    /**
     * Returns the state of altered (fuzzified) waveform sonification.
     * 
     * @return True whenever sonification uses an altered (fuzzified) waveform
     *         to indicate negative y values.
     */
    public boolean isUsingAlteredWaveform() {
        return usingAlteredWaveform;
    }

    /**
     * Enables altered (fuzzed) waveform sonification.
     * 
     * @param usingAlteredWaveform The value indicating whether or not negative
     *      values of y are to be sonified with an altered (fuzzed) waveform.
     */
    public void setUsingAlteredWaveform(boolean usingAlteredWaveform) {
        this.usingAlteredWaveform = usingAlteredWaveform;
    }

    /**
     * Returns the state of the X-axis crossing chirp flag.
     * 
     * @return True whenever chirps are present to indicate X-axis crossings.
     */
    public boolean isUsingChirp() {
        return usingChirp;
    }

    /**
     * Enables the X-axis crossings chirp flag.
     * 
     * @param usingChirp Determines whether X-axis crossings will be indicated
     *          with chirps.
     */
    public void setUsingChirp(boolean usingChirp) {
        this.usingChirp = usingChirp;
    }

    /**
     * Returns the state of the Ding flag.
     * 
     * @return True whenever a ding will indicate the trace crossing the Y-axis.
     */
    public boolean isUsingDing() {
        return usingDing;
    }

    /**
     * Enables the X-axis crosing chirp flag.
     * @param usingDing Determines whether the trace crossing the X-axis is
     *          indicated with a ding.
     */
    public void setUsingDing(boolean usingDing) {
        this.usingDing = usingDing;
    }

    /**
     * Returns the state of the Hiss flag.
     * 
     * @return True whenever a hiss is used to indicate negative y values.
     */
    public boolean isUsingHiss() {
        return usingHiss;
    }

    /**
     * Enables the hiss sonification for negative y values.
     * 
     * @param usingHiss Determines whether negative y values will be indicated
     *          with a hiss.
     */
    public void setUsingHiss(boolean usingHiss) {
        this.usingHiss = usingHiss;
    }

    /**
     * Determines which waveform is to be used for sonification.
     * 
     * @return a define constant indicating the waveform indicating which
     *         waveform.
     * @see gov.nasa.ial.mde.sound.MultiWave#SINE
     * @see gov.nasa.ial.mde.sound.MultiWave#SAW
     * @see gov.nasa.ial.mde.sound.MultiWave#TRIANGLE
     * @see gov.nasa.ial.mde.sound.MultiWave#SQUARE
     * @see gov.nasa.ial.mde.sound.MultiWave#VARIABLE
     */
    public int getWhichWaveform() {
        return whichWaveform;
    }

    /**
     * Sets which waveform to use for sonification.
     * 
     * @param whichWaveform a define constant indicating the waveform.
     * @see gov.nasa.ial.mde.sound.MultiWave#SINE
     * @see gov.nasa.ial.mde.sound.MultiWave#SAW
     * @see gov.nasa.ial.mde.sound.MultiWave#TRIANGLE
     * @see gov.nasa.ial.mde.sound.MultiWave#SQUARE
     * @see gov.nasa.ial.mde.sound.MultiWave#VARIABLE
     */
    public void setWhichWaveform(int whichWaveform) {
        switch (whichWaveform) {
        case MdeSettings.SINE:
            this.whichWaveform = MultiWave.SINE;
            return;

        case MdeSettings.TRIANGLE:
            this.whichWaveform = MultiWave.TRIANGLE;
            return;

        case MdeSettings.SAW:
            this.whichWaveform = MultiWave.SAW;
            return;

        case MdeSettings.SQUARE:
            this.whichWaveform = MultiWave.SQUARE;
            return;

        case MdeSettings.VARIABLE:
            this.whichWaveform = MultiWave.VARIABLE;
            return;

        default:
            throw new IllegalArgumentException("Unsupported value of whichWaveForm = " + whichWaveform);
        } // end switch
    }

    /**
     * Changes the duration of the sound which indicates an X-axis crossing.
     * 
     * @param duration must be one of <code>MdeSettings.SHORT</code>,
     *            <code>MdeSettings.MEDIUM</code> or
     *            <code>MdeSettings.LONG</code>
     * @see gov.nasa.ial.mde.properties.MdeSettings
     */
    public void setXAxisIndicatorDuration(int duration) {
        switch (duration) {
        case MdeSettings.SHORT:
            source.setChirpDuration(50); // milliseconds
            return;

        case MdeSettings.MEDIUM:
            source.setChirpDuration(150); // milliseconds
            return;

        case MdeSettings.LONG:
            source.setChirpDuration(300); // milliseconds
            return;

        default:
            throw new IllegalArgumentException("Unsupported value of duration = " + duration);
        } // end switch
    }

    /**
     * Changes the pitch of the sound which indicates an X-axis crossing.
     * 
     * @param frequency must be one of <code>MdeSettings.HIGH</code>,
     *            <code>MdeSettings.MEDIUM</code> or
     *            <code>MdeSettings.LOW</code>
     * @see gov.nasa.ial.mde.properties.MdeSettings
     */
    public void setXAxisIndicatorFrequency(int frequency) {
        switch (frequency) {
        case MdeSettings.LOW:
            source.setChirpFrequency(1000f); // Hz
            return;

        case MdeSettings.MEDIUM:
            source.setChirpFrequency(2000f); // Hz
            return;

        case MdeSettings.HIGH:
            source.setChirpFrequency(4000f); // Hz
            return;

        default:
            throw new IllegalArgumentException("Unsupported value of frequency = " + frequency);
        } // end switch
    }

    /**
     * Changes the duration of the sound which indicates a Y-axis crossing.
     * 
     * @param duration must be one of <code>MdeSettings.SHORT</code>,
     *            <code>MdeSettings.MEDIUM</code> or
     *            <code>MdeSettings.LONG</code>
     * @see gov.nasa.ial.mde.properties.MdeSettings
     */
    public void setYAxisIndicatorDuration(int duration) {
        switch (duration) {
        case MdeSettings.SHORT:
            dingTime = 50; // milliseconds
            return;

        case MdeSettings.MEDIUM:
            dingTime = 150; // milliseconds
            return;

        case MdeSettings.LONG:
            dingTime = 300; //Milliseconds
            return;

        default:
            throw new IllegalArgumentException("Unsupported value of duration = " + duration);
        } // end switch
    }

    /**
     * Changes the pitch of the sound which indicates a Y-axis crossing.
     * 
     * @param frequency must be one of <code>MdeSettings.HIGH</code>,
     *            <code>MdeSettings.MEDIUM</code> or
     *            <code>MdeSettings.LOW</code>
     * @see gov.nasa.ial.mde.properties.MdeSettings
     */
    public void setYAxisIndicatorFrequency(int frequency) {
        switch (frequency) {
        case MdeSettings.LOW:
            dingFreq = 1000f; // Hz
            return;

        case MdeSettings.MEDIUM:
            dingFreq = 2000f; // Hz
            return;

        case MdeSettings.HIGH:
            dingFreq = 4000f; //Hz
            return;

        default:
            throw new IllegalArgumentException("Unsupported value of frequency = " + frequency);
        } // end switch
    }

    /**
     * Can be used to determine the current level of "buzz" inserted to indicate
     * negative Y-values.
     * 
     * @return the alteredWaveformAmplitude.
     */
    public float getAlteredWaveformAmplitude() {
        return alteredWaveformAmplitude;
    }

    /**
     * Used to modify the degree of "buzz" inserted to indicate negative
     * Y-values.
     * 
     * @param alteredWaveformAmplitude the alteredWaveformAmplitude to set.
     */
    public void setAlteredWaveformAmplitude(float alteredWaveformAmplitude) {
        this.alteredWaveformAmplitude = alteredWaveformAmplitude;
    }
    
} // end class Sounder
