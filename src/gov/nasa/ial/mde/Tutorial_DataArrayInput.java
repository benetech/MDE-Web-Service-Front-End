package gov.nasa.ial.mde;
/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */

import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.describer.Describer;
import gov.nasa.ial.mde.sound.Sounder;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;

public class Tutorial_DataArrayInput {

    public static void main(String[] args) {
        // MDE Init as always:
        MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);
        Sounder sounder = new Sounder(solver, currentSettings);

        // Let's create some data for this demonstration of MDE data array input.
        // Make two columns of data with headers:
        String timesHeader = "TIME";
        String valuesHeader = "VALUE_AT_TIME";

        double[] times = new double[10];
        double[] values = new double[10];

        for (int i = 0; i < 10; i++) {
            times[i] = i ;
            values[i] = i * i;
        }
        

        // Let's take our data columns and headers and create an MDE
        // AnalyzedData object:
        AnalyzedData myData = new AnalyzedData(timesHeader, valuesHeader, times, values);

        // Now give Solver the AnalyzedData object and ask it to solve
        solver.add(myData);
        solver.solve();

        // Now we ask for a description and sonification as before
        if (solver.anyDescribable()) {
            String description = describer.getDescriptions("standards");
            System.out.println("Description of data: " + description);
        } else {
            System.out.println("MDE could not generate a description for your data.");
        }

        // Now let's sonify our data
        if (solver.anySonifiable()) {
           System.out.println("Sonifying your data: ");
            sounder.sweep(3.0);
            sounder.close();
        }

        // Clear Solver so next data set will be processed singly
        // (we only want one description at a time)
        solver.removeAll();
    } // end main

} // end class Tutorial_DataArrayInput
