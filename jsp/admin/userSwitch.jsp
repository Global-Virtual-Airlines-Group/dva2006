<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Mass Mailing Sent</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Switched to ${user.name}</div>
<br />
You have switched user credentials to ${user.name}, ${user.pilotCode}. You will continue to 
operate with these credentials until you log out, at which point you will revert to your proper
credentials.<br />
<br />
<b>Please be aware that the system is incapable of distinguishing between you and the user you 
are impersonating - <span class="sec">${user.name}</span>!<br />
<br />
<content:copyright />
</div>
</body>
</html>
