/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 */
package gov.nasa.ial.mde.solver;

import gov.nasa.ial.mde.math.IntervalXY;
import gov.nasa.ial.mde.math.NumberModel;
import gov.nasa.ial.mde.math.PointXY;
import gov.nasa.ial.mde.properties.MdeSettings;
import gov.nasa.ial.mde.solver.classifier.QuadraticClassifier;
import gov.nasa.ial.mde.solver.features.individual.FocalLengthFeature;
import gov.nasa.ial.mde.solver.features.individual.FocusFeature;
import gov.nasa.ial.mde.solver.features.individual.VertexFeature;
import gov.nasa.ial.mde.solver.symbolic.AnalyzedEquation;

/**
 * Subclass of SolvedGraph responsible for recording features unique to parabolas.
 * 
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 * 
 * Edits made by Andrew Rosen beginning 6/4/10
 */


//TODO: add extrema detection
public class SolvedParabola extends SolvedConic implements VertexFeature, FocalLengthFeature, FocusFeature{
    
    /** Identify new features so we can access them with SolvedGraph.putFeature */
    protected String[] newFeatures = { 
                "vertex", 
                "focalLength", 
                "focus", 
                "directrix", 
                "axis", 
                "axisInclination", 
                "directrixInclination", 
                "openDirection" };

    /* enums for opening direction */
    private final static int NO_D = 0, UP = 1, DOWN = 2, RIGHT = 3, LEFT = 4;
    private int ope = NO_D;

    /**
     * Constructs a solved parabola for the specified analyzed equation.
     * 
     * @param equation the analyzed equation.
     */
    public SolvedParabola(AnalyzedEquation equation) {
        super(equation);

        // specificFeatureNames = new String[newFeatures.length];
        // System.arraycopy(newFeatures, 0, specificFeatureNames, 0, newFeatures.length);

        /* QC is the QuadraticClassifier field in SolvedConic */
        double alpha = QC.getRotation(); // rotation angle in degrees
        /*
		 * coeffs={a, b, c, d, e} where a(u-h)^2 + b(v-k)^2 + cu + dv + e = 0
		 */
        double[] coeffs = QC.getNormalizedCoefficients();
        double[] transUV = QC.getTranslation(); // transUV={h,k}
        double[] vertexUV = new double[2];
        double axisInclination, 
               directrixInclination, 
               focalLength = 0.0, 
               t;

        putNewFeatures(newFeatures); // enable use of new features
        putFeature("graphName", "parabola"); // self-explanatory
        putFeature("equationType", "conic section"); // ditto
        putFeature("graphClosure", "false"); // might be hard to determine in
											 // general

        /* figure out which way it opens in UV space */
        /* default is ope=NO_D which will cause an exception */
        if (Math.abs(coeffs[0]) > Math.abs(coeffs[1])) {
	        /*
			 * We are in the form a(u-h)^2 + dv + e = 0, so normalize out
			 * coefficient of v
			 */
            t = -1.0 / coeffs[3];
            for (int i = 0; i < 5; i++)
                coeffs[i] *= t;
            if (coeffs[0] > 0.0)
                ope = UP;
            else
                ope = DOWN;

            focalLength = Math.abs(0.25 / coeffs[0]);
        } // end if
        else {
            /*
			 * We are in the form b(v-k)^2 + cu + e = 0, so normalize out the
			 * coefficient of u
			 */
            t = -1.0 / coeffs[2];
            for (int i = 0; i < 5; i++)
                coeffs[i] *= t;

            if (coeffs[1] > 0.0)
                ope = RIGHT;
            else
                ope = LEFT;

            focalLength = Math.abs(0.25 / coeffs[1]);
        } // end if

        /* first calculate vertex */
        switch (ope) {
            case UP :
                vertexUV[0] = transUV[0];
                vertexUV[1] = coeffs[4];
                axisInclination = alpha + 90.0;
                break;

            case DOWN :
                vertexUV[0] = transUV[0];
                vertexUV[1] = coeffs[4];
                axisInclination = alpha - 90.0;
                break;

            case RIGHT :
                vertexUV[1] = transUV[1];
                vertexUV[0] = coeffs[4];
                axisInclination = alpha;
                break;

            case LEFT :
                vertexUV[1] = transUV[1];
                vertexUV[0] = coeffs[4];
                axisInclination = alpha + 180.0;
                break;

            default :
                throw new IllegalStateException("Should have computed a valid opening direction.");
        } // end switch

        /* normalize angles between -179.9999 and 180.0 */
        directrixInclination = axisInclination - 90.0;
        axisInclination = QuadraticClassifier.normalizeAngleInDegrees(axisInclination);
        directrixInclination = QuadraticClassifier.normalizeAngleInDegrees(directrixInclination);

        /* rotate vertex from UV to XY coordinate system */
        PointXY vertex = new PointXY(QC.UV2XY(vertexUV));

        putFeature("vertex", vertex);
        putFeature("axis", "line given by " + QuadraticClassifier.getEquationOfALine(vertex, axisInclination, analyzedEq.getActualVariables()));
        putFeature("axisInclination", new NumberModel(axisInclination));
        putFeature("directrixInclination", new NumberModel(directrixInclination));

        /*
		 * displacement of focus relative to vertex. Focus is located
		 * focalDistance along the axis from the vertex in the open direction
		 * of the parabola
		 */
        PointXY focalDisplacement =
            new PointXY(focalLength * Math.cos(Math.PI * axisInclination / 180.0), focalLength * Math.sin(Math.PI * axisInclination / 180.0));
        PointXY focus = vertex.sum(focalDisplacement); // self-explanatory
        /*
		 * directrix is perpendicular to the axis, crossing at directrixPoint
		 * which is the mirror image of the focus
		 */
        PointXY directrixPoint = vertex.difference(focalDisplacement);

        putFeature("focalLength", new NumberModel(focalLength));
        putFeature("focus", focus);
        putFeature(
            "directrix",
            "line given by " + QuadraticClassifier.getEquationOfALine(directrixPoint, directrixInclination, analyzedEq.getActualVariables()));
        putFeature("openDirection", SolvedGraph.getGeneralDir(ope));

        /* find domain, range and ascending/descending regions */
        if (alpha == 0.0) { // otherwise much more complicated -- defer for now
            IntervalXY D, R; // domain and range
            IntervalXY ar, dr; // ascending/descending regions

            switch (ope) {
                case UP :
                    D = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                    D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("domain", D);

                    R = new IntervalXY(analyzedEq.getActualVariables()[1], vertex.y, Double.POSITIVE_INFINITY);
                    R.setEndPointExclusions(IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("range", R);

                    ar = new IntervalXY(analyzedEq.getActualVariables()[0], vertex.x, Double.POSITIVE_INFINITY);
                    ar.setEndPointExclusions(IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("ascendingRegions", ar);

                    dr = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, vertex.x);
                    dr.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X);
                    putFeature("descendingRegions", dr);
                    break;

                case DOWN :
                    D = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                    D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("domain", D);

                    R = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, vertex.y);
                    R.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X);
                    putFeature("range", R);

                    dr = new IntervalXY(analyzedEq.getActualVariables()[0], vertex.x, Double.POSITIVE_INFINITY);
                    dr.setEndPointExclusions(IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("descendingRegions", dr);

                    ar = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, vertex.x);
                    ar.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X);
                    putFeature("ascendingRegions", ar);
                    break;

                case RIGHT :
                    D = new IntervalXY(analyzedEq.getActualVariables()[0], vertex.x, Double.POSITIVE_INFINITY);
                    D.setEndPointExclusions(IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("domain", D);

                    R = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                    R.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("range", R);
                    break;

                case LEFT :
                    D = new IntervalXY(analyzedEq.getActualVariables()[0], Double.NEGATIVE_INFINITY, vertex.x);
                    D.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X);
                    putFeature("domain", D);

                    R = new IntervalXY(analyzedEq.getActualVariables()[1], Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                    R.setEndPointExclusions(IntervalXY.EXCLUDE_LOW_X | IntervalXY.EXCLUDE_HIGH_X);
                    putFeature("range", R);
                    break;

                default :
                    throw new IllegalStateException("Invalid opening direction in SolvedParabola");
            } // end switch
        } // end if
        
        if(MdeSettings.DEBUG){
        	System.out.println(getXMLString());
            getYIntercepts();
            getVertex();
            getFocus();
        }
        //getMinima();
       // getFocalLength();
       

    } // end SolvedParabola

	
	public PointXY getVertex() {
		Object value = this.getValue(VertexFeature.PATH, VertexFeature.KEY);
		String vertexString = (String)value;
		//System.out.println("Getting vertex.\nVertex is : " + vertexString);
		String[] split = vertexString.split(",");
		split[0] = split[0].replace("(", "");
		split[1] = split[1].replace(")", "");
		double xPos = Double.valueOf(split[0]);
		double yPos = Double.valueOf(split[1]);
		
		return (new PointXY(xPos,yPos));
	}


	public PointXY getFocus() {
		Object value = this.getValue(FocusFeature.PATH, FocusFeature.KEY);
		String focusString = (String)value;
		//System.out.println("Getting Focus.\nFocus is : " + focusString);
		String[] split = focusString.split(",");
		split[0] = split[0].replace("(", "");
		split[1] = split[1].replace(")", "");
		double xPos = Double.valueOf(split[0]);
		double yPos = Double.valueOf(split[1]);
		
		return (new PointXY(xPos,yPos));
	}

	public Double getFocalLength() {
		Object value = this.getValue(FocalLengthFeature.PATH, FocalLengthFeature.KEY);
		Double doubleValue = new Double((String)value);	
		//System.out.println("Getting Focal Length.\nFocal Length is : " + doubleValue);
		return doubleValue;
	}
	
	
    
} // end class SolvedParabola
