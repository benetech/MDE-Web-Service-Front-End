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

import gov.nasa.ial.mde.describer.Describer;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Tutorial_CommandLineDescriber {

    public static void main(String[] args) {
        // MDE Init:
        MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);

        // Process equations
        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(isr);

            // Prompt user for input until they enter CTRL-C.
            while (true) {
                System.out.println("\n\nEnter equation (or CTRL-C to exit): ");

                String equation = reader.readLine();

                // Give Solver equation and solve
                solver.add(equation);
                solver.solve();

                if (solver.anyDescribable()) {
                    String description = describer.getDescriptions("standards");
                    System.out.println("Description: " + description);
                } else {
                    System.out.println("MDE could not generate a description for "
                                    + equation + ".");
                }

                // Clear Solver so next equation will be processed singly
                // (we only want one description at a time)
                solver.removeAll();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    } // end main

} // end class Tutorial_CommandLineDescriber
