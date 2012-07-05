<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:comment>Copyright 2006, United States Government as represented by the 
  Administrator for the National Aeronautics and Space Administration. No 
  copyright is claimed in the United States under Title 17, U.S. Code. All 
  Other Rights Reserved.</xsl:comment>
  
  <xsl:import href="mdeFeatureTemplates.xsl"/>

  <xsl:template match="GraphData">
     <xsl:apply-templates select="equationPrint"/>

     <xsl:apply-templates select="graphName"/>

     <xsl:choose>
       <xsl:when test="graphName='line' or graphName='horizontal line' or graphName='vertical line'">
         <xsl:call-template name="lineSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='parabola'">
         <xsl:call-template name="parabolaSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='circle'">
         <xsl:call-template name="circleSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='ellipse'">
         <xsl:call-template name="ellipseSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='hyperbola'">
         <xsl:call-template name="hyperbolaSpecifics"/>
       </xsl:when>
       
       <!-- Function description -->
       <xsl:when test="graphName='polynomial'">
<xsl:apply-templates select="FunctionAnalysisData"/>
	       </xsl:when>
	       
	       <xsl:when test="graphName='RationalFunction'">
This is the graph of a function.  
	<xsl:apply-templates select="FunctionAnalysisData"/>
	       </xsl:when>

	       <xsl:when test="graphName='FunctionOverInterval'">
	       	<xsl:apply-templates select="DataID"/>
		<xsl:apply-templates select="ComputedFunctionData"/>
	       </xsl:when>

	<!-- Polar graphs -->
       <xsl:when test="graphName='polar rose'">
         <xsl:call-template name="polarRoseSpecifics"/>
       </xsl:when>

       <xsl:when test="graphName='polar lemniscate'">
         <xsl:call-template name="polarLemniscateSpecifics"/>
       </xsl:when>
       
       <xsl:when test="graphName='cardioid'">
       	<xsl:call-template name="cardioidSpecifics"/>
       </xsl:when>
       
       <xsl:when test="graphName='loopWithinALoop'">
       	<xsl:call-template name="loopWithinALoopSpecifics"/>
       </xsl:when>
       
       <xsl:when test="graphName='eccentricCircle'">
       	<xsl:call-template name="eccentricCircleSpecifics"/>
       </xsl:when>
       
       <xsl:when test="graphName='alternatingLoops'">
       	<xsl:call-template name="alternatingLoopSpecifics"/>
       </xsl:when>
       
       <xsl:when test="graphName='nestedLoops'">
       <xsl:call-template name="nestedLoopsSpecifics"/>       	
       </xsl:when>

       <xsl:when test="graphName='pinchedLoops'">
       <xsl:call-template name="pinchedLoopsSpecifics"/>       	
       </xsl:when>
       
       <xsl:when test="graphName='lumpyCircle'">
       	<xsl:call-template name="lumpyCircleSpecifics"/>
       </xsl:when>

       <xsl:when test="graphName='unclassified'">
         <xsl:call-template name="unclassifiedSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='null set'">
         <xsl:call-template name="nullsetSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='all points'">
         <xsl:call-template name="allpointsSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='single point'">
         <xsl:call-template name="singlePointSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='two lines'">
         <xsl:call-template name="twoLinesSpecifics"/>
       </xsl:when>
       <xsl:when test="graphName='two intersecting lines'">
         <xsl:call-template name="twoIntersectingLinesSpecifics"/>
       </xsl:when>
     </xsl:choose>

     <!--<xsl:apply-templates select="graphBoundaries"/>-->

     <xsl:apply-templates select="xIntercepts">
       <xsl:with-param name="axis"><xsl:value-of select="abscissaSymbol"/></xsl:with-param>
     </xsl:apply-templates> 
       
     <xsl:apply-templates select="yIntercepts">
       <xsl:with-param name="axis"><xsl:value-of select="ordinateSymbol"/></xsl:with-param>
     </xsl:apply-templates>

     <xsl:apply-templates select="graphClosure"/>

     <xsl:apply-templates select="ascendingRegions"/>

     <xsl:apply-templates select="descendingRegions"/>

     <xsl:apply-templates select="equationType"/>

     <xsl:apply-templates select="domain"/>

     <xsl:apply-templates select="range"/>
<p></p>
</xsl:template>

  <xsl:template name="lineSpecifics">
     <xsl:apply-templates select="slope"/>
     <xsl:apply-templates select="slopeDefined"/>
<!--
-->
     <xsl:apply-templates select="inclination">
       <xsl:with-param name="rad"><xsl:value-of select="incrad/decimalValue"/></xsl:with-param>
     </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="parabolaSpecifics">
       <xsl:apply-templates select="vertex" />
       <xsl:apply-templates select="axis" />
       <xsl:apply-templates select="axisInclination">
        <xsl:with-param name="abscissaSymbol" select="abscissaSymbol"/>
       </xsl:apply-templates>
       <xsl:apply-templates select="openDirection" mode="qualifying"/>
       <xsl:apply-templates select="focus" />
       <xsl:apply-templates select="focalLength"/>
       <xsl:apply-templates select="directrix" />
       <xsl:apply-templates select="directrixInclination" />
  </xsl:template>

  <xsl:template name="circleSpecifics">
       <xsl:apply-templates select="center" />
       <xsl:apply-templates select="radius"/>
  </xsl:template>

  <xsl:template name="ellipseSpecifics">
       <xsl:apply-templates select="center" />
       <xsl:apply-templates select="eccentricity" />
       <xsl:apply-templates select="semiMajorAxis" />
       <xsl:apply-templates select="majorAxis" />
       <xsl:apply-templates select="majorAxisInclination" />
       <xsl:apply-templates select="semiMinorAxis" />
       <xsl:apply-templates select="minorAxis" />
       <xsl:apply-templates select="minorAxisInclination" />
       <xsl:apply-templates select="focus" />
       <xsl:apply-templates select="focalLength" />
  </xsl:template>

  <xsl:template name="hyperbolaSpecifics">
       <xsl:apply-templates select="center" />
       <xsl:apply-templates select="vertex" />
       <xsl:apply-templates select="eccentricity" />
       <xsl:apply-templates select="focalLength" />
       <xsl:apply-templates select="transverseAxis" />
       <xsl:apply-templates select="semiTransverseAxis" />
       <xsl:apply-templates select="conjugateAxis" />
       <xsl:apply-templates select="semiConjugateAxis" />
       <xsl:apply-templates select="focus" />
       <xsl:apply-templates select="asymptotes" />
  </xsl:template>

<!-- Templates for polar graphs-->
<xsl:template name="polarRoseSpecifics">
       <xsl:call-template name="numPetals"/>
<xsl:apply-templates select="petalLength"/>
       <xsl:apply-templates select="petalInclinations" />
       <xsl:apply-templates select="petalTips" />
</xsl:template>

<xsl:template name="polarLemniscateSpecifics">
The graph consists of two loops located symmetrically on opposite sides of the
origin in the shape of a figure eight.  The length of each loop is 
<xsl:call-template name="numberModel">
<xsl:with-param name="featureName" select="bladeLength"/>
</xsl:call-template>.
The axis of the blades is oriented at an angle of 
<xsl:call-template name="angleModel">
<xsl:with-param name="featureName" select="inclination"/>
</xsl:call-template> from the positive X-axis.
</xsl:template>

<xsl:template name="cardioidSpecifics">
Curves of this type have a characteristic heart shape with the "cleft" located at the 
origin.  This figure has an axis of symmetry that runs in a 
<xsl:value-of select="axis"/> direction from the cleft of the heart 
to its point most distant from the center.  This axis is inclined at an angle of 
<xsl:call-template name="angleModel">
	<xsl:with-param name="featureName" select="axisInclination"/>
</xsl:call-template>.  
The distance from the cleft to the far side of the curve is 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="maxLength"/>
</xsl:call-template>.
</xsl:template>

	<xsl:template name="loopWithinALoopSpecifics">
This figure looks like a large loop with a smaller loop drawn inside it.  	The curve 
crosses itself at the origin to make the smaller loop.  Both loops are symmetric 
about an axis which runs in a 
<xsl:value-of select="axis"/> direction from the crossing 
point at the origin through the smaller loop to the most distant point on the curve.  
This axis is inclined at an angle of 
<xsl:call-template name="angleModel">
	<xsl:with-param name="featureName" select="axisInclination"/>
</xsl:call-template>.
The size of the large loop is 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="maxLength"/>
</xsl:call-template>.  The size of the smaller loop is 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="minLength"/>
</xsl:call-template>.
</xsl:template>

<xsl:template name="eccentricCircleSpecifics">
	This is a smooth, simple close curve that surrounds the origin.  It resembles a 
	circle that has been elongated on one side and 
<xsl:choose>
<xsl:when test="isConvex='true'">
flattened	
</xsl:when>	
<xsl:otherwise>
	indented
</xsl:otherwise>
</xsl:choose> on the opposite side.  
It is symmetric about an axis which runs 	<xsl:value-of select="axis"/> from the 
elongated end to the opposite side.  The orientation of this axis of symmetry is 
	<xsl:call-template name="angleModel">
		<xsl:with-param name="featureName" select="axisInclination"/>
	</xsl:call-template>.  The elongated end is a distance of 
	<xsl:call-template name="numberModel">
		<xsl:with-param name="featureName" select="maxLength"/>
	</xsl:call-template> from the origin, and the closest point on the curve is a 
	distance of 
	<xsl:call-template name="numberModel">
		<xsl:with-param name="featureName" select="minLength"/>
	</xsl:call-template> from the origin.
</xsl:template>

<xsl:template name="alternatingLoopSpecifics">
	The length of the longer loops is 
<xsl:call-template name="numberModel">
<xsl:with-param name="featureName" select="maxLength"/>
</xsl:call-template>, and the length of the shorter 
loops is 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="minLength"/>
</xsl:call-template>.  
<xsl:apply-templates select="loopAngles"/>
</xsl:template>

<xsl:template name="nestedLoopsSpecifics">
	The curve consists of 
	<xsl:value-of select="thetaMultiple"/> larger loops arranged in a symmetric 
	pattern about the origin.  Inside each of these loops, there is a smaller loop.  
	The larger loops extend a distance of 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="maxLength"/>
</xsl:call-template> from the origin, and 
	the length of the shorter loops is 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="minLength"/>
</xsl:call-template>.  
	<xsl:apply-templates select="loopAngles"/>
</xsl:template>

<xsl:template name="pinchedLoopsSpecifics">
	The curve consists of <xsl:value-of select="thetaMultiple"/> identical 
	loops located symmetrically about the origin.  
	<xsl:apply-templates select="loopAngles"/>.
</xsl:template>

<xsl:template name="lumpyCircleSpecifics">
	The graph is a simple closed curve like a circle except that there is 
	<xsl:if test="isConvex='false'">
	an alternating pattern of dents interspersed with 
	</xsl:if>
	<xsl:if test="isConvex='true'">
		a pattern of 
	</xsl:if>
	bulges space at regular intervals around the 
	curve.  There are <xsl:value-of select="thetaMultiple"/> bulges 
	<xsl:if test="isConvex='false'">
	and <xsl:value-of select="thetaMultiple"/> dents
	</xsl:if>.
	<xsl:apply-templates select="loopAngles"/>.  
One could inscribe this graph in a circle of radius 
	<xsl:call-template name="numberModel">
		<xsl:with-param name="featureName" select="maxLength"/>
	</xsl:call-template>, and 
		the curve would circumscribe a smaller circle of radius 
		<xsl:call-template name="numberModel">
			<xsl:with-param name="featureName" select="minLength"/>
		</xsl:call-template>
</xsl:template>

  <xsl:template name="unclassifiedSpecifics">
  </xsl:template>

  <xsl:template name="nullsetSpecifics">
       <xsl:call-template name="nullSetAlt"/>
  </xsl:template>

  <xsl:template name="allpointsSpecifics">
       <xsl:call-template name="allPointsAlt"/>
  </xsl:template>

  <xsl:template name="singlePointSpecifics">
       <xsl:call-template name="singlePointAlt"/> 
  </xsl:template>

  <xsl:template name="twoLinesSpecifics">
    <xsl:call-template name="twoLinesTemp"/>
  </xsl:template>

  <xsl:template name="twoIntersectingLinesSpecifics">
     <xsl:call-template name="twoIntersectingLinesTemp"/>
  </xsl:template>
</xsl:stylesheet>


