<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Image Gallery Entry Deleted</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/gallery/header.jspf" %> 
<%@ include file="/jsp/gallery/sideMenu.jspf" %>
<content:sysdata var="forumName" name="airline.forum" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Image Gallery Entry Deleted</div>
<br />
This Image Gallery entry has been successfully deleted from the database. If this Image was linked to a ${forumName} message thread, the link has also been removed.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
