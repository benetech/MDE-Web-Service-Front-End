/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 26, 2005
 */
package gov.nasa.ial.mde.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * The <code>ComponentUtil</code> class contains utility methods for working
 * with swing components.
 * 
 * @author Dan Dexter
 * @version 1.0
 * @since 1.0
 */
public class ComponentUtil {
    
    /**
     * Returns the parent <code>JFrame</code> of the specified component.
     * 
     * @param component the component to find the parent JFrame for.
     * @return the parent JFrame.
     */
    public static JFrame getParentJFrame(Component component) {
        if ((component == null) || (component instanceof JFrame)) {
            return (JFrame)component;
        }
        Container c = component.getParent();
        while ((c != null) && !(c instanceof JFrame)) { 
            c = c.getParent();
        }
        return (JFrame)c;
    }
    
    /**
     * Sets the background color for the specified container and all the
     * JPanel, JLabel, JCheckBox, JComboBox, JTextField, JRadioButton, and
     * JScrollPane components that it contains to the same color.
     * 
     * @param c the container to set the background color of.
     * @param bg the background color.
     */
    public static void setBackground(Container c, Color bg) {
        c.setBackground(bg);
        Component[] children = c.getComponents();
        if (children == null) {
            return;
        }
        Component child;
        int len = children.length;
        if (bg != null) {
            for (int i = 0; i < len; i++) {
                child = children[i];
                if (!bg.equals(child.getBackground()) &&
                		  ((child instanceof JPanel) || 
                		   (child instanceof JLabel) || 
						   (child instanceof JCheckBox) ||
						   (child instanceof JComboBox) ||
						   (child instanceof JTextField) ||
						   (child instanceof JRadioButton) ||
						   (child instanceof JScrollPane))) {
                    child.setBackground(bg);
                }
            }
        } else {
            // Null background color case
            for (int i = 0; i < len; i++) {
                child = children[i];
                if ((child.getBackground() != null) &&
                        ((child instanceof JPanel) || 
                         (child instanceof JLabel) || 
						 (child instanceof JCheckBox))) {
                    child.setBackground(null);
                } //end if
            } // end for
        } //end if else
    } // end setBackground
    
    /**
     * Sets the background color for the specified <code>ButtonGroup</code> and
     * all the JCheckBox, JComboBox, JButton, and JRadioButton components that 
     * it contains to the same color.
     * 
     * @param buttons the button group to set the background for.
     * @param bg the background color.
     */
    public static void setBackground(ButtonGroup buttons, Color bg) {
        Enumeration<?> children = buttons.getElements();
        if (children == null) {
            return;
        }
        Component child;
        
        if (bg != null) {
            while (children.hasMoreElements()) {
                child = (Component)children.nextElement();
                if (!bg.equals(child.getBackground()) &&
                		  ((child instanceof JCheckBox) ||
						   (child instanceof JComboBox) ||
						   (child instanceof JButton) ||
						   (child instanceof JRadioButton))) {
                    child.setBackground(bg);
                }
            }
        } 
    } // end setBackground
}
