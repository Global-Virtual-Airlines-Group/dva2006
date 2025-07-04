<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Location Updated</title>
<content:css name="main" />
<content:css name="form" />
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
<c:if test="${!isDelete}">
<div class="updateHdr">Pilot Location Updated</div>
<br />
The geographical location for ${user.name} has been updated. For privacy reasons, when this location is displayed on the Pilot Location Board, a random adjustment within a 3 mile circle will be applied 
to hide your true location.<br />
<c:if test="${!empty geoCode}">
<br />
Your home town has been set to <b>${geoCode.cityState}</b> in your Pilot profile.<br /> 
</c:if>
</c:if>
<c:if test="${isDelete}">
<div class="updateHdr">Pilot Location Removed</div>
<br />
The geographical location for ${user.name} has been removed from the database, and will no longer appear on the Pilot Location Board.<br />
</c:if>
<br />
To view the <content:airline /> Pilot Location Board, please <el:cmd url="pilotboard" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
