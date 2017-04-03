<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Applicant Questionnaires</title>
<content:css name="main" />
<content:css name="view" />
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

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:10%">&nbsp;</td>
 <td style="width:20%">APPLICANT NAME</td>
 <td class="nophone" style="width:20%">E-MAIL ADDRESS</td>
 <td class="nophone" style="width:10%">APPLIED ON</td>
 <td style="width:10%">SUBMITTED ON</td>
 <td class="nophone">REMOTE HOST NAME</td>
</tr>

<!-- Table Questionnaire Data -->
<c:forEach var="exam" items="${viewContext.results}">
<c:set var="applicant" value="${applicants[exam.authorID]}" scope="page" />
<tr>
 <td><el:cmdbutton url="questionnaire" link="${exam}" label="SCORE" /></td>
 <td class="pri bld"><el:cmd url="applicant" link="${applicant}">${applicant.name}</el:cmd></td>
 <td class="nophone"><a href="mailto:${applicant.email}">${applicant.email}</a></td>
 <td class="sec bld nophone"><fmt:date fmt="d" date="${applicant.createdOn}" /></td>
 <td class="bld"><fmt:date fmt="d" date="${exam.submittedOn}" /></td>
 <td class="small nophone">${applicant.registerHostName}</td>
</tr>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar>&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
