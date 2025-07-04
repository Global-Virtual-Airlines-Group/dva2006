<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Routes Updated</title>
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
<c:choose>
<c:when test="${isImport}">
<div class="updateHdr">Preferred Routes Imported</div>
<br />
The Federal Aviation Administration Preferred Routes database (in CSV format) has been successfully imported into the database. <fmt:int value="${routeCount}" /> Preferred Routes have been successfully
added to the database.<br />
<c:if test="${!empty warnings}">
<br />
<span class="warn bld">The following Errors and Warnings occured during the import process:</span><br />
<c:forEach var="warning" items="${warnings}">
${warning}<br />
</c:forEach>
</c:if>
</c:when>
<c:when test="${isDelete}">
<div class="updateHdr">Oceanic Route Deleted</div>
<br />
The Oceanic Route was successfully deleted from the database.<br />
</c:when>
<c:when test="${purgeOceanic}">
<div class="updateHdr">Oceanic Routes Purged</div>
<br />
<fmt:int value="${rowsDeleted}" /> Oceanic Routes have been purged from the database.<br />
</c:when>
</c:choose>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
