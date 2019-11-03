<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> IP GeoLocation Data Updated</title>
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
<c:if test="${isBlock}">
<div class="updateHdr">IP Network Block Data Imported</div>
<br />
The <content:airline /> IP Network Block database has been updated. <fmt:int value="${entryCount}" /> network block entries have been imported.<br />
</c:if>
<c:if test="${isLocation}">
<div class="updateHdr">IP Network Location Data Imported</div>
<br />
The <content:airline /> IP Network Location database has been updated. <fmt:int value="${locationCount}" /> network location entries have been imported.<br />
</c:if>
<c:if test="${!empty msgs}">
<br />
The following import warning/error messages were logged:<br />
<c:forEach var="msg" items="${msgs}">
<br />${msg}</c:forEach>
</c:if>
<br />
To return to the IP Network Block import page, <el:cmd url="ipimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the IP Network Location import page, <el:cmd url="ipgeoimport" className="sec bld">Click Here</el:cmd>.<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
