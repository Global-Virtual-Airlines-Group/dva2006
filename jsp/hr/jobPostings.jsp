<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Career Opportunities</title>
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
<c:set var="cspan" value="${empty viewContext.results ? 1 : 4}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="careers">
<tr class="title">
 <td class="caps left" colspan="${cspan}"><content:airline /> CAREER OPPORTUNITIES</td>
</tr>
<tr>
 <td class="left" colspan="${cspan}">The culmination of your virtual career with <content:airline /> is joining our all-volunteer staff, giving back to the community you have enjoyed being a member of.
We periodically need new volunteers to join our staff, handling such duties as grading Examinations, Check Rides, processing Flight Reports and answering questions about <content:airline /> and its
programs and policies.<br />
<c:if test="${!empty viewContext.results}">
<content:sysdata var="mailHR" name="airline.mail.hr" />
<br />
Please review the list of career opportunities below. If you believe that you would be a good match for them, we welcome your application and interest. If you have questions about the positions or any
aspects of being a member of the <content:airline /> volunteer staff, do not hesitate to contact our Human Resources Department at <a href="mailto:${mailHR}">${mailHR}</a>.</c:if>
</td>
</tr>
<c:if test="${empty viewContext.results}">
<tr>
 <td class="pri bld left" colspan="${cspan}">There are no currently open career opportunities within the <content:airline /> volunteer staff. This area is constantly updated, so please check back
regularly as new volunteer opportunities are posted on a regular basis.</td>
</tr>

<c:if test="${access.canEdit}">
<tr class="title">
 <td colspan="${cspan}"><el:cmd url="job" op="edit">CREATE NEW JOB POSTING</el:cmd></td>
</tr>
</c:if>
</c:if>
<c:if test="${!empty viewContext.results}">
<!-- Table Header Bar-->
<tr class="title">
 <td style="width:20%">TITLE</td>
 <td class="nophone" style="width:10%">CREATED ON</td>
 <td class="nophone" style="width:10%">CLOSES ON</td>
 <td class="left">SUMMARY</td>
</tr>

<!-- Table Job Posting Data -->
<c:forEach var="job" items="${viewContext.results}">
<view:row entry="${job}">
 <td><el:cmd className="pri bld" url="job" link="${job}">${job.title}</el:cmd></td>
 <td class="small nophone"><fmt:date date="${job.createdOn}" fmt="d" /></td>
 <td class="small nophone bld"><fmt:date date="${job.closesOn}" fmt="d" /></td>
 <td class="left">${job.summary}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="${cspan}"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>
<c:if test="${access.canEdit}"> <el:cmd url="job" op="edit">CREATE NEW JOB POSTING</el:cmd></c:if>
<content:filter roles="HR"><view:legend width="90" labels="Open,Staff Only,Closed,Shortlisted,Selected,Complete" classes=" ,warn,opt2,opt3,opt4,opt1" /></content:filter></td>
</tr>
</c:if>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
