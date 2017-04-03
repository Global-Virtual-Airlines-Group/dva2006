<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Applicant Updated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Pilot Application Updated</div>
<br />
The <content:airline /> Pilot application from ${applicant.name} has been updated.<br />
<br />
To review this Applicant's profile, <el:cmd url="applicant" className="sec bld" link="${applicant}">Click Here</el:cmd>.<br />
<br />
To return to the Applicant Queue, <el:cmd url="applicants" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
