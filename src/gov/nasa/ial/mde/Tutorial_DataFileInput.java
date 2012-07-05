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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import gov.nasa.ial.mde.describer.Describer;
import gov.nasa.ial.mde.io.TextDataFileParser;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.Solver;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedData;

public class Tutorial_DataFileInput {

    public static void main(String[] args) {
        // MDE Init:
        MdeSettings currentSettings = new MdeSettings("myAppsMdeProperties");
        Solver solver = new Solver();
        Describer describer = new Describer(solver, currentSettings);
        describer.setOutputFormat(Describer.TEXT_OUTPUT);
        
        
        File file = new File("InputText.txt");
                
        TextDataFileParser  fileParser = new TextDataFileParser(file);
        AnalyzedData data = null;
        
   
		
		try {
			List<AnalyzedData> list = fileParser.parse();
			System.out.println(list.size());
			data = list.get(0);
			System.out.println(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now give Solver the AnalyzedData object and ask it to solve
        solver.add(data);
        solver.solve();

        // Now we ask for a description and sonification as before
        if (solver.anyDescribable()) {
            String description = describer.getDescriptions("standards");
            System.out.println("Description of data: " + description);
        } else {
            System.out.println("MDE could not generate a description for your data.");
        }

        // Clear Solver so next data set will be processed singly
        // (we only want one description at a time)
        solver.removeAll();
    } // end main

} // end class Tutorial_DataFileInput
