/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.ui;

import gov.nasa.ial.mde.math.MultiPointXY;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solution;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.sound.Sounder;
import gov.nasa.ial.mde.ui.graph.CartesianGraph;
import gov.nasa.ial.mde.ui.util.ComponentUtil;
import gov.nasa.ial.mde.util.ResourceUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Graphical control for sonification and exploration of graphs.
 * Includes buttons to play, pause or reset the sonification;
 * a slider to manually explore the graph; and a window to display numerical values.
 * There is also a volume control to adjust the level of 
 * the sonification independently from the computer system volume,
 * thus independently of assistive technology such as screen reading software. 
 * 
 * @author Dr. Robert Shelton
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class SoundControl extends JPanel implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1617335415419566389L;

	/** Reference to the sounder component. */
    protected Sounder sounder = null;
    
    /** Reference to the solver. */
    protected Solver solver;
    
    /** The graph values text area. */
    protected JTextArea graphValues = new JTextArea(3, 14);
    
    /** Current X-slider position. */
    protected JSlider xPosition;
    
    /** Current sweep X position. */
    protected double sweepX = 0.0;
    
    /** Flag indicating that the sonification sweeping is taking place. */
    protected boolean sweeping = false;
    
    /** Flag indicating if the sound is enabled. */
    protected boolean soundEnabled = false;
    
    /** The points summary class. */
    protected PointsSummary ptsSummary = new PointsSummary();
    
    private float volumeLevel;
    
    /** Reference to the graph UI. */
    protected CartesianGraph graph = null;
    
    private MdeSettings settings;
    private boolean inSimulation = false;
    
    private JButton sweep;
    private JButton volume;
    private KeyEventDispatcher ked;
    private KeyControls KC;
    
    private JScrollPane valuePane = new JScrollPane(graphValues);
    private String sweepLabel, pauseLabel;
    private String sweepLabelShort, pauseLabelShort;
    private String volumeDownLabel;
    private String volumeUpLabel;
    private String indiVar;
    private int lim = Sounder.LATENCY_IN_MILLISECONDS;
    private double sweepTime;
    private double sweepIncrement = 0.0;
    
	private JButton soundSettingsBtn;
	private String soundSettingsLabel;
	private SoundSettingsDialog soundSettings = null;
    
    private ImageIcon playIcon1;
	private ImageIcon playIcon2;
	private ImageIcon pauseIcon1;
	private ImageIcon pauseIcon2;
	private ImageIcon volumeIcon1;
	private ImageIcon volumeIcon2;
	private ImageIcon soundSettingsIcon1;
	private ImageIcon soundSettingsIcon2;
	
    private final static int VOLUME_SLIDER = 1, X_POSITION_SLIDER = 2;
    private final static float VOLUME_UP_FACTOR = (float) Math.pow(10.0, 0.1); // + 1 or two DBs
                                                                               // depending on how
                                                                               // you count
    private final static float VOLUME_DOWN_FACTOR = 1.0f / VOLUME_UP_FACTOR;

    /**
     * Creates a new SoundControl graphical interface for exploring a visual graph. 
     * @param solver contains the data which is to be sonified.
     * @param settings contains user preferences for the acoustical display. 
     */
    public SoundControl(Solver solver, MdeSettings settings) {
        if (solver == null) {
            throw new NullPointerException("Null solver");
        }
        if (settings == null) {
            throw new NullPointerException("Null settings");
        }
        this.solver = solver;
        this.settings = settings;
		
		this.soundSettings = new SoundSettingsDialog(this,settings);

        KC = new KeyControls(3, null) {

            public void onSlider(int which) {
                int n = (int) Math.rint(100 * getValue(which));

                switch (which) {
                case SoundControl.VOLUME_SLIDER:
                    setVolume(volumeLevel = (float) getValue(which));
                    if (volumeLevel < 0.0001 || volumeLevel > 0.9999) {
                        volumeLimit();
                    }
                    break;

                case SoundControl.X_POSITION_SLIDER:
                    doPauseSound();
                    xPosition.setValue(n);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid slider number: " + which);
                } // end switch
            } // end onSlider
        }; // end new KeyControls

        KC.setIncrement(0.02, VOLUME_SLIDER);
        KC.setValue(1.0, VOLUME_SLIDER);
        KC.setIncrement(1.0, X_POSITION_SLIDER);
        KC.setIncrementAccelerator(0.01, X_POSITION_SLIDER);
        KC.setValue(0.0, X_POSITION_SLIDER);

        sweepLabel = "Play Sound Sweep";
        pauseLabel = "Pause Sound Sweep";
        sweepLabelShort = "Play";
        pauseLabelShort = "Pause";
        volumeDownLabel = "Decrease Volume";
        volumeUpLabel = "Increase Volume";
		soundSettingsLabel = "Settings";
        // TODO: Get actual independent variable symbol from somewhere
		indiVar = "Explore Values";

        buttonInit();
        sliderInit();
        hotkeyInit();

//        sweepTime = 2.0; // seconds
        updateSettings(settings);
        
        graphValues.setEditable(false);
        graphValues.setToolTipText("Sounded out graph values");
        graphValues.getAccessibleContext().setAccessibleName("Graph Values");

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(3, 1));
        buttons.add(sweep);
        buttons.add(volume);
		buttons.add(soundSettingsBtn);
//        buttons.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel sliders = new JPanel();
		sliders.setLayout(new GridLayout(2, 1));
        sliders.add(xPosition);
        sliders.add(valuePane);
        
        sliders.setBorder(BorderFactory.createLoweredBevelBorder());
//        sliders.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
//                "Explore Values",
//                TitledBorder.CENTER,
//                TitledBorder.TOP));
        
		setLayout(new BorderLayout());
        add(buttons, BorderLayout.WEST);
        add(sliders, BorderLayout.CENTER);
        
        // Update the background color and of the children as well.
        ComponentUtil.setBackground(this,ColorDefaults.BUTTON_BG_COLOR);
    } // end SoundControl

    private void hotkeyInit() {
        ked = new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                int i, k = e.getKeyCode();

                if ((k == KeyEvent.VK_F7 || k == KeyEvent.VK_F8) && inSimulation) {
                    return false;
                }

                if (k == KeyEvent.VK_F5 || k == KeyEvent.VK_F6 || k == KeyEvent.VK_F7 || k == KeyEvent.VK_F8) {
                    KeyEvent newE = new KeyEvent(e.getComponent(),
                                                 i = e.getID(),
                                                 e.getWhen(),
                                                 e.getModifiersEx(),
                                                 e.getKeyCode(),
                                                 e.getKeyChar());

                    switch (i) {
                    case KeyEvent.KEY_PRESSED:
                        KC.keyPressed(newE);
                        e.consume();
                        return true;

                    case KeyEvent.KEY_RELEASED:
                        KC.keyReleased(newE);
                        e.consume();
                        return true;

                    default:
                        break;
                    } // end switch
                } // end if

                return false;
            } // end dispatchKeyEvent
        }; // end KeyEventDispatcher
    } // end hotkeyInit

    /**
     * Utility method that causes the sweep (play) button 
     * to request focus.  We use this method and others like it to implement
     * hotkey shortcuts from the main GUI.
     */
    public void setFocusOnSweepButton() {
        if (sweep != null) {
            sweep.grabFocus();
        }
    }

    /**
     * Enables or disables the sound controls.
     * 
     * @param b true to enable the sound controls, false to disable.
     */
    public void setControlsEnabled(boolean b) {
        if (sweep != null) {
            sweep.setEnabled(b);
        }
        if (xPosition != null) {
            xPosition.setEnabled(b);
        }
    }
    
    private void buttonInit() {
        try {
			ResourceUtil ru = new ResourceUtil(MdeSettings.RESOURCES_PATH);
			
			playIcon1 = new ImageIcon(ru.getImage("play1.gif"), "Sound Button");
			playIcon2 = new ImageIcon(ru.getImage("play2.gif"), "Sound Mouseover");
			pauseIcon1 = new ImageIcon(ru.getImage("pause1.gif"), "Pause Button");
			pauseIcon2 = new ImageIcon(ru.getImage("pause2.gif"), "Pause Mouseover");
			volumeIcon1 = new ImageIcon(ru.getImage("volume1.gif"), "Volume Button");
			volumeIcon2 = new ImageIcon(ru.getImage("volume2.gif"), "Volume Mouseover");
			
			soundSettingsIcon1 = new ImageIcon(ru.getImage("set1.gif"), "Sound Settings Button");
			soundSettingsIcon2 = new ImageIcon(ru.getImage("set2.gif"), "Sound Settings Mouseover");
			
//			playIcon1 = new ImageIcon(ru.getImage("draw1.gif"), "Sound Button");
//			playIcon2 = new ImageIcon(ru.getImage("draw2.gif"), "Sound Mouseover");
//			pauseIcon1 = new ImageIcon(ru.getImage("draw1.gif"), "Pause Button");
//			pauseIcon2 = new ImageIcon(ru.getImage("draw2.gif"), "Pause Mouseover");
//			volumeIcon1 = new ImageIcon(ru.getImage("draw1.gif"), "Volume Button");
//			volumeIcon2 = new ImageIcon(ru.getImage("draw2.gif"), "Volume Mouseover");
			
			sweep = new JButton(playIcon1);
			sweep.setBorderPainted(false);
			sweep.setRolloverIcon(playIcon2); 
			sweep.setBackground(Color.white);
			sweep.setFocusPainted(true);
			
			volume = new JButton(volumeIcon1);
			volume.setBorderPainted(false);
			volume.setRolloverIcon(volumeIcon2); 
			volume.setBackground(Color.white);
			volume.setFocusPainted(true);
			
			soundSettingsBtn = new JButton(soundSettingsIcon1);
			soundSettingsBtn.setBorderPainted(false);
			soundSettingsBtn.setRolloverIcon(soundSettingsIcon2); 
			soundSettingsBtn.setBackground(Color.white);
			soundSettingsBtn.setFocusPainted(true);
			
		} // end try
		catch (IOException ioe) {
			throw new RuntimeException("Missing a gif file for one or more display control buttons");
		}

		sweep.getAccessibleContext().setAccessibleName(sweepLabel);
		sweep.setToolTipText("Hear the graph (CTRL+S)");
		String sweepAD = "Navigation Shortcut CTRL+S. This button lets you play and pause the sound of your graph. "+
		 "Making pictures with sound is called sonification. " +
		 "MathTrax sonifies graphs by changing graph values into sound from the graph's left side to its right side. " +
		 "You should be able to hear the sound move from left to right, especially if you have headphones on. " +
		 "Y values have high tones if they are near the top of the graph and low tones if they are near the bottom of the graph. " +
		 "Can you guess what a y value in the middle would sound like? " +
		 "Play around with the Sound Controls and find out." +
		 "Read more about MathTrax sounds in the User's Guide.";
		
		sweep.getAccessibleContext().setAccessibleDescription(sweepAD);
		sweep.addActionListener(this);
        sweep.setBackground(ColorDefaults.BUTTON_BG_COLOR);

        String volumeAD = "Keep pressing this button to turn the sound up and down. Or change volume by holding down the F5 and F6 "+
			"keys while the sound sweep is paused. The MathTrax volume controls will only change volume for the graph sound. "+
			"It will not turn your computer sound down.";
        volume.setActionCommand(volumeDownLabel);
		volume.getAccessibleContext().setAccessibleName("Graph Volume");
		volume.setToolTipText("Graph Volume Cycler -or- Holding F5/decreases, F6/increases.");
		volume.getAccessibleContext().setAccessibleDescription(volumeAD);
		volume.setBackground(ColorDefaults.BUTTON_BG_COLOR);
		volume.addActionListener(this);
		volumeLevel = 1.0f;
		
		soundSettingsBtn.setActionCommand(soundSettingsLabel);
		soundSettingsBtn.getAccessibleContext().setAccessibleName("Sound Settings");
		soundSettingsBtn.setToolTipText("Change the sound settings");
		String soundSettingsAD = "Change the sound settings";
		soundSettingsBtn.getAccessibleContext().setAccessibleDescription(soundSettingsAD);
		soundSettingsBtn.addActionListener(this);
		soundSettingsBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
    } // end buttonInit

    private void sliderInit() {
        Hashtable<Integer, JLabel> xPositionLabelTable = new Hashtable<Integer, JLabel>();

        xPosition = new JSlider(JSlider.HORIZONTAL);
        xPosition.setBackground(ColorDefaults.BUTTON_BG_COLOR);
        xPosition.getAccessibleContext().setAccessibleName(indiVar);
        
        String xPositionAD = 
		"You can find points on your graph by using the Explore Values Sound Slider bar and the Graph Values Display Window. "+
		"For example, let's say you want to find the highest point on your graph. "+
		"Use the sound slider to move to the point that has the highest pitch tone. "+
		"After you find it, look in the Graph Values window, next tab stop, to see the x and y values for that point.";
        xPosition.setToolTipText("Explore the graph values");
        xPosition.getAccessibleContext().setAccessibleDescription(xPositionAD);
        xPositionLabelTable.put(new Integer(50), new JLabel(indiVar));
        xPosition.setLabelTable(xPositionLabelTable);
        xPosition.setPaintLabels(true);
//        xPosition.setPaintLabels(false);
        xPosition.setValue(0);
//        xPosition.addKeyListener(KC);
        xPosition.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                JSlider js = (JSlider) ev.getSource();

                double newSweepX = 0.01 * js.getValue();

                if (sweepX != newSweepX) {
                    sweepX = newSweepX;
                    KC.setValue(sweepX, X_POSITION_SLIDER);
                    render(sweepX);
                }
            } // end stateChanged
        } // end ChangeListener
        ); // end addChangeListener
    } // end sliderInit
	
    /**
     * Method to invoke a new collection of user settings
     * for sonification.  initializes the sound settings pop-out dialog 
     * with the new user settings.
     * @param mdeSettings the new sonification settings to be used.
     */
    public void updateSettings(MdeSettings mdeSettings) {
        if (mdeSettings == null) {
            throw new NullPointerException("Null settings.");
        }
        this.settings = mdeSettings;
		if (soundSettings != null) {
			soundSettings.updateSettings(mdeSettings);
		}
            
		switch (settings.getTraceSweepSpeed()) {
            case MdeSettings.SLOW :
                sweepTime = 20.0;
            break;
            
            case MdeSettings.MEDIUM :
                sweepTime = 10.0;
            break;
            
            case MdeSettings.FAST :
                sweepTime = 2.5;
            break;
            
            default :
                throw new RuntimeException ("Incorrect value of MdeSettings.traceSweepSpeed: "+
                        settings.getTraceSweepSpeed());
        } // end switch
        
        if (sounder != null) {
            sounder.updateSettings(mdeSettings);
            
            // If we are not sweeping then have the sounder render/sonify the current
            // point, which will end up using the new settings. If we are sweeping then
            // it will automatically use the new settings for the next sonified point.
            if (!sweeping) {
                sounder.render(0.01 * xPosition.getValue());
            }
        }
    }
    
    /**
     * Sets the volume of the sonification independently of
     * computer system volume, thus leaving volume of assistive technology devices 
     * such as software synthesizers unaffected.
     * @param level The volume level for the sonification -- 0=silent; 1=max.
     */
    public void setVolume(float level) {
        if (sounder != null) {
            sounder.setVolume(level);
        }
    } // end setVolume

    /**
     * Called to pause or restart sonification of a simulation.
     * @param inSim false to pause; true to restart.
     */
    public void setInSimulation(boolean inSim) {
        inSimulation = inSim;
    } // end setInSimulation

//    /**
//     * Method to change the label of the play/pause (sweep) button.
//     * @param sweepLabel the new label.
//     */
//    public void setLabel(String sweepLabel) {
//        this.sweepLabel = sweepLabel;
//        sweep.setText(sweepLabel);
//    }

    /**
     * Sets the duration of the sonification
     * sweep in seconds.
     * @param time the duration of the sonification sweep.
     */
    public void setSweepTime(double time) {
        sweepTime = time;
    }

    /**
     * Grabs the <code>KeyEventDispatcher</code> created for this <code>SoundControl</code>
     * Useful when a <code>SoundControl</code> is to be managed with 
     * other GUI components which implement global keyboard controls.
     * @return The <code>KeyEventDispatcher</code> for 
     * this <code>SoundControl</code>
     * @see java.awt.KeyEventDispatcher
     */
    public KeyEventDispatcher getKed() {
        return ked;
    } // end getKed

    /**
     * Accesses the field which specifies the length of the 
     * sonification sweep in seconds.
     * @return The duration of the sonification sweep in seconds.
     */
    public double getSweepTime() {
        return sweepTime;
    }

    /**
     * Registers the <code>CartesianGraph</code to
     * be used as the visual display.
     * @param g the <code>CartesianGraph</code> to which 
     * the sonification is to be linked.
     */
    public void setGrapher(CartesianGraph g) {
        this.graph = g;
    }

    private void doStartSound() {
        startSounder();

        new Thread(new Runnable() {

            public void run() {
                sweeping = true;
                sweepIncrement = 0.001 * lim / sweepTime;

                // Reset the sweep X value if it is at the end.
                if (sweepX >= 1.0) {
                    sweepX = 0.0;
                }

                while (sweeping) {
                    if (sweepX > 1.0) {
                        break;
                    }

                    try {
                        Thread.sleep(lim);
                    } catch (InterruptedException ie) {
                    } // end try

                    KC.setValue(sweepX, X_POSITION_SLIDER);
                    xPosition.setValue((int) Math.rint(100.0 * sweepX));

                    sweepX += sweepIncrement;
                } // end while

                if (!sweeping) { // we were interrupted
                    return;
                }

                doStopSound();
            } // end run
        }).start();

        sweep.setActionCommand(pauseLabel);
		sweep.setIcon(pauseIcon1);
		sweep.setRolloverIcon(pauseIcon2);

        sweep.getAccessibleContext().setAccessibleName(pauseLabelShort);
    } // end doStartSound()

    /**
     * Handles all button events for this <code>SoundControl</code>
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        String arg = evt.getActionCommand();

        if (arg.equals(sweepLabel)) {
            doStartSound();
        }

        if (arg.equals(pauseLabel)) {
            doPauseSound();
        }

        if (arg.equals(volumeUpLabel)) {
            if (volumeLevel <= 0f) {
                volumeLevel = 0.1f;
            } else {
                volumeLevel *= VOLUME_UP_FACTOR;
            }
            volumeLimit();
            setVolume(volumeLevel);
            KC.setValue(volumeLevel, VOLUME_SLIDER);
        }

        if (arg.equals(volumeDownLabel)) {
            volumeLevel *= VOLUME_DOWN_FACTOR;
            volumeLimit();
            setVolume(volumeLevel);
            KC.setValue(volumeLevel, VOLUME_SLIDER);
        } // end if
		
		if (arg.equals(soundSettingsLabel)) {
			soundSettings.show();
		}
		
    } // end actionPerformed

    private void volumeLimit() {
        if (volumeLevel < 0.1f) {
            volumeLevel = 0f;
            volume.setActionCommand(volumeUpLabel);
            volume.getAccessibleContext().setAccessibleName(volumeUpLabel);
        } // end if

        if (volumeLevel >= 1.0) {
            volume.setActionCommand(volumeDownLabel);
            volume.getAccessibleContext().setAccessibleName(volumeDownLabel);
            volumeLevel = 1.0f;
        } // end if
    } // end volumeLimit

    /**
     * Stops the sonification and frees resources. 
     * The SoundControl is not valid after this method is called. There is no 
     * open or initialize method -- just make a new one.
     */
    public void close() {
        sweeping = false;
        if (sounder != null) {
            sounder.close();
        }
    }

    /**
     * Return true if audio is playing, otherwise false.
     * 
     * @return true if audio is playing, otherwise false.
     */
    public boolean isPlaying() {
        return (sounder != null) ? sounder.isPlaying() : false;
        
    }
    
    /**
     * Determines whether this <code>SoundControl</code> has 
     * a valid <code>Sounder</code>
     * @return True if <code>Sounder</code> is ready for use.
     * @see gov.nasa.ial.mde.sound.Sounder
     */
    public boolean isOpen() {
        return (sounder != null) ? sounder.isOpen() : false;
    } // end isOpen

    /**
     * Enable/disable sound from this <code>SoundControl</code>
     * This is useful for interfaces which may show or hide instances of
     * <code>SoundControl</code> in that disabling prevents accidental
     * activation which can be triggered by movement of the slider. 
     * @param soundEnabled true to enable; false to disable.
     */
    public void setSoundControlEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    } // end setSoundControlEnabled

    /**
     * Resets the <code>SoundControl</code> to its initial state.
     */
    public void reset() {
        doStopSound();
        KC.setValue(sweepX, X_POSITION_SLIDER);
        xPosition.setValue((int) Math.rint(100.0 * sweepX));
        graphValues.setText("");
    }

    /**
     * Releases the <code>Sounder</code> sonification object used
     * by this <code>SoundControl</code>
     * @see gov.nasa.ial.mde.sound.Sounder
     */
    public void hush() {
        sweeping = false;
        if (sounder != null) {
            sounder.close();
            sounder = null;
        } // end if
        
        // Make sure we reset the sweep button back to the Play label and setting.
        sweep.setActionCommand(sweepLabel);
        sweep.getAccessibleContext().setAccessibleName(sweepLabel);
        sweep.setIcon(playIcon1);
        sweep.setRolloverIcon(playIcon2);
    } // end hush

    /**
     * Terminates the sonification and resets all controls to their 
     * initial state -- ready to start a new sonification sweep. 
     */
    public void doStopSound() {
        sweeping = false;
        hush();
        sweepX = 0.0;
        sweep.setActionCommand(sweepLabel);
        sweep.getAccessibleContext().setAccessibleName(sweepLabel);
        sweep.setIcon(playIcon1);
		sweep.setRolloverIcon(playIcon2);
    }

    private void doPauseSound() {
        sweeping = false;
        sweep.setActionCommand(sweepLabel);
        sweep.getAccessibleContext().setAccessibleName(sweepLabelShort);
        sweep.setIcon(playIcon1);
		sweep.setRolloverIcon(playIcon2);
    } // end doPauseSound

    private void render(double position) {
        if ((solver == null) || solver.isEmpty()) {
            graphValues.setText("");
            return;
        }

        if (!soundEnabled) {
            position = -1.0;
        }

        boolean mute;
        if (position >= 1.0) {
            position = 1.0;
            mute = true;
        } else if (position <= 0.0) {
            position = 0.0;
            mute = true;
        } else {
            mute = false;
            startSounder();
        }

        // Update the x-position slider if it does not match the current position.
        int n = (int) Math.rint(100.0 * position);
        if (n != xPosition.getValue()) {
            xPosition.setValue(n);
        }

        double left = solver.getLeft();
        double right = solver.getRight();
        double x = left + position * (right - left);

        // Clear the summary
        ptsSummary.clear();

        // Set an initial x-value.
        ptsSummary.setX(x);

        // Build the points summary.
        Solution solution;
        MultiPointXY point;
        int numSolutions = solver.size();
        for (int solIndex = 0; solIndex < numSolutions; solIndex++) {
            solution = solver.get(solIndex);

            point = solution.getPointNear(x);

            // Only use the points for those graphs that are sonified.
            if (solution.isSonifyGraph()) {
                ptsSummary.add(point);
            }
        }

        // Display a summary of all the points for all the graphed solutions.
        graphValues.setText(ptsSummary.toString());

        // Sonify the given relative point on the graph if we are not muted.
        if (!mute) {
            sounder.render(position);
        }

        // Draw the trace.
        if (graph != null) {
            boolean onlyPolarSonify = (solver.getSonifyPolarCount() != 0) && (solver.getSonifyCartesianCount() == 0);

            // Use the Polar graph trace if we have only polar graphs to sonify and no others.
            if (onlyPolarSonify) {
                if (ptsSummary.isEmpty()) {
                    // Hide the trace since we don't have any values to sonify.
                    graph.drawTrace(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
                } else {
                    graph.drawTrace(ptsSummary.getX(), ptsSummary.getFirstY());
                }
            } else {
                graph.drawTrace(ptsSummary.getX(), 0.0);
            }

            if (graph.isSimulationBallEnabled()) {
                graph.setSimulationBallEnabled(false);
            }
        }

        if (mute) {
            // Call hush last because it takes a little while to flush the sound buffers.
            hush();
        }
    } // end render

    /**
     * Starts the <code>Sounder</code> sonification engine.
     */
    public void startSounder() {
        if (sounder != null) {
            // The doPlay() method is synchronized and the startSounder() is
            // called over and over again in the render method/thread so to reduce
            // overhead we check to see if we are playing before calling doPlay().
            if (!sounder.isPlaying()) {
                sounder.doPlay();
            }
        } else {
            sounder = new Sounder(solver, settings);
            setVolume(volumeLevel);
        }
        try {
            sounder.updateSettings(settings);
        } catch (NullPointerException npe) {
            // ignore exception
        }
    } // end startSounder

} // end class SoundControl
