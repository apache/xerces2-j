<?xml version='1.0' encoding='US-ASCII'?>
<!-- $Id$ -->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>

 <xsl:template match='schedule'>
  <HTML>
   <HEAD>
    <TITLE>Xerces 2 | Schedule</TITLE>
    <LINK rel='stylesheet' type='text/css' href='site.css'/>
   </HEAD>
   <BODY>
    <SPAN class='netscape'>
    <H1>Xerces 2 Schedule</H1>
    <xsl:for-each select='milestone'>
     <A name='{@id}'/>
     <H2>
      Milestone #<xsl:value-of select='position()'/>
      (<xsl:value-of select='@id'/>)
     </H2>
     <P>
      <xsl:apply-templates select='.'/>
     </P>
    </xsl:for-each>
    </SPAN>
    <HR/>
    <SPAN class='netscape'>
     Last modified: <xsl:value-of select='date'/>
    </SPAN>
   </BODY>
  </HTML>
 </xsl:template>

 <xsl:template match='milestone'>
  <TABLE border='0'>
   <TR>
    <TH>Title:</TH>
    <TD><xsl:value-of select='title'/></TD>
   </TR>
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
   <xsl:if test='goal'>
    <TR>
     <TH>Goals:</TH>
     <TD>
      <xsl:for-each select='goal'>
       <xsl:value-of select='.'/>
       <xsl:if test='not(position()=last())'><BR/></xsl:if>
      </xsl:for-each>
     </TD>
    </TR>
   </xsl:if>
  </TABLE>
 </xsl:template>

</xsl:stylesheet>