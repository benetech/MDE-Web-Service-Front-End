/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.ui.graph;

import gov.nasa.ial.mde.math.Bounds;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.GraphTrail;
import gov.nasa.ial.mde.solver.Solution;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedItem;
import gov.nasa.ial.mde.ui.GraphNavKeys;
import gov.nasa.ial.mde.util.MathUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.NumberFormat;

import javax.swing.JPanel;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * The <code>CartesianGraph</code> class is used to draw the solutions
 * found by the <code>Solver</code>.
 *
 * @author Dan Dexter
 * @author Dat Truong
 * @version 1.0
 * @since 1.0
 * @see gov.nasa.ial.mde.solver.Solver
 */
public class CartesianGraph extends JPanel {
    
  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1857799135537915445L;

	private Solver solver;
    
    // To allow for faster rendering of the graph we do our own buffering of the
    // drawn grid and plotted equation.
	private BufferedImage bi = null;
	private Graphics2D big2 = null;
	private BasicStroke userSpecifiedStroke;
	private BasicStroke scatterPlotStroke;
    
    // The saved path for the graph that is to be drawn.
	private GeneralPath cachedPath = null;
	private GeneralPath cachedScatterPlotPath = null;
	private boolean clearGraph = true;
    
    // The left right bottom and top extent of the screen in cartesian coordinates,
    // which is based on the bounds of the solver.
    private Bounds solverBounds = new Bounds();
    
    // The previous bounds of this component.
    private Rectangle prevBounds = getBounds();
    
	private int sideBorderWidth;
	private int bottomBorderHeight;
	private Rectangle bounds;
	private Rectangle graphBounds = getBounds();
	private FontMetrics fontMetrics;
	private Font f;
	private Color bgColor;
	private boolean useBlackAndWhiteShade = false;
	private MdeSettings currentSettings = null;
    
	private NumberFormat displayNumberFormat;
    
	private boolean use1To1AspectRatio = true;
	private boolean resetBounds = true;
    
	private double traceXaxis = Double.NEGATIVE_INFINITY;
	private double traceYaxis = Double.NEGATIVE_INFINITY;
    
	private boolean simBallEnabled = false;
	private double simBallXaxis = Double.NEGATIVE_INFINITY;
	private double simBallYaxis = Double.NEGATIVE_INFINITY;
	private PointXY simBallNormalVector = new PointXY(0.0, 0.0);
    
    private static final int BALL_RADIUS = 4;
	private static final int BALL_DIAMETER = 2 * BALL_RADIUS;
    
	private static final double MAX_VALUE = Double.MAX_VALUE; // was 1000.0
    
    private static final boolean USE_CACHED_GRAPH_DRAWING = true;
    
    @SuppressWarnings("unused")
	private CartesianGraph() {
        throw new RuntimeException("Default constructor not allowed.");
    }
    
    /**
     * Creates an instance of <code>CartesianGraph</code> using the specified
     * solver and settings.
     *
     * @param solver The solutions from this solver will be graphed.
     * @param settings The settings to use for the graph.
     */
    public CartesianGraph(Solver solver, MdeSettings settings) {
        this.solver = solver;
        this.currentSettings = settings;
        this.userSpecifiedStroke = new BasicStroke(currentSettings.getLineSize());
        this.scatterPlotStroke = new BasicStroke(3);
        
        // This is used to format the numbers displayed on the graph.
        displayNumberFormat = NumberFormat.getInstance();
        displayNumberFormat.setMinimumFractionDigits(1);
        displayNumberFormat.setMaximumFractionDigits(3);

        f = new Font("SansSerif", Font.PLAIN, 9);
        setFont(f);
        fontMetrics = getFontMetrics(f);
        sideBorderWidth = fontMetrics.stringWidth("-999.999");
        bottomBorderHeight = fontMetrics.getHeight() + 5;
        bgColor = Color.black;
        setBackground(bgColor);
        setPreferredSize(new Dimension(300, 300));
        setFocusable(true);
		
        GraphNavKeys gnk = new GraphNavKeys(solver, this, graphBounds);
        addKeyListener(gnk);
        addMouseListener(gnk);
    } // end CartesianGraph
    
    public Solver getSolver() {
		return solver;
	}

	/**
     * Create a duplicate of this <code>CartesianGraph</code> instance.
     *
     * @return a duplicate of the <code>CartesianGraph</code> instance
     */
    public CartesianGraph duplicate() {
        CartesianGraph g = new CartesianGraph(solver, currentSettings);
        g.bounds = new Rectangle(this.bounds);
        g.graphBounds = new Rectangle(this.graphBounds);
        g.use1To1AspectRatio = this.use1To1AspectRatio;
        g.resetBounds = true;
        return g;
    }
    
    /**
     * Translate the center of the graph given the specified X and Y offsets.
     *
     * @param dx the amount to shift the X-axis by
     * @param dy the amount to shift the Y-axis by
     * @see #dilate(double)
     */
    public void translate(double dx, double dy) {
        // Note: Don't need to call repaint() because the updateSolution method
        // will fire a change event, which will result in the stateChanged()
        // method below being called and the graph repainted. D.Dexter 8/20/2003
        solver.solve(solver.getLeft()+dx, solver.getRight()+dx, solver.getTop()+dy, solver.getBottom()+dy);
    } // end translate
    
    /**
     * Dialate (change the size of) the graph given the specified scale factor.
     *
     * @param factor the amount to scale the size of the graph by
     * @see #translate(double,double)
     */
    public void dilate(double factor) {
        // Note: Don't need to call repaint() because the updateSolution method
        // will fire a change event, which will result in the stateChanged()
        // method below being called and the graph repainted. D.Dexter 8/20/2003
        solver.solve(solver.getLeft() * factor, solver.getRight() * factor, solver.getTop() * factor, solver.getBottom() * factor);
    } // end dilate
    
	/**
	 * Returns true if the graph will be drawn with a width to height aspect
	 * ratio of one-to-one (1:1).
	 * <p>
	 * With a one-to-one aspect ratio a circle will appear as a perfectly round
	 * circle and not stretched out like an egg.
	 * <p>
     * The default value for this property is true.
	 * 
	 * @return true for a one-to-one aspect ratio, false otherwise.
	 * @see #enableOneToOneAspectRatio(boolean)
	 */
	public boolean isOneToOneAspectRatio() {
        return use1To1AspectRatio;
    }
	
    /**
     * If true the graph will be drawn with a width to height aspect ratio
     * of one-to-one (1:1).
     * <p>
     * With a one-to-one aspect ratio a circle will appear as a perfectly round
     * circle. Otherwise the graph will be drawn using the the current size of the
     * <code>CartesianGraph</code> component and a circle could appear to be stretched
     * out in an egg shape because the aspect ration may not be one-to-one.
     * <p>
     * The default value for this property is true.
     * 
     * @param enable true to enable a one-to-one aspect ratio, false to use the full size
     * of the <code>CartesianGraph</code> component for drawing.
     * @see #isOneToOneAspectRatio()
     */
    public void enableOneToOneAspectRatio(boolean enable) {
        // Determine if we need to reset the bounds. Reset the bounds the
        // aspect ratio flag has changed.
        if (enable != use1To1AspectRatio) {
            resetBounds = true;
        }
        use1To1AspectRatio = enable;
        
        // If the bounds need to be reset then repaint the display.
        if (resetBounds) {
            repaint();
        }
    }
    
	/**
	 * Returns true if the graph will be drawn using black and white with shades of gray.
	 * Returns false if the graph will be in color.
	 * <p>
	 * The default value for this property is false.
	 *
	 * @return true if the graph will be in black and white, false otherwise for a color graph.
	 * @see #enableBlackAndWhite(boolean)
	 */
	public boolean isBlackAndWhite() {
		return useBlackAndWhiteShade;
	}
	
    /**
     * If true the graph will be drawn using black and white with shades of gray.
     * If false the graph will be in color.
     * <p>
     * The default value for this property is false.
     *
     * @param useBlackWhiteShade true for a black and white graph, false for a color graph
     * @see #isBlackAndWhite()
     */
    public void enableBlackAndWhite(boolean useBlackWhiteShade) {
        this.useBlackAndWhiteShade = useBlackWhiteShade;
    }
	
    /**
     * Clears the graph which will result in the sonification trace and simulation
     * ball indicators being cleared, the one-to-one aspect ratio is disabled, and
     * the AWT Thread will be notified to repaint the <code>CartesianGraph</code> component.
     * 
     * @see #paintComponent(Graphics)
     */
    public void clearGraph() {
        clearGraph = true;
        nullifyCachedPaths();
        traceXaxis = Double.NEGATIVE_INFINITY;
        traceYaxis = Double.NEGATIVE_INFINITY;
        simBallXaxis = Double.NEGATIVE_INFINITY;
        simBallYaxis = Double.NEGATIVE_INFINITY;
        enableOneToOneAspectRatio(false);
        repaint();
    }
	
    /**
     * Draws a graph of the solutions from the solver.  The AWT Thread will be
     * notified to repaint the <code>CartesianGraph</code> component.
     */
    public void drawGraph() {
        clearGraph = false;
        resetBounds = true;
        nullifyCachedPaths();
        repaint();
    }
	
    /**
     * Draws a graph of the solutions from the solver using the specified
     * settings. The settings will be updated to use those specified and then
     * the <code>drawGraph()</code> method is called.
     *
     * @param settings the MDE settings.
     * @see #drawGraph()
     */
    public void drawGraph(MdeSettings settings) {
        this.currentSettings = settings;
        drawGraph();
    }
    
	/**
	 * Draws a graph of the solutions from the solver to the specified image.
	 *
	 * @param graphImage the image to draw the graph to
	 */
	public void drawGraphToImage(Image graphImage) {
		// Rest the flags to ensure we draw the graph from scratch.
		clearGraph = false;
        resetBounds = true;
        nullifyCachedPaths();
		
		if (graphImage instanceof BufferedImage) {
			paintComponent(((BufferedImage)graphImage).createGraphics());
		} else {
			paintComponent(graphImage.getGraphics());
		}
		
		// Reset the flags so that the next time we draw we start from scratch.
		clearGraph = false;
        resetBounds = true;
        nullifyCachedPaths();
	}
	
    /**
     * Draws a trace at the given X and Y coordinates if the trace is enabled in
     * the <code>MdeSettings</code>.  If the solution is polar then a ball will
     * be drawn at the given coordinates, otherwise a vertical line will be drawn
     * at the given Y cordinate.
     *
     * @param x the X coordinate of the trace
     * @param y the Y coordinate of the trace
     * @see gov.nasa.ial.mde.properties.MdeSettings
     */
    public void drawTrace(double x, double y) {
        if (currentSettings.showTrace()) {
            traceXaxis = x;
            traceYaxis = y;
            repaint();
        }
    }
    
    /**
     * Returns true if the simulation ball is enabled.
     * <p>
     * The default value for this property is false.
     *
     * @return true if the simulation ball is enabled, false otherwise
     * @see #setSimulationBallEnabled(boolean)
     * @see #drawSimulationBall(double,double,double,double) 
     */
    public boolean isSimulationBallEnabled() {
        return simBallEnabled;
    }
    
    /**
     * If true the simulation ball will be drawn. If false the simulation ball
     * will not be drawn. The AWT Thread will be notified to repaint the
     * <code>CartesianGraph</code> component.
     * <p>
     * The default value for this property is false.
     *
     * @param enable true to enable/show the simulation ball, false to disable/hide it
     * @see #isSimulationBallEnabled()
     * @see #drawSimulationBall(double,double,double,double)
     */
    public void setSimulationBallEnabled(boolean enable) {
		if (enable != simBallEnabled) {
			simBallEnabled = enable;
			
			// Clear the ball location if we transition to a disabled state.
			if (!simBallEnabled) {
				simBallXaxis = Double.NEGATIVE_INFINITY;
				simBallYaxis = Double.NEGATIVE_INFINITY;
			}
			repaint();
		}
    }
    
    /**
     * Draws the simulation ball if it is enabled at the specified X and Y 
     * coordinates and normal vector. In addition the AWT Thread will be
     * notified to repaint the <code>CartesianGraph</code> component.
     * <p>
     * The normal vector is used to allow the ball to roll along the given
     * point.
     *
     * @param x the X coordinate of the ball location
     * @param y the Y coordinate of the ball location
     * @param xNormal the X-axis component of the normal vector for the ball
     * @param yNormal the Y-axis component of the normal vector for the ball
     * @see #isSimulationBallEnabled()
     * @see #setSimulationBallEnabled(boolean)
     */
    public void drawSimulationBall(double x, double y, double xNormal, double yNormal) {
		if (simBallEnabled) {
			this.simBallXaxis = x;
	        this.simBallYaxis = y;
	        this.simBallNormalVector.x = xNormal;
	        this.simBallNormalVector.y = yNormal;
			repaint();
		}
    }

    /**
         * Paints the <code>CartesianGraph</code> component.
         *
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        public String getSVG() {
            setupBounds();

            // Get a DOMImplementation.
            DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);

            // Create an instance of the SVG Generator.
            SVGGraphics2D g2 = new SVGGraphics2D(document);

            if ((cachedPath == null) || !USE_CACHED_GRAPH_DRAWING) {
                setupGraph(g2);
                graphData(g2);
            }

            // Enable antialiasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Use the user specified line size for the stroke.
            if (userSpecifiedStroke.getLineWidth() != currentSettings.getLineSize()) {
                userSpecifiedStroke = new BasicStroke(currentSettings.getLineSize());
            }
            g2.setStroke(userSpecifiedStroke);

            // Draw the sonification trace.
            if (currentSettings.showTrace()) {
                drawSonificationTrace(g2);
            }

            // Draw the simulation Ball.
            if (simBallEnabled) {
                // Use the normal vector to determine the offset of the ball.
                // Note: The ball radius in pixels must be converted into real numbers
                // of X and Y that is why we scale it by the screen and graph diminsions.
                drawSimulationBall(g2);
            }

            // Disable antialiasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Finally, stream out SVG to the standard output using
            // UTF-8 encoding.
            boolean useCSS = true; // we want to use CSS style attributes
            String result = null;
            Writer out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                out = new OutputStreamWriter(baos, "UTF-8");
                g2.stream(out, useCSS);
                result = new String(baos.toByteArray(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (SVGGraphics2DIOException e) {
                e.printStackTrace();
            }
            return result;

        } // end paintComponent

    private void setupBounds() {
        // Reset the graph bounds if the solver bounds have changed.
        if (!solverBounds.equals(solver.getBounds())) {
            resetBounds = true;
        }

        // Update our cached solver bounds.
        solverBounds.setBounds(solver.getBounds());

        // Update the graph bounds if the bounds have changed or need to be reset.
        bounds = getBounds();
        if (resetBounds || !bounds.equals(prevBounds)) {
            resetBounds = false;
            prevBounds = bounds;
            graphBounds.x = bounds.x;
            graphBounds.y = bounds.y;
            graphBounds.width = bounds.width - sideBorderWidth;
            graphBounds.height = bounds.height - bottomBorderHeight;
            nullifyCachedPaths();
        }

        // Force the graph to a 1:1 aspect ratio by adjusting the graph bounds.
        if (use1To1AspectRatio) {
            // Now adjust to the bounds of the data to be drawn.
            double deltaWidth = (solverBounds.right - solverBounds.left) / graphBounds.width;
            double deltaHeight = (solverBounds.top - solverBounds.bottom) / graphBounds.height;
            if (deltaWidth < deltaHeight) {
                int newWidth = (int)Math.round(graphBounds.width * (deltaWidth / deltaHeight));
                if (graphBounds.width != newWidth) {
                    graphBounds.width = newWidth;
                    nullifyCachedPaths();
                }
            } else if (deltaHeight < deltaWidth) {
                int newHeight = (int)Math.round(graphBounds.height * (deltaHeight / deltaWidth));
                if (graphBounds.height != newHeight) {
                    graphBounds.height = newHeight;
                    nullifyCachedPaths();
                }
            }
        }
    }

    private void drawSonificationTrace(SVGGraphics2D g2) {
        int traceXaxisPixel = x2pix(traceXaxis);

        if ((traceXaxisPixel >= 0) && (traceXaxisPixel <= graphBounds.width)) {

            // Use the Polar graph trace only if we have one polar graph to
            // sonify and no other graphs to sonify.
            if ((solver.getSonifyPolarCount() == 1) &&
                    (solver.getSonifyCartesianCount() == 0)) {

                int traceYaxisPixel = y2pix(traceYaxis);
                if (traceYaxisPixel == 0) {
                    traceYaxisPixel = 1; // <== TODO: Why is this needed?????
                }

                // Draw a blue ball with a line around it.
                double x = (traceXaxisPixel >= 0) ? traceXaxisPixel - 3 : traceXaxisPixel + 3;
                double y = (traceYaxisPixel >= 0) ? traceYaxisPixel - 3 : traceYaxisPixel + 3;
                g2.setColor(Color.blue);
                g2.fill(new Ellipse2D.Double(x, y, BALL_DIAMETER, BALL_DIAMETER));

                // Moving Object Simulation (circle)
                g2.setColor(Color.white);
                g2.draw(new Ellipse2D.Double(x, y, BALL_DIAMETER, BALL_DIAMETER));
            } else {
                g2.setColor(Color.white);
                drawLine(traceXaxisPixel, graphBounds.height, traceXaxisPixel, 0, g2);
            }
        }
    }

    /**
     * Paints the <code>CartesianGraph</code> component.
     * 
     * @param g the <code>Graphics</code> context to paint to
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        // Reset the graph bounds if the solver bounds have changed.
        setupBounds();
        
        Graphics2D g2;

        if ((cachedPath == null) || !USE_CACHED_GRAPH_DRAWING) {
            
            if (USE_CACHED_GRAPH_DRAWING) {
                if ((bi == null) || (bi.getWidth() != bounds.width) || (bi.getHeight() != bounds.height)) {
                    bi = (BufferedImage)this.createImage(bounds.width, bounds.height);
					if (bi != null) {
	                    big2 = bi.createGraphics();
						if (big2 == null) {
							big2 = (Graphics2D)g;
						}
					} else {
						big2 = (Graphics2D)g;
					}
					if (big2 != null) {
						// Make sure we use the current font.
						big2.setFont(getFont());
					}
                }
                g2 = big2; // Draw to the graphics 2D object of the buffered image.
            } else {
                g2 = (Graphics2D)g;
            }
            setupGraph(g2);


            // Don't graph the data if the clearGraph flag is set and the
            // path is still cached.
            if (!clearGraph && (cachedPath == null)) {
                graphData(g2);
            }
        }
        
        // Now use the 2D graphics for the component.
        g2 = (Graphics2D)g;
        
        if (USE_CACHED_GRAPH_DRAWING) {
            // Draw the cached graph image buffer.
            g2.drawImage(bi, 0, 0, this);
            
            // Clip any drawing outside of our graph bounds.
            g2.setClip(0, 0, graphBounds.width, graphBounds.height);
        }
        
        // Enable antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Use the user specified line size for the stroke.
        if (userSpecifiedStroke.getLineWidth() != currentSettings.getLineSize()) {
            userSpecifiedStroke = new BasicStroke(currentSettings.getLineSize());
        }
        g2.setStroke(userSpecifiedStroke);
        
        // Draw the sonification trace.
        if (currentSettings.showTrace()) {
            int traceXaxisPixel = x2pix(traceXaxis);
            
            if ((traceXaxisPixel >= 0) && (traceXaxisPixel <= graphBounds.width)) {
                
                // Use the Polar graph trace only if we have one polar graph to
                // sonify and no other graphs to sonify.
                if ((solver.getSonifyPolarCount() == 1) &&
                        (solver.getSonifyCartesianCount() == 0)) {
                    
                    int traceYaxisPixel = y2pix(traceYaxis);
                    if (traceYaxisPixel == 0) {
                        traceYaxisPixel = 1; // <== TODO: Why is this needed?????
                    }
                    
                    // Draw a blue ball with a line around it.
                    double x = (traceXaxisPixel >= 0) ? traceXaxisPixel - 3 : traceXaxisPixel + 3;
                    double y = (traceYaxisPixel >= 0) ? traceYaxisPixel - 3 : traceYaxisPixel + 3;
                    g2.setColor(Color.blue);
                    g2.fill(new Ellipse2D.Double(x, y, BALL_DIAMETER, BALL_DIAMETER));
                    
                    // Moving Object Simulation (circle)
                    g2.setColor(Color.white);
                    g2.draw(new Ellipse2D.Double(x, y, BALL_DIAMETER, BALL_DIAMETER));
                } else {
                    g2.setColor(Color.white);
                    drawLine(traceXaxisPixel, graphBounds.height, traceXaxisPixel, 0, g2);
                }
            }
        }
        
        // Draw the simulation Ball.
        if (simBallEnabled) {
            drawSimulationBall(g2);

        }
        
        // Disable antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
    } // end paintComponent

    private void drawSimulationBall(Graphics2D g2) {
        // Use the normal vector to determine the offset of the ball.
        // Note: The ball radius in pixels must be converted into real numbers
        // of X and Y that is why we scale it by the screen and graph diminsions.
        double pointX = simBallXaxis + simBallNormalVector.x * BALL_RADIUS * (solverBounds.right - solverBounds.left) / graphBounds.width;
        double pointY = simBallYaxis + simBallNormalVector.y * BALL_RADIUS * (solverBounds.top - solverBounds.bottom) / graphBounds.height;

        // Don't use x2pix() or y2pix() because they will clip the values.
        // The range of the ball position is checked before it is drawn.
        int ballXaxisPixel = (int) (((pointX - solverBounds.left) / (solverBounds.right - solverBounds.left)) * graphBounds.width); //x2pix(x);
        int ballYaxisPixel = (int) (((solverBounds.top - pointY) / (solverBounds.top - solverBounds.bottom)) * graphBounds.height); //y2pix(y);

        // Draw the ball only if any part of it is visible.
        if ((ballXaxisPixel >= -BALL_RADIUS) &&
            (ballXaxisPixel <= graphBounds.width + BALL_RADIUS) &&
            (ballYaxisPixel >= -BALL_RADIUS) &&
            (ballYaxisPixel <= graphBounds.height + BALL_RADIUS)) {

            if ((ballYaxisPixel == 0) && (solver.size() == 1)) {
                // NOTE: We only support one equation/data item for the simulation.
                Solution solution = solver.get(0);
                AnalyzedItem analyzedItem = solution.getAnalyzedItem();
                if ((analyzedItem instanceof AnalyzedEquation) && ((AnalyzedEquation)analyzedItem).isPolar()) {
                    ballYaxisPixel = 1; // <== Why is this needed?????
                }
            }
            int xBall = ballXaxisPixel - BALL_RADIUS;
            int yBall = ballYaxisPixel - BALL_RADIUS;

            // Moving Object Simulation (ball)
            g2.setColor(Color.red);
            g2.fill(new Ellipse2D.Double(xBall, yBall, BALL_DIAMETER, BALL_DIAMETER));

            // Moving Object Simulation (outline of ball)
            g2.setColor(Color.white);
            g2.draw(new Ellipse2D.Double(xBall, yBall, BALL_DIAMETER, BALL_DIAMETER));
        }
    }

    private void setupGraph(Graphics2D g2) {
        // Reset the clip region to the size of our bounds.
        g2.setClip(0, 0, bounds.width, bounds.height);

        if (useBlackAndWhiteShade) {
            g2.setColor(Color.white);
            g2.fillRect(0, 0, bounds.width, bounds.height);
            g2.setColor(Color.black);
        } else {
            g2.setColor(currentSettings.getBackgroundColor());
            g2.fillRect(0, 0, bounds.width, bounds.height);
            g2.setColor(currentSettings.getAxisColor());
        }

        // Axis lines use a basic solid line that is 1 pixel wide.
        g2.setStroke(new BasicStroke(1));

        int xAxisPixel = -1;
        if ((solver.getLeft() < 0.0) && (solver.getRight() > 0.0)) {
            xAxisPixel = x2pix(0.0);
            drawLine(xAxisPixel, graphBounds.height, xAxisPixel, 0, g2);
        } // end if

        int yAxisPixel = -1;
        if ((solver.getTop() > 0.0) && (solver.getBottom() < 0.0)) {
            yAxisPixel = y2pix(0.0);
            drawLine(0, yAxisPixel, graphBounds.width, yAxisPixel, g2);
        } // end if

        // Grid uses dashed lines.
        BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);
        g2.setStroke(dashedStroke);

        String axisLbl;
        int w,xi,yi;

        double delta = MathUtil.findDelta(solverBounds.right - solverBounds.left);
        double lastValue = solverBounds.right + 0.5 * delta;
        int labelYPos = graphBounds.height + bottomBorderHeight - fontMetrics.getMaxDescent();

        // Draw the X axis labels.
        for (double x = delta * Math.ceil(solverBounds.left / delta); x < lastValue; x += delta) {
            xi = x2pix(x);

            if (Math.abs(xi - xAxisPixel) > 3) {
                if (useBlackAndWhiteShade) {
                    g2.setColor(Color.lightGray);
                } else {
                    g2.setColor(currentSettings.getGridColor());
                }
                drawLine(xi, 0, xi, graphBounds.height, g2);
            }
            axisLbl = displayDouble(x);
            if (useBlackAndWhiteShade) {
                g2.setColor(Color.darkGray);
            } else {
                g2.setColor(currentSettings.getAxisColor());
            }
            g2.drawString(axisLbl, xi, labelYPos);
        } // end for x

        delta = MathUtil.findDelta(solverBounds.top - solverBounds.bottom);
        lastValue = solverBounds.top + 0.5 * delta;

        // Draw the Y axis labels.
        for (double y = delta * Math.ceil(solverBounds.bottom / delta); y < lastValue; y += delta) {
            yi = y2pix(y);

            if (Math.abs(yi - yAxisPixel) > 3) {
                if (useBlackAndWhiteShade) {
                    g2.setColor(Color.lightGray);
                } else {
                    g2.setColor(currentSettings.getGridColor());
                }
                drawLine(0, yi, graphBounds.width, yi, g2);
            }

            axisLbl = displayDouble(y);
            w = (int)(((fontMetrics.getHeight() + 5.0) * (graphBounds.height - yi)) / graphBounds.height);
            if (useBlackAndWhiteShade) {
                g2.setColor(Color.darkGray);
            } else {
                g2.setColor(currentSettings.getAxisColor());
            }
            g2.drawString(axisLbl, graphBounds.width, yi + w - fontMetrics.getMaxDescent());
        } // end for y

        // Clip any drawing outside of our graph bounds.
        g2.setClip(0, 0, graphBounds.width, graphBounds.height);
    }

    private void graphData(Graphics2D g2) {
        int w;
        cachedPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        cachedScatterPlotPath = null;

        Solution solution;
        AnalyzedData analyzedData;
        GraphTrail[] graphTrails;
        PointXY[] points;
        double[] xData,yData;
        int i,numPoints,numTrails;
        int xp,yp,xdrawn,ydrawn,leftIndex,rightIndex;
        int numSolutions = solver.size();

        // Draw each of the solutions.
        for (int solutionIndex = 0; solutionIndex < numSolutions; solutionIndex++) {
            solution = solver.get(solutionIndex);
            graphTrails = solution.getGraphTrails();

            if (solution.isShowGraph() && (graphTrails != null)) {
                numTrails = graphTrails.length;

                for (i = 0; i < numTrails; i++) {
                    points = graphTrails[i].getPoints();
                    numPoints = (points != null) ? points.length : 0;
                    if (numPoints < 2) {
                        continue;
                    }

                    // Move to the first point in the trail.
                    xp = x2pix(points[0].x);
                    yp = y2pix(points[0].y);
                    cachedPath.moveTo(xp,yp);

                    // Draw the first line segment and setup the xdrawn and ydrawn values.
                    xp = x2pix(points[1].x);
                    yp = y2pix(points[1].y);
                    cachedPath.lineTo(xp,yp);
                    xdrawn = xp;
                    ydrawn = yp;

                    // Draw the remaining line segments.
                    for (w = 2; w < numPoints; w++) {
                        xp = x2pix(points[w].x);
                        yp = y2pix(points[w].y);

                        // Draw the line if we have not drawn it to this point before.
                        // This will exclude duplicates.
                        if ((xp != xdrawn) || (yp != ydrawn)) {
                            cachedPath.lineTo(xp,yp);
                            xdrawn = xp;
                            ydrawn = yp;
                        }
                    }

                    // Move to the last point drawn and close the path.
                    cachedPath.moveTo(xdrawn,ydrawn);
                    cachedPath.closePath();
                }

                // Generate the real data scatter-plot path.
                if (currentSettings.isDataPointsShown() &&
                        (solution.getAnalyzedItem() instanceof AnalyzedData)) {

                    analyzedData = (AnalyzedData)solution.getAnalyzedItem();
                    xData = analyzedData.getXValues();
                    yData = analyzedData.getYValues();
                    leftIndex = analyzedData.getLeftIndexBound();
                    rightIndex = analyzedData.getRightIndexBound();

                    if ((xData != null) && (yData != null) && (rightIndex >= 0) && (leftIndex <= rightIndex)) {
                        if (cachedScatterPlotPath == null) {
                            cachedScatterPlotPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                        }

                        // Draw the first point and setup the xdrawn and ydrawn values.
                        xp = x2pix(xData[leftIndex]);
                        yp = y2pix(yData[leftIndex]);
                        cachedScatterPlotPath.moveTo(xp,yp);
                        cachedScatterPlotPath.lineTo(xp,yp);
                        xdrawn = xp;
                        ydrawn = yp;

                        // Draw the remaining points.
                        for (i = leftIndex+1; i <= rightIndex; i++) {
                            xp = x2pix(xData[i]);
                            yp = y2pix(yData[i]);

                            // Draw the point if we have not drawn it before.
                            // This will exclude duplicates.
                            if ((xp != xdrawn) || (yp != ydrawn)) {
                                cachedScatterPlotPath.moveTo(xp,yp);
                                cachedScatterPlotPath.lineTo(xp,yp);
                                xdrawn = xp;
                                ydrawn = yp;
                            }
                        }

                        // Move to the last point drawn and close the path.
                        cachedScatterPlotPath.moveTo(xdrawn,ydrawn);
                        cachedScatterPlotPath.closePath();
                    }
                }
            }
        }

        if (userSpecifiedStroke.getLineWidth() != currentSettings.getLineSize()) {
            userSpecifiedStroke = new BasicStroke(currentSettings.getLineSize());
        }
        g2.setStroke(userSpecifiedStroke);

        // Set the line color to use for the plotted data.
        if (useBlackAndWhiteShade) {
            g2.setColor(Color.black);
        } else {
            g2.setColor(currentSettings.getLineColor());
        }

        // Enable antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the line path.
        g2.draw(cachedPath);

        // Disable antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Draw the scatter plot path.
        if ((cachedScatterPlotPath != null) && currentSettings.isDataPointsShown()) {
            // Draw the real-data points as a dot with an odd width that is larger
            // than the line width used to line-plot the model data.
            int lineWidth = Math.max(3,(2 * currentSettings.getLineSize()) - 1);
            if (scatterPlotStroke.getLineWidth() != lineWidth) {
                scatterPlotStroke = new BasicStroke(lineWidth);
            }
            g2.setStroke(scatterPlotStroke);
            if (useBlackAndWhiteShade) {
                g2.setColor(Color.black);
            } else {
                g2.setColor(currentSettings.getDataPointColor());
            }
            g2.draw(cachedScatterPlotPath);
        }
    }


    private void drawLine(int x1, int y1, int x2, int y2, Graphics2D g2) {
        g2.draw(new Line2D.Float(x1, y1, x2, y2));
    } // end drawLine
	
	private void nullifyCachedPaths() {
        cachedPath = null;
        cachedScatterPlotPath = null;
    }
    
	private int x2pix(double x) {
        // We need to do clipping to avoid a known Java2D bug caused by drawing
        // lines for large coordinates with AntiAliasing turn on. DDexter 12/8/2003
        if (x < solverBounds.left) {
            return -1;
        }
        if (x > solverBounds.right) {
            return bounds.width + 1;
        }
        return (int)(((x - solverBounds.left) / (solverBounds.right - solverBounds.left)) * graphBounds.width);
    } // end x2pix
    
    private int y2pix(double y) {
        // We need to do clipping to avoid a known Java2D bug caused by drawing
        // lines for large coordinates with AntiAliasing turn on. DDexter 12/8/2003
        if (y > solverBounds.top) {
            return -1;
        }
        if (y < solverBounds.bottom) {
            return bounds.height + 1;
        }
        return (int)(((solverBounds.top - y) / (solverBounds.top - solverBounds.bottom)) * graphBounds.height);
    } // end y2pix
    
	private String displayDouble(double x) {
        if (x < -MAX_VALUE) {
            return "-infinity";
        }
        if (x > MAX_VALUE) {
            return "infinity";
        }
        return displayNumberFormat.format(x);
    } // end displayDouble
    
} // end class CartesianGraph
