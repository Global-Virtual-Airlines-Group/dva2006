<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName} Channel Updated</title>
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
<c:if test="${isCreate}">
<!-- ${forumName} Channel created -->
<div class="updateHdr">${forumName} Channel Created</div>
<br />
The <content:airline /> ${forumName} Channel <span class="pri bld">${channel.name}</span> has been successfully 
saved in the database.<br />
</c:if>

<c:if test="${isUpdate}">
<!-- ${forumName} Channel updated -->
<div class="updateHdr">${forumName} Channel Updated</div>
<br />
The <content:airline /> ${forumName} Channel <span class="pri bld">${channel.name}</span> has been successfully 
updated in the database.<br />
<c:if test="${isRename}">
This ${forumName} Channel has been renamed to <span class="pri bld">${newName}</span>.<br />
</c:if>
</c:if>
<br />
To return to the list of ${forumName} channels, <el:cmd url="channeladmin" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
