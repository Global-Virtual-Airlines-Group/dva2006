<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Security Violation</title>
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
<div class="updateHdr"><content:airline /> SECURITY VIOLATION</div>
<br />
Oops. You attempted to access something that you're not allowed to.<br />
<content:filter roles="Anonymous">
<br />
<c:if test="${isExpired}">
<span class="ita bld">Your user session has expired.</span><br />
<br />
</c:if>
To log in to the <content:airline /> web site, <el:cmd url="login" className="sec bld">Click Here</el:cmd>.<br />
</content:filter>
<content:filter roles="!Anonymous">
<br />
<span class="bld">${servlet_error}</span><br />
</content:filter>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
