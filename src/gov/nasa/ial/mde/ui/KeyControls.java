/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The <code>KeyControls</code> class represents a hot-key functionality.
 * 
 * @author Dr. Robert Shelton
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class KeyControls implements KeyListener {

    private static final int DO_NOTHING = 0, DECREMENT = 1, INCREMENT = 2;

    private int doWhich = DO_NOTHING;

    /** Odd numbered function keys. */
    private static final int[] DECREMENT_KEYS = { KeyEvent.VK_F3, KeyEvent.VK_F5, KeyEvent.VK_F7, KeyEvent.VK_F9, KeyEvent.VK_F11 };
    
    /** Even numbered function keys. */
    private static final int[] INCREMENT_KEYS = { KeyEvent.VK_F4, KeyEvent.VK_F6, KeyEvent.VK_F8, KeyEvent.VK_F10, KeyEvent.VK_F12 };

    private static final int MAX_SLIDES = INCREMENT_KEYS.length;

    private int numSlides;
    private int numHotKeys;

    private String[] hotKeys = new String[0];

    private int whichSlide = -1;

    private int updateRate = 60; // 60 ms
    private int initialPause = 800; // 800 ms
    private boolean keyIsPressed = false;
    private boolean keyHeldDown = false;

    private double[] increment;
    private double[] slideIncrement;
    private double[] incrementAccelerator;
    private double[] value;

    private Object lock = new Object();

    /**
     * Constructs a key control for the specified slider control and hot key 
     * names.
     * 
     * @param ns number of slides.
     * @param hk hot-key names.
     */
    public KeyControls(int ns, String[] hk) {
        if (ns > MAX_SLIDES) {
            throw new IllegalArgumentException("Too many slide controls; Max = " + MAX_SLIDES);
        }
        
        increment = new double[numSlides = ns];
        value = new double[numSlides];
        incrementAccelerator = new double[numSlides];
        slideIncrement = new double[numSlides];

        for (int i = 0; i < numSlides; i++) {
            incrementAccelerator[i] = 0.0;
            slideIncrement[i] = 0.0;
            increment[i] = 0.0;
            value[i] = 0.0;
        }
        
        if (hk != null) {
            hotKeys = hk;
        }
        
        numHotKeys = hotKeys.length;

        new Thread(new Runnable() {
            public void run() {
                boolean initialKeyPress = true;
                boolean keyHeldDn = false;
                long waitTime = 0;

                while (true) {
                    if (keyIsPressed || keyHeldDown) {
                        if (keyHeldDown) {
                            // Set local flag, captures the held state when we want it to.
                            keyHeldDn = true;
                        }
                        if (initialKeyPress) {
                            initialKeyPress = false;
                            waitTime = initialPause;
                        } else {
                            waitTime = updateRate;
                        }
                    } else {
                        initialKeyPress = true;
                        waitTime = 0; // wait until a nofity() is called
                        for (int i = 0; i < numSlides; i++) {
                            slideIncrement[i] = 0;
                        }

                        // Make sure we do the last update when the user releases the key
                        // if it had been held down for a while. Ignore it if they just
                        // taped the key.
                        if (keyHeldDn && (whichSlide >= 0) && (whichSlide < numSlides)) {
                            keyHeldDn = false; // clear local flag.
                            onSlider(whichSlide);
                        }
                    }
                    
                    synchronized (lock) {
                        try {
                            lock.wait(waitTime);
                        } catch (InterruptedException ie) { } // end try
                    }

                    if ((whichSlide < 0) || (whichSlide >= numSlides)) {
                        continue;
                    }

                    switch (doWhich) {
                        case INCREMENT :
                            slideIncrement[whichSlide] = Math.min(increment[whichSlide], slideIncrement[whichSlide] + incrementAccelerator[whichSlide]);
                            value[whichSlide] = Math.min(1.0, value[whichSlide] + slideIncrement[whichSlide]);
                            onSlider(whichSlide);
                            break;

                        case DECREMENT :
                            slideIncrement[whichSlide] = Math.min(increment[whichSlide], slideIncrement[whichSlide] + incrementAccelerator[whichSlide]);
                            value[whichSlide] = Math.max(0.0, value[whichSlide] - slideIncrement[whichSlide]);
                            onSlider(whichSlide);
                            break;

                        default :
                            for (int i = 0; i < numSlides; i++) {
                                slideIncrement[i] = 0;
                            }
                            break;
                    } // end switch
                } // end while
            } // end run
        } // end Runnable
        ).start();
    } // end KeyControls

    
    /**
     * Called by a slider key event to handle the new slider value.
     * 
     * @param w which slider changed.
     */
    public void onSlider(int w) {
        // do nothing
    } // end onSlider

    /**
     * Called by a hot-key event to handle the new hot key value.
     * 
     * @param w which hot-key what pressed.
     */
    public void onHotKey(int w) {
        // do nothing
    } // end onHotKey

    /**
     * Returns the slider value for the specified slider index.
     * 
     * @param w which slider.
     * @return the slider value.
     */
    public double getValue(int w) {
        return value[w];
    } // end getValue

    /**
     * Returns the update rate.
     * 
     * @return the update rate.
     */
    public int getUpdatetRate() {
        return updateRate;
    }

    /**
     * Sets the update rate in milliseconds.
     * 
     * @param ms the update rate in milliseconds.
     */
    public void setUpdateRate(int ms) {
        // Don't allow values less than 60 ms
        this.updateRate = Math.max(60, ms);
    }

    /**
     * Returns the duration of the initial pause.
     * 
     * @return the duration of the initial pause in milliseconds.
     */
    public int getInitialPause() {
        return this.initialPause;
    }

    /**
     * Sets the initial pause in milliseconds.
     * 
     * @param ms the initial pause in milliseconds.
     */
    public void setInitialPause(int ms) {
        // Don't allow values less than 60 ms
        this.initialPause = Math.max(60, ms);
    }

    /**
     * Returns the state of a key press.
     * 
     * @return true if a key is held down, false otherwise.
     */
    public boolean isKeyHeldDown() {
        return keyHeldDown;
    }

    /**
     * Process a focus lost event.
     */
    public void processFocusLost() {
        doWhich = DO_NOTHING;
        keyIsPressed = false;
        keyHeldDown = false;
    }

    /**
     * Returns the increment value for the specified slider.
     * 
     * @param w which slider.
     * @return the increment value.
     */
    public double getIncrement(int w) {
        return increment[w];
    } // end getIncrement

    /**
     * Sets the increment for the specified slider.
     * 
     * @param dx increment value.
     * @param w which slider.
     */
    public void setIncrement(double dx, int w) {
        increment[w] = dx;
        if (incrementAccelerator[w] == 0.0)
            incrementAccelerator[w] = dx;
    } // end setIncrement

    /**
     * Sets the value for the specified slider.
     * 
     * @param v the value.
     * @param w which slider
     */
    public void setValue(double v, int w) {
        value[w] = v;
    } // end setValue

    /**
     * Sets the slider increment accelerator value.
     * 
     * @param fraction the slider increment accelerator value.
     * @param w which slider.
     */
    public void setIncrementAccelerator(double fraction, int w) {
        incrementAccelerator[w] = fraction * increment[w];
    } // end setIncrementAccelerator

    /**
     * Handle the key pressed events for the sliders and hot-keys.
     * 
     * @param ke the key event.
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {
        int i, k = ke.getKeyCode();

        if ((i = whichKey(k, INCREMENT_KEYS)) >= 0) {
            doWhich = INCREMENT;
            whichSlide = i;
            if (keyIsPressed) {
                keyHeldDown = true;
            } else {
                keyIsPressed = true;
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
            return;
        } // end if

        if ((i = whichKey(k, DECREMENT_KEYS)) >= 0) {
            doWhich = DECREMENT;
            whichSlide = i;
            if (keyIsPressed) {
                keyHeldDown = true;
            } else {
                keyIsPressed = true;
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
            return;
        } // end if
    } // end keyPressed

    /**
     * Handle the key released events for the sliders and hot-keys.
     * 
     * @param ke the key event.
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent ke) {
        doWhich = DO_NOTHING;
        keyIsPressed = false;
        keyHeldDown = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    } // end keyReleased

    /**
     * Handle the key typed events for the sliders and hot-keys.
     * 
     * @param ke the key event.
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
        int k = whichHK(ke.getKeyChar());

        if (k >= 0) {
            onHotKey(k);
        }
    } // end keyTyped

    private int whichKey(int keyCode, int[] keyCodes) {
        int i, n = keyCodes.length;

        for (i = 0; i < n; i++) {
            // System.out.print (" " + keyCodes[i]);
            if (keyCode == keyCodes[i])
                return i;
        } // end for i

        return -1;
    } // end whichKey

    private int whichHK(char c) {
        for (int i = 0; i < numHotKeys; i++) {
            if (hotKeys[i].indexOf(c) >= 0) {
                return i;
            }
        }

        return -1;
    } // end whichHK

} // end class KeyControls
