<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<xsl:comment>
		Copyright 2006, United States Government as represented by the
		Administrator for the National Aeronautics and Space Administration.
		No
		copyright is claimed in the United States under Title 17, U.S. Code. All
		Other Rights Reserved.
	</xsl:comment>

	<xsl:template match="equationPrint">
		<xsl:text>Your input equation is </xsl:text>
		<xsl:variable name="equation" select="." />
		<xsl:value-of select="concat('`', $equation, '`')" />.
	</xsl:template>
	
	<xsl:template match="originalEquationPrint">
		<xsl:text>Your input equation is </xsl:text>
		<xsl:variable name="equation" select="." />
		<xsl:value-of select="concat('`', $equation, '`')" />.
	</xsl:template>
	
	<xsl:template match="graphName">
		<xsl:choose>
			<xsl:when test=".='unclassified'">
				The Math Description Engine was not able to classify this graph.
			</xsl:when>
			<xsl:when test=".='all points'">
				The solution is the set of all points.
			</xsl:when>
			<xsl:when test=".='two lines'">
				The graph of the equation is two parallel lines.
			</xsl:when>
			<xsl:when test=".='two intersecting lines'">
				The graph of the equation is two intersecting lines.
			</xsl:when>
			<xsl:when test=".='absolute value'">
				The graph is of an absolute value function.
			</xsl:when>
			<xsl:when test=".='alternatingLoops'">
				The graph consists of
				<xsl:value-of select="../thetaMultiple" />
				loops alternating with
				<xsl:value-of select="../thetaMultiple" />
				shorter loops in a symmetric pattern
				around the origin. Each smaller loop appears between a pair of
				adjacent longer loops.
			</xsl:when>
			<xsl:when test=".='nestedLoops'" />
			<xsl:when test=".='pinchedLoops'" />
			<xsl:when test=".='lumpyCircle'" />
			<xsl:when test=".='loopWithinALoop'" />
			<xsl:when test=".='eccentricCircle'" />

			<xsl:when test=".='parabola'">
				This graph is a parabola: a member of the quadratic family.
			</xsl:when>


			<xsl:when test=".='polynomial'"></xsl:when>

			<xsl:when test=".='square root'">
				This is a graph that contains a square root function.
			</xsl:when>
			<xsl:when test=".='sine function'">
				This is a trigonometric sine function.
			</xsl:when>
			<xsl:when test=".='cosine function'">
				This is a trigonometric cosine function.
			</xsl:when>
			<xsl:when test=".='tangent function'">
				This is a trigonometric tangent function.
			</xsl:when>

			<xsl:when test=".='RationalFunction'"></xsl:when>
			<xsl:when test=".='FunctionOverInterval'"></xsl:when>
			<xsl:otherwise>
				The graph of the equation is
				<xsl:call-template name="aOrAn" />
				<xsl:value-of select="." />
				<xsl:text>. </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="DataID">
		The
		<xsl:value-of select="." />
		curve has the following characteristics.
	</xsl:template>

	<xsl:template match="graphBoundaries">
		The equation is graphed from
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="equationType">
		The equation is
		<xsl:call-template name="aOrAn" />
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>


	<!-- this is stuff for slope -->
	<xsl:template match="slope">
		<xsl:if test="decimalValue > 1">
			<xsl:text>It rises steeply from left to right </xsl:text>
		</xsl:if>
		<xsl:if test="decimalValue = 1">
			<xsl:text>It rises at a 45 degree angle from left to right </xsl:text>
		</xsl:if>
		<xsl:if test="decimalValue > 0 and decimalValue &lt; 1">
			<xsl:text>It rises gradually from left to right </xsl:text>
		</xsl:if>
		<xsl:if test="decimalValue = -1">
			<xsl:text>It falls at a 45 degree angle from left to right </xsl:text>
		</xsl:if>
		<xsl:if test="decimalValue > -1 and decimalValue &lt; 0">
			<xsl:text>It falls gradually from left to right </xsl:text>
		</xsl:if>
		<xsl:if test="decimalValue &lt; -1">
			<xsl:text>It falls steeply from left to right </xsl:text>
		</xsl:if>
		<xsl:if test="decimalValue = 0">
			<xsl:text>It is flat </xsl:text>
		</xsl:if>
		<xsl:text>with a slope of </xsl:text>
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />.
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					<xsl:text>approximately </xsl:text>
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="slopeDefined">
		<xsl:if test=". = 'false'">
			<xsl:text>The slope is undefined. </xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="focus">
		<xsl:choose>
			<xsl:when test="last() = 1">
				The
				<a href="glossary.html#foci">focus</a>
				<xsl:text> is located at the point </xsl:text>
				<xsl:value-of select="current()" />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="position() = 1">
					The
					<a href="glossary.html#foci">foci</a>
					<xsl:text> are located at the points </xsl:text>
				</xsl:if>
				<xsl:value-of select="current()" />
				<xsl:if test="not(position()=last())">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(position()=last())">
					<xsl:text>. </xsl:text>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="vertex">
		<xsl:choose>
			<xsl:when test="last() = 1">
				The
				<a href="glossary.html#vertex">vertex</a>
				<xsl:text> is located at the point </xsl:text>
				<xsl:value-of select="current()" />
				<xsl:text>.  </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="position() = 1">
					The
					<a href="glossary.html#vertex">vertices</a>
					<xsl:text> are located at the points </xsl:text>
				</xsl:if>
				<xsl:value-of select="current()" />
				<xsl:if test="not(position()=last())">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(position()=last())">
					<xsl:text>. </xsl:text>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="oscillation">
		<xsl:text>The entered equation oscillates with an amplitude of </xsl:text>
		<xsl:value-of select="./amplitude" />
		<xsl:text> and a frequency of </xsl:text>
		<xsl:value-of select="./frequency" />
		<xsl:text>. </xsl:text>
		<xsl:text> The wave also has a period of </xsl:text>
		<xsl:value-of select="./period" />
		<xsl:text>. </xsl:text>


		<xsl:if test="not(./phase =0)">
			<xsl:text> This wave has a phase of </xsl:text>
			<xsl:value-of select="./phase" />
			.
		</xsl:if>

	</xsl:template>


	<xsl:template match="orientation">
		<xsl:text>The curve of the graph veers in the direction of </xsl:text>
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="absDirection">
		<xsl:text>The graph opens </xsl:text>
		<xsl:value-of select="." />
		from the vertex.
	</xsl:template>

	<xsl:template match="coordinateSystem">
		<xsl:text>When graphed with the </xsl:text>
		<xsl:value-of select="." />
		coordinate system,
	</xsl:template>

	<xsl:template match="abscissaSymbol">
		<xsl:text>The horizontal axis name (abscissa Symbol) is </xsl:text>
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="ordinateSymbol">
		<xsl:text>The vertical axis name (ordinate Symbol) is </xsl:text>
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="domain">
		The domain of the equation is
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="range">
		The range of the equation is
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="xIntercepts">
		<xsl:param name="axis" />
		<xsl:call-template name="print-Intercepts">
			<xsl:with-param name="cnt" select="last()" />
			<xsl:with-param name="axis" select="$axis" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="yIntercepts">
		<xsl:param name="axis" />
		<xsl:call-template name="print-Intercepts">
			<xsl:with-param name="cnt" select="last()" />
			<xsl:with-param name="axis" select="$axis" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="radius">
		The radius is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="radius" mode="simple">
		The width of the circle is
		<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
			approximately
		</xsl:if>
		<xsl:value-of select="format-number(2*decimalValue,'#.###')" />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="center">
		The center is at
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template name="centerNamedTemplateTest">
		<xsl:param name="valueOnly">
			false
		</xsl:param>
		<!--<xsl:value-of select="$valueOnly"/> -->
		<xsl:choose>
			<xsl:when test="$valueOnly = 'true'">
				<xsl:value-of select="center" />
			</xsl:when>
			<xsl:otherwise>
				The center is at
				<xsl:value-of select="center" />
				<xsl:text>. </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="graphClosure">
		<xsl:choose>
			<xsl:when test="current()='true'">
				<xsl:text>It is a closed curve.  </xsl:text>
			</xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="descendingRegions">
		<xsl:choose>
			<xsl:when test="last() = 1">
				<xsl:text>The descending region is </xsl:text>
				<xsl:value-of select="." />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="position() = 1">
					<xsl:text> The descending regions are: </xsl:text>
				</xsl:if>
				<xsl:value-of select="current()" />
				<xsl:if test="not(position()=last())">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(position()=last())">
					<xsl:text>. </xsl:text>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="ascendingRegions">
		<xsl:choose>
			<xsl:when test="last() = 1">
				<xsl:text>The ascending region is </xsl:text>
				<xsl:value-of select="." />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="position() = 1">
					<xsl:text> The ascending regions are: </xsl:text>
				</xsl:if>
				<xsl:value-of select="current()" />
				<xsl:if test="not(position()=last())">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(position()=last())">
					<xsl:text>. </xsl:text>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="print-Intercepts">
		<xsl:param name="cnt" />
		<xsl:param name="axis" />
		<xsl:choose>
			<xsl:when test="$cnt = 1">
				<xsl:text>The </xsl:text>
				<xsl:value-of select="$axis" />
				<xsl:text>-intercept is </xsl:text>
				<xsl:value-of select="format-number(current(),'#.###')" />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="position() = 1">
					<xsl:text>The </xsl:text>
					<xsl:value-of select="$axis" />
					<xsl:text> intercepts are </xsl:text>
				</xsl:if>
				<xsl:value-of select="format-number(current(),'#.###')" />
				<xsl:if test="not(position()=last())">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(position()=last())">
					<xsl:text>. </xsl:text>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>



	<xsl:template match="inclination">
		<xsl:param name="rad" />
		The graph has an inclination of
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text> degrees or </xsl:text>
		<xsl:if test="string-length(substring-after($rad,'.'))  > 3">
			approximately
		</xsl:if>
		<xsl:value-of select="format-number(number($rad),'#.###')" />
		radians.
	</xsl:template>


	<xsl:template match="directrix">
		The directrix is the
		<xsl:value-of select="." />
		<xsl:text>.</xsl:text>
	</xsl:template>

	<!-- axis of symmetry - currently defined for parabola -->
	<xsl:template match="axis">
		The curve has an axis of symmetry which is the
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<!-- inclination of axis of symmetry - currently defined for parabola -->
	<xsl:template match="axisInclination">
		<xsl:param name="abscissaSymbol" />
		Its axis of symmetry is oriented at an angle of
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		degrees from the positive
		<xsl:value-of select="$abscissaSymbol" />
		-axis.
	</xsl:template>

	<!-- length of the semiTransverseAxis- currently defined for hyperbola -->
	<xsl:template match="semiTransverseAxis">
		The length of the semitransverse axis is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>. </xsl:text>
	</xsl:template>

	<!-- length of the semiConjugateAxis- currently defined for hyperbola -->
	<xsl:template match="semiConjugateAxis">
		The length of the semiconjugate axis is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="transverseAxis">
		The equation of the transverse axis is
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="conjugateAxis">
		The equation of the conjugate axis is
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="asymptotes">
		<xsl:if test="position() = 1">
			The equations of the asymptotes are:
		</xsl:if>
		<xsl:value-of select="." />
		<xsl:if test="not(position()=last())">
			and
		</xsl:if>
		<xsl:if test="(position()=last())">
			<xsl:text>. </xsl:text>
		</xsl:if>
	</xsl:template>


	<xsl:template match="directrixInclination">
		The angle of inclination of the directrix is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>.  </xsl:text>
	</xsl:template>

	<xsl:template match="eccentricity">
		The
		<a href="glossary.html#eccentricity">eccentricity</a>
		is
		<!-- <xsl:choose> <xsl:otherwise> -->
		<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
			approximately
		</xsl:if>
		<xsl:value-of select="format-number(decimalValue,'#.###')" />
		<!-- </xsl:otherwise> </xsl:choose> -->
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="focalLength">
		The
		<a href="glossary.html#focalLength">focal length</a>
		is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="openDirection">
		It opens
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="openDirection" mode="qualifying">
		In other words, the curve opens to the
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="openDirection" mode="parabola">
		The parabola opens
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="reducedEquation">
		The reduced equation is
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>


	<xsl:template match="semiMajorAxis">
		The semimajor axis is half the distance across the ellipse
		along the longest of its axes.
		The length of the
		<a href="glossary.html#semiMajorAxis">semimajor axis</a>
		is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="semiMajorAxis" mode="simple">
		The length of the major axis is
		<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
			approximately
		</xsl:if>
		<xsl:value-of select="format-number(2*decimalValue,'#.###')" />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="semiMinorAxis">
		The semiminor axis is half the distance across the ellipse
		along its shortest principal axis.
		The length of the
		<a href="glossary.html#semiMinorAxis">semiminor axis</a>
		is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
				<xsl:text>. </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="semiMinorAxis" mode="simple">
		The length of the minor axis is
		<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
			approximately
		</xsl:if>
		<xsl:value-of select="format-number(2*decimalValue,'#.###')" />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="majorAxis">
		The
		<a href="glossary.html#majorAxis">major axis</a>
		is given by the line
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="minorAxis">
		The
		<a href="glossary.html#minorAxis">minor axis</a>
		is given by the line
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="majorAxisInclination">
		The
		<a href="glossary.html#majorAxisInclination">major axis inclination</a>
		is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="isApproximation = 'true'">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text> degrees.</xsl:text>
	</xsl:template>

	<xsl:template match="minorAxisInclination">
		The
		<a href="glossary.html#minorAxisInclination">minor axis inclination</a>
		is
		<xsl:choose>
			<xsl:when test="count(rationalValue)=1">
				<xsl:value-of select="rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="isApproximation = 'true'">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text> degrees.</xsl:text>
	</xsl:template>

	<xsl:template name="aOrAn">
		<xsl:choose>
			<xsl:when
				test="starts-with(current(),'a') or starts-with(current(),'e') or
        starts-with(current(),'i') or starts-with(current(),'o') or starts-with(current(),'u')">
				<xsl:text> an </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> a </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="simpleParabolaDescription">
	</xsl:template>

	<xsl:template name="simpleHyperbolaDescription">
		The graph consists of two separate pieces, or branches, that reflect
		each other across a diagonal line of symmetry.
		<!-- <xsl:apply-templates select="vertex"/> The midpoint of the line segment 
			between the vertices is called the center of the hyperbola. -->
		<xsl:apply-templates select="center" />
		<!-- Way out on each branch, a hyperbola is nearly straight and actually 
			approaches a straight line called an asymptote. -->
		<xsl:apply-templates select="asymptotes" />
	</xsl:template>

	<xsl:template name="nullSetAlt">
		The equation has no solution.
	</xsl:template>

	<xsl:template name="allPointsAlt">
		The solution will not be graphed.
	</xsl:template>

	<xsl:template name="singlePointAlt">
		The single point solution is
		<xsl:call-template name="centerNamedTemplateTest">
			<xsl:with-param name="valueOnly">
				true
			</xsl:with-param>
		</xsl:call-template>
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template name="parabolaWidthTest">
		<!--Focal length can be a measure of a parabola's width -->
		<xsl:if test="focalLength/decimalValue = .250">

			The focal length of this parabola is
			<xsl:if test="string-length(substring-after(decimalValue,'.'))  > 3">
				approximately
			</xsl:if>
			<xsl:value-of select="format-number(focalLength/decimalValue,'#.###')" />
			<xsl:text>. </xsl:text>
			This is a good 'reference parabola' to compare other parabolas to.
		</xsl:if>

		<xsl:if test="focalLength/decimalValue != .250">
			and we can compare this parabola to a reference parabola, y=x^2,
			which has a focal length 0.25.

			<xsl:if test="focalLength/decimalValue > .25">
				At
				<!-- <xsl:choose> <xsl:when test="count(focalLength/rationalValue)=1"> 
					<xsl:value-of select="focalLength/rationalValue"/> </xsl:when> <xsl:otherwise> -->
				<xsl:if
					test="string-length(substring-after(focalLength/decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(focalLength/decimalValue,'#.###')" />
				<!-- </xsl:otherwise> </xsl:choose> -->
				this parabola's focal length is about
				<xsl:value-of select="format-number(4 * focalLength/decimalValue,'#.#')" />
				times the focal length of the reference parabola
			</xsl:if>
			<xsl:if test="focalLength/decimalValue &lt; .25">
				At
				<!-- <xsl:choose> <xsl:when test="count(focalLength/rationalValue)=1"> 
					<xsl:value-of select="focalLength/rationalValue"/> </xsl:when> <xsl:otherwise> -->
				<xsl:if
					test="string-length(substring-after(focalLength/decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(focalLength/decimalValue,'#.###')" />
				<!-- </xsl:otherwise> </xsl:choose> -->
				this parabola's focal length is only about
				<xsl:value-of select="format-number(4 * focalLength/decimalValue,'#.#')" />
				times the focal length of the reference parabola
			</xsl:if>
			<xsl:choose>
				<xsl:when test="focalLength/decimalValue &lt;= .125">
					so this parabola's opening is very narrow
				</xsl:when>
				<xsl:when
					test="focalLength/decimalValue &lt; .250 and focalLength/decimalValue > .125 ">
					so this parabola's opening is moderately narrow
				</xsl:when>
				<xsl:when
					test="focalLength/decimalValue > .250 and focalLength/decimalValue &lt; .500">
					so this parabola's opening is moderately wide
				</xsl:when>
				<xsl:when test="focalLength/decimalValue >= .500">
					so this parabola's opening is very wide
				</xsl:when>
			</xsl:choose>
			compared to the reference parabola.
		</xsl:if>
		<!-- What happens to the focal length and parabola width when you change 
			the coefficient of x^2? Enter y=c*x^2, with c=1. Then change c to see what 
			happens to the parabola. -->


	</xsl:template>

	<xsl:template name="ellipseEccentricityTest">
		<!-- This is another quick and dirty solution to meet a deadline. -->
		Ellipses are oval shaped curves. How 'flat' or how rounded the oval is
		depends on the length of the
		<a href="glossary.html#majorAxis">major axis</a>
		compared to the length of the
		<a href="glossary.html#minorAxis">minor axis</a>
		<xsl:text>. </xsl:text>
		The longer the major axis compared to the minor axis, the 'flatter' the
		ellipse.
		Another term for flatness is
		<a href="glossary.html#eccentricity">eccentricity</a>
		<xsl:text>. </xsl:text>
		The major axis of this ellipse with length
		<xsl:value-of select="format-number(2*semiMajorAxis/decimalValue,'#.###')" />
		<xsl:choose>
			<xsl:when test="eccentricity/decimalValue >= .975">
				is approximately
				<xsl:value-of
					select="format-number(semiMajorAxis/decimalValue div semiMinorAxis/decimalValue,'#.##')" />
				times the length of the minor axis with length
				<xsl:value-of select="format-number(2*semiMinorAxis/decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
				This ellipse is very 'flat'.
			</xsl:when>
			<xsl:when
				test=".916 &lt; eccentricity/decimalValue and eccentricity/decimalValue &lt; .975">
				is approximately
				<xsl:value-of
					select="format-number(semiMajorAxis/decimalValue div semiMinorAxis/decimalValue,'#.##')" />
				times the length of the minor axis with length
				<xsl:value-of select="format-number(2*semiMinorAxis/decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
				This ellipse is pretty 'flat'. It's a nice long oval.
			</xsl:when>
			<xsl:when
				test=".747 &lt; eccentricity/decimalValue and eccentricity/decimalValue &lt; .916">
				is approximately
				<xsl:value-of
					select="format-number(semiMajorAxis/decimalValue div semiMinorAxis/decimalValue,'#.##')" />
				times the length of the minor axis with length
				<xsl:value-of select="format-number(2*semiMinorAxis/decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
				This ellipse could be called moderately 'flat'.
			</xsl:when>
			<xsl:when
				test="eccentricity/decimalValue &lt;= .747 and eccentricity/decimalValue > .603">
				is only about
				<xsl:value-of
					select="format-number(semiMajorAxis/decimalValue div semiMinorAxis/decimalValue,'#.##')" />
				times as long as the minor axis with length
				<xsl:value-of select="format-number(2*semiMinorAxis/decimalValue,'#.###')" />
				<xsl:text>. </xsl:text>
				This ellipse is a nice round oval shape.
			</xsl:when>
			<xsl:when test="eccentricity/decimalValue &lt;= .603">
				is only about
				<xsl:value-of
					select="format-number(semiMajorAxis/decimalValue div semiMinorAxis/decimalValue,'#.##')" />
				times as long as the minor axis with length
				<xsl:value-of select="format-number(2*semiMinorAxis/decimalValue,'#.###')" />
				<xsl:text>. This ellipse is not very flat. It is nearly circular.</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="twoLinesTemp">
		The lines are a distance of
		<xsl:choose>
			<xsl:when test="count(separation/rationalValue)=1">
				<xsl:value-of select="separation/rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if
					test="string-length(substring-after(separation/decimalValue,'.'))  > 3">
					approximately
					<xsl:value-of select="format-number(separation/decimalValue,'#.###')" />
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
		units apart. They have an inclination of
		<xsl:choose>
			<xsl:when test="count(inclination/rationalValue)=1">
				<xsl:value-of select="inclination/rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if
					test="string-length(substring-after(inclination/decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of select="format-number(inclination/decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		degrees.
	</xsl:template>

	<xsl:template name="twoIntersectingLinesTemp">
		The lines cross at the point
		<xsl:value-of select="intersectionPoint" />
		and have inclinations of
		<xsl:choose>
			<xsl:when test="count(inclination[position()=1]/rationalValue)=1">
				<xsl:value-of select="inclination[position()=1]/rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if
					test="string-length(substring-after(inclination[position()=1]/decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of
					select="format-number(inclination[position()=1]/decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		degrees and
		<xsl:choose>
			<xsl:when test="count(inclination[position()=2]/rationalValue)=1">
				<xsl:value-of select="inclination[position()=2]/rationalValue" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:if
					test="string-length(substring-after(inclination[position()=2]/decimalValue,'.'))  > 3">
					approximately
				</xsl:if>
				<xsl:value-of
					select="format-number(inclination[position()=2]/decimalValue,'#.###')" />
			</xsl:otherwise>
		</xsl:choose>
		degrees.
	</xsl:template>

	<!-- Templates for functions -->
	<xsl:template match="FunctionAnalysisData">
		<xsl:apply-templates select="degree" />
		<xsl:apply-templates select="intervalDescription" />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="ComputedFunctionData">

		<!-- Take note here for input arrays -->
		<!-- Looks like Alternate Equation as well -->
		<xsl:apply-templates select="NumSegments" />
		<xsl:apply-templates select="FunctionAnalysisData" />
		<xsl:apply-templates select="AlternateEquation" />
	</xsl:template>

	<!-- Default template for CriticalPoint; say nothing -->
	<xsl:template match="CriticalPoint" priority="-9.0" />

	<xsl:template match="intervalDescription">
		<xsl:choose>
			<xsl:when test="position()=1">
				The curve
			</xsl:when>
			<xsl:when test="position()=last()">
				and
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>, </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="direction" />
		<xsl:if test="position()=1">
			from
			<xsl:apply-templates select="left/Type" />
			the
			<xsl:call-template name="graphPoint">
				<xsl:with-param name="point" select="left" />
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="left/discontinuity='true'">
			from
			<xsl:apply-templates select="left/rightY" />
		</xsl:if>
		to
		<xsl:apply-templates select="right/leftY" />
		<xsl:apply-templates select="right/Type" />
		the
		<xsl:call-template name="graphPoint">
			<xsl:with-param name="point" select="right" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="AlternateEquation">
		This function very closely matches a portion of the graph of the
		equation
		<xsl:value-of select="." />
		<xsl:text>. </xsl:text>
		<!-- Try typing in this equation to get a description of its graph. -->
	</xsl:template>

	<xsl:template match="NumSegments">
		<xsl:choose>
			<xsl:when test=".=0">
				No portion of the graph is inside the visible window.
			</xsl:when>
			<xsl:when test=".=1">
				The portion of the graph in the visible window consists of a single
				continuous graph.
			</xsl:when>
			<xsl:otherwise>
				The graph consists of
				<xsl:value-of select="." />
				continuous segments.
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="degree">
		This is the graph of a
		<xsl:choose>
			<xsl:when test=".=3">
				<xsl:text>cubic polynomial</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="." />
				<xsl:text>th degree polynomial</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Templates for polar curves -->
	<xsl:template name="numPetals">
		<xsl:choose>
			<xsl:when test="numPetals=1">
				The graph consists of a circle that touches the origin.
			</xsl:when>
			<xsl:otherwise>
				The graph consists of
				<xsl:value-of select="numPetals" />
				petals located symmetrically about the origin.
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="petalLength">
		<xsl:choose>
			<xsl:when test="../numPetals=1">
				The diameter of the circle is
			</xsl:when>
			<xsl:otherwise>
				The length of each petal is
			</xsl:otherwise>
		</xsl:choose>
		<xsl:call-template name="numberModel">
			<xsl:with-param name="featureName" select="." />
		</xsl:call-template>
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="petalInclinations">
		<xsl:if test="position()=1">
			<xsl:choose>
				<xsl:when test="../numPetals=1">
					The diameter of the circle is oriented at an angle of
				</xsl:when>
				<xsl:otherwise>
					The petals are oriented at angles of
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:apply-templates select="angleInfo" />
	</xsl:template>

	<xsl:template match="petalTips">
		<xsl:choose>
			<xsl:when test="../numPetals=1">
				The diameter of the circle extends from the origin to the point
			</xsl:when>
			<xsl:otherwise>
				The petals are symmetric about lines extending from the origin to
				the points
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="pointInfo" />
		<xsl:text>. </xsl:text>
	</xsl:template>

	<xsl:template match="loopAngles">
		The axes of the
		<xsl:value-of select="graphObject" />
		are oriented at angles of
		<xsl:apply-templates select="angleInfo" />
	</xsl:template>

	<xsl:template match="angleInfo">
		<xsl:choose>
			<xsl:when test="position()=1"></xsl:when>
			<xsl:when test="position()=last()">
				<xsl:text>and</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>, </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="degreeValue" />
		<xsl:text>degrees</xsl:text>
		<xsl:if test="(position()=last())">
			<xsl:text>from the positive X-axis.</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="pointInfo">
		<xsl:choose>
			<xsl:when test="position()=1"></xsl:when>
			<xsl:when test="position()=last()">
				<xsl:text>and</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>, </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="current()" />
	</xsl:template>

	<xsl:template name="numberModel">
		<xsl:param name="featureName" />
		<xsl:param name="level">
			<xsl:text>3</xsl:text>
		</xsl:param>
		<xsl:choose>
			<xsl:when test="$level=3">
				<xsl:choose>
					<xsl:when
						test="count($featureName/quadraticValue)=1 and count($featureName/rationalValue)=0">
						<xsl:value-of select="$featureName/quadraticValue" />
						<xsl:text> or approximately </xsl:text>
						<xsl:value-of select="$featureName/approximateDecimalValue" />
					</xsl:when>
					<xsl:when test="count($featureName/rationalValue)=1">
						<xsl:value-of select="$featureName/rationalValue" />
						<xsl:if test="$featureName/isApproximation='true'">
							<xsl:text> or approximately </xsl:text>
							<xsl:value-of select="$featureName/approximateDecimalValue" />
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="$featureName/isApproximation='true'">
							<xsl:text>approximately</xsl:text>
						</xsl:if>
						<xsl:value-of select="$featureName/approximateDecimalValue" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$level=2">
				<xsl:choose>
					<xsl:when test="count($featureName/quadraticValue)=1">
						<xsl:value-of select="$featureName/quadraticValue" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="$featureName/isApproximation='true'">
							<xsl:text>approximately</xsl:text>
						</xsl:if>
						<xsl:value-of select="$featureName/approximateDecimalValue" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$level=1">
				<xsl:choose>
					<xsl:when test="count($featureName/rationalValue)=1">
						<xsl:value-of select="$featureName/rationalValue" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="$featureName/isApproximation='true'">
							<xsl:text>approximately</xsl:text>
						</xsl:if>
						<xsl:value-of select="$featureName/approximateDecimalValue" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="isApproximation='true'">
					<xsl:text>approximately</xsl:text>
				</xsl:if>
				<xsl:value-of select="featureName/approximateDecimalValue" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="angleModel">
		<xsl:param name="featureName"></xsl:param>
		<xsl:if
			test="string-length(substring-after($featureName/degreeValue,'.'))  > 3">
			<xsl:text>approximately</xsl:text>
		</xsl:if>
		<xsl:value-of select="format-number($featureName/degreeValue,'#.###')" />
		<xsl:text>degrees</xsl:text>
		<xsl:if test="count($featureName/fractionalRadians)=1">
			<xsl:text>or</xsl:text>
			<xsl:value-of select="$featureName/fractionalRadians" />
			<xsl:text>radians</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template name="graphPoint">
		<xsl:param name="point" />
		<xsl:choose>
			<xsl:when test="$point/X='-infinity' and $point/Y='-infinity'">
				<xsl:text>far lower left</xsl:text>
			</xsl:when>
			<xsl:when test="$point/X='-infinity' and $point/Y='infinity'">
				<xsl:text>far upper left</xsl:text>
			</xsl:when>
			<xsl:when test="$point/X='infinity' and $point/Y='-infinity'">
				<xsl:text>far lower right</xsl:text>
			</xsl:when>
			<xsl:when test="$point/X='infinity' and $point/Y='infinity'">
				<xsl:text>far upper right</xsl:text>
			</xsl:when>
			<xsl:when test="$point/X='-infinity'">
				<xsl:text>line y =</xsl:text>
				<xsl:value-of select="$point/Y" />
				<xsl:text>at the far left </xsl:text>
			</xsl:when>
			<xsl:when test="$point/X='infinity'">
				<xsl:text>line y =</xsl:text>
				<xsl:value-of select="$point/Y" />
				<xsl:text>at the far right</xsl:text>
			</xsl:when>
			<xsl:when test="$point/Y='-infinity'">
				<xsl:text>bottom</xsl:text>
			</xsl:when>
			<xsl:when test="$point/Y='infinity'">
				<xsl:text>top</xsl:text>
			</xsl:when>
			<xsl:when
				test="$point/leftY='infinity' or $point/rightY='infinity' or leftY='-infinity' or rightY='-infinity'">
				<xsl:text>line x =</xsl:text>
				<xsl:value-of select="$point/X" />
			</xsl:when>
			<xsl:otherwise>
				point (<xsl:value-of select="$point/X" /><xsl:text>, </xsl:text><xsl:value-of select="$point/Y" />)</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="direction">
		<xsl:if test=".='increases'">
			<xsl:text>rises </xsl:text>
		</xsl:if>
		<xsl:if test=".='decreases'">
			<xsl:text>falls </xsl:text>
		</xsl:if>
		<xsl:if test=".='remains constant'">
			<xsl:text>is nearly flat </xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="Type">
		<xsl:call-template name="aOrAn" />
		<xsl:value-of select="." />
		<xsl:text> at</xsl:text>
	</xsl:template>

	<xsl:template match="leftY">
		<xsl:value-of select="." />
		<xsl:text> at</xsl:text>
	</xsl:template>

	<xsl:template match="rightY">
		<xsl:value-of select="." />
		<xsl:text> </xsl:text>
	</xsl:template>
</xsl:stylesheet>


