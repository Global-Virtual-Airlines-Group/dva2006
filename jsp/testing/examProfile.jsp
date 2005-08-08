<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Examination Profile - ${eProfile.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
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
<tr>
 <td class="label">&nbsp;</td>
<c:if test="${eProfile.active}">
 <td class="data ter bld caps">Examination is Available</td>
</c:if>
<c:if test="${!eProfile.active}">
 <td class="data error bld caps">Examination is Not Available</td>
</c:if>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
<c:if test="${access.canEdit}">
 <td><el:cmdbutton url="eprofile" linkID="${eProfile.name}" op="edit" label="EDIT EXAMINATION PROFILE" /></td>
</c:if>
<c:if test="${!access.canEdit}">
 <td>&nbsp;</td>
</c:if>
</tr>
</el:table>
<content:copyright />
</div>
</body>
</html>
