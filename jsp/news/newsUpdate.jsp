<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<c:set var="entryType" value="${isNews ? 'System News' : 'NOTAM'}" scope="page" />
<c:set var="opName" value="${isCreate ? 'created' : 'updated'}" scope="page" />
<html lang="en">
<head>
<title><content:airline /> ${entryType} Updated</title>
<content:css name="main" />
<content:js name="common" />
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
<div class="updateHdr">${entryType} ${opName}</div>
<br />
<c:if test="${!isDelete}">
This ${entryType} has been successfully ${opName} in the database.<br />
<c:if test="${!empty notifyUsers}">
<fmt:int value="${notifyUsers}" />&nbsp;<content:airline /> Pilots have been notified via e-mail.<br /></c:if>
<br />
To view it, please <el:cmd className="sec bld" url="${isNews ? 'news' : 'notams'}">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isDelete}">
This ${entryType} has been successfully removed from the database.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
