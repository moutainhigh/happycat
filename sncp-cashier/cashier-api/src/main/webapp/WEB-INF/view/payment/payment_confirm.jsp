<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:htmlEscape defaultHtmlEscape="true" />
<%
	response.setHeader("Cache-Control","no-cache, no-store, must-revalidate"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server
%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
	    <link href="//www3.woniu.com/includes/css/reset.css" type="text/css" rel="stylesheet" />
	    <link href="//www3.woniu.com/includes/css/common.css" type="text/css" rel="stylesheet" />
	    <link href="//www3.woniu.com/includes/css/index.css" type="text/css" rel="stylesheet" />
	    <link href="//www3.woniu.com/includes/css/custom.css" type="text/css" rel="stylesheet" />
	    <link href="//www3.woniu.com/pay/css/pay.css" type="text/css" rel="styleSheet"/>
	    <script src="//www3.woniu.com/pay/js/wn_sso.js"></script>
	    <title><spring:message code="payment.confirm.title"/></title>
	    <meta name="Copyright" content="Copyright (c) 2011 <spring:message code="payment.company.name"/>" />
		<meta name="Keywords" content="提供最完善，最优质的游戏服务，了解蜗牛最及时的游戏资讯，拥有最真实的玩家互动社区" />
	</head>
    <body>
    	<form id="payform" action="/payment/api/dp" class="hidden" method="post">
        	<input type="hidden" name="orderno" value="${ret.orderno}"/>
        	<input type="hidden" id="merchantid" name="merchantid" value="${ret.merchantid}"/>
        	<input type="hidden" name="gameid" value="${ret.gameid}"/>
        	<input type="hidden" name="account" value="${ret.account}"/>
        	<input type="hidden" id="money" name="money" value="${ret.money}"/>
        	<input type="hidden" name="clientip" value="${ret.clientip}"/>
        	<input type="hidden" name="mode" value="${ret.imprestmode}"/>
        	<input type="hidden" name="productname" value="${ret.productname}"/>
        	<input type="hidden" name="backendurl" value="${ret.backendurl}"/>
        	<input type="hidden" name="fontendurl" value="${ret.fontendurl}"/>
        	<input type="hidden" name="currency" value="${ret.currency}"/>
        	<input type="hidden" name="aid" value="${ret.aid}"/>
        	<input type="hidden" name="platformid" id="platformId"/>
        	<input type="hidden" name="bankcd" id="bankcdId"/>
        	<input type="hidden" name="bankCardType" id="bankCardType"/>
        	<input type="hidden" name="sign" value="${ret.sign}"/>
        	<input type="hidden" name="ext" value="${ret.ext}"/>
        	<input type="hidden" name="terminalType" value="${ret.terminaltype}"/>
        	<input type="hidden" name="timeoutExpress" value="${ret.timeoutexpress}"/>
        	<input type="hidden" name="goodsDetail" value="${ret.goodsdetail}"/>
        	<input type="hidden" name="body" value="${ret.body}"/>
        	<!-- 获取验证码使用 -->
        	<%-- <input type="hidden" id="accountid" name="accountid" value="${ret.accountid}"/> --%>
        	<input type="hidden" id="cardtype" name="cardtype" value=""/>
        	<input type="hidden" id="refererId" name="referer" value="${referer}"/>
        	<!--信用卡分期参数  -->
        	<input type="hidden" id="stagePlan" name="stagePlan"/>
        	<input type="hidden" id="stageNum" name="stageNum"/>
        	<!-- 代金卷参数 -->
        	<input type="hidden" id="ttbDjjMoney" name="ttbDjjMoney"/>
        	<input type="hidden" id="smscode" name="smscode"/>
        	<!-- 手机充值卡参数 -->
        	<input type="hidden" id="cardNo" name="cardNo"/>
        	<input type="hidden" id="cardPwd" name="cardPwd"/>
        	<input type="hidden" id="captchaValue" name="captchaValue"/>
        	
        	<!-- 组合使用翡翠币 -->
        	<input type="hidden" id="yueMoney" name="yueMoney"/>
        	<input type="hidden" id="yueCurrency" name="yueCurrency"/>
        	<input type="hidden" id="fcbsmscode" name="fcbsmscode"/>
        </form>
        <div class="pay_header">
            <div class="logo_con">
                <img src="//www3.woniu.com/pay/images/top.png" />
                <img style="float: right;padding-top: 10px;" src="//www3.woniu.com/pay/images/customer_phone.png" />
            </div>
        </div>
        <div class="pay_body">
            <div class="bread">
            	<!-- 
                <a href="javascript:;">首页</a>&gt;<a href="javascript:;">订单确认</a>&gt;<a href="javascript:;">支付选择</a>
                 -->
            </div>
            <div class="order_detail">
                <div class="status">
                	<div class="status_str">
                    	<spring:message code="payment.confirm.msg.order.success.info"/>
                    </div>
                	<div class="total"><spring:message code="payment.confirm.lable.amount.pay"/><span class="num">￥<em id="money-old">${ret.money}</em></span></div>
                    <div class="total"><spring:message code="payment.confirm.lable.order.amount"/><span class="num">${ret.money} <spring:message code="payment.confirm.lable.order.currency"/></span>&nbsp;&nbsp;</div>
                    <div class="status_str"><spring:message code="payment.confirm.lable.order.no"/><span id="order_no" class="order_no">${ret.orderno}</span></div>
                    <div class="clear"></div>
                </div>
                <div class="order_info">
                    <!-- 订单编号： -->
                    <p><spring:message code="payment.confirm.lable.order.no"/>${ret.orderno}</p>
                    <!-- 商品名称： -->
                    <p><spring:message code="payment.confirm.lable.product.name"/>${ret.productname}</p>
                    <!-- 交易金额： -->
                    <p><spring:message code="payment.confirm.lable.order.amount1"/>${ret.money} 
                    	<spring:message code="payment.confirm.lable.order.currency"/></p>
                    <!-- 下单时间： -->
                    <p><spring:message code="payment.confirm.lable.order.createtime"/>${ret.paydate}</p>
                </div>
            </div>
            <div class="toggle"><span><spring:message code="payment.confirm.msg.order.info"/></span></div> 
            <div class="clear"></div>
            <div class="navs">
            	<c:if test="${not empty ret.debitlist}">
            		<a href="javascript:;" class="nav" tar="debit_card"><spring:message code="payment.confirm.tab.pay.bank.card"/></a>
            	</c:if>
            	<c:if test="${not empty ret.thirdlist}">
            		<a href="javascript:;" class="nav" tar="thrid_platform"><spring:message code="payment.confirm.tab.pay.platform"/></a>
            	</c:if>
            	<c:if test="${not empty ret.creditlist}">
            		<a href="javascript:;" class="nav" tar="credit_card">信用卡分期</a>
            	</c:if>
            	<c:if test="${not empty ret.ttbList}">
            		<a href="javascript:;" class="navt" tar="ttb" id="xt">兔兔币支付</a>
            	</c:if>
            	<c:if test="${(not empty ret.yxCardList) or (not empty ret.mobileCardList) or (not empty ret.wnMobileSpecCardList)}">
            		<a href="javascript:;" class="nav" tar="other">充值卡支付</a>
            	</c:if>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
            
            <c:if test="${ not empty ret.debitlist}">
           	<div class="bank_list list" id="debit_card" val="B" style="display:block;">
                <p></p>
                <c:forEach items="${ret.debitlist}" var="debit" varStatus="idx">
                <c:choose>
	                <c:when test="${idx.count le 18 }">
	                <div class="item card" bankcd="${debit.content}" val="${debit.bankPlatformId}"
	                bankType="${debit.debitPayType}" payType="${debit.creditPayType}">
	                    <img src="//www3.woniu.com/pay/images/logo/bank/${debit.content}.gif" />
	                    <div class="border"></div>
	                </div>
	                </c:when>
	                <c:otherwise>
	                <div class="item card hidden" bankcd="${debit.content}" val="${debit.bankPlatformId}"
	                bankType="${debit.debitPayType}" payType="${debit.creditPayType}">
	                    <img src="//www3.woniu.com/pay/images/logo/bank/${debit.content}.gif" />
	                    <div class="border"></div>
	                </div>	
	                </c:otherwise>
                </c:choose>
                </c:forEach>
                <div class="clear"></div>
                <c:if test="${fn:length(ret.debitlist) > 18}">
                <div class="more_bank"></div>
                </c:if>
                <!-- 按钮选择 -->
                <div class="cardType">
                    <ul class="card_ul">
                        <li>
	                        <span class="cardLabel">银行卡类型：</span>
	                        <label for="bankType1"><span id="bankType1" class="radio" data-value="0">储蓄卡</span></label>
	                        <label for="bankType2"><span id="bankType2" class="radio" data-value="1">信用卡</span></label>
                        </li>
                        <li>
                        	<span class="cardLabel">付款方式：</span>
                        	<label for="payType1"><span id="payType1" class="radio" data-value="0">网银支付</span></label>
                        	<label for="payType2"><span id="payType2" class="radio" data-value="1">快捷支付</span></label>
                        </li>
                    </ul>
                </div>
            </div>
           	</c:if>
           	
           	<c:if test="${ not empty ret.thirdlist}">
            <div class="platforms list" id="thrid_platform" val="T">
                <p></p>
                <div class="platform_list">
                	<c:forEach items="${ret.thirdlist}" var="third" varStatus="idx">
                    <div class="item platform" pid="${third.content}">
                    	<c:if test="${third.dispFlag eq 'N'}">
                    		<div class="new"></div>
                    	</c:if>
                        <img src="//www3.woniu.com/pay/images/logo/platform/${third.content}.png" />
                        <div class="border"></div>
                    </div>
                    </c:forEach>
                    <div class="clear"></div>
                </div>
            </div>
            </c:if>
           	
           	<c:if test="${ not empty ret.creditlist}">
           	<div class="bank_list list" id="credit_card" val="S" style="display:block;">
                <p class="step">1.选择银行卡</p>
                <c:forEach items="${ret.creditlist}" var="credit" varStatus="idx">
                <c:choose>
	                <c:when test="${idx.count le 18 }">
	                <div class="item card" bankcd="${credit.content}" val="${credit.bankPlatformId}">
	                    <img src="//www3.woniu.com/pay/images/logo/bank/${credit.content}.gif" />
	                    <div class="border"></div>
	                </div>
	               	</c:when>
	                <c:otherwise>
	                <div class="item card hidden" bankcd="${credit.content}" val="${credit.bankPlatformId}">
	                    <img src="//www3.woniu.com/pay/images/logo/bank/${credit.content}.gif" />
	                    <div class="border"></div>
	                </div>
	                </c:otherwise>
	            </c:choose>
                </c:forEach>
                <div class="clear"></div>
                <c:if test="${fn:length(ret.creditlist) > 18}">
                <div class="more_bank"></div>
                </c:if>
                <!-- 分期 -->
                <p class="step">2.选择分期数</p>
                <ul class="payNumList" id="creStage">
                    <!--  <li class="payNum">3期<em></em></li>
                    <li class="payNum">6期<em></em></li>
                    <li class="payNum">12期<em></em></li>-->
                </ul>
                <div class="explain">
                    <p> 付款金额：<span class="c-red" id="totalMoney">*</span>元 = 订单应付金额：<span class="c-black" id="shouldMoney">*</span>元 + 分期付款手续费：<span class="c-black" id="poundageMoney">*</span>元（分期手续费具体以银行收取为准）</p>
                    <p> 还款规则：每期应还款：<span class="c-red" id="everyMoney">*</span>元 × <span id="time">*</span>期（月）</p>
                </div>
            </div>
           	</c:if>
           	
           	<!-- 兔兔币 -->
           	<c:if test="${ not empty ret.ttbList}">
           	<iframe src="//cashier.woniu.com/sso/isSSOLogin" style="display: none"></iframe>
           	<div class="platforms ttb" id="ttb" val="Q" style="display: none;">
           		<c:forEach items="${ret.ttbList}" var="ttb" varStatus="idx">
                <div class="platform_list clearfix" id="coupo" pid="${ttb.content}">
                  <p>
                      <label>兔兔币余额：</label>
                      <span id="ttbMoney">*</span><sub>(1兔兔币=1元)</sub>
                  </p>
                  <ul id="ttbDjj">
                   		<!--
          				<li>
          					<span class="coupo"><input type="checkbox" />使用代金券2：<em>5.00</em>元</span>
                        	<input type="text" placeholder="使用金额" class="textInput" />
                        	<em>过期时间：2016-09-06</em>
          				</li>-->
                   	</ul>
                   	<p class="pay-info">您的账户余额不足，请先充值兔兔币，或试试其它支付方式。</p>
                </div>
                </c:forEach>
            </div>
            </c:if>
            <!-- 其他支付 -->
            <c:if test="${(not empty ret.yxCardList) or (not empty ret.mobileCardList) or (not empty ret.wnMobileSpecCardList)}">
            <div class="platforms pt40 other list" id="other" val="O">
                <div class="clearfix">
                  <div class="other-pay clearfix">
                  	<c:if test="${(not empty ret.yxCardList) }">
                      <div class="card item" tar="game_card" >
                         <img src="//www3.woniu.com/pay/images/game_card.png" />
                         <div class="border"></div>
                      </div>
                    </c:if>
                   <c:if test="${(not empty ret.mobileCardList) }">
                     <div class="card item" tar="tel_card">
                         <img src="//www3.woniu.com/pay/images/tel_card.png" />
                         <div class="border"></div>
                     </div>
                   </c:if>
                   <c:if test="${(not empty ret.wnMobileSpecCardList) }">
                     <div class="card item" tar="wn_spec_card">
                         <img src="//www3.woniu.com/pay/images/wn_spec_card.png" />
                         <div class="border"></div>
                     </div>
                   </c:if>
                  </div>
                  
                  <div class="other-box" style="display:block">
                  	<c:if test="${(not empty ret.yxCardList) }">
	                    <div class="clearfix other-title" id="game_card">
	                      <label class="lh44">支持种类：</label>
	                      <ul class="payNumList clearfix">
	                       	<c:forEach items="${ret.yxCardList}" var="yxCard" varStatus="idx">
	                       		<li class="payNum" otype="${yxCard.type}" cardcd="${yxCard.content}" pid="${yxCard.bankPlatformId}"><img src="//www3.woniu.com/pay/images/logo/card/${yxCard.content}.gif" /><em></em></li>
	                       	</c:forEach>
	                      </ul>
	                      <div class="clearfix">
		                      <label>充值卡号：</label><input id="ycardNo" type="text" placeholder="请输入卡号" />
		                      <span class="redText"></span>
		                    </div>
		                    <div class="clearfix">
		                      <label>充值密码：</label><input id="ycardPwd" type="text" placeholder="请输入密码" />
		                      <span class="redText"></span>
		                    </div>
	                    </div>
                    </c:if>
                    <c:if test="${(not empty ret.mobileCardList) }">
	                    <div class="clearfix other-title" id="tel_card">
	                      <label class="lh44">运营商：</label>
	                      <ul class="payNumList clearfix">
	                      	<c:forEach items="${ret.mobileCardList}" var="mCard" varStatus="idx">
	                       		<li class="payNum" otype="${mCard.type}" cardcd="${mCard.content}" pid="${mCard.bankPlatformId}"><img src="//www3.woniu.com/pay/images/logo/card/${mCard.content}.gif" /><em></em></li>
	                       	</c:forEach>
	                      </ul>
	                      <div class="clearfix">
		                      <label>充值卡号：</label><input id="ycardNo" type="text" placeholder="请输入卡号" />
		                      <span class="redText"></span>
		                    </div>
		                    <div class="clearfix">
		                      <label>充值密码：</label><input id="ycardPwd" type="text" placeholder="请输入密码" />
		                      <span class="redText"></span>
		                    </div>
	                    </div>
                    </c:if>
                    
                    <c:if test="${(not empty ret.wnMobileSpecCardList) }">
	                    <div class="clearfix other-title" id="wn_spec_card">
	                      <label class="lh44">运营商：</label>
	                      <ul class="payNumList clearfix">
	                      	<c:forEach items="${ret.wnMobileSpecCardList}" var="wmSpecCard" varStatus="idx">
	                       		<li class="payNum" otype="${wmSpecCard.type}" cardcd="${wmSpecCard.content}" pid="${wmSpecCard.bankPlatformId}"><img src="//www3.woniu.com/pay/images/logo/card/${wmSpecCard.content}.gif" /><em></em></li>
	                       	</c:forEach>
	                      </ul>
	                      <div class="clearfix">
		                      <label>充值卡号：</label><input id="ycardNo" type="text" placeholder="请输入卡号" />
		                      <span class="redText"></span>
		                    </div>
	                      <div class="clearfix">
		                      <label>充值密码：</label><input id="ycardPwd" type="text" placeholder="请输入卡密" />
		                      <span class="redText"></span>
		                    </div>
	                    </div>
                    </c:if>
                    <div class="clearfix">
                      <label>验证码：</label><input type="text" id="ycapVal" placeholder="点击获取验证码"  style="width:191px;" />
                      <img id="captchaImg" src="" />
                      <span class="redText"></span>
                    </div>
                    <div class="other-tips redText">充值卡实际面额必须和需付金额一致，否则付款失败</div>
                  </div>
                  
                 
                 <!--  <div class="other-box" id="tel_card">
                   
                    <div class="clearfix">
                      <label>充值卡号：</label><input id="mcardNo" type="text" placeholder="请输入充值卡号" />
                    </div>
                    <div class="clearfix">
                      <label>充值密码：</label><input id="mcardPwd" type="text" placeholder="请输入充值卡密码" />
                    </div>
                    <div class="other-tips redText">充值卡不支持分次充值，卡实际面额必须和应付金额一致</div>
                  </div>-->
                  
                </div> 
            </div>
            </c:if>
            
            <!-- 翡翠币 -->
            <c:if test="${not empty ret.fcbList}">
            	<div class="fcb-box">
	            	<input type="hidden" id="fcbAmount" value="${ret.fcbInfo[0].amount}" />
	            	<input type="hidden" id="fcbPhone" value="${ret.fcbPhone}" />
	            	<input type="hidden" id="fcbAccount" value="${ret.fcbAccount}" />
	            	<input type="hidden" id="fcbPlatformid" value="${ret.fcbList[0].bankPlatformId}" />
	            	<input type="hidden" id="payTypeId" value="${ret.fcbInfo[0].payTypeId}" />
	                <p>
	                	<i class="fcb-select"></i>使用${ret.fcbInfo[0].payTypeName} ，当前余额<em style="color:#f60;">${ret.fcbInfo[0].amount}</em>
	                	<span class="fcb-selected"><input type="text" class="fcbInput" placeholder="请输入了使用金额" value="" />
	                	剩余付款金额<em style="color:#f60;" data="${ret.money}" class="fcb-yue">${ret.money}</em></span>
	                </p>
	            </div>
            </c:if>
            
            
            <a href="javascript:;" class="to_pay"><spring:message code="payment.confirm.button.pay"/></a>
            
            <p id="tips" style="float:left;color:#999;padding:20px 0 10px 55px;display: none;">温馨提示：订单已提交，我们将为您保留订单24小时，请尽快完成支付。</p>	
			<iframe id="qrCodeIframe" style="text-align: center;display: none;" frameborder="0" name="qrCodeIframe" width="600px" height="300px">
				
			</iframe>
        </div>
        <div style="width: 965px; margin: 25px auto;" name="wxQrCodeDv">
				<div class="wxQrCodeDv" id="wxQrCodeDv" style="display:none;margin: 0 auto;width: 200px;"></div>
				<div class="wxNoteDv"
					style="font-size: 16px; color: #000000;"></div>
		</div>
        <div class="pay_bottom">
            <p class="question_title">支付遇到问题</p>
            <p class="question">网银支付支持哪些银行？</p>
            <p class="answer">答：网银支付需要提前开通网银，可以选择网上银行（支持工商银行、农业银行、建设银行、中国银行、交通银行、兴业银行等在内的16家银行）</p>
            <p class="question">帐户支付支持哪些帐户？</p>
            <p class="answer">答：帐户支付可以支持支付宝、财付通帐户进行支付。</p>
        </div>
        
        <!-- sms -->
        <div class="pop" id="pop"></div>

        <img src="//www3.woniu.com/pay/images/loading.gif" style="display: none;" class="loading"/>

          <!-- <div class="sms" id="sms" style="display:none">
           <span class="title"></span>
           <i class="sms_close" id="smsclose"></i>
           <p class="sms_tel"><label>验证手机</label>:<span id="smsMobile"></span><i id="resend" style="display: none;">重新发送</i><i id="minsend"><span id="119min"></span>秒</i></p>
           <div class="smsCode">
               <input type="text" maxlength="1" />
               <input type="text" maxlength="1" />
               <input type="text" maxlength="1" />
               <input type="text" maxlength="1" />
           </div>
           <p class="line">
               <a href="#" id="smsSure">确定</a>
           </p>
           </div>
 -->





             <div class="bind_mobile_wrap" id="sms">
              <div class="c_wrap">
                 <a href="javascript:;" class="close_bind" id="smsclose"></a>
                 <h2>付款验证</h2>
                 <div class="content">
                     <ul>
                         <li style="overflow:hidden;">
                         <span>手机号码：</span>
                         <span id="smsMobile"></span>
                             <!-- <input type="text" class="bind_mobile" style="width:120px" placeholder="请输入手机号码" maxlength="11"/> -->
                          <a href="javascript:;" class="send_bind_sms" id="resend">获取验证码</a>
                         </li>
                         <li>
                             <span>验证码：</span>
                             <input type="text" style="width:254px;" class="bind_verify" placeholder="请输入短信验证"/>
                             <div class="sms_status"></div>
                         </li>
                     </ul>
                     <a href="javascript:;" class="to_bind" id="smsSure">确定</a>
                 </div>
              </div>
             </div>
	
	<script src="//www3.woniu.com/includes/js/jquery-latest.js"></script>
        <script src="//www3.woniu.com/pay/js/payment.js"></script>
        <script src="//www3.woniu.com/pay/js/md5.js"></script>
        <script src="//www3.woniu.com/imp/script/qrcode/qrcode.js"></script>
    </body>
</html>