package gov.nasa.ial.mde;
/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Sep 3, 2004
 *
 * @author Terry Hodgson
 */

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.ui.graph.CartesianGraph;

import javax.swing.JFrame;
import java.io.IOException;

public class Tutorial_CartesianGraph {

    public static void main(String[] args) throws IOException {
        // MDE Init:
        MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();

        // Create a Java Swing window for our graph:
        JFrame window = new JFrame("Tutorial_CartesianGraph");
        // Create an MDE CartesianGraph instance:
        CartesianGraph grapher = new CartesianGraph(solver, currentSettings);

        // Add our graph panel to the window.
        window.getContentPane().add(grapher);
        window.pack();
        window.setVisible(true);
        window.toFront();

        // Give Solver an equation attempt to solve:
        String equation = "y=x^2-2";
        solver.add(equation);
        solver.solve();

        // If our equation is graphable, draw the graph.
        if (solver.anyGraphable()) {

            //Write svg to system out
            String svg = grapher.getSVG();
            System.out.println(svg);

            grapher.drawGraph();

            //Create .png image file from Java graph
            //BufferedImage bi = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            //grapher.drawGraphToImage(bi);
            //ImageIO.write(bi, "png", new File("/tmp/test.png"));
        } else {
            System.out.println("MDE could not generate a graph for " + equation + ".");
        }
    } // end main

} // end class Tutorial_CartesianGraph
