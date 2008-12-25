<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : tohtml.xsl
    Created on : Septmber 4, 2006, 23:45
    Author     : maks
    Description:
        For transformation NET-Simulator projects into html reports.
-->
       
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="generatingDate" select="''" />
    <xsl:param name="projectFile" select="''" />
    <xsl:param name="image" select="''" />
    

    <xsl:template match="project">
        <html>
            <head>
                <title>NET-Simulator Project Report</title>
            </head>
            <body>
                <h1>NET-Simulator Project Report</h1>
                <strong>Project file:</strong><xsl:text> </xsl:text><xsl:value-of select="$projectFile" /><br></br>
                <strong>Author:</strong><xsl:text> </xsl:text><xsl:value-of select="@author" /><br></br>
                <strong>Description:</strong><xsl:text> </xsl:text><xsl:value-of select="@description" /><br></br>
                <strong>Project created at:</strong><xsl:text> </xsl:text><xsl:value-of select="@createDate" /><br></br>
                <strong>Report generated at:</strong><xsl:text> </xsl:text><xsl:value-of select="$generatingDate" /><br></br>
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="$image" /></xsl:attribute></img><br></br>
                <xsl:apply-templates select="*" />
            </body> 
        </html>
    </xsl:template>

    
    
    <xsl:template match="routerShape">
        <h2>Router</h2>
        <table cellpadding="1" cellspacing="0" border="1" width="600">
            <tr>
                <th width="20%" align="left">Name:</th>
                <td><xsl:value-of select="@name" /></td>
            </tr>
            <tr>
                <th width="20%" align="left">Description:</th>
                <td>
                    <xsl:choose>
                        <xsl:when test="@description = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@description" /></xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
        <xsl:apply-templates select="IP4Router" />
    </xsl:template>


        
    <xsl:template match="desktopShape">
        <h2>Desktop</h2>
        <table cellpadding="1" cellspacing="0" border="1" width="600">
            <tr>
                <th width="20%" align="left">Name:</th>
                <td><xsl:value-of select="@name" /></td>
            </tr>
            <tr>
                <th width="20%" align="left">Description:</th>
                <td>
                    <xsl:choose>
                        <xsl:when test="@description = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@description" /></xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
        <xsl:apply-templates select="IP4Router" />
    </xsl:template>
    
    
    
    <xsl:template match="switchShape">
        <h2>Switch</h2>
        <table cellpadding="1" cellspacing="0" border="1" width="600">
            <tr>
                <th width="20%" align="left">Name:</th>
                <td><xsl:value-of select="@name" /></td>
            </tr>
            <tr>
                <th width="20%" align="left">Description:</th>
                <td>
                    <xsl:choose>
                        <xsl:when test="@description = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@description" /></xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template >

    
    
    <xsl:template match="hubShape">
        <h2>Hub</h2>
        <table cellpadding="1" cellspacing="0" border="1" width="600">
            <tr>
                <th width="20%" align="left">Name:</th>
                <td><xsl:value-of select="@name" /></td>
            </tr>
            <tr>
                <th width="20%" align="left">Description:</th>
                <td>
                    <xsl:choose>
                        <xsl:when test="@description = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@description" /></xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template >
    
    

    <xsl:template match="IP4Router">
        <h3>Interfaces:</h3>
        <table cellpadding="1" cellspacing="0" border="1" width="600">
        <tr>
            <th>Name</th>
            <th>Status</th>
            <th>IP Address</th>
            <th>Netmask</th>
            <th>Broadcast</th>
        </tr>
        <xsl:for-each select="eth">
            <tr>
                <td><xsl:value-of select="@name" /></td>
                <td>
                    <xsl:choose>
                        <xsl:when test="@status = 0">DOWN</xsl:when>
                        <xsl:when test="@status = 1">UP</xsl:when>
                        <xsl:otherwise>Error!</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="@ip4 = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@ip4" /></xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="@ip4mask = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@ip4mask" /></xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="@ip4bcast = '' ">&#160;</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@ip4bcast" /></xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each>
        </table>
    
        <h3>Routing table:</h3>
        <table cellpadding="1" cellspacing="0" border="1" width="600">
        <tr>
            <th>Target</th>
            <th>Netmask</th>
            <th>Gateway</th>
            <th>Metric</th>
            <th>Interface</th>            
        </tr>
        <xsl:for-each select="routingTable/row">
            <tr>
                <td><xsl:value-of select="@target" /></td>
                <td><xsl:value-of select="@netmask" /></td>
                <td>
                    <xsl:choose>
                        <xsl:when test="@gateway = '' ">*</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@gateway" /></xsl:otherwise>
                    </xsl:choose>
                </td>
                <td align="right"><xsl:value-of select="@metric" /></td>
                <td><xsl:value-of select="@iface" /></td>
            </tr>
        </xsl:for-each>
        </table>
    </xsl:template>

    

</xsl:stylesheet>
