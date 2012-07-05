/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.sound;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * A class used to generate the audio as an audio input stream.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class Generator extends AudioInputStream {
    
    private float amplitude;
    private float amplitudeFraction;

    /**
     * Constructs a <code>Generator</code> with the specified audio format and volum.
     * 
     * @param f the audio format to use.
     * @param volume the volume to use.
     */
    public Generator(AudioFormat f, float volume) {
        super(new ByteArrayInputStream(
                    new byte[0]),
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                                    f.getSampleRate(), 
                                    16, 
                                    2, 
                                    4, 
                                    f.getFrameRate(), 
                                    f.isBigEndian()),
              AudioSystem.NOT_SPECIFIED);

        amplitudeFraction = amplitude = volume * (float)Math.pow(2.0, getFormat().getSampleSizeInBits() - 1.0);
    } // end Generator

    /**
     * Sets the volume level.
     * 
     * @param v the volume level.
     */
    public void setVolume(float v) {
        float f = (float)Math.max(0.0, Math.min(1.0, v));

        amplitudeFraction = f * amplitude;
    } // end setVolume

    /**
     * Number of bytes available which is always <code>Integer.MAX_VALUE</code>.
     * 
     * @return always <code>Integer.MAX_VALUE</code>.
     */
    public int available() {
        return Integer.MAX_VALUE;
    } // end available

    /**
     * Always throws an IOException since we do not support this method for the
     * stream.
     * 
     * @exception java.io.IOException always thrown.
     */
    public int read() throws IOException {
        throw new IOException("Cannot use this method now");
    } // end read

    /**
     * Reads the speificed number of items from the stream and places them into
     * the buffer at the specified offset.
     * 
     * @param b the buffer to place the read items into.
     * @param off the offset with in the buffer to place the items.
     * @param len the number of items to read.
     * @return number of items read
     * @exception java.io.IOException is thrown if the length is not an integer
     *      multiple of frame size.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int fs = getFormat().getFrameSize();

        if ((len % fs) != 0) {
            throw new IOException("Length must be an integer multiple of frame size");
        }

        for (int i = 0; i < len; i += fs) {
            byte[] r = convert(nextFloat());

            System.arraycopy(r, 0, b, i, fs);
        } // end for i

        return len;
    } // end read

    private final byte[] convert(float f) {
        int v = Math.round(f * amplitudeFraction);
        byte low = (byte) (v & 0xFF);
        byte high = (byte) ((v >>> 8) & 0xFF);
        byte[] r = { low, high, low, high };

        return r;
    } // end convert

    /**
     * The next floating point value of the waveform.
     * 
     * @return the next floating point value of the waveform.
     */
    protected float nextFloat() {
        return 0f;
    } // end nextFloat
    
} // end class Generator
