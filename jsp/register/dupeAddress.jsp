<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Duplicate Registration Address</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlineName" name="airline.name" />
<content:sysdata var="addrInterval" name="registration.ip_interval" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Duplicate Registration Address</div>
<br />
Your IP Address ( <b>${pageContext.request.remoteAddr}</b> ) has been used within the past <fmt:int value="${addrInterval} " /> 
days to register at the <content:airline /> web site, and the previous application is Pending or has already 
been Approved. You cannot register again from this IP address.<br />
<br />
<span class="pri bld">If you are a former <content:airline /> Pilot who has been marked Retired or Inactive, 
you do NOT need to register again.</span> To reactivate your membership at <content:airline />, please
<el:cmd url="register" op="dupe" className="sec bld">Click Here</el:cmd>.<br />
<br />

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton ID="HomeButton" url="home" label="${airlineName} HOME PAGE" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
