/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.ui;

import gov.nasa.ial.mde.solver.Solver;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

/**
 * The <code>GraphNavKeys</code> class allows for the arrow keys and the mouse
 * to be used to navigate the graph.
 * 
 * @version 1.0
 * @since 1.0
 */
public class GraphNavKeys implements KeyListener, MouseListener {

    private Solver solver;
    private Rectangle graphBounds;
    private JPanel graphPanel;

    /**
     * Constructs a <code>GraphNavKeys</code> for the specified solver, graph
     * panel and graph bounds.
     * 
     * @param solver the solver to be called when navigating the graph.
     * @param graphPanel the graph that we are navigating.
     * @param graphBounds the bounds of the graph.
     */
    public GraphNavKeys(Solver solver, JPanel graphPanel, Rectangle graphBounds) {
        this.solver = solver;
        this.graphBounds = graphBounds;
        this.graphPanel = graphPanel;
    }

    /**
     * Ignored for now.
     * 
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
        // do nothing for now
    }
    
    /**
     * Ignored for now.
     * 
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent ke) {
        // do nothing for now
    }

    /**
     * Handle the arrow key events for navigating the graph.
     * 
     * @param ke the key event to handle.
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {
        double offset;
        switch (ke.getKeyCode()) {
        case KeyEvent.VK_HOME:
	        // Reset the solution by using the preferred bounds.
	        solver.solve(solver.getPreferredBounds());
            break;
        case KeyEvent.VK_DOWN:
            offset = 0.02 * (solver.getTop() - solver.getBottom());
            solver.solve(solver.getLeft(), solver.getRight(), solver.getTop() - offset, solver.getBottom() - offset);
        	break;
        case KeyEvent.VK_UP:
            offset = 0.02 * (solver.getTop() - solver.getBottom());
            solver.solve(solver.getLeft(), solver.getRight(), solver.getTop() + offset, solver.getBottom() + offset);
            break;
        case KeyEvent.VK_LEFT:
            offset = 0.02 * (solver.getRight() - solver.getLeft());
            solver.solve(solver.getLeft() - offset, solver.getRight() - offset, solver.getTop(), solver.getBottom());
            break;
        case KeyEvent.VK_RIGHT:
            offset = 0.02 * (solver.getRight() - solver.getLeft());
            solver.solve(solver.getLeft() + offset, solver.getRight() + offset, solver.getTop(), solver.getBottom());
            break;
        }
    }

    /**
     * Ignored for now.
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
        // do nothing for now.
    } // end mouseEntered

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        // do nothing for now.
    } // end mouseExited

    /**
     * Ignored for now.
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        // do nothing for now.
    } // end mousePressed

    /**
     * Ignored for now.
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        // do nothing for now.
    } // end mouseReleased

    /**
     * Handle the mouse click event to allow for navigation of the graph.
     * 
     * @param me the mouse event to handle.
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        graphPanel.grabFocus();
        int x = me.getX();
        int y = me.getY();
        double deltaX = (x - graphBounds.width / 2.0) * ((solver.getRight() - solver.getLeft())) / graphBounds.width;
        double deltaY = (graphBounds.height / 2.0 - y) * ((solver.getTop() - solver.getBottom())) / graphBounds.height;
        solver.solve(solver.getLeft() + deltaX, 
                     solver.getRight() + deltaX, 
                     solver.getTop() + deltaY, 
                     solver.getBottom() + deltaY);
    }

}
