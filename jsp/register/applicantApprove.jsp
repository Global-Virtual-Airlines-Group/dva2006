<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Hired</title>
<content:css name="main" />
<content:pics />
<content:favicon />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Pilot Hired</div>
<br />
${applicant.name} has been hired as a <content:airline /> pilot, as a ${applicant.rank.name} in the
${eqType.name} (Stage ${eqType.stage}) program. An e-mail message has been sent to ${applicant.email}.<br />
<br />
To review this Applicant's profile, <el:cmd url="applicant" className="sec bld" link="${applicant}">Click Here</el:cmd>.<br />
To review this Pilot's profile, <el:cmd url="profile" className="bld" linkID="${fn:hex(applicant.pilotID)}">Click Here</el:cmd>.<br />
<br />
To return to the Applicant Queue, <el:cmd url="applicants" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
