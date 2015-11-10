<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<meta http-equiv="REFRESH" content="4" />
<title>Welcome to <content:airline /></title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="infoEmail" name="airline.mail.hr" />
<content:sysdata var="domain" name="airline.domain" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Welcome to <content:airline /></div>
<br />
Thanks for your interest in <content:airline />. We will now redirect you to our applicant registration 
page. If you are not redirected, your browser probably has difficuties accepting cookies from our site. 
Please go into your browser's preferences, and ensure that you can accept cookies from <span class="sec bld">${domain}</span>.<br />
<br />
If you continue to have difficulties, please contact our <el:link url="mailto:${infoEmail}">HR department</el:link>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
 