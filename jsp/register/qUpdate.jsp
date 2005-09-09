<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Questionnaire Submitted</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="infoEmail" name="airline.mail.hr" />

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Questionnaire Submitted</div>
<br />
<c:if test="${isSubmit}">
Thank you for completing the initial membership questionnaire at <content:airline />! This is an
important stage in the membership process here. Based on the results of your questionnaire, we'll be
able to find the optimum aircraft program here at <content:airline />, that best matches your skills
and experience as a virtual airline pilot.<br />
<c:if test="${!empty addrValid}">
<br />
<span class="sec bld">You do not appear to have validated your e-mail address yet.</span> In order to
complete the membership process at <content:airline />, we must have a valid e-mail address on file
for you. When you first registered, an e-mail message should have been sent to you with a URL to click
to validate your address. If you have not received this e-mail message, please feel free to contact
us at <a href="mailto:${infoEmail}" class="bld">${infoEmail}</a>.<br />
</c:if>
<br />
Your questionnaire will be reviewed within the next 48 to 96 hours, and we will contact you again at
that time.<br />
</c:if>
<c:if test="${isScore}">
The ${questionnaire.name} for ${applicant.name} has been scored. <fmt:int value="${questionnaire.score}" /> out 
of <fmt:int value="${questionnaire.size}" /> questions were answered correctly.<br />
<c:if test="${!empty addrValid}">
<br />
The e-mail address for ${applicant.name} has not yet been validated. Until this occurs, the application 
should not be approved. The validation message may be resent from the Applicant profile.<br />
</c:if>
<br />
To return to the Applicant questionnaire queue, <el:cmd url="questionnaires" className="sec bld">click here</el:cmd>.<br />
To review this Applicant's profile, <el:cmd url="applicant" linkID="0x${applicant.ID}" className="sec bld">click here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</div>
</body>
</html>
