<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:htmlEscape defaultHtmlEscape="true" />
<%@page import="java.util.Map"%>
<%
	response.setHeader("Cache-Control",
			"no-cache, no-store, must-revalidate"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><spring:message code="payment.confirm.title"/></title>
	<meta name="Copyright" content="Copyright (c) 2011 <spring:message code="payment.company.name"/>" />
	<meta name="Keywords" content="提供最完善，最优质的游戏服务，了解蜗牛最及时的游戏资讯，拥有最真实的玩家互动社区" />
</head>
<body>

	<form style="display: none" id="this_form" method="${infoMap.method == null?'post':'get'}" action="${infoMap.payUrl}" target="_top" accept-charset="${infoMap.acceptCharset}" name="form_bot">
	    <c:if test="${infoMap.paymentParams != null}">
	        <c:forEach var="e" items="${infoMap.paymentParams}" varStatus="index">
	       		<c:if test="${(infoMap.pName == 'AlipayWapAppPayment') && (e.key == 'method')}">
	            	<input type="hidden" name="${e.key}" value="${e.value}" />
	            </c:if>
	       		<c:if test="${e.key != 'method'}">
	            	<input type="hidden" name="${e.key}" value="${e.value}" />
	            </c:if>
	        </c:forEach>
	    </c:if>
	</form>

	<script type="text/javascript">
		//document.charset="${infoMap.acceptCharset}";
		form_bot.submit();
	</script>
</body>

</html>