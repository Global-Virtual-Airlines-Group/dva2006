<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Examination / Question Updated</title>
<content:css name="main" />
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
<c:if test="${(!empty question) && isUpdate}">
<div class="updateHdr">Examination Question Updated</div>
<br />
This Pilot Examination Question has been succesfully updated in the database.<br />
<br />
To view this Question, <el:cmd url="qprofile" link="${question}" className="sec bld">Click Here</el:cmd>.<br />
<br />
This Question has been included in <fmt:int value="${question.exams.size()}" /> Pilot Examinations. To view all Questions in these Examinations, select one from the list below:<br />
<br />
<c:forEach var="exam" items="${question.exams}">
<el:cmd url="qprofiles" linkID="${exam}">${exam}</el:cmd><br />
</c:forEach>
</c:if>
<c:if test="${!empty exam}">
<div class="updateHdr">Examination Updated</div>
<br />
This Pilot Examination has been succsfully updated in the database.<br />
<br />
To view this Examination Profile, <el:cmd url="eprofile" linkID="${exam.name}" className="sec bld">Click Here</el:cmd>.<br />
To view all Examination Profiles, <el:cmd url="eprofiles" className="sec bld">Click Here</el:cmd>.<br />
To view Questions in this Examination, <el:cmd url="qprofiles" linkID="${exam.name}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${(!empty script) && isUpdate}">
<div class="updateHdr">Check Ride Script Updated</div>
<br />
The script for the ${script.equipmentType} Check Ride in the <span class="sec bld">${script.program}</span> equipment program has been saved in the database.<br />
<br />
To view the list of Check Ride scripts, <el:cmd url="crscripts" className="sec bld">Click Here</el:cmd>.<br />
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
<content:googleAnalytics />
</body>
</html>
