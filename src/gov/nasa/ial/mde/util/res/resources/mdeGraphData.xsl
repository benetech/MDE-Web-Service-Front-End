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
       
       <!-- Function descriptions -->
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

	<!-- Polar graphs-->
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
<p></p>
</xsl:template>


  <xsl:template name="lineSpecifics">
     <xsl:apply-templates select="slope"/>
     <xsl:apply-templates select="slopeDefined"/>
  </xsl:template>

  <xsl:template name="parabolaSpecifics">
     <xsl:apply-templates select="openDirection"/>
     <xsl:call-template name="parabolaWidthTest"/> 
  </xsl:template>


  <xsl:template name="circleSpecifics">
       <xsl:apply-templates select="center" />
       <xsl:apply-templates select="radius" mode="simple"/>
  </xsl:template>

  <xsl:template name="ellipseSpecifics">
     <!--
       <xsl:apply-templates select="center" />
       <xsl:apply-templates select="semiMajorAxis" mode="simple"/>
       <xsl:apply-templates select="semiMinorAxis" mode="simple"/>
     -->
       <xsl:call-template name="ellipseEccentricityTest"/> 
  </xsl:template>

  <xsl:template name="hyperbolaSpecifics">
      <xsl:call-template name="simpleHyperbolaDescription"/>
      <!--
      <xsl:apply-templates select="eccentricity"/>
      <xsl:apply-templates select="focalLength"/>
      <xsl:apply-templates select="center"/>
      <xsl:apply-templates select="vertex"/>
      -->
  </xsl:template>

<!-- Test template for polar rose -->
<xsl:template name="polarRoseSpecifics">
<xsl:choose>
<xsl:when test="numPetals=1">
The graph consists of a simple circle that passes through the origin.  The
length of its diameter is 
<xsl:call-template name="numberModel">
	<xsl:with-param name="featureName" select="petalLength"/>
</xsl:call-template>.
</xsl:when>
<xsl:otherwise>
The graph looks like a <xsl:value-of select="numPetals"/>-bladed propeller 
with its blades symmetric about the origin.
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="polarLemniscateSpecifics">
The graph looks like a figure eight or the mathematical symbol for infinity.
</xsl:template>

<xsl:template name="cardioidSpecifics">
Curves of this type have a characteristic heart shape with the "cleft" located at the 
origin.  
<xsl:choose>
	<xsl:when test="axisInclination/degreeValue=-90">
		The curve is oriented so that the "heart" appears right-side-up on your 
		screen with its "cleft" at the top.  
	</xsl:when>
<xsl:when test="axisInclination/degreeValue=90">
The "heart" is shown in an upside down orientation with its "cleft" at the 
bottom of the picture.  
	</xsl:when>
	<xsl:when test="axisInclination/degreeValue=0 or axisInclination/degreeValue=180">
		The figure is shown with the "heart" lying on its side.  
	</xsl:when>
	<xsl:otherwise>
		The "heart" is turned at an odd angle; try the "Math Description" in the 
		Settings menu for further details.
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>

	<xsl:template name="loopWithinALoopSpecifics">
This figure looks like a large loop with a smaller loop drawn inside it.  	The curve 
crosses itself at the origin to make the smaller loop.  Both loops are symmetric 
about a single line through the origin (try the Math Description in the Settings menu 
for details).  The general equation of this kind of curve is
r = a*cos(theta) + b*sin(theta) + c.  When c is much less than a or b, you 
get two loops that are very nearly on top of each other forming a circle that is 
traced twice.  Going the other way, if you make c larger, the smaller loop 
pinches down until it disappears by popping out to make a simple closed curve 
around the origin.
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
</xsl:choose> on the opposite side.  The general equation for this kind of curve is 
r = a*cos(theta) + b*sin(theta) + c, where c is large compared to a or b.  
If c is just somewhat larger than a and b, then the foreshortened side of the curve 
will have a "dent" and thus the curve will not be convex.  Making c larger flattens out the dent so that the resulting 
curve is convex.  Experiment with r = c + cos(theta) to see how big you 
need to make c to get rid of the dent.
</xsl:template>

<xsl:template name="alternatingLoopSpecifics">
The general equation of this curve is 
r = a*cos(2*n*theta) + b*sin(2*n*theta) + c where c is small compared to a and b.  
When you make c very small, the shorter loops are nearly the same length as the larger ones, and you 
approach the case of a polar rose.
</xsl:template>

<xsl:template name="nestedLoopsSpecifics">
	The curve consists of 
	<xsl:value-of select="thetaMultiple"/> larger loops arranged in a symmetric 
	pattern about the origin.  Inside each of these loops, there is a smaller loop.  
	The general equation of this kind of curve is
	r = a*cos((2*n+1)*theta) + b*sin((2*n+1)*theta) + c.  
	In this case, because the coefficient of theta is an odd number, the 
smaller set of loops falls inside the larger loops.  When you make 
c small compared to a and b, the smaller loops come close to drawing over the larger ones, and you approach 
the case of a polar rose with an odd number of loops.
</xsl:template>

<xsl:template name="pinchedLoopsSpecifics">
	The curve consists of <xsl:value-of select="thetaMultiple"/> identical 
	loops located symmetrically about the origin.  
</xsl:template>

<xsl:template name="lumpyCircleSpecifics">
<xsl:choose>
	<xsl:when test="thetaMultiple=2">
		<xsl:if test="isConvex='true'">
			The curve looks like a circle that has been stretched slightly to 
			make an oval shaped figure.  
		</xsl:if>
		<xsl:if test="isConvex='false'">
			The curve is larger at its ends and smaller in the middle forming a 
			shape which might resemble the silhouette of a dumbell.  
		</xsl:if>
	</xsl:when>
	<xsl:when test="thetaMultiple=3">
		The curve could be visualized as a triangle with softly rounded 
		corners
				<xsl:if test="isConvex='false'">
 and sides bent in towards the center
		</xsl:if>
	</xsl:when>
<xsl:otherwise>
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
</xsl:otherwise>
</xsl:choose>
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


