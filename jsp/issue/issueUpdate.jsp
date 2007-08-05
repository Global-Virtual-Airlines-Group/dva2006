<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Issue Updated</title>
<content:pics />
<content:css name="main" browserSpecific="true" />
<meta http-equiv="refresh" content="3;url=/issue.do?id=0x<fmt:hex value="${issue.ID}" />&amp;op=read" />
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
The new Issue <span class="pri bld">&quot;${issue.subject}&quot;</span> has been created.<br />
<c:if test="${emailSent}">
<br />
An e-mail notification has been sent to ${assignee.name} at ${assignee.email} informing him or her of this
new Issue.<br />
</c:if>
</c:when>
<c:when test="${isComment}">
Your new comment on the issue <span class="pri bld">&quot;${issue.subject}&quot;</span> has been posted.<br />
</c:when>
<c:otherwise>
The Issue <span class="pri bld">&quot;${issue.subject}&quot;</span> has been modified.<br />
</c:otherwise>
</c:choose>
<br />
The Issue will automatically be displayed within 3 seconds. If your browser does not return to the Issue 
or you are impatient, you can <el:cmd className="sec bld" url="issue" link="${issue}" op="read">click here</el:cmd>
to display the Issue.<br />
<br />
To view all open Issues, <el:cmd className="sec bld" url="issues" op="Open">Click here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
