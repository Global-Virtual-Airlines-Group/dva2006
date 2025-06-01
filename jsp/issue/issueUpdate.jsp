<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Issue Updated</title>
<content:css name="main" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="refresh" content="3;url=/issue.do?id=${issue.hexID}" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Issue Updated</div>
<br />
<c:choose>
<c:when test="${isCreated}">
The new Issue <span class="pri bld">${issue.subject}</span> has been created.<br />
<c:if test="${emailSent}">
<br />
An e-mail notification has been sent to ${assignee.name} at ${assignee.email} informing him or her of this new Issue.<br />
</c:if>
</c:when>
<c:when test="${isComment}">
Your new comment on the issue <span class="pri bld">${issue.subject}</span> has been posted.<br />
</c:when>
<c:otherwise>
The Issue <span class="pri bld">${issue.subject}</span> has been modified.<br />
</c:otherwise>
</c:choose>
<br />
The Issue will automatically be displayed within 3 seconds. If your browser does not return to the Issue, you can <el:cmd className="sec bld" url="issue" link="${issue}" op="read">Click Here</el:cmd> to display the Issue.<br />
<br />
To view all open Issues, <el:cmd className="sec bld" url="issues" op="Open">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
