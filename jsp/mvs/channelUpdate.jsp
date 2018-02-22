<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> MVS Configuration Updated</title>
<content:css name="main" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isUpdate}">
<div class="updateHdr">MVS Voice Channel Updated</div>
<br />
The MVS Voice Channel <span class="sec bld">${channel.name}</span> has been successfully updated.<br />
</c:if>
<c:if test="${isDelete}">
<div class="updateHdr">MVS Voice Channel Deleted</div>
<br />
The MVS Voice Channel <span class="sec bld">${channel.name}</span> has been deleted.<br />
</c:if>
<br />
To return to the list of MVS voice channels, <el:cmd url="mvschannels" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
