<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<c:set var="entryType" value="${isNews ? 'System News' : 'NOTAM'}" scope="request" />
<c:set var="opName" value="${isCreate ? 'created' : 'updated'}" scope="request" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<c:if test="${isNews}">
<title><content:airline /> System News Updated</title>
</c:if>
<c:if test="${isNOTAM}">
<title><content:airline /> NOTAM Updated</title>
</c:if>
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
<div class="updateHdr">${entryType} ${opName}</div>
<br />
<c:if test="${!isDelete}">
This ${entryType} has been successfully ${opName} in the database.<br />
<c:if test="${!empty notifyUsers}">
<fmt:int value="${notifyUsers}" /> <content:airline /> Pilots have been notified via e-mail.<br />
</c:if>
<br />
To view it, please <el:cmd url="${isNews ? 'news' : 'notams'}">Click Here</el:cmd>.<br />
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
