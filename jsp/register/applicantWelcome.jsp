<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Application Submitted</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="ourDomain" name="airline.domain" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Welcome to <content:airline /></div>
<br />
${applicant.firstName}, on behalf of <content:airline /> welcome to our virtual aviation community! We're delighted that you are interested in joining one of the oldest, largest and most successful virtual airlines on the Internet.<br />
<br />
Unlike most virtual airlines that track seniority simply by hours flown or transferred from other virtual airlines, <content:airline /> grants promotions and type ratings based on pilot knowledge and skill handling his or her aircraft. 
Because of our philosophy, <span class="bld">you will be placed in the aircraft stage or program that best matches your skills</span>.<br />
<br />
To help us determine this, you'll be asked to take a short <fmt:int value="${questionnaire.size}" />-question questionnaire. It should take just a few minutes to complete, and based upon the results we will be able to place you in 
the right program for you.<br />
<br />
In order to complete the membership process at <content:airline />, we must have a valid e-mail address on file for you. An e-mail message has been sent to you with a URL to click to validate your e-mail address. 
<span class="error bld">It is critical that your e-mail provider is able to receive e-mail messages from the domain  <span class="ita">${ourDomain}</span> without any filtering. If you do not respond to the automated e-mail verification 
message, your application to <content:airline /> cannot be approved!</span><br />
<br />
<c:if test="${!empty questionnaire}">
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="questionnaire" link="${questionnaire}" label="INITIAL QUESTIONNAIRE" /></td>
</tr>
</el:table>
<br />
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
