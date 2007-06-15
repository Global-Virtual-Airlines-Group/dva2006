<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Applicant Questionnaires</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view" pad="default" space="default">
<!-- Table Header Bar -->
<tr class="title">
 <td width="10%">&nbsp;</td>
 <td width="20%">APPLICANT NAME</td>
 <td width="20%">E-MAIL ADDRESS</td>
 <td width="10%">APPLIED ON</td>
 <td width="10%">SUBMITTED ON</td>
 <td>REMOTE HOST NAME</td>
</tr>

<!-- Table Questionnaire Data -->
<c:forEach var="exam" items="${examQueue}">
<c:set var="applicant" value="${applicants[exam.pilotID]}" scope="request" />
<tr>
 <td><el:cmdbutton url="questionnaire" link="${exam}" label="SCORE" /></td>
 <td class="pri bld"><el:cmd url="applicant" link="${applicant}">${applicant.name}</el:cmd></td>
 <td><a href="mailto:${applicant.email}">${applicant.email}</a></td>
 <td class="sec bld"><fmt:date fmt="d" date="${applicant.createdOn}" /></td>
 <td class="bld"><fmt:date fmt="d" date="${exam.submittedOn}" /></td>
 <td class="small">${applicant.registerHostName}</td>
</tr>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
