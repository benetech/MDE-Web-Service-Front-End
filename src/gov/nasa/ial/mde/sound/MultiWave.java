/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.sound;

import javax.sound.sampled.AudioFormat;

/**
 * A Multi-Waveform Generator.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MultiWave extends Generator {
    
    private int numFrequencies = 0;

    private int[] whichWaveForm;

    private float[] frequencies;

    private float[] amplitudes;

    private double[] phases;

    private double[] deltaPhases;

    private int numActive = 0;

    private boolean[] isActive;

    private float frameRate;

    /* parameters to support transient events such as ``dings'' */
    private double transientDuration = 0.0; // transient duration in seconds

    private int transientNumFrames = 0; // length of transient in frames

    private int transientFrameCount = 0;

    private double lambda; // decay constant in inverse seconds

    private double chirpFactor;

    private float chirpFrequency = 4000f; // Hz

    private int chirpDuration = 150; // milliseconds

    private double dingFreq = 0.0; // fundamental frequency of ding

    private static double DECAY_MULTIPLE = 3.0; // number of factors of 1/e in ding interval
    
    /* parameter to support addition of white noise to indicate negative values */
    private double noiseCoefficient = 0.0; // coefficient of white noise
    
    /* Settings for use in the case of variable waveforms */
    private double[] defaultMixingCoefficients = { 1.0, 0.0, 0.0, 0.0 };

    private double[][] mixingCoefficients;

    private double highCutoff = 300.0, midCutoff = 220.0, lowCutoff = 100.0;

    /** A constant with the value 2 * Pi. */
    public static final double TWO_PI = 2.0 * Math.PI;
    
    /**
     * Constant indicating a pure sine wave.
     */
    public static final int SINE = 0;

    /**
     * Constant indicating a sawtooth waveform.
     */
    public static final int SAW = 1;

    /**
     * Constant indicating a triangular waveform.
     */
    public static final int TRIANGLE = 2;

    /**
     * Constant indicating a square wave.
     */
    public static final int SQUARE = 3;

    /**
     * Constant indicating a waveform whose shape varies with pitch.
     */
    public static final int VARIABLE = 4;

    /**
     * The waveform cross correlations.
     * 
     * <p>0,0:SINE x SINE
     * <p>1,0:SAW x SINE, 1,1:SAW x SAW
     * <p>2,0:TRIANGLE x SINE, 2,1:TRIANGLE x SAW, 2,2:TRIANGLE x TRIANGLE
     * <p>3,0:SQUARE x SINE, 3,1:SQUARE x SAW, 3,2:SQUARE x TRIANGLE, 3,3:SQUARE x SQUARE
     */
    public static final float[][] CROSS_CORRELATIONS = {
            { 0.5f }, // SINE x SINE
            { (float)(1.0 / Math.PI), (float) (1.0 / 3.0) }, // SAW x SINE, SAW x SAW
            { (float)(4.0 / (Math.PI * Math.PI)), 0.25f, (float) (1.0 / 3.0) }, // TRIANGLE x SINE,
                                                                                // TRIANGLE x SAW,
                                                                                // TRIANGLE x TRIANGLE
            { (float)(2.0 / Math.PI), 0.5f, 0.5f, 1.0f } }; // SQUARE x SINE,
                                                            // SQUARE x SAW,
                                                            // SQUARE x TRIANGLE,
                                                            // SQUARE x SQUARE

    private static final int SINE_TABLE_SIZE = 2500;

    private float[] sineTable = new float[MultiWave.SINE_TABLE_SIZE + 1];

    /**
     * Creates an instance of <code>MultiWave</code> with the specified audio
     * format, amplitude, and number of waveforms.
     * 
     * @param f the audio-format to use.
     * @param a the amplitude/volume level.
     * @param n the number of waveforms.
     */
    public MultiWave(AudioFormat f, float a, int n) {
        super(f, a);
        frameRate = getFormat().getFrameRate();
        amplitudes = new float[numFrequencies = n];
        frequencies = new float[n];
        whichWaveForm = new int[n];
        phases = new double[n];
        deltaPhases = new double[n];
        isActive = new boolean[n];
        mixingCoefficients = new double[n][MultiWave.VARIABLE];

        for (int i = 0; i < n; i++) {
            isActive[i] = false;
            phases[i] = 0.0;
            deltaPhases[i] = 0.0;
            for (int j = MultiWave.SINE; j < MultiWave.VARIABLE; j++) {
                mixingCoefficients[i][j] = defaultMixingCoefficients[j];
            }
        } // end for i
        initializeSineTable();
    } // end MultiWave

    /**
     * Convince method for calculating the power for a given set of coefficients.
     * @param coefficients the waveform coefficients.
     * @return the power.
     */
    public static float power(float[] coefficients) {
        float p = 0f;

        for (int i = MultiWave.SINE; i < MultiWave.VARIABLE; i++) {
            p += (MultiWave.CROSS_CORRELATIONS[i][i] * coefficients[i] * coefficients[i]);
            for (int j = MultiWave.SINE; j < i; j++) {
                p += (2.0f * MultiWave.CROSS_CORRELATIONS[i][j] * coefficients[i] * coefficients[j]);
            }
        } // end for i

        return p;
    } // end power

    /**
     * Set the noise coefficient for the waveform.
     * 
     * @param noiseCoefficient the amount of noise to add to the waveform.
     */
    public void setNoise(double noiseCoefficient) {
        this.noiseCoefficient = noiseCoefficient;
    }

    /**
     * Activates a voice with a given waveform with a default amplitude of 1
     * 
     * @param which The designation of which voice is to be activated
     * @param freq  The desired frequency
     * @param wf  Which waveform, one of MultiWave.SINE, MultiWave.SAW,
     *            MultiWave.TRIANGLE, MultiWave.SQUARE, or MultiWave.VARIABLE
     */
    public void activate(int which, float freq, int wf) {
        activate(1f, which, freq, wf);
    } // end activate

    /**
     * Activates a voice with a given waveform
     * 
     * @param a  The desired amplitude
     * @param which  The designation of which voice is to be activated
     * @param freq  The desired frequency
     * @param wf  Which waveform, one of MultiWave.SINE, MultiWave.SAW,
     *            MultiWave.TRIANGLE, MultiWave.SQUARE, or MultiWave.VARIABLE 
     */
    public void activate(float a, int which, float freq, int wf) {
        if (wf == MultiWave.VARIABLE)
            if (freq > highCutoff) {
                mixingCoefficients[which][MultiWave.SINE] = 1.0;
                mixingCoefficients[which][MultiWave.SAW] = mixingCoefficients[which][MultiWave.SQUARE] = mixingCoefficients[which][MultiWave.TRIANGLE] = 0.0;
            } else if (freq < lowCutoff) {
                mixingCoefficients[which][MultiWave.SAW] = 0.5;
                mixingCoefficients[which][MultiWave.TRIANGLE] = 1.0;
                mixingCoefficients[which][MultiWave.SINE] = mixingCoefficients[which][MultiWave.SQUARE] = 0.0;
            } else {
                mixingCoefficients[which][MultiWave.SINE] = (freq - lowCutoff)/(highCutoff - lowCutoff);
                mixingCoefficients[which][MultiWave.TRIANGLE] = 1.0 - mixingCoefficients[which][MultiWave.SINE];
                if (freq < midCutoff) {
                    mixingCoefficients[which][MultiWave.SAW] = 0.5*(midCutoff - freq)/(midCutoff - lowCutoff);
                } else
                    mixingCoefficients[which][MultiWave.SAW] = 0.0;
            }

        if (!isActive[which]) {
            numActive++;
            isActive[which] = true;
        } // end if

        frequencies[which] = freq;
        amplitudes[which] = a;
        whichWaveForm[which] = wf;
        deltaPhases[which] = TWO_PI * freq / frameRate;
    } // end activate

    /**
     * Deactivate the specified waveform.
     * 
     * @param which the waveform index to deactivate.
     */
    public void deactivate(int which) {
        if (isActive[which]) {
            isActive[which] = false;
            numActive--;
        } // end if
    } // end deactivate

    /**
     * Deactivate all the waveforms.
     */
    public void deactivateAll() {
        for (int i = 0; i < numFrequencies; i++)
            isActive[i] = false;

        numActive = 0;
    } // end deactivateAll

    /**
     * Returns the number of active waveforms.
     * 
     * @return the number of active waveforms.
     */
    public int getNumActive() {
        return numActive;
    } // end getNumActive

    /**
     * Sets the Ding frequency and duration.
     * 
     * @param freq the frequency of the ding.
     * @param duration the duration of the ding in milliseconds.
     */
    public void ding(float freq, int duration) {
        ding(freq, duration, 0.0);
    } // end ding

    /**
     * Sets the Ding frequency, duration, and chirp-factor.
     * 
     * @param freq the frequency of the ding.
     * @param durationInMillisecs the duration of the ding in milliseconds.
     * @param cf the cirp factor.
     */
    private void ding(float freq, int durationInMillisecs, double cf) {
        this.dingFreq = freq;
        this.chirpFactor = cf;
        transientFrameCount = transientNumFrames = (int) Math.rint(frameRate * (transientDuration = durationInMillisecs / 1000.0));
        this.lambda = MultiWave.DECAY_MULTIPLE / transientDuration;
    }

    /**
     * Chirp going up.
     */
    public void chirpUp() {
        ding(chirpFrequency, chirpDuration, 1.0 / 0.15);
    } // end chirpUp

    /**
     * Chirp doing down.
     */
    public void chirpDown() {
        ding(chirpFrequency, chirpDuration, -1.0 / 0.15);
    } // end chirpDown

    /**
     * Returns the next sample in the waveform.
     * 
     * @return the next sample in the waveform.
     */
    public float nextFloat() {
        int i, n = 0;
        float r = 0.0f;

        for (i = 0; i < numFrequencies; i++)
            if (isActive[i]) {
                r += (amplitudes[i] * waveForm(phases[i], whichWaveForm[i], i));
                phases[i] += deltaPhases[i];
                while (phases[i] > TWO_PI)
                    phases[i] -= TWO_PI;
                n++;
            } // end if

        if (transientFrameCount > 0) {
            double t = (transientNumFrames - transientFrameCount--) / frameRate;
            double u = TWO_PI * t * dingFreq * Math.pow(2.0, t * chirpFactor);

            while (u >= TWO_PI)
                u -= TWO_PI;

            n++;
            r += (Math.exp(-lambda * t) * waveForm(u, MultiWave.SQUARE, -1));
        }
        if (noiseCoefficient != 0.0) {
            r += noiseCoefficient * (2.0 * Math.random() - 1.0);
            return (n > 0) ? r / (float) Math.sqrt(n) : r;
        }

        return (n > 0) ? r / (float) Math.sqrt(n) : 0f;
    } // end nextFloat

    private void initializeSineTable() {
        double d = 2.0 * Math.PI / MultiWave.SINE_TABLE_SIZE;
        double x = 0.0;

        for (int i = 0; i < MultiWave.SINE_TABLE_SIZE; i++) {
            sineTable[i] = (float) Math.sin(x);
            x += d;
        } // end for i

        sineTable[MultiWave.SINE_TABLE_SIZE] = 0f;
    } // end initializeSineTable

    private float sine(double x) {
        double w = 0.5 * x / Math.PI;
        double f = w - Math.floor(w);
        double y = f * MultiWave.SINE_TABLE_SIZE;
        double n = Math.floor(y);
        double beta = y - n, alpha = 1.0 - beta;

        return (float) (alpha * sineTable[(int) n] + beta * sineTable[1 + (int) n]);
    } // end sine

    /**
     * Returns the value of the specified waveform at the specified x value and
     * coefficient index.
     * 
     * @param x the x-value of the waveform.
     * @param wf Which waveform, one of MultiWave.SINE, MultiWave.SAW,
     *            MultiWave.TRIANGLE, MultiWave.SQUARE, or MultiWave.VARIABLE
     * @param fNum the index to the coefficients values to use.
     * @return the waveform value given x.
     * @see gov.nasa.ial.mde.sound.MultiWave#SINE
     * @see gov.nasa.ial.mde.sound.MultiWave#SAW
     * @see gov.nasa.ial.mde.sound.MultiWave#TRIANGLE
     * @see gov.nasa.ial.mde.sound.MultiWave#SQUARE
     * @see gov.nasa.ial.mde.sound.MultiWave#VARIABLE
     */
    public float waveForm(double x, int wf, int fNum) {
        switch (wf) {
        case MultiWave.SINE:
            //                return (float)Math.sin(x);
            return sine(x);

        case MultiWave.SAW:
            return (float) (x / Math.PI - 1f);

        case MultiWave.TRIANGLE:
            return (float) (1.0 - 2.0 * Math.abs(x - Math.PI) / Math.PI);

        case MultiWave.SQUARE:
            return (x < Math.PI) ? 1f : -1f;

        case MultiWave.VARIABLE: {
            double r = 0.0, s = 0.0;

            for (int i = MultiWave.SINE; i < MultiWave.VARIABLE; i++) {
                r += mixingCoefficients[fNum][i] * waveForm(x, i, -1);
                s += (mixingCoefficients[fNum][i] * mixingCoefficients[fNum][i]);
            }
            return (float) (r / Math.sqrt(s));
        }

        default:
            throw new RuntimeException();
        } // end switch
    } // end waveForm

    /**
     * Returns the chirp duration.
     * 
     * @return the chirp duration in milliseconds.
     */
    public int getChirpDuration() {
        return chirpDuration;
    }

    /**
     * Sets the chirp duration.
     * 
     * @param chirpDuration the chirp duration in milliseconds.
     */
    public void setChirpDuration(int chirpDuration) {
        this.chirpDuration = chirpDuration;
    }

    /**
     * Returns the chirp frequency.
     * 
     * @return the chirp frequency.
     */
    public float getChirpFrequency() {
        return chirpFrequency;
    }

    /**
     * Sets the chirp frequency.
     * 
     * @param chirpFrequency the chirp frequency.
     */
    public void setChirpFrequency(float chirpFrequency) {
        this.chirpFrequency = chirpFrequency;
    }
    
} // end class MultiWave
