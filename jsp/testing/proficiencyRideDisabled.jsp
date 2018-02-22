<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Proficiency Check Rides Disabled</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> PROFICIENCY CHECK RIDES DISABLED</div>

You have opted out of <content:airline />'s currency-based Check Ride program. We have reviewed your Examination and Check Ride history and recalculated all
equipment type ratings that you are eligible to hold. The following type ratings have been restored:<br />
<br />
<fmt:list value="${ratingDelta}" delim=", " /><br />
<br />
You can return to <content:airline />'s currency-based Check Rides at any point in the future. We will recalculate your Examination and Check Ride history and update your ratings
 when you do so.<br />
<br />
To return to the <content:airline /> Testing Center, <el:cmd url="testcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
