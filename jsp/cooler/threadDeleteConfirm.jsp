<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title>Confirm ${forumName} Discussion Thread Deletion</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Confirm ${forumName} Discussion Thread Deletion</div>
<br />
This <content:airline /> ${forumName} discussion thread is about to be deleted. Are you sure you want to do this? This
operation <span class="bld">cannot be undone</span>.<br />
<br />
To return to the discussion thread, <el:cmd url="thread" link="${thread}" className="sec bld">Click Here</el:cmd>.<br />
<br />
<span class="pri bld">To delete the discussion thread,</span> <el:cmd url="threadkill" link="${thread}" op="force" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
