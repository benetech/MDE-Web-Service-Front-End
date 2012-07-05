/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.ui;

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.ui.util.ComponentUtil;
import gov.nasa.ial.mde.util.ResourceUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * <code>ZoomButtons</code> is a reusable four button GUI widget that
 * provides controls for drawing, zooming in, zooming out or resetting MDE graph
 * solution bounds to the default values. It effectively makes a callback to the
 * input Solver object to recompute the solution over the new bounds. (The
 * Solver object will then notify any registered listeners that the solution
 * bounds have changed.)
 * 
 * To the end-user, the effect of each button press is as follows: <blockquote>
 * draw graph - (re)draw the graph using the current bounds <br>
 * zoom in - zoom in on the graph (decreased left, right, upper, lower bounds)
 * <br>
 * zoom out - zoom out on the graph <br>
 * reset graph - draw the graph using the default bounds </blockquote>
 * 
 * <p>
 * This class uses the look and feel of the MathTrax application, i.e.,
 * roll-over "Draw" and "Reset" icons, a plus sign (+) icon for the zoom in
 * button and a minus sign (-) icon for the zoom out button.
 * <p>
 * All the buttons have accessibleNames and accessibleDescriptions.
 * <p>
 * The ZoomButtons constructor takes an instance of Solver. Every click of
 * a button calls <code>Solver.solve</code> after recomputing the new bounds
 * (for zoom in/out and reset). Any components that are registered listeners for
 * that Solver object will be notified that the Solution bounds have changed and
 * can respond accordingly.
 * <p>
 * 
 * Zooming in decreases the left, right, top and bottom solution bounds
 * symmetrically, according to a scale factor. You can set the zoom scale factor
 * with the <code>setIncrement</code> method. The default scale factor is
 * sqrt(2). The scaling algorithm is:
 * 
 * <code>
 * <blockquote>...
 * <br>if (zoomIn) {
 *	<br>// Zoom in
 *	<br>  newHalfWidth = (right - left) / (2.0 * scaleFactor);
 *	 <br> newHalfHeight = (top - bottom) / (2.0 * scaleFactor);
 * <br>} else {
 *	<br>// Zoom out
 *	<br> newHalfWidth = scaleFactor * (right - left) / 2.0;
 *	 <br>newHalfHeight = scaleFactor * (top - bottom) / 2.0;
 * <br>}
 * 
 * </blockquote>
 *</code>
 * <p>
 */
public class ZoomButtons extends JPanel implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1094388747710888202L;

	private JButton zoomOutBtn;

    private JButton zoomInBtn;

    private JButton resetBtn;

    private JButton graphButton;

    private String zoomOutLabel;

    private String zoomInLabel;

    private String resetLabel;

    private String graphLabel;

    private double scaleFactor;

    private Solver solver;

    /**
     * You can use the ZoomButtons.ZOOM_IN flag when calling the
     * <code>zoomGraph</code> method for a zoom in.
     */
    public static final boolean ZOOM_IN = true;

    /**
     * You can use the ZoomButtons.ZOOM_OUT flag when calling the
     * <code>zoomGraph</code> method for a zoom out.
     */
    public static final boolean ZOOM_OUT = false;

    @SuppressWarnings("unused")
	private ZoomButtons() {
        throw new RuntimeException("Default constructor not allowed.");
    }

    /**
     * Create a new ZoomButtons graph controls widget using the given 
     * Solver object.
     *
     * @param solver a reference to the solver.
     */
    public ZoomButtons(Solver solver) {
        this.solver = solver;
        scaleFactor = Math.sqrt(2.0); // was 2.0;
        zoomOutLabel = "Outward Zoom"; //"Increase X";
        zoomInLabel = "Inward Zoom"; //"Decrease X";
        resetLabel = "Reset Graph";
        graphLabel = "Graph It";
        buttonInit();

        Box topBox = new Box(BoxLayout.X_AXIS);
        topBox.add(Box.createHorizontalGlue());
        topBox.add(graphButton);
        topBox.add(Box.createHorizontalGlue());

        Box middleBox = new Box(BoxLayout.X_AXIS);
        middleBox.add(Box.createHorizontalGlue());
        middleBox.add(zoomInBtn);
        middleBox.add(zoomOutBtn);
        middleBox.add(Box.createHorizontalGlue());

        Box bottomBox = new Box(BoxLayout.X_AXIS);
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(resetBtn);
        bottomBox.add(Box.createHorizontalGlue());

        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BoxLayout(mainPnl, BoxLayout.Y_AXIS));
        mainPnl.setBorder(BorderFactory.createLoweredBevelBorder());
        mainPnl.add(topBox);
        mainPnl.add(middleBox);
        mainPnl.add(bottomBox);

        // Update the background color and of the children as well.
        ComponentUtil.setBackground(mainPnl, ColorDefaults.BUTTON_BG_COLOR);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Graph Controls", TitledBorder.LEFT,
                TitledBorder.TOP));

        add(mainPnl, BorderLayout.CENTER);

        // Update the background color and of the children as well.
        setBackground(ColorDefaults.PANEL_BG_COLOR);
    }

    /**
     * Set button graphics icons, accessible names and accessible descriptions.
     */
    protected void buttonInit() {
        try {
            ResourceUtil ru = new ResourceUtil(MdeSettings.RESOURCES_PATH);

            ImageIcon graphIcon1 = new ImageIcon(ru.getImage("draw1.gif"),
                    "Graph Button");
            ImageIcon graphIcon2 = new ImageIcon(ru.getImage("draw2.gif"),
                    "Graph Button Mouseover");
            ImageIcon plusIcon1 = new ImageIcon(ru.getImage("Plus1_s1.gif"),
                    "Zoom In Button");
            ImageIcon plusIcon2 = new ImageIcon(ru.getImage("Plus2_s1.gif"),
                    "Zoom In Mouseover");
            ImageIcon minusIcon1 = new ImageIcon(ru.getImage("Minus1_s1.gif"),
                    "Zoom Out");
            ImageIcon minusIcon2 = new ImageIcon(ru.getImage("Minus2_s1.gif"),
                    "Zoom Out Mouseover");
            ImageIcon renameIcon1 = new ImageIcon(ru.getImage("reset1.gif"),
                    "Reset Button");
            ImageIcon renameIcon2 = new ImageIcon(ru.getImage("reset2.gif"),
                    "Reset Mouseover");

            //			ImageIcon graphIcon1 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Graph Button");
            //			ImageIcon graphIcon2 = new ImageIcon(ru.getImage("draw2.gif"),
            // "Graph Button Mouseover");
            //			ImageIcon plusIcon1 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Zoom In Button");
            //			ImageIcon plusIcon2 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Zoom In Mouseover");
            //			ImageIcon minusIcon1 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Zoom Out");
            //			ImageIcon minusIcon2 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Zoom Out Mouseover");
            //			ImageIcon renameIcon1 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Reset Button");
            //			ImageIcon renameIcon2 = new ImageIcon(ru.getImage("draw1.gif"),
            // "Reset Mouseover");

            zoomOutBtn = new JButton(minusIcon1);
            zoomOutBtn.getAccessibleContext().setAccessibleName(zoomOutLabel);
            String zoomOutBtnAD = "Navigation Shortcut CTRL+O. Use the zoom out button to increase the graph's X bounds. Each click will increase "
                    + "the left and right bounds by 2 units.";
            zoomOutBtn.getAccessibleContext().setAccessibleDescription(
                    zoomOutBtnAD);
            zoomOutBtn.setToolTipText("Zoom Out (CTRL-O)");
            zoomOutBtn.setBorderPainted(false);
            zoomOutBtn.setRolloverIcon(minusIcon2);
            zoomOutBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
            zoomOutBtn.setFocusPainted(true);
            zoomOutBtn.setHorizontalAlignment(SwingConstants.RIGHT);
            zoomOutBtn.addActionListener(this);
            zoomOutBtn.setActionCommand(zoomOutLabel);

            zoomInBtn = new JButton(plusIcon1);
            zoomInBtn.getAccessibleContext().setAccessibleName(zoomInLabel);
            String zoomInBtnAD = "Navigation Shortcut CTRL+I. Use the zoom in button to decrease the graph's X bounds. Each click will decrease "
                    + "the left and right bounds by 2 units.";
            zoomInBtn.getAccessibleContext().setAccessibleDescription(
                    zoomInBtnAD);
            zoomInBtn.setToolTipText("Zoom In (CTRL + I)");
            zoomInBtn.setRolloverIcon(plusIcon2);
            zoomInBtn.setBorderPainted(false);
            zoomInBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
            zoomInBtn.setFocusPainted(true);
            zoomInBtn.setHorizontalAlignment(SwingConstants.LEFT);
            zoomInBtn.addActionListener(this);
            zoomInBtn.setActionCommand(zoomInLabel);

            resetBtn = new JButton(renameIcon1);
            resetBtn.getAccessibleContext().setAccessibleName(resetLabel);
            String resetBtnAD = "Navigation Shortcut CTRL+R. The Reset button changes the graph bounds back to the default values. ";
            resetBtn.getAccessibleContext()
                    .setAccessibleDescription(resetBtnAD);
            resetBtn.setToolTipText("Reset the graph (CTRL + R)");
            resetBtn.setRolloverIcon(renameIcon2);
            resetBtn.setBorderPainted(false);
            resetBtn.setBackground(ColorDefaults.BUTTON_BG_COLOR);
            resetBtn.setFocusPainted(true);
            resetBtn.setActionCommand(resetLabel);
            resetBtn.addActionListener(this);

            graphButton = new JButton(graphIcon1);
            graphButton.getAccessibleContext().setAccessibleName(
                    "Draw The Graph");
            String graphButtonAD = "Use the Draw button to draw the graph. The graph is automatically drawn when you type or  "
                    + "select an equation and hit enter, when you load a data file, and when you view physics results.";
            graphButton.getAccessibleContext().setAccessibleDescription(
                    graphButtonAD);
            graphButton.setToolTipText("Draw the Graph");
            graphButton.setRolloverIcon(graphIcon2);
            graphButton.setDisabledIcon(graphIcon1);
            graphButton.setBorderPainted(false);
            graphButton.setBackground(ColorDefaults.BUTTON_BG_COLOR);
            graphButton.setFocusPainted(true);
            graphButton.setActionCommand(graphLabel);
            graphButton.addActionListener(this);
        } // end try
        catch (IOException ioe) {
            throw new RuntimeException(
                    "Missing a gif file for one or more display control buttons");
        }
    }

    /**
     * Set the scale factor for zooming in and out. The default for this class
     * is SQRT(2).
     * 
     * @param scaleFactor the scale factor for zooming
     */
    public void setScaleFactor(double scaleFactor) {
        if (scaleFactor <= 1.0) {
            throw new IllegalArgumentException("Scale-factor must be > 1");
        }
        this.scaleFactor = scaleFactor;
    }

    /**
     * Return the current scale factor used for zoom in/out calculations.
     * 
     * @return the current scale factor used for zoom in/out calculations.
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * zoomGraph computes new bounds for a zoom in or a zoom out request. It
     * calls Solver.solve to recompute the graph solution over the new bounds.
     * External classes can make use of this zoomGraph method (if desired),
     * e.g., for implementation of keyboard shortcuts that execute zooming.
     * 
     * @param zoomIn
     *            true if we want to zoom in on the graph. false if we want to
     *            zoom out.
     */
    public void zoomGraph(boolean zoomIn) {
        double left = solver.getLeft();
        double right = solver.getRight();
        double top = solver.getTop();
        double bottom = solver.getBottom();
        if (left > right) {
            double tmp = left;
            left = right;
            right = tmp;
        } else if (left == right) {
            left -= 1.0E-12;
            right += 1.0E-12;
        }
        if (bottom > top) {
            double tmp = bottom;
            bottom = top;
            top = tmp;
        } else if (bottom == top) {
            bottom -= 1.0E-12;
            top += 1.0E-12;
        }

        double xCenter = (left + right) / 2.0;
        double yCenter = (top + bottom) / 2.0;

        double newHalfWidth, newHalfHeight;
        if (zoomIn) {
            // Zoom in
            newHalfWidth = (right - left) / (2.0 * scaleFactor);
            newHalfHeight = (top - bottom) / (2.0 * scaleFactor);
        } else {
            // Zoom out
            newHalfWidth = scaleFactor * (right - left) / 2.0;
            newHalfHeight = scaleFactor * (top - bottom) / 2.0;
        }

        double newLeft = xCenter - newHalfWidth;
        double newRight = xCenter + newHalfWidth;
        double newTop = yCenter + newHalfHeight;
        double newBottom = yCenter - newHalfHeight;

        solver.solve(newLeft, newRight, newTop, newBottom);
    }

    /**
     * Performs the appropriate action depending on which of the four buttons
     * have been pressed. zoomGraph is called with the appropriate flag if one
     * of the zoom buttons was pressed. Solver.solve is called with default
     * bounds if the reset button was pressed. Solver.solve is called with the
     * current bounds if the draw button was pressed.
     * 
     * @param evt the action event.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        String arg = evt.getActionCommand();
        if (arg.equals(zoomOutLabel)) {
            zoomGraph(ZOOM_OUT);
        } else if (arg.equals(zoomInLabel)) {
            zoomGraph(ZOOM_IN);
        } else if (arg.equals(resetLabel)) {
            // Reset the solution by using the preferred bounds.
            solver.solve(solver.getPreferredBounds());
        } else {
            solver.solve();
        }
    }

}