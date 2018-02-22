<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html lang="en">
<head>
<title><content:airline /> User Switched</title>
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
<div class="updateHdr">Switched to ${user.name}</div>
<br />
You have switched user credentials to ${user.name}<c:if test="${!empty user.pilotCode}"> (${user.pilotCode})</c:if>. You will continue to operate with these credentials until you log out, at which point you will revert to your proper
credentials.<br />
<br />
<span class="bld">Please be aware that the system is incapable of distinguishing between you and the user you are impersonating - <span class="sec">${user.name}!</span></span><br />
<br />
To navigate to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br /> 
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
