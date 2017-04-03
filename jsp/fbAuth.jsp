<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Facebook Authorization Completed</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<script type="text/javascript">
golgotha = {local:{}};
golgotha.local.closeRefresh = function() {
	if (window.opener) window.opener.location.reload();
	window.close();
	return true;	
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${fbAuth}">
<div class="updateHdr">Facebook Authorization Complete</div>
<br />
The Facebook authorization has completed successfully. <content:airline /> can now publish news
about your career with us to your Facebook news feed, and use Facebook to validate your e-mail address.<br />
<c:if test="${fbPageAuth}">
<br />
You are a <content:airline /> Facebook Page Administrator. You have been given access to publish content
to <content:airline />'s Facebook page, and your credentials may be used by the application server to publish
content on behalf of <content:airline />.<br />
</c:if>
</c:when>
<c:when test="${fbDeauth}">
<div class="updateHdr">Facebook Credentials Removed</div>
<br />
Your Facebook credentials have been removed from the database. <content:airline /> will no longer attempt to
publish data to your Facebook news feed, or use Facebook to validate your e-mail address.<br />
</c:when>
</c:choose>
<br />
<el:link url="javascript:void golgotha.local.closeRefresh()" className="sec bld">Click Here</el:link> to close this window.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
