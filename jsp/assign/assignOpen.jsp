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
<title><content:airline /> Flight Assignment Open</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<content:pics />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Flight Assignment Already Reserved</div>
<br />
You cannot reserve a new <content:airline /> Flight Assigment, since you already have an reserved Flight 
Assignment. If you have completed all the legs in this Flight Assignment, it may take a few hours for the system 
to record this.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
