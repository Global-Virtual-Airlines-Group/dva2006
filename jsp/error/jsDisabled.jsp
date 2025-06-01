<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html lang="en">
<head>
<title><content:airline /> JavaScript Disabled Error</title>
<content:css name="main" />
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
<div class="updateHdr"><content:airline /> JAVASCRIPT DISABLED</div>
<br />
You appear to have disabled JavaScript execution in your web browser. You must have JavaScript enabled in order to visit the <content:airline /> web site. 
We have an extensive number of dynamic web features to enhance your experience here which require JavaScript to operate correctly.<br />
<br />
Please <el:cmd url="login" className="sec bld">Click Here</el:cmd> to return to the login page.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
