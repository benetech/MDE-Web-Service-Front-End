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
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;

public class CSVTest {

    public static void main(String[] args) {
        // MDE Init:
        MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);
        
        String timesHeader = "TIME";
        String valuesHeader = "VALUE_AT_TIME";

        double[] times = new double[10];
        double[] values = new double[10];
        
        
        for(int i = 0; i<10; i++)
        {
        	times[i] = i;
        	values[i] = i*i;
        	System.out.println(times[i] + " " + values[i]);
        }
        
        
        
        // Let's take our data columns and headers and create an MDE
        // AnalyzedData object:
        
        AnalyzedData myData = new AnalyzedData(timesHeader, valuesHeader, times, values);
        solver.add(myData);
        solver.solve();
        
        
        //myData.getFeatures();
        

    } // end main

} 
