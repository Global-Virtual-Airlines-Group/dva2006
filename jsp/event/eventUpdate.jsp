<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event Updated</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/event/header.jsp" %> 
<%@include file="/jsp/event/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Online Event Updated</div>
<br />
The <span class="pri bld">${event.name}</span> Online has been successfully updated.<br />
<br />


</div>
</body>
</html>
