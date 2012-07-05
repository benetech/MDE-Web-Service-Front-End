package gov.nasa.ial.mde;
/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.sound.Sounder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Tutorial_CommandLineSonifier {

    public static void main(String[] args) {

        MdeSettings settings = new MdeSettings("myMDEProps.prop");

        Solver solver = new Solver();

        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(isr);

            while (true) {
                System.out.println("\n\nEnter equation (or CTRL-C to exit): ");
                String equation = reader.readLine();
                solver.add(equation);
                solver.solve();

                // Does user want to sonify equation?
                if (solver.anySonifiable()) {
                    boolean sonflag = true;
                    while (sonflag) {
                        Sounder sounder = new Sounder(solver, settings);

                        System.out.println("Sonifying " + equation
                                + " from x = " + solver.getLeft()
                                + " to x = " + solver.getRight());
                        sounder.sweep(3.0);
                        sounder.close();

                        // Do they want to hear it again?
                        System.out.println("\n\nSonify again? (y/n): ");
                        String s = reader.readLine();
                        if ((s != null) && s.equals("n")) {
                            sonflag = false;
                        }
                    } // end while sonflag
                }
                solver.removeAll();

            } // end while true
        } catch (Exception e) {
            System.out.println(e);
        }
    } // end main

} // end class Tutorial_CommandLineSonifier
