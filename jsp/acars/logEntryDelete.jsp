<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>ACARS Log Entry Removal</title>
<content:css name="main" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${infoDelete}">
<div class="updateHdr">ACARS Flight Log Entry Removed</div>
<br />
The ACARS log entry for Flight ID <fmt:int value="${info.ID}" /> has been successfully removed from 
the database. All Position reports for this Flight have also been removed.<br />
</c:if>
<c:if test="${pirep}">
<div class="updateHdr">Cannot remove ACARS Flight Log Entry</div>
<br />
This ACARS Flight has an associated Flight Report, and therefore cannot be removed from the ACARS 
log database.<br />
<br />
To view this Flight Report, <el:cmd url="pirep" link="${pirep}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${conDelete}">
<div class="updateHdr">ACARS Connection Log Entry Removed</div>
<c:if test="${fn:sizeof(deletedIDs) > 0}">
<br />
The ACARS Connection log entries for Connections <fmt:list value="${deletedIDs}" delim=", " /> have 
been successfully removed from the database. All Text Messages for these Connections have also been 
removed.<br />
</c:if>
<c:if test="${fn:sizeof(skippedIDs) > 0}">
The ACARS Connection log entries for Connections <fmt:list value="${skippedIDs}" delim=", " /> have 
associated Flight Information entries and therefore cannot be removed from the ACARS log database. 
The Flight Information log entries must be removed first.<br />
</c:if>
</c:if>
<c:if test="${!empty info && !infoDelete}">
To view this flight's ACARS log entry, <el:cmd url="acarsinfo" link="${info}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${errorDelete}">
<div class="updateHdr">ACARS Client Error Log Entry Removed</div>
<br />
The ACARS Client Error log entry has been successfully removed from the database.<br />
<br />
To return to the list of ACARS Client Error Log entries, <el:cmd url="acarserrors" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
