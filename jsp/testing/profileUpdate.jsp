<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Examination/Question Updated</title>
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
<c:if test="${(!empty question) && isUpdate}">
<div class="updateHdr">Examination Question Updated</div>
<br />
This Pilot Examination Question has been succesfully updated in the database.<br />
<br />
To view this Question, <el:cmd url="qprofile" link="${question}">Click Here</el:cmd>.<br />
<br />
This Question has been included in <fmt:int value="${fn:sizeof(question.examNames)}" /> Pilot
Examinations. To view all Questions in these Examinations, select one from the list below:<br />
<br />
<c:forEach var="examName" items="${question.examNames}">
<el:cmd url="qprofiles" linkID="${examName}">${examName}</el:cmd><br />
</c:forEach>
</c:if>
<c:if test="${!empty exam}">
<div class="updateHdr">Examination Updated</div>
<br />
This Pilot Examination has been succsfully updated in the database.<br />
<br />
To view this Examination Profile, <el:cmd url="eprofile" linkID="${exam.name}">Click Here</el:cmd>.<br />
To view all Examination Profiles, <el:cmd url="eprofiles">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${(!empty script) && isUpdate}">
<div class="updateHdr">Check Ride Script Updated</div>
<br />
The script for the ${script.equipmentType} Check Ride in the <span class="sec bld">${script.program}</span> 
equipment program has been saved in the database.<br />
</c:if>
<c:if test="${!(empty question) && isDelete}">
<div class="updateHdr">Examination Question Deleted</div>
<br />
This Pilot Examination Question has been succesfully removed from the database.<br />
</c:if>
<c:if test="${!(empty script) && isDelete}">
<div class="updateHdr">Check Ride Script Deleted</div>
<br />
The script for the ${script.equipmentType} Check Ride has been succesfully removed from the database.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
