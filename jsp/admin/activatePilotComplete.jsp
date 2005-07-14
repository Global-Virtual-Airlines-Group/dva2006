<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Reactivated</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr">Pilot Reactivated</div>
<br />
${pilot.name} has been restored to the <content:airline /> Pilot Roster as an Active ${pilot.rank} in
the ${pilot.eqType} equipment program. This Pilot's password has been reset.<br />
<br />
An e-mail message has been sent to ${pilot.name} containing the new password, informing ${pilot.firstName} 
of this change in status, and welcoming him or her back to <content:airline />.<br />
<br />
<content:copyright />
</div>
</body>
</html>
