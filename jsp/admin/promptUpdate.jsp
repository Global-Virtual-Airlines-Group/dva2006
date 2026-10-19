<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>LLM Prompt Updated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:js name="common" />
<content:copyright visible="false" />
<content:cspHeader />
</head>
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">LLM Prompt Updated</div>
<br>
The LLM Prompt <span class="pri bld">${prompt.ID}</span> has been updated successfully.<br>
<br>
To return to the list of LLM prompts, <el:cmd url="prompts" className="sec bld">Click Here</el:cmd>.<br>
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br>
<br>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
