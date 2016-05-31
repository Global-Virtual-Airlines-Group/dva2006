<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Virtual Airline Profile Updated</title>
<content:css name="main" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:copyright visible="false" />
</head>
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Virtual Airline Profile Updated</div>
<br />
The virtual airline profile for <span class="pri bld">${aInfo.name}</span> has been updated successfully.<br />
<br />
To return to the list of Virtual Airline profiles, <el:cmd url="aInfoList" className="sec bld">Click Here</el:cmd>.<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
