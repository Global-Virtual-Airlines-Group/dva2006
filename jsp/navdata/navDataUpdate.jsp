<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Navigation Database Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isImport}">
<div class="updateHdr">Navigation Data Imported</div>
<br />
<fmt:int value="${entryCount}" /> records loaded. <fmt:int value="${regionCount}" /> records have had the proper 
ICAO region code set after import.<br />
<br />
<c:choose>
<c:when test="${navData}">
Navigation aid data from the latest PSS AIRAC Navigation Data cycle has been imported into the <content:airline /> 
Navigation Database. This data will be available for all pilots.<br />
</c:when>
<c:when test="${airway}">
Airway data from the latest PSS AIRAC Navigation Data cycle has been imported into the <content:airline /> 
Navigation Database. This data will be available for all pilots.<c:if test="${doPurge}"> <span class="bld">The Airway 
table was purged prior to the import.</span></c:if><br />
</c:when>
<c:when test="${terminalRoute}">
SID/STAR data from the latest PSS AIRAC Navigation Data cycle has been imported into the <content:airline /> 
Navigation Database. This data will be available for all pilots.<c:if test="${doPurge}"> <span class="bld">The SID/STAR 
table was purged prior to the import.</span></c:if><br />
</c:when>
</c:choose>
<c:if test="${!empty errors}">
<br />
The following errors occurred during the import of this AIRAC data file:<br />
<br />
<div class="small">
<c:forEach var="error" items="${errors}">
${error}<br />
</c:forEach>
</div>
</c:if>
</c:if>
<c:if test="${isPurge}">
<div class="updateHdr">Navigation Data Purged</div>
<br />
The <content:airline /> Navigation Database has been purged. <fmt:int value="${rowsDeleted}" /> entries have 
been deleted from the database. You may now import a new AIRAC cycle's data.<br />
</c:if>
<br />
To return to the AIRAC Navigation Data import page, <el:cmd url="navimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the AIRAC Airway import page, <el:cmd url="awyimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the AIRAC Terminal Route import page, <el:cmd url="trouteimport" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
