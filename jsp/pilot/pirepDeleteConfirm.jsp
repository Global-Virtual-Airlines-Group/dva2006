<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Confirm Flight Report Deletion</title>
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
<div class="updateHdr">Confirm Flight Report Deletion</div>
<br />
This <content:airline /> Flight Report is about to be deleted. Are you sure you want to do this? This
operation <span class="bld">cannot be undone</span>.<br />
<br />
To return to the Flight Report, <el:cmd url="pirep" link="${pirep}" className="sec bld">Click Here</el:cmd>.<br />
<br />
<span class="pri bld">To delete the Flight Report,</span> <el:cmd url="pirepdelete" link="${pirep}" op="force" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
