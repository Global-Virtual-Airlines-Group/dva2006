<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Equipment Type Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Equipment Program Updated</div>
<br />
The Equipment Profile for the <span class="pri bld">${eqType.name}</span> program has been successfully
updated in the database.<br />
<br />
<c:if test="${isRename}">
This Equipment Profile has been renamed. It was formerly called the <span class="sec bld">${oldName}</span>
program, and all Pilots in this program have been updated.<br />
<br />
</c:if>
<content:copyright />
</div>
</body>
</html>
