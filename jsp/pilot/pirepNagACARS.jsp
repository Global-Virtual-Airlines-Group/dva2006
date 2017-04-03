<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Flight Report Option</title>
<content:css name="main" />
<content:js name="common" />
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
<div class="updateHdr">Flight Report Logging using <content:airline /> ACARS</div>
<br />
<span class="bld ita">Do you know that you can log all of your Flights here at <content:airline /> automatically?</span><br />
<br />
Our ACARS package has successfully logged hundreds of thousands of <content:airline /> flights and provides a wealth of information to
Pilots about their route and flight data. Additionally, ACARS allows you to communicate with other <content:airline /> Pilots while
flying, load Charts and Navigation information and load fuel on your aircraft based on your route and weather conditions. The
vast majority of our Flight Reports are logged using ACARS because it makes the process so easy.<br />
<br />
<c:if test="${eqType.ACARSPromotionLegs}">
Since you are a member of the <span class="sec bld">${eqType.name}</span> program, all of the 
<fmt:int className="bld" value="${eqType.promotionLegs}" /> flight legs required for promotion to Captain <b>must</b> be 
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
Framework. All versions of ACARS run on Microsoft Windows 2000, XP, Vista, Windows 7 (including 64-bit) and are fully compatible with 
Microsoft Flight Simulator 2002, 2004 and Flight Simulator X. Our latest version of ACARS supports Lockheed-Martin's Prepar3D and
Flight Simulator X: Steam Edition. You can also run ACARS on a seperate computer if you have a registered version of 
<el:link external="true" target="_new" url="http://www.schiratti.com/dowson.html">Peter Dowson's WideFS</el:link>.<br />
<br />
The final benefit to using ACARS is that it will get rid of this page. To continue filing your Flight Report manually, 
<el:cmd url="pirep" op="edit" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
