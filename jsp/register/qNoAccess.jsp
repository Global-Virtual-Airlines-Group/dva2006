<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Questionnaire Already Submitted</title>
<content:css name="main" browserSpecific="true" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="infoEmail" name="airline.mail.hr" />

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Questionnaire Already Submitted</div>
<br />
This <content:airline /> Pilot Questionnaire has already been submitted, and cannot be reviewed or
modified at this time. If you have any questions about the registration process, please feel free to 
contact us at <a href="mailto:${infoEmail}" class="bld">${infoEmail}</a>.<br />
<br />
<content:copyright />
</div>
</body>
</html>
