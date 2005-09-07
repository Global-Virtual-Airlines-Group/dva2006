<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Welcome Message Sent</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Welcome Message Sent</div>
<br />
The <content:airline /> welcome e-mail has been re-sent to ${applicant.name} at ${applicant.email}.<br />
<br />
To review this applicant's profile, <el:cmd url="applicant" linkID="0x${applicant.ID}">click here</el:cmd>.<br />
To return to the Applicant queue, <el:cmd url="applicants">click here</el:cmd>.<br />
<br />
<content:copyright />
</div>
</body>
</html>
