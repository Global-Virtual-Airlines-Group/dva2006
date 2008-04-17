<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Examination Profile - ${eProfile.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
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
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION PROFILE - ${eProfile.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data">${empty eProfile.equipmentType ? 'N/A' : eProfile.equipmentType}</td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><fmt:int value="${eProfile.stage}" /></td>
</tr>
<tr>
 <td class="label">Minimum Stage</td>
 <td class="data"><fmt:int value="${eProfile.minStage}" /></td>
</tr>
<tr>
 <td class="label">Questions</td>
 <td class="data"><fmt:int value="${eProfile.size}" /></td>
</tr>
<tr>
 <td class="label">Passing Score</td>
 <td class="data"><fmt:int value="${eProfile.passScore}" /></td>
</tr>
<tr>
 <td class="label">Testing Time</td>
 <td class="data"><fmt:int value="${eProfile.time}" /> minutes</td>
</tr>
<c:if test="${!empty eProfile.scorerIDs}">
<tr>
 <td class="label" valign="top">Allowed Scorers</td>
 <td class="data"><c:forEach var="scorer" items="${scorers}">${scorer.name} (${scorer.pilotCode})<br /></c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data bld caps">
<c:if test="${eProfile.active}"><span class="ter">Examination is Available</span></c:if>
<c:if test="${!eProfile.active}"><span class="error">Examination is Not Available</span></c:if>
<c:if test="${eProfile.notify}"><br />
<span class="bld">Notify Scorers when Examination is Submitted</span></c:if>
<c:if test="${eProfile.academy}"><br />
Examination is part of the <content:airline /> Flight Academy</c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>
<c:if test="${access.canEdit}">
<el:cmdbutton url="eprofile" linkID="${eProfile.name}" op="edit" label="EDIT EXAMINATION PROFILE" />
 <el:cmdbutton url="epools" linkID="${eProfile.name}" label="VIEW QUESTION POOLS" /></td>
</c:if>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
