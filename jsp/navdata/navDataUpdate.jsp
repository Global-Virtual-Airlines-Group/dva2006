<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Navigation Database Updated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
<c:if test="${!empty navCycleID}">Navigation Data Cycle <span class="pri bld">${navCycleID}</span> Imported<br />
<br /></c:if>
<c:if test="${purgeCount > 0}"><fmt:int value="${purgeCount}" />&nbsp;${navaidType.name} records purged. </c:if><fmt:int value="${entryCount}" /> 
<c:if test="${legacyCount > 0}"> current and <fmt:int value="${legacyCount}" /> legacy</c:if>&nbsp;${navaidType.name} records loaded. <fmt:int value="${regionCount}" /> records have had the proper ICAO region code set after import.<br />
<br />
<c:choose>
<c:when test="${navData}">
Navigation aid data from the latest PSS AIRAC Navigation Data cycle has been imported into the <content:airline /> Navigation Database. This data will be available for all pilots.<br />
</c:when>
<c:when test="${airway}">
Airway data from the latest PSS AIRAC Navigation Data cycle has been imported into the <content:airline /> Navigation Database. This data will be available for all pilots.<c:if test="${doPurge}"> <span class="bld">The Airway 
data was purged prior to the import.</span></c:if><br />
</c:when>
<c:when test="${terminalRoute}">
SID/STAR data from the latest PSS AIRAC Navigation Data cycle has been imported into the <content:airline />  Navigation Database. This data will be available for all pilots.<c:if test="${doPurge}"> <span class="bld">The SID/STAR 
data was purged prior to the import.</span></c:if><br />
</c:when>
<c:when test="${airspaceData}">
Airspace boundary data has been imported into the <content:airline /> Navigation Database. This data will be avialable for all pilots.<br />
</c:when>
</c:choose>
<c:if test="${!empty errors}">
<br />
The following errors occurred during the import of this AIRAC data file:<br />
<br />
<span class="small"><c:forEach var="error" items="${errors}">
${error}<br />
</c:forEach></span>
</c:if>
</c:if>
<br />
To return to the AIRAC Navigation Data import page, <el:cmd url="navimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the AIRAC Airway import page, <el:cmd url="awyimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the AIRAC Terminal Route import page, <el:cmd url="trouteimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the Airspace Data import page, <el:cmd url="airspaceimport" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
