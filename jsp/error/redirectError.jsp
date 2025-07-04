<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" isErrorPage="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Redirection Error</title>
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
<div class="updateHdr">REDIRECTION ERROR</div>
<br />
Oops. The server lost track of where you were supposed to go from ${referer}.<br />
<br />
<el:link url="/" className="sec bld">Click Here</el:link> to return to the <content:airline /> home page.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
