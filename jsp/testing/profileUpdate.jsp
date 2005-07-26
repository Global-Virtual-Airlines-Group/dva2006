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
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<c:if test="${!empty question}">
<div class="updateHdr">Examination Question Updated</div>
<br />
This Pilot Examination Question has been succesfully updated in the database.<br />
<br />
To view this Question, <el:cmd url="qprofile" linkID="0x${question.ID}">Click Here</el:cmd>.<br />
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
<br />
<content:copyright />
</div>
</body>
</html>
