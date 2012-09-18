<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Flight Plan Saved</title>
<content:css name="main" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Online Event Flight Plan Saved</div>
<br />
The ${flightPlan.typeName} format Flight Plan for the <span class="pri bld">${event.name}</span> 
Online Event has been successfully updated, and will now be available for download by <content:airline /> 
pilots from the Online Event page.<br />
<br />
To view this Online Event, <el:cmd url="event" link="${event}" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
