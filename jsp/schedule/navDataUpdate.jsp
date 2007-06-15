<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
<fmt:int value="${entryCount}" /> records loaded.<br />
<br />
The latest AIRAC Navigation Data cycle has been imported into the <content:airline /> Navigation Database. This
data will be available for all pilots.<br />
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
To return to the AIRAC Navigation Data import page, <el:cmd url="navimport">click here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
