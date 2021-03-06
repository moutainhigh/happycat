<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
	<title>蜗牛收银台</title>
	<link rel="icon" type="image/x-icon" href="/images/favicon.ico" />    
	<link rel="shortcut icon" type="image/x-icon" href="/images/favicon.ico" />    
	<link rel="bookmark" type="image/x-icon" href="/images/favicon.ico" />
	<link href="//uswww3.woniu.com/includes/css/reset.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/includes/css/common.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/includes/css/index.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/includes/css/custom.css" type="text/css" rel="stylesheet" />
    <link href="//uswww3.woniu.com/pay/css/pay.css" type="text/css" rel="styleSheet"/>
	<meta name="Copyright" content="Copyright (c) 2011 苏州蜗牛数字科技股份有限公司" />
	<meta name="Keywords" content="提供最完善，最优质的游戏服务，了解蜗牛最及时的游戏资讯，拥有最真实的玩家互动社区" />
	<script src="//uswww3.woniu.com/includes/js/jquery-latest.js"></script>
	<script type="text/javascript">
		$(function(){
			var num =0;
			var i = setInterval(function(){
				num ++;
				$.ajax({
					url: "/payment/order/query",
					  data: {
					    orderNo: $('#orderno').html(),
						merchantid: '${requestScope.infoMap.paymentOrder.merchantId}'
					  },
					  success: function(data) {
						  if(data.status =='success'){
							  var imprestOrder = data.data.paymentOrder;
							  if(imprestOrder.payState =='1' && imprestOrder.state =='1'){
								  $('.tips-icon').addClass('payOk');
								  $('.tips-msg').html('支付成功');
							  }
						  }else{
							 alert(data.message);
						  }
					  }
				});
				if($('.tips-icon').hasClass('payOk')){
					clearInterval(i);
				}
				if(num > 10){
					clearInterval(i);
				}
			},10000);
			
			
			//判断订单方式
			if('${requestScope.infoMap.paymentOrder.imprestMode}' == 'V'){
				$('#this_form').submit();
			}
		});
	</script>
</head>
<body>
	<div class="pay_header">
         <div class="logo_con">
             <img src="//uswww3.woniu.com/pay/images/top.png" />
          </div>
    </div>
	<div class="pay-result">
           <!--   <h2><i class="tips-icon payOk"></i><span class="tips-msg">支付成功</span></h2>
            <h2><i class="tips-icon payError"></i><span class="tips-msg">充值失败</span></h2>-->
            <h2><i class="tips-icon"></i><span class="tips-msg">订单正在处理,请稍后...</span></h2>
            <span id="merchantid" ></span>
            <ul>
                <li><label>订单编号:</label><span id="orderno">${requestScope.infoMap.paymentOrder.paypartnerOtherOrderNo}</span></li>
                <c:if test="${requestScope.infoMap.paymentOrder.productname != null}">
                <li><label>商品名称:</label><span id="productname">${requestScope.infoMap.paymentOrder.productname}</span></li>
                </c:if>
                <li><label>支付金额:</label><span id="money">${requestScope.infoMap.paymentOrder.money}元</span></li>
                <li><label>下单时间:</label><span id="time"><fmt:formatDate value="${requestScope.infoMap.paymentOrder.create}" type="both"/></span></li>
            </ul>
            <button class="btn" id="close" onclick="javascript:window.opener=null;window.open('','_self');window.close();">关闭</button>
   </div>

   
  	<form method="post" style="display: none" id="this_form" target="_top" name="form_bot" action="${requestScope.infoMap.paymentOrder.paypartnerFrontCall}&orderNo=${requestScope.infoMap.paymentOrder.paypartnerOtherOrderNo }&productName=${requestScope.productname }">
		<!-- <input type="hidden" name="orderNo" value=""/>
		<input type="hidden" name="productName" value=""/> -->
	</form>
</body>
</html>