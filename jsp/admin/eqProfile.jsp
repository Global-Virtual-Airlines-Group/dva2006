<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<c:if test="${!empty eqType}">
<title><content:airline /> ${eqType.name} Program</title>
</c:if>
<c:if test="${empty eqType}">
<title>New <content:airline /> Equipment Program</title>
</c:if>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.eqType, t:'Equipment Type'});
golgotha.form.validate({f:f.cp, t:'Chief Pilot'});
golgotha.form.validate({f:f.stage, min:1, t:'Equipment Stage'});
golgotha.form.validate({f:f.captLegs, min:0, t:'Flight Legs for Promotion'});
golgotha.form.validate({f:f.captDistance, min:0, t:'Flight Distance for Promotion'});
golgotha.form.validate({f:f.switchDistance, min:0, t:'Flight Distance for Time Acceleration'});
golgotha.form.validate({f:f.maxAccel, min:0, t:'Flight Time with Time Acceleration'});
golgotha.form.validate({f:f.min1X, min:0, t:'Flight time without Time Acceleration'});
golgotha.form.validate({f:f.ranks, min:2, t:'Ranks'});
golgotha.form.validate({f:f.pRatings, min:1, t:'Primary Rating'});
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
<content:enum var="ranks" className="org.deltava.beans.Rank" />
<content:sysdata var="airlines" name="apps" mapValues="true" />
<content:sysdata var="acarsEnabled" name="acars.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="eqtype.do" linkID="${eqType.name}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Equipment Profile Title Bar -->
<tr class="title caps">
<c:if test="${!empty eqType}">
 <td colspan="2">${eqType.name} PROGRAM</td>
</c:if>
<c:if test="${empty eqType}">
 <td colspan="2">NEW EQUIPMENT PROGRAM</td>
</c:if>
</tr>

<!-- Equipment Profile Data -->
<tr>
 <td class="label">Program Name</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="[ NAME ]" className="req" options="${allEQ}" value="${eqType.name}" /></td>
</tr>
<tr>
 <td class="label">Chief Pilot</td>
 <td class="data"><el:combo name="cp" size="1" idx="*" firstEntry="" className="req" options="${chiefPilots}" value="${eqType.CPID}" /></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><el:text name="stage" size="1" max="1" idx="*" value="${empty eqType ? 1 : eqType.stage}" className="req" /></td>
</tr>
<tr>
 <td class="label">Available Ranks</td>
 <td class="data"><el:check name="ranks" cols="6" width="145" className="pri small" newLine="true" checked="${eqType.ranks}" options="${ranks}" /></td>
</tr>
<tr>
 <td class="label top">Primary Ratings</td>
 <td class="data"><el:check name="pRatings" cols="8" width="95" className="small req" newLine="true" checked="${eqType.primaryRatings}" options="${allEQ}" /></td>
</tr>
<tr>
 <td class="label top">Secondary Ratings</td>
 <td class="data"><el:check name="sRatings" cols="8" width="95" className="small" newLine="true" checked="${eqType.secondaryRatings}" options="${allEQ}" /></td>
</tr>
<tr>
 <td class="label">Web Applications</td>
 <td class="data"><el:check name="airline" width="175" idx="*" options="${airlines}" className="req" checked="${eqType.airlines}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">PILOT PROMOTION REQUIREMENTS</td>
</tr>
<tr>
 <td class="label top">First Officer Examinations</td>
 <td class="data"><el:check name="examFO" width="180" idx="*" cols="4" newLine="true" className="small" options="${exams}" checked="${fn:examFO(eqType)}" /></td>
</tr>
<tr>
 <td class="label">Flights for Promotion</td>
 <td class="data"><el:text name="captLegs" size="2" max="2" idx="*" className="req" value="${empty eqType ? 10 : eqType.promotionLegs}" /> legs</td>
</tr>
<tr>
 <td class="label">Flight Distance for Promotion</td>
 <td class="data"><el:text name="captDistance" size="3" max="4" idx="*" className="req" value="${eqType.promotionMinLength}" /> miles</td>
</tr>
<tr>
 <td class="label">Time Acceleration</td>
 <td class="data">Below <el:text name="switchDistance" size="2" max="5" idx="*" className="req" value="${eqType.promotionSwitchLength}" /> miles, no more
 than <el:text name="maxAccel" size="2" max="5" idx="*" className="req" value="${eqType.maximumAccelTime}" /> seconds at 2X or above. Otherwise, at least
 <el:text name="min1X" size="2" max="5" idx="*" className="req" value="${eqType.minimum1XTime}" /> seconds without time acceleration</td>
</tr>
<tr>
 <td class="label top">Captain Examinations</td>
 <td class="data"><el:check name="examC" width="180" idx="*" cols="4" newLine="true" className="small" options="${exams}" checked="${fn:examC(eqType)}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data small"><c:if test="${!eqType.isDefault}"><el:box name="active" idx="*" value="true" checked="${eqType.active}" label="Equipment Program is Active" /><br />
 <el:box name="makeDefault" idx="*" value="true" label="Make this the default Equipment Program" /><br /></c:if>
 <c:if test="${eqType.isDefault}"><span class="pri bld caps">This is the default Equipment Program and cannot be disabled</span><br /></c:if>
 <el:box name="newHires" idx="*" value="true" checked="${eqType.newHires}" label="Equipment Program accepts new Hires" /><br />
<c:if test="${acarsEnabled}"><el:box name="acarsPromote" idx="*" value="true" className="bld" checked="${eqType.ACARSPromotionLegs}" label="Require ACARS usage on Flights for Promotion" /></c:if></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="updateRatings" idx="*" value="true" label="Update Pilot Ratings" /></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}">
<el:button ID="SaveButton" type="submit" label="SAVE EQUIPMENT PROGRAM" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
