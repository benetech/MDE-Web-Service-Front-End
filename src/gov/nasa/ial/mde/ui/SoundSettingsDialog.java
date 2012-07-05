/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on June 15, 2005
 */
package gov.nasa.ial.mde.ui;

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.ui.util.ComponentUtil;
import gov.nasa.ial.mde.ui.util.SpringUtilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;

/**
 * The <code>SoundSettingsDialog</code> class is a user interface dialog for
 * the sound/sonification settings.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class SoundSettingsDialog {

	private JComponent parent;
	private String title;
	private MdeSettings settings;
	
	private JDialog dialog = null;
	
	private JRadioButton[] sonificationWaveform;
	
	private JRadioButton[] traceSweepSpeed;
	
	private JRadioButton[] yAxisIndicators;
	private JRadioButton[] yAxisDingFrequency;
	private JRadioButton[] yAxisDingDuration;
	
	private JCheckBox[] negYValuesIndicators;
	
	private JRadioButton[] xAxisCrossingIndicators;
	private JRadioButton[] xAxisChirpFrequency;
	private JRadioButton[] xAxisChirpDuration;
	
	private JButton okBtn;
	private JButton defaultsBtn;
	private JButton cancelBtn;
	
	@SuppressWarnings("unused")
	private SoundSettingsDialog() {
		throw new RuntimeException("Default constructor not allowed.");
	}
	
	/**
     * Constructs the sound settings dialog.
     * 
	 * @param parent the parent component calling this constructor.
	 * @param settings the MDE settings.
	 */
	public SoundSettingsDialog(JComponent parent, MdeSettings settings) {
		this(parent,null,settings);
	}
	
	/**
     * Constructs the sound settings dialog.
     * 
	 * @param parent the parent component calling this constructor.
	 * @param title the title to display on the dialog.
	 * @param settings the MDE settings.
	 */
	public SoundSettingsDialog(JComponent parent, String title, MdeSettings settings) {
		super();
		if (parent == null) {
			throw new NullPointerException("Null parent component");
		}
        if (settings == null) {
            throw new NullPointerException("Null settings");
        }
		this.parent = parent;
		this.title = (title != null) ? title : "Sound Settings";
		this.settings = settings;
	}
	
	/**
     * Updates the MDE settings used for the sound settings dialog.
     * 
	 * @param mdeSettings the MDE settings.
	 */
	public void updateSettings(MdeSettings mdeSettings) {
        if (mdeSettings == null) {
            throw new NullPointerException("Null settings.");
        }
        this.settings = mdeSettings;
    }
	
	/**
	 * Show the sound settings dialog, this is a blocking call.
	 */
	public void show() {
		// Dan Dexter 6/15/2005 NOTE: We must construct the Sound Settings once
        // the user fires the action to show sound settings dialog so that we
        // can get a reference to the parent Frame which does not exist when the
        // SoundSettingsDialog object was created.  This is because of the loose
        // coupling between the JFrame that displays the UI and the component
        // panels that make up the UI.
		if (dialog == null) {
			initComponents();
		}
		
		updateDisplay();
		
		dialog.setVisible(true);
		dialog.toFront();
	}
	
	private int getSelectedItemsCode(JToggleButton[] toggleBtns, int[] codes) {
		if (toggleBtns == null) {
			return 0;
		}
		int combinedCode = 0;
		int len = toggleBtns.length;
		for (int i = 0; i < len; i++) {
			if (toggleBtns[i].isSelected()) {
				combinedCode |= codes[i];
			}
		}
		return combinedCode;
	}
	
	private void save() {
		int value;
		
		// Sonficiation waveform
		value = getSelectedItemsCode(sonificationWaveform,MdeSettings.BASIC_WAVEFORMS);
		if (value != settings.getSonificationWaveform()) {
			settings.setSonificationWaveform(value);
		}
		
		// Y-axis indicator
		value = getSelectedItemsCode(yAxisIndicators,MdeSettings.Y_AXIS_INDICATORS);
		if (value != settings.getYAxisIndicator()) {
			settings.setYAxisIndicator(value);
		}
		
		// Y-axis indicator duration
		value = getSelectedItemsCode(yAxisDingDuration,MdeSettings.INDICATOR_DURATIONS);
		if (value != settings.getYAxisIndicatorDuration()) {
			settings.setYAxisIndicatorDuration(value);
		}
		
		// Y-axis indicator frequency
		value = getSelectedItemsCode(yAxisDingFrequency,MdeSettings.INDICATOR_FREQUENCIES);
		if (value != settings.getYAxisIndicatorFrequency()) {
			settings.setYAxisIndicatorFrequency(value);
		}
		
		// Negative Y-values indicators
		value = getSelectedItemsCode(negYValuesIndicators,MdeSettings.NEG_Y_VALUES_INDICATORS);
		if (value != settings.getNegativeYValuesIndicator()) {
			settings.setNegativeYValuesIndicator(value);
		}
		
		// X-axis indicator
		value = getSelectedItemsCode(xAxisCrossingIndicators,MdeSettings.X_AXIS_INDICATORS);
		if (value != settings.getXAxisIndicator()) {
			settings.setXAxisIndicator(value);
		}
		
		// X-axis indicator duration
		value = getSelectedItemsCode(xAxisChirpDuration,MdeSettings.INDICATOR_DURATIONS);
		if (value != settings.getXAxisIndicatorDuration()) {
			settings.setXAxisIndicatorDuration(value);
		}
		
		// X-axis indicator frequency
		value = getSelectedItemsCode(xAxisChirpFrequency,MdeSettings.INDICATOR_FREQUENCIES);
		if (value != settings.getXAxisIndicatorFrequency()) {
			settings.setXAxisIndicatorFrequency(value);
		}
		
		// Trace sweep speed, determine the index from the name of the selected item.
		value = getSelectedItemsCode(traceSweepSpeed,MdeSettings.TRACE_SWEEP_SPEEDS);
		if (value != settings.getTraceSweepSpeed()) {
			settings.setTraceSweepSpeed(value);
		}
		
		// update parent
		((SoundControl)parent).updateSettings(settings);
		
	}
	
	private void updateDisplayToDefaults() {
		updateToggleButtonStates(MdeSettings.TRIANGLE, sonificationWaveform, MdeSettings.BASIC_WAVEFORMS);
		updateToggleButtonStates(MdeSettings.DING, yAxisIndicators, MdeSettings.Y_AXIS_INDICATORS);
		updateToggleButtonStates(MdeSettings.MEDIUM, yAxisDingDuration, MdeSettings.INDICATOR_DURATIONS);
		updateToggleButtonStates(MdeSettings.MEDIUM, yAxisDingFrequency, MdeSettings.INDICATOR_FREQUENCIES);
		updateToggleButtonStates(MdeSettings.HISS, negYValuesIndicators, MdeSettings.NEG_Y_VALUES_INDICATORS);
		updateToggleButtonStates(MdeSettings.NO_INDICATION, xAxisCrossingIndicators, MdeSettings.X_AXIS_INDICATORS);
		updateToggleButtonStates(MdeSettings.MEDIUM, xAxisChirpDuration, MdeSettings.INDICATOR_DURATIONS);
		updateToggleButtonStates(MdeSettings.MEDIUM, xAxisChirpFrequency, MdeSettings.INDICATOR_FREQUENCIES);
		updateToggleButtonStates(MdeSettings.MEDIUM, traceSweepSpeed, MdeSettings.TRACE_SWEEP_SPEEDS);
	}
	
	private void updateDisplay() {
		updateToggleButtonStates(settings.getSonificationWaveform(), sonificationWaveform, MdeSettings.BASIC_WAVEFORMS);
		updateToggleButtonStates(settings.getYAxisIndicator(), yAxisIndicators,  MdeSettings.Y_AXIS_INDICATORS);
		updateToggleButtonStates(settings.getYAxisIndicatorDuration(),yAxisDingDuration, MdeSettings.INDICATOR_DURATIONS);
		updateToggleButtonStates(settings.getYAxisIndicatorFrequency(),yAxisDingFrequency, MdeSettings.INDICATOR_FREQUENCIES);
		updateToggleButtonStates(settings.getNegativeYValuesIndicator(), negYValuesIndicators, MdeSettings.NEG_Y_VALUES_INDICATORS);
		updateToggleButtonStates(settings.getXAxisIndicator(), xAxisCrossingIndicators, MdeSettings.X_AXIS_INDICATORS);
		updateToggleButtonStates(settings.getXAxisIndicatorDuration(), xAxisChirpDuration, MdeSettings.INDICATOR_DURATIONS);
		updateToggleButtonStates(settings.getXAxisIndicatorFrequency(), xAxisChirpFrequency, MdeSettings.INDICATOR_FREQUENCIES);
		updateToggleButtonStates(settings.getTraceSweepSpeed(),traceSweepSpeed,MdeSettings.TRACE_SWEEP_SPEEDS);
	}
	
	private void updateToggleButtonStates(int code, JToggleButton[] toggleBtns, int[] validCodeFlags) {
		int len = validCodeFlags.length;
		for (int i = 0; i < len; i++) {
			// Enable the radio button if its flag is set in the code, otherwise disable it.
			toggleBtns[i].setSelected((validCodeFlags[i] & code) == validCodeFlags[i]);
		}
	}
	
	private void initComponents() {
		JPanel topPnl = new JPanel(new SpringLayout());
        int rowCount = 0;
		
		// Trace Sweep Speed: Slow, Medium, Fast
		addTraceSweepSpeedPanel(topPnl);
		rowCount++;
		
		// Sonification Waveform: sine, triangle, saw, square
        addSonificationWaveformPanel(topPnl);
        rowCount++;
		
		// Negative Y-Values Indicator: any combo of Hiss | Tambor Change
		addNegativeYValuesIndicatorPanel(topPnl);
		rowCount++;
		
		// Y-Axis Indicator: No_Indication or Ding
        addYAxisIndicatorPanel(topPnl);
        rowCount++;
		
		// X-Axis Indicator: No_Indication | ding (possibly different from other ding)
		addXAxisIndicatorPanel(topPnl);
		rowCount++;
		
		// Y-axis Ding Indicator Duration
		addYAxisDingDurationPanel(topPnl);
		rowCount++;
		
		// Y-axis Ding Indicator Frequency
		addYAxisDingFrequencyPanel(topPnl);
		rowCount++;
		
		// X-axis Ding Indicator Duration
		addXAxisChirpDurationPanel(topPnl);
		rowCount++;
		
		// X-axis Ding Indicator Frequency
		addXAxisChirpFrequencyPanel(topPnl);
		rowCount++;
		
		// Layout the top panel.
        SpringUtilities.makeCompactGrid(topPnl,
                						rowCount, 2,	//rows, cols
                                        6, 6,   		//initX, initY  was 6,6
                                        6, 6);  		//xPad, yPad    was 6,6
		
		okBtn = new JButton("OK");
		okBtn.getAccessibleContext().setAccessibleName("OK");
		okBtn.setToolTipText("Use the sound settings shown");
		okBtn.getAccessibleContext().setAccessibleDescription("Use the settings shown.");
		okBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				save();
				dialog.setVisible(false);
			}
		});
		
		defaultsBtn = new JButton("Defaults");
		defaultsBtn.getAccessibleContext().setAccessibleName("Defaults");
		defaultsBtn.setToolTipText("Reset to the default sound settings");
		defaultsBtn.getAccessibleContext().setAccessibleDescription("Reset to the default settings.");
		defaultsBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
		defaultsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateDisplayToDefaults();
			}
		});
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.getAccessibleContext().setAccessibleName("Cancel");
		cancelBtn.setToolTipText("Cancel any changes to the sound settings");
		cancelBtn.getAccessibleContext().setAccessibleDescription("Cancel any changes made to the settings.");
		cancelBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dialog.setVisible(false);
			}
		});
		
		JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		buttonPanel.add(defaultsBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalGlue());
		
		JPanel mainPnl = new JPanel(new BorderLayout());
        mainPnl.add(topPnl,BorderLayout.CENTER);
        mainPnl.add(buttonPanel,BorderLayout.SOUTH);
        
        JPanel contentPnl = new JPanel(new BorderLayout());
        contentPnl.add(mainPnl,BorderLayout.NORTH);
        contentPnl.add(new JPanel(),BorderLayout.CENTER); // Free hog space
        
        JScrollPane scrollPane = new JScrollPane(contentPnl,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
		JFrame parentJFrame = ComponentUtil.getParentJFrame(parent);
		dialog = new JDialog(parentJFrame,title,true);
		dialog.setResizable(false);
		dialog.getContentPane().add(scrollPane,BorderLayout.CENTER);
		dialog.pack();
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {
                // Same action as the user clicking the Cancel button.
                dialog.setVisible(false);
		    }
		});
		
		// Set the location of the dialog based on the parent frames location.
		if (parentJFrame != null) {
			Point parentLoc = parentJFrame.getLocation();
			dialog.setLocation(parentLoc.x+40,parentLoc.y+40);
		}
		
		ComponentUtil.setBackground(mainPnl,ColorDefaults.PANEL_BG_COLOR);
        ComponentUtil.setBackground(buttonPanel,ColorDefaults.PANEL_BG_COLOR);
        ComponentUtil.setBackground(contentPnl,ColorDefaults.PANEL_BG_COLOR);
	}
	
	// Sonification Waveform: sine, triangle, saw, square
	private void addSonificationWaveformPanel(JPanel p) {
		int len = MdeSettings.BASIC_WAVEFORM_STRINGS.length;
		sonificationWaveform = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			sonificationWaveform[i] = new JRadioButton(MdeSettings.BASIC_WAVEFORM_STRINGS[i]);
			msg = MdeSettings.BASIC_WAVEFORM_STRINGS[i]+" sound wave";
			sonificationWaveform[i].getAccessibleContext().setAccessibleName(msg);
			sonificationWaveform[i].getAccessibleContext().setAccessibleDescription(msg);
			sonificationWaveform[i].setToolTipText(msg);
			group.add(sonificationWaveform[i]);
			rowPanel.add(sonificationWaveform[i]);
		}

	    ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Sound Wave:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// Trace Sweep Speed: Slow | Medium | Fast
	private void addTraceSweepSpeedPanel(JPanel p) {
		int len = MdeSettings.TRACE_SWEEP_SPEED_STRINGS.length;
		traceSweepSpeed = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			traceSweepSpeed[i] = new JRadioButton(MdeSettings.TRACE_SWEEP_SPEED_STRINGS[i]);
			msg = MdeSettings.TRACE_SWEEP_SPEED_STRINGS[i] + " speed";
			traceSweepSpeed[i].getAccessibleContext().setAccessibleName(msg);
			traceSweepSpeed[i].getAccessibleContext().setAccessibleDescription(msg);
			//traceSweepSpeed[i].setToolTipText(msg);
			group.add(traceSweepSpeed[i]);
			rowPanel.add(traceSweepSpeed[i]);
		}

	    ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Speed:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// Negative Y-values Indicator: Hiss | Tambor Change
	private void addNegativeYValuesIndicatorPanel(JPanel p) {
		int len = MdeSettings.NEG_Y_VALUES_INDICATOR_STRINGS.length;
		negYValuesIndicators = new JCheckBox[len];
		
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			negYValuesIndicators[i] = new JCheckBox(MdeSettings.NEG_Y_VALUES_INDICATOR_STRINGS[i]);
			msg = "Use a "+MdeSettings.NEG_Y_VALUES_INDICATOR_STRINGS[i] + " for negative y-values";
			negYValuesIndicators[i].getAccessibleContext().setAccessibleName(msg);
			negYValuesIndicators[i].getAccessibleContext().setAccessibleDescription(msg);
			negYValuesIndicators[i].setToolTipText(msg);
			rowPanel.add(negYValuesIndicators[i]);
			
			negYValuesIndicators[i].setBackground(ColorDefaults.PANEL_BG_COLOR);
		}

		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Negative Y-values:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// Y-Axis Crossing: No_Indication or Ding
	private void addYAxisIndicatorPanel(JPanel p) {
		int len = MdeSettings.Y_AXIS_INDICATOR_STRINGS.length;
		yAxisIndicators = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    String msg;
		
		for (int i = 0; i < len; i++) {
			if (MdeSettings.Y_AXIS_INDICATORS[i] == MdeSettings.NO_INDICATION) {
				msg = MdeSettings.NO_INDICATION_STRING+" of y axis crossings";
			} else {
				msg = "Use a " + MdeSettings.Y_AXIS_INDICATOR_STRINGS[i]
                        + " for y axis crossings";
			}
			yAxisIndicators[i] = new JRadioButton(MdeSettings.Y_AXIS_INDICATOR_STRINGS[i]);
			yAxisIndicators[i].getAccessibleContext().setAccessibleName(msg);
			yAxisIndicators[i].getAccessibleContext().setAccessibleDescription(msg);
			yAxisIndicators[i].setToolTipText(msg);
			
			group.add(yAxisIndicators[i]);
			rowPanel.add(yAxisIndicators[i]);
		}

		ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Y-Axis Crossings:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// X-Axis Crossing: No_Indication | ding 
	private void addXAxisIndicatorPanel(JPanel p) {
		int len = MdeSettings.X_AXIS_INDICATOR_STRINGS.length;
		xAxisCrossingIndicators = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    String msg;
		
		for (int i = 0; i < len; i++) {
			if (MdeSettings.X_AXIS_INDICATORS[i] == MdeSettings.NO_INDICATION) {
				msg = MdeSettings.NO_INDICATION_STRING+" of X axis crossings";
			} else {
				msg = "Use a "+MdeSettings.X_AXIS_INDICATOR_STRINGS[i] +" for X axis crossings";
			}
			xAxisCrossingIndicators[i] = new JRadioButton(MdeSettings.X_AXIS_INDICATOR_STRINGS[i]);
			xAxisCrossingIndicators[i].getAccessibleContext().setAccessibleName(msg);
			xAxisCrossingIndicators[i].getAccessibleContext().setAccessibleDescription(msg);
			xAxisCrossingIndicators[i].setToolTipText(msg);
			
			group.add(xAxisCrossingIndicators[i]);
			rowPanel.add(xAxisCrossingIndicators[i]);
		}

		ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("X-Axis Crossings:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// Y-axis Indicator Duration
	private void addYAxisDingDurationPanel(JPanel p) {
		int len = MdeSettings.INDICATOR_DURATION_STRINGS.length;
		yAxisDingDuration = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			yAxisDingDuration[i] = new JRadioButton(MdeSettings.INDICATOR_DURATION_STRINGS[i]);
			msg = MdeSettings.INDICATOR_DURATION_STRINGS[i]+" ding length";
			yAxisDingDuration[i].getAccessibleContext().setAccessibleName(msg);
			yAxisDingDuration[i].getAccessibleContext().setAccessibleDescription(msg);
			yAxisDingDuration[i].setToolTipText(msg);
			group.add(yAxisDingDuration[i]);
			rowPanel.add(yAxisDingDuration[i]);
		}

	    ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Ding Length:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// Y-axis Indicator Frequency
	private void addYAxisDingFrequencyPanel(JPanel p) {
		int len = MdeSettings.INDICATOR_FREQUENCY_STRINGS.length;
		yAxisDingFrequency = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			yAxisDingFrequency[i] = new JRadioButton(MdeSettings.INDICATOR_FREQUENCY_STRINGS[i]);
			msg = MdeSettings.INDICATOR_FREQUENCY_STRINGS[i]+" ding pitch";
			yAxisDingFrequency[i].getAccessibleContext().setAccessibleName(msg);
			yAxisDingFrequency[i].getAccessibleContext().setAccessibleDescription(msg);
			yAxisDingFrequency[i].setToolTipText(msg);
			group.add(yAxisDingFrequency[i]);
			rowPanel.add(yAxisDingFrequency[i]);
		}

	    ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Ding Pitch:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// X-axis Indicator Duration
	private void addXAxisChirpDurationPanel(JPanel p) {
		int len = MdeSettings.INDICATOR_DURATION_STRINGS.length;
		xAxisChirpDuration = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			xAxisChirpDuration[i] = new JRadioButton(MdeSettings.INDICATOR_DURATION_STRINGS[i]);
			msg = MdeSettings.INDICATOR_DURATION_STRINGS[i]+" chirp length";
			xAxisChirpDuration[i].getAccessibleContext().setAccessibleName(msg);
			xAxisChirpDuration[i].getAccessibleContext().setAccessibleDescription(msg);
			xAxisChirpDuration[i].setToolTipText(msg);
			group.add(xAxisChirpDuration[i]);
			rowPanel.add(xAxisChirpDuration[i]);
		}

	    ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Chirp Length:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
	// X-axis Indicator Frequency
	private void addXAxisChirpFrequencyPanel(JPanel p) {
		int len = MdeSettings.INDICATOR_FREQUENCY_STRINGS.length;
		xAxisChirpFrequency = new JRadioButton[len];
		
		ButtonGroup group = new ButtonGroup();
		JPanel rowPanel = new JPanel();
	    rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
	    
		String msg;
		for (int i = 0; i < len; i++) {
			xAxisChirpFrequency[i] = new JRadioButton(MdeSettings.INDICATOR_FREQUENCY_STRINGS[i]);
			msg = MdeSettings.INDICATOR_FREQUENCY_STRINGS[i]+" chirp pitch";
			xAxisChirpFrequency[i].getAccessibleContext().setAccessibleName(msg);
			xAxisChirpFrequency[i].getAccessibleContext().setAccessibleDescription(msg);
			xAxisChirpFrequency[i].setToolTipText(msg);
			group.add(xAxisChirpFrequency[i]);
			rowPanel.add(xAxisChirpFrequency[i]);
		}

	    ComponentUtil.setBackground(group,ColorDefaults.PANEL_BG_COLOR);
		rowPanel.setBackground(ColorDefaults.PANEL_BG_COLOR);
		
	    JLabel label = new JLabel("Chirp Pitch:",JLabel.RIGHT);
	    label.setLabelFor(rowPanel);
	    label.getAccessibleContext().setAccessibleName(" ");
	    
	    // Add the label and settings components to the parent panel.
	    p.add(label);
	    p.add(rowPanel);
	}
	
}
