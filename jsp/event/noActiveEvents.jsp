<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>No <content:airline /> Online Events</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/event/header.jspf" %> 
<%@include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="ivaoImg" name="online.ivao.banner" />
<content:sysdata var="vatsimImg" name="online.vatsim.banner" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">NO AVAILABLE SCHEDULED ONLINE EVENTS</div>
<br />
At the moment, no <content:airline /> Online Events are currently available for signup. Please be sure to check 
back often - Events are often scheduled on short notice. <content:airline /> typically holds one 
major event per month, and ad hoc events approximately once every seven to ten days.<br />
<br />
<el:table space="2" pad="2">
<tr>
 <td><el:link url="http://www.vatsim.net/" target="_NEW"><el:img src="network/${vatsimImg}" border="0" caption="VATSIM" /></el:link></td>
 <td><el:link url="http://www.ivao.org/" target="_NEW"><el:img src="network/${ivaoImg}" border="0" caption="IVAO" /></el:link></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
