<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Flight Tour Updated</title>
<content:pics />
<content:favicon />
<content:css name="main" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isNew}">
<div class="updateHdr">New <content:airline /> Flight Tour Saved</div>
<br />
A new <content:airline /> Flight Tour <span class="pri bld">${tour.name}</span> has been successfully saved in the database.<br />
</c:when>
<c:when test="${isDelete}">
<div class="updateHdr">Flight Tour Deleted</div>
The <content:airline /> Flight Tour <span class="pri bld">${tour.name}</span> has been successfully deleted from the database.<br />
</c:when>
<c:otherwise>
<div class="updateHdr">Flight Tour Updated</div>
<br />
The <content:airline /> Flight Tour <span class="pri bld">${tour.name}</span> has been successfully updated in the database.<br />
</c:otherwise>
</c:choose>
<br />
To modify this Flight Tour, <el:cmd url="tour" link="${tour}" op="edit" className="sec bld">Click Here</el:cmd>.<br />
To return to the list of Flight Tours, <el:cmd url="tours" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
