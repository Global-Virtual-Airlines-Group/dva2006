<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html lang="en">
<head>
<title>Facebook Authorization Error</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Facebook Authorization Failed</div>
<br />
Oops. The Facebook authorization attempt has failed. Here's the reason that Facebook gave us:<br />
<br />
<span class="error bld"><content:sysmsg /></span><br />
<br />
Please close this window and try the authorization attempt again.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
