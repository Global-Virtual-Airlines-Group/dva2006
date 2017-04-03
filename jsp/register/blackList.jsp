<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Registration Problem</title>
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
<content:sysdata var="hrEmail" name="airline.mail.hr" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Registration Cannot be Completed</div>
<br />
Sorry, but you cannot apply to <content:airline />. Please contact our Human Resources Department at <el:link url="mailto:${hrEmail}">${hrEmail}</el:link> 
if you have any questions about why this has occurred.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
