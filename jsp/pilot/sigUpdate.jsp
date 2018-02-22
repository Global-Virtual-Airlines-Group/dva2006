<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName} Signature Update</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.coolerImg, ext:['gif','jpg','png'], t:'Signature Image'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="sigX" name="cooler.sig_max.x" />
<content:sysdata var="sigY" name="cooler.sig_max.y" />
<content:sysdata var="sigSize" name="cooler.sig_max.size" />
<content:sysdata var="airlineName" name="airline.name" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="sigupdate.do" method="post" link="${pilot}" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left">${forumName} SIGNATURE IMAGE UPDATE - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Signature Image</td>
 <td class="data"><el:file name="coolerImg" className="small" idx="*" size="96" max="144" /><br />
 <span class="small sec">The maximum size for a signature image is <fmt:int value="${sigX}" />x<fmt:int value="${sigY}" /> pixels, and the maximum file size is <fmt:int value="${sigSize}" />K.</span>
<content:hasmsg>
<br />
<span class="error bld"><content:sysmsg /></span>
</content:hasmsg>
 </td>
</tr>
<content:filter roles="HR,Signature">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isAuth" idx="*" className="small" value="true" label="Authorized ${airlineName} ${forumName} Signature" /></td>
</tr>
</content:filter>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE SIGNATURE IMAGE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
