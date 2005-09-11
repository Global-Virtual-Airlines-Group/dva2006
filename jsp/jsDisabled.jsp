<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> JavaScript Disabled Error</title>
<content:css name="main" browserSpecific="true" force="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr"><content:airline /> JAVASCRIPT DISABLED</div>
<br />
You appear to have disabled JavaScript execution in your web browser. You must have JavaScript enabled in 
order to visit the <content:airline /> web site. We have an extensive number of dynamic web features to enhance
your experience here, which require JavaScript to operate correctly.<br />
<br />
Please <el:cmd url="login" className="sec bld">click here</el:cmd> to return to the login page.<br />
<br />
<content:copyright />
</div>
</body>
</html>
