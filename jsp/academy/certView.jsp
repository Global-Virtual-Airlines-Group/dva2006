<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Flight Academt Certification - ${cert.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CERTIFICATION - ${cert.name}</td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data bld"><fmt:int value="${cert.stage}" /></td>
</tr>
<tr>
 <td class="label">Prerequisites</td>
 <td class="data sec bld">${cert.reqName}</td>
</tr>
<tr>
 <td class="label">Examinations</td>
 <td class="data"><fmt:list value="${cert.examNames}" delim=", " /></td>
</tr>
<c:if test="${!empty docs}">
<tr>
 <td class="label" valign="top">Study Documents</td>
 <td class="data"><span class="sec bld"><i>To modify this list, please update the Documents in the 
<content:airline /> Document Library.</i></span><br />
<c:forEach var="doc" items="${docs}">
<el:link url="/library/${doc.fileName}">${doc.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data bld"><c:if test="${cert.active}"><span class="ter">CERTIFICATION IS AVAILABLE</span></c:if>
<c:if test="${!cert.active}"><span class="warn">CERTIFICATION IS NOT AVAILABLE</span></c:if>
<c:if test="${cert.autoEnroll}"><br /><span class="bld">AUTOMATICALLY ENROLL STUDENTS IN COURSE</span></c:if></td>
</tr>

<!-- Certification Requirements -->
<tr class="title caps">
 <td colspan="2">REQUIREMENTS FOR COMPLETION</td>
</tr>
<c:if test="${!empty cert.requirements}">
<c:set var="reqNum" value="${0}" scope="request" />
<c:forEach var="req" items="${cert.requirements}">
<c:set var="reqNum" value="${reqNum + 1}" scope="request" />
<tr>
 <td class="label">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><fmt:text value="${req.text}" /></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${empty cert.requirements}">
<tr>
 <td colspan="2" class="pri bld mid">This Flight Academy Certification has no extra requirements.</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td> 
<c:if test="${access.canEdit}">
 <el:cmdbutton url="cert" linkID="${cert.name}" op="edit" label="EDIT CERTIFICATION PROFILE" />
 <el:cmdbutton url="certreqs" linkID="${cert.name}" op="edit" label="EDIT REQUIREMENTS" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton url="certdelete" linkID="${cert.name}" label="DELETE CERTIFICATION PROFILE" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
