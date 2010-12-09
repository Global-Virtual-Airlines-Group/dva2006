<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Facebook Authorization Completed</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<script type="text/javascript">
function closeRefresh()
{
if (window.opener)
	window.opener.location.reload();

window.close();
return true;	
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Facebook Authorization Complete</div>
<br />
The Facebook authorization has completed successfully. <content:airline /> can now publish news
about your career with us to your Facebook news feed, and use Facebook to validate your e-mail address.<br />
<br />
<c:if test="${!empty fbToken}">
The token is ${fbToken}<br />
<br />
</c:if>
<el:link url="javascript:void closeRefresh()" className="sec bld">Click Here</el:link> to close this window.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
