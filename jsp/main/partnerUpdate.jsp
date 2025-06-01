<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Partner Updated</title>
<content:css name="main" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isCreated}">
<div class="updateHdr">Partner Information Created</div>
<br />
An entry for <content:airline /> partner <span class="pri bld">${partner.name}</span> has been created in the database and will appear in the list of Virtual Airline partners.<br />
</c:when>
<c:when test="${isDeleted}">
<div class="updateHdr">Partner Information Deleted</div>
<br />
Information for <span class="pri bld">${partner.name}</span> has been deleted from the database.<br />
</c:when>
<c:otherwise>
<div class="updateHdr">Partner Information Updated</div>
<br />
The entry for <content:airline /> partner <span class="pri bld">${partner.name}</span> has been updated in the database.<br />
</c:otherwise>
</c:choose>
<br />
To return to the list of <content:airline /> Partners, <el:cmd url="partners" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
