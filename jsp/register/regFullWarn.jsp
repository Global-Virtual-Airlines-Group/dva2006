<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Registration Delays</title>
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
<content:sysdata var="maxPilots" name="users.max" />
<content:sysdata var="airlineName" name="airline.name" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Pilot Registration Delay</div>
<br />
<content:airline /> currently has over <fmt:int value="${airlineSize}" /> active Pilots. In order to more 
effectively serve our members, we restrict Pilot Registration once we have over <fmt:int value="${maxPilots}" /> 
Pilots. Therefore, there may be a delay before your application is processed.<br />
<br />
<span class="pri bld">If you are a former <content:airline /> Pilot who has been marked Retired or Inactive, 
you do NOT need to register again.</span> To reactivate your membership at <content:airline />, please
<el:cmd url="register" op="dupe" className="sec bld">Click Here</el:cmd>.<br />
<br />
If you still wish to apply for membership at <content:airline />, we welcome your interest. Please submit your application. We 
will contact you at the email address on your application when there is an opening.<br />
<br />

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton ID="RegisterButton" url="register" op="force" label="REGISTER AT ${airlineName}" />
 <el:cmdbutton ID="HomeButton" url="home" label="NO THANKS" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
