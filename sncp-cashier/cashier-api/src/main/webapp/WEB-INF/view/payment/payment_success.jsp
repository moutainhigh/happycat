<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%> 
<spring:htmlEscape defaultHtmlEscape="true" />
<%@page import="java.util.Map"%>
<%
	response.setHeader("Cache-Control",
			"no-cache, no-store, must-revalidate"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><spring:message code="payment.confirm.title"/></title>
	<link rel="icon" type="image/x-icon" href="/images/favicon.ico" />    
	<link rel="shortcut icon" type="image/x-icon" href="/images/favicon.ico" />    
	<link rel="bookmark" type="image/x-icon" href="/images/favicon.ico" />
	<link href="//uswww3.woniu.com/includes/css/reset.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/includes/css/common.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/includes/css/index.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/includes/css/custom.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/pay/css/pay.css" type="text/css" rel="styleSheet"/>
	<meta name="Copyright" content="Copyright (c) 2011 <spring:message code="payment.company.name"/>" />
	<meta name="Keywords" content="提供最完善，最优质的游戏服务，了解蜗牛最及时的游戏资讯，拥有最真实的玩家互动社区" />
</head>
<body>
	<div class="pay_header">
         <div class="logo_con">
             <img src="//uswww3.woniu.com/pay/images/top.png" />
          </div>
    </div>
	<div class="pay-result">
            <h2><i class="tips-icon payOk"></i><span class="tips-msg"><spring:message code="payment.confirm.msg.order.pay.success" /></span></h2>
           <!--  <h2><i class="tips-icon payError"></i><span class="tips-msg">充值失败</span></h2>
            <h2><i class="tips-icon"></i><span class="tips-msg">订单正在处理,请稍后...</span></h2> -->
            <ul>
                <li><label><spring:message code="payment.confirm.lable.order.no" /></label><span id="orderno">${requestScope.paymentOrder.paypartnerOtherOrderNo}</span></li>
                <li><label><spring:message code="payment.confirm.lable.product.name" /></label><span id="productname">${requestScope.paymentOrder.productname}</span></li>
                <li><label><spring:message code="payment.confirm.lable.amount.pay1" /></label><span id="money">${requestScope.paymentOrder.money}<spring:message code="payment.confirm.lable.order.currency"/></span></li>
                <li><label><spring:message code="payment.confirm.lable.order.createtime" /></label><span id="time"><fmt:formatDate value="${requestScope.paymentOrder.create}" pattern="yyyy-MM-dd HH:mm:ss"/> </span></li>
            </ul>
            <button class="btn" id="close" onclick="javascript:window.opener=null;window.open('','_self');window.close();">关闭</button>
   </div>

</body>

</html>