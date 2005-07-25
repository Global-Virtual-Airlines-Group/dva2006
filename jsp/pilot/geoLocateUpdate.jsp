<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Location Updated</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Pilot Location Updated</div>
<br />
The geographical location for ${user.name} has been updated. For privacy reasons, when this location
is displayed on the Pilot Location Board, a random adjustment of +/- 1 mile will be applied to hide
your true location.<br />
<br />
To view the Pilot Location Board, please <el:cmd url="pilotboard">Click here</el:cmd>.<br />
<br />
<content:copyright />
</div>
</body>
</html>
