<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Hired</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Pilot Hired</div>
<br />
${applicant.name} has been hired as a <content:airline /> pilot, as a ${applicant.rank} in the
${eqType.name} (Stage ${eqType.stage}) program. An e-mail message has been sent to ${applicant.email}.<br />
<br />
To review this Applicant's profile, <el:cmd url="applicant" className="sec bld" linkID="0x${applicant.ID}">click here</el:cmd>.<br />
To review this Pilot's profile, <el:cmd url="profile" className="sec bld" linkID="0x${applicant.pilotID}">click here</el:cmd>.<br />
<br />
To return to the Applicant Queue, <el:cmd url="applicants" className="sec bld">click here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
