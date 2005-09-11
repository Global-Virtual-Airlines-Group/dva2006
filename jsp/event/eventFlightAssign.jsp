<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event Flight Assignments Created</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/event/header.jsp" %> 
<%@ include file="/jsp/event/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Online Event Flight Assignments Created</div>
<br />
The flight assignments for the <span class="pri bld">${event.name}</span> Online Event have been successfully created.<br />
<br />
To return to the event page you can <el:cmd className="sec bld" url="event" linkID="0x${event.ID}">click here</el:cmd>.<br />
<br />
<content:copyright />
</div>
</body>
</html>