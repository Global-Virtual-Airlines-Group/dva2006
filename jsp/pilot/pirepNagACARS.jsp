<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Flight Report Option</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Flight Report Logging using ACARS</div>
<br />
<span class="bld"><i>Do you know that you can log all of your Flights here at <content:airline /> automatically?</i></span><br />
<br />
Our ACARS package has successfully logged thousands of <content:airline /> flights and provides a wealth of information to
Pilots about their route and flight data. Additionally, ACARS allows you to communicate with other <content:airline /> Pilots while
flying, load Charts and Navigation information and load fuel on your aircraft based on your route and weather conditions. The
vast majority of our Flight Reports are logged using ACARS because it makes the process so easy.<br />
<br />
<c:if test="${eqType.ACARSPromotionLegs}">
Since you are a member of the <span class="sec bld">${eqType.name}</span> program, all of the 
<fmt:int className="bld" value="${fn:promoLegs(eqType, 'Captain')}" /> flight legs required for promotion to Captain <b>must</b> be 
logged using ACARS.<br />
<br />
</c:if>
Finally, all <content:airline /> Check Rides (used for promotion into new aircraft programs and requesting additional aircraft ratings) 
require the use of ACARS. You <b>cannot</b> be promoted without having used ACARS on your Check Ride. What better time than 
right now to start getting familair with ACARS and all of the great things it can do for you?<br />
<br />
You can download ACARS from the <content:airline /> <el:cmd url="fleetlibrary" className="sec bld">Fleet Library</el:cmd>. There 
are two different versions of ACARS available for download, depending on your needs and your computer's capabilities. ACARS 1.0
is a basic version of ACARS, designed for lower-end systems. ACARS 2.0 is the latest version, running on the Microsoft .NET 2.0 
Framework. All versions of ACARS run on Microsoft Windows 2000, XP, and Vista (including 64-bit) and are fully compatible with 
Microsoft Flight Simulator 2002, 2004 and Flight Simulator X. You can also run ACARS on a seperate computer if you have a 
registered version of <el:link external="true" url="http://www.schiratti.com/dowson.html">Peter Dowson's WideFS</el:link>.<br />
<br />
The final benefit to using ACARS is that it will get rid of this nag page. To continue filing your Flight Report manually, 
<el:cmd url="pirep" op="edit&amp;force=true" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
