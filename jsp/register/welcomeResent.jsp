<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Welcome Message Sent</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
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
</body>
</html>
