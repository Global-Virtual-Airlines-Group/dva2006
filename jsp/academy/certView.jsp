<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CERTIFICATION - ${cert.name}</td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data bld"><fmt:int value="${cert.stage}" /></td>
</tr>
<tr>
 <td class="label">Prerequisites</td>
 <td class="data"><span class="sec bld">${cert.reqName}</span><c:if test="${!empty preReqCert}"> - 
 <el:cmd url="cert" linkID="${preReqCert.name}" className="pri bld">${preReqCert.name}</el:cmd></c:if></td>
</tr>
<c:if test="${!empty cert.examNames}">
<tr>
 <td class="label">Examinations</td>
 <td class="data"><fmt:list value="${cert.examNames}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty cert.roles}">
<tr>
 <td class="label">Enrollment Roles</td>
 <td class="data"><fmt:list value="${cert.roles}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty docs}">
<tr>
 <td class="label top">Study Documents</td>
 <td class="data"><span class="sec bld ita">To modify this list, please update the Documents in the 
<content:airline /> Document Library.</span><br />
<c:forEach var="doc" items="${docs}">
<el:link url="/library/${doc.fileName}">${doc.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
<c:if test="${cert.hasCheckRide}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data pri bld caps">This Certification requires a Check Ride
<c:if test="${noScriptWarn}"><br /><span class="error">No Check Ride Script for this Certification exists</span></c:if></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data bld"><c:if test="${cert.active}"><span class="ter caps">CERTIFICATION IS AVAILABLE</span></c:if>
<c:if test="${!cert.active}"><span class="warn caps">CERTIFICATION IS NOT AVAILABLE</span></c:if>
<c:if test="${cert.autoEnroll}"><br /><span class="bld caps">AUTOMATICALLY ENROLL STUDENTS IN COURSE</span></c:if></td>
</tr>
<c:if test="${!empty cert.description}">
<tr>
 <td class="label top">Instructions</td>
 <td class="data top"><fmt:msg value="${cert.description}" /></td>
</tr>
</c:if>

<!-- Certification Requirements -->
<tr class="title caps">
 <td colspan="2">REQUIREMENTS FOR COMPLETION</td>
</tr>
<c:if test="${!empty cert.requirements}">
<c:set var="reqNum" value="0" scope="page" />
<c:forEach var="req" items="${cert.requirements}">
<c:set var="reqNum" value="${reqNum + 1}" scope="page" />
<c:set var="hasExam" value="${!empty req.examName}" scope="page" />
<tr>
 <td class="label top" rowspan="${hasExam ? 2 : 1}">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><fmt:msg value="${req.text}" /></td>
</tr>
<c:if test="${hasExam}">
 <td class="data"><span class="small ita">Requires successful completion of the <span class="pri bld caps">${req.examName}</span> Examination</span></td>
</c:if>
</c:forEach>
</c:if>
<c:if test="${empty cert.requirements}">
<tr>
 <td colspan="2" class="pri bld mid">This Flight Academy Certification has no extra requirements.</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
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
