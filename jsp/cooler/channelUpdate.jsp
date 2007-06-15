<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler Channel Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/cooler/header.jspf" %> 
<%@include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isCreate}">
<!-- Water Cooler Channel created -->
<div class="updateHdr">Water Cooler Channel Created</div>
<br />
The <content:airline /> Water Cooler Channel <span class="pri bld">${channel.name}</span> has been successfully 
saved in the database.<br />
</c:if>

<c:if test="${isUpdate}">
<!-- Water Cooler Channel updated -->
<div class="updateHdr">Water Cooler Channel Updated</div>
<br />
The <content:airline /> Water Cooler Channel <span class="pri bld">${channel.name}</span> has been successfully 
updated in the database.<br />
<c:if test="${isRename}">
This Water Cooler Channel has been renamed to <span class="pri bld">${newName}</span>.<br />
</c:if>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
