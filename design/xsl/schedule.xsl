<?xml version='1.0' encoding='US-ASCII'?>
<!-- $Id$ -->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>

 <xsl:template match='schedule'>
  <HTML>
   <HEAD>
    <TITLE>Xerces 2 | Schedule</TITLE>
    <LINK rel='stylesheet' type='text/css' href='css/site.css'/>
   </HEAD>
   <BODY>
    <SPAN class='netscape'>
    <H1>Xerces 2 Schedule</H1>
    <H2>Milestones</H2>
    <xsl:for-each select='milestone'>
     <xsl:apply-templates select='.'/>
    </xsl:for-each>
    </SPAN>
    <HR/>
    <SPAN class='netscape'>
     Last modified: <xsl:value-of select='@date'/>
    </SPAN>
   </BODY>
  </HTML>
 </xsl:template>

 <xsl:template match='milestone'>
  <A name='{@id}'/>
  <H3>
   <xsl:value-of select='title'/>
   (<xsl:value-of select='@id'/>)
  </H3>
  <P>
   <TABLE border='0'>
    <xsl:if test='@date'>
     <TR>
      <TH>Date:</TH>
      <TD><xsl:value-of select='@date'/></TD>
     </TR>
    </xsl:if>
    <xsl:if test='depends'>
     <TR>
      <TH>Depends:</TH>
      <TD>
       <xsl:for-each select='depends'>
        <A href='#{@idref}'><xsl:value-of select='@idref'/></A>
        <xsl:if test='not(position()=last())'>, </xsl:if>
       </xsl:for-each>
      </TD>
     </TR>
    </xsl:if>
    <xsl:for-each select='goal'>
     <TR>
      <TH>Goal:</TH>
      <TD>
       <xsl:value-of select='.'/>
       <xsl:if test='@owner'>
	<BR/>
        <STRONG>Owner:</STRONG> 
	<xsl:choose>
	 <xsl:when test='id(@owner)/@email'>
	  <A href='mailto:{id(@owner)/@email}'><xsl:value-of select='id(@owner)'/></A>
	 </xsl:when>
	 <xsl:otherwise>
	  <xsl:value-of select='@owner'/>
	 </xsl:otherwise>
	</xsl:choose>
       </xsl:if>
      </TD>
     </TR>
    </xsl:for-each>
   </TABLE>
  </P>
 </xsl:template>

</xsl:stylesheet>