<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Dispatch Route SID / STAR Update</title>
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
<div class="updateHdr">Dispatch Routes Updated</div>
<br />
<c:if test="${updateCount == 0}">
No <content:airline /> ACARS Dispatch Routes have had their SID or STAR updated.<br /></c:if>
<c:if test="${updateCount > 0}">
<fmt:int value="${updateCount}" />&nbsp;<content:airline /> ACARS Dispatch Routes had their SID or STAR updated. The following changes were made:<br />
<br />
<c:forEach var="msg" items="${msgs}">
${msg}<br />
</c:forEach>
</c:if>
<hr />
<br />
<c:if test="${isPreview}"><span class="pri bld">This is a preview.</span> To save these changes to the database, <el:cmd url="dsptrouteupdate" op="save" className="sec bld">Click Here</el:cmd>.<br /></c:if>
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
