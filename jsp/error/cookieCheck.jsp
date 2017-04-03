<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Cookies Disabled</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="domain" name="airline.domain" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Cookies Disabled</div>
<br />
Oops. It looks like your browser cannot accept cookies from our site. Please go into your browser's preferences, and
ensure that you can accept cookies from <span class="sec bld">${domain}</span>.<br />
<br />
<el:cmd url="login" className="sec bld">Click Here</el:cmd> to return to the login page.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
