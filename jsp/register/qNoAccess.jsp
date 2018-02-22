<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Questionnaire Already Submitted</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="infoEmail" name="airline.mail.hr" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Questionnaire Already Submitted</div>
<br />
This <content:airline /> Pilot Questionnaire has already been submitted, and cannot be reviewed or modified at this time. If you have any questions about the registration process, please feel free to 
contact us at <a href="mailto:${infoEmail}" class="bld">${infoEmail}</a>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
