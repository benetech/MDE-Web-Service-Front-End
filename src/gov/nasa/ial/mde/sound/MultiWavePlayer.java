/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Jun 13, 2005
 */
package gov.nasa.ial.mde.sound;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Multi-Waveform Player
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MultiWavePlayer {

    /**
     * <code>J_BUFF_SIZE</code>-- Size of the buffer internal to the Java
     * sound system. This parameter controls the latency in the response of the
     * sound because a sample must traverse this internal buffer in a time given
     * by t =<code>J_BUFF_SIZE </code> /(4* <code>SAMPLE_RATE </code>). Make
     * it too small, and you will have breaks in the sound; too big, and you
     * increase latency in responses to user commands to change the output. This
     * value corresponds to a command latency of approximately 0.07 seconds, or
     * about 14.4 commands per second.
     */
    public final static int J_BUFF_SIZE = 12288;

    /**
     * <code>LATENCY_IN_MILLISECONDS</code>-- the time between changes in the
     * data stream and changes in the output sound. For default parameters, the
     * value is approximately 70 milliseconds.
     */
    public final static int LATENCY_IN_MILLISECONDS = 
                (int)Math.rint(1000.0*MultiWavePlayer.J_BUFF_SIZE /
                                      (4 * MultiWavePlayer.SAMPLE_RATE));

    /**
     * <code>BUFF_SIZE</code> is the size of the buffer filled by the user
     * which mediates data transfer from <code>source</code> to
     * <code>line</code>.
     */
    private final static int BUFF_SIZE = 256;

    /**
     * <code>FUNDAMENTAL</code>-- Commonly accepted pitch for A above middle C.
     */
    public final static float FUNDAMENTAL = 440.0f;

    /**
     * <code>SAMPLE_RATE</code>-- the perennial sampling rate equals 44,100
     * samples per second.
     */
    public final static float SAMPLE_RATE = 44100.0f;

    /**
     * <code>buff</code>-- Data buffer that mediates transfer from
     * <code>source</code> to <code>line</code>.
     */
    private byte[] buff = new byte[MultiWavePlayer.BUFF_SIZE];

    /**
     * <code>format</code>-- The object which encapsulates information
     * required to set up an audio channel. 16 bits, 2 channels, two
     * bytes/channel, framesize = 4.
     */
    private AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED, MultiWavePlayer.SAMPLE_RATE, 16,
            2, 4, MultiWavePlayer.SAMPLE_RATE, false);

    /**
     * <code>line</code>-- The object that links the software to the audio
     * hardware. Consumes data read from <code>source</code>.
     */
    private SourceDataLine line;

    /**
     * <code>pan</code>-- left/right stereo control
     */
    private FloatControl pan;

    /**
     * <code>play</code>-- controls buffer-filling thread.
     */
    private boolean play = false;

    /**
     * <code>source</code>-- the object encapsulating the software which
     * generates the waveforms to be rendered by the audio system.
     */
    protected MultiWave source;

    /**
     * <code>maxTones</code>-- number of simultaneous tones this
     * <code>MultiWavePlayer</code> can handle.
     */

    /**
     * Disallows default constructor by throwing a RuntimeException.
     */
    protected MultiWavePlayer() {
        throw new RuntimeException("Default constructor not allowed");
    } // end MultiWavePlayer

    /**
     * Constructs a <code>MultiWavePlayer</code> with specified capacity for
     * simultaneous tones.
     * 
     * @param maxTones --
     *            the maximum number of simultaneous tones which can be handled
     *            by this <code>MultiWavePlayer</code>.
     */
    public MultiWavePlayer(int maxTones) {
        source = new MultiWave(format, 0.2f, maxTones);
    } // end MultiWavePlayer

    /**
     * Allocates <code>line</code>, a new <code>SourceDataLine</code> for
     * this <code>MultiWavePlayer</code>. opens and Starts <code>line</code>.
     * Starts the player thread.
     * 
     * @see gov.nasa.ial.mde.sound.MultiWavePlayer#line
     * @see javax.sound.sampled.SourceDataLine 
     */
    public void initLine() {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, MultiWavePlayer.J_BUFF_SIZE);
            pan = (FloatControl) line.getControl(FloatControl.Type.PAN);
        } // end try
        catch (LineUnavailableException lue) {
            System.err.println("No audio line available; no sound will play.");
            return;
        } // end catch

        line.start(); // starts the internal buffer-filling operation
        doPlay(); // starts my buffer-filling thread
    }

    /**
     * Set the pan (left/right) values.  It does what you might think Allowable,
     * where the range for p = [-1, 1], with -1 corresponding to full left and
     * 1 corresponding to full right.
     * 
     * @param p the pan value.
     */
    public void setPan(float p) {
    	p = Math.min(p, 1.0f);
    	p = Math.max(p, -1.0f);
        pan.setValue(p);
    } // end setPan

    /**
     * Sets the volume level.
     * 
     * @param v the volume level.
     */
    public void setVolume(float v) {
        source.setVolume(v);
    } // end setVolume

    /**
     * Cranks the buffer-filling thread. Does nothing if
     * <code>MultiWavePlayer.play</code> is true, i.e. the thread is already
     * running If necessary, opens <code>MultiWavePlayer.line</code>.
     */
    public synchronized void doPlay() {

        // Just return if the play thread is running so that we don't create
        // another one.
        if (play) {
            return;
        }
        play = true;

        // Make sure we have an open line.
        if (!isOpen()) {
            initLine();
        }

        new Thread(new Runnable() {
            public void run() {
                while (play) {
                    try {
                        int nRead = source.read(buff);
                        line.write(buff, 0, nRead);
                    } catch (IOException ioe) {
                        play = false;
                        ioe.printStackTrace();
                        System.exit(1);
                    } // end catch
                } // end while
            } // end run
        } // end new Runnable
        ).start();
    } // end doPlay

    /**
     * Stops playing, the buffer-filler-killer.
     */
    public void doStop() {
        play = false;
    } // end doStop

    /**
     * Shuts off all tones currently playing by calling
     * <code>deactivateAll</code>, waits for all buffers to empty, and then
     * terminates the thread which plays the sound.
     * 
     * @see gov.nasa.ial.mde.sound.MultiWave#deactivateAll()
     */
    private void hush() {
        try {
            source.deactivateAll();
            
            // Let the source buffer empty before we stop the threads next.
            try {
                Thread.sleep(3 * MultiWavePlayer.LATENCY_IN_MILLISECONDS);
            } catch (InterruptedException ie) {
                // ignore this exception
            }
        } finally {
            // We do want to stop even if we have an error above.
            doStop();
        }
    } // end hush

    /**
     * Closes this <code>MultiWavePlayer</code> by calling <code>hush</code> then
     * closes <code>line</code>.
     */
    public void close() {
        hush();
        if (line != null) {
            line.close();
        }
    }

    /**
     * Return true if the thread is playing audio, otherwise false.
     * 
     * @return true if the thread is playing audio, otherwise false.
     */
    public boolean isPlaying() {
        return play;
    }

    /**
     * Returns the pan control for this <code>MultiWavePlayer</code>
     * 
     * @return the <code>FloatControl</code> that controls stereo panning for
     *         this <code>MultiWavePlayer</code>
     * @see javax.sound.sampled.FloatControl
     */
    public FloatControl getPan() {
        return pan;
    } // end getPan

    /**
     * Returns the status of the <code>SourceDataLine</code> for this
     * <code>MultiWavePlayer</code>.
     * 
     * @return true if the <code>SourceDataLine</code> is non-null and open;
     *         false otherwise.
     * @see gov.nasa.ial.mde.sound.MultiWave
     * @see javax.sound.sampled.SourceDataLine
     */
    public boolean isOpen() {
        return ((line != null) && line.isOpen());
    }

    /**
     * Gets the instance of <code>MultiWave</code> which is the source for
     * this <code>MultiWavePlayer</code>.
     * 
     * @return <code>MultiWave</code> source for this
     *         <code>MultiWavePlayer</code>
     */
    public MultiWave getSource() {
        return source;
    }
    
} // end class MultiWavePlayer
