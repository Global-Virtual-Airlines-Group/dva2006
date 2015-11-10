<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Welcome Message Sent</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Welcome Message Sent</div>
<br />
The <content:airline /> welcome e-mail has been re-sent to ${applicant.name} at ${applicant.email}. 
<c:if test="${!empty questionnaire && fn:passed(questionnaire)}">${applicant.name} has already 
completed the Initial Questionnaire.</c:if><br />
<c:if test="${passwordUpdated}">
<br />
Since ${applicant.name} has already been hired as a <content:airline /> pilot, the welcome message has 
been resent, and his or her password has been reset.<br />
</c:if>
<br />
To review this applicant's profile, <el:cmd url="applicant" link="${applicant}" className="sec bld">Click Here</el:cmd>.<br />
To return to the Applicant queue, <el:cmd url="applicants" className="bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
