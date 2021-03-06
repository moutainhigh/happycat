package com.woniu.sncp.pay.core.service.payment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.common.memcache.MemcacheCluster;
import com.woniu.pay.common.utils.PaymentConstant;
 import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.Constant;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.ContentUtils;
import com.woniu.sncp.pay.core.service.CorePassportService;
import com.woniu.sncp.pay.core.service.GameManagerService;
import com.woniu.sncp.pay.core.service.PaymentDiscountService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PaymentService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.wap.AlipayWapAppPayment;
import com.woniu.sncp.pay.core.service.payment.process.PaymentProcess;
import com.woniu.sncp.pay.repository.pay.Game;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.payment.PaymentOrderDiscount;
import com.woniu.sncp.pojo.payment.PaymentOrderDiscountRecord;
import com.woniu.sncp.web.IpUtils;


/**
 * 支付接口流程
 * 
 * @author luzz
 *
 */
@Service("paymentFacade")
public class PaymentFacade {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Resource
	private CorePassportService corePassportService;
	
	@Resource
	private PaymentOrderService paymentOrderService;
	
	@Resource
	private GameManagerService gameManagerService;
	
	@Resource
	private PaymentService paymentService;
	
	@Resource
	private PlatformService platformService;
	@Resource
	private PaymentDiscountService paymentDiscountService;
	/**
	 * 折扣变化金额
	 * @param merchantId
	 * @param paymentId
	 * @param money
	 * @return
	 */
	public Map<String, Object> discount (long merchantId, long paymentId, String money) {
		Map<String, Object> result = new HashMap<String, Object>();

		
		BigDecimal min=null;
		PaymentOrderDiscount data=null;
		List<PaymentOrderDiscount> list = paymentDiscountService.queryOrderDiscount(merchantId, paymentId);

		for (PaymentOrderDiscount discount : list) {

			BigDecimal _value = null;

			float value = discount.getValue();

			if (StringUtils.equals(discount.getCalcStandard(), PaymentOrderDiscount.PERSENT_STANDARD)) {// 按百分比计算
				value=value/100f;
				if (StringUtils.equals(discount.getDiscountType(), PaymentOrderDiscount.FEE_TYPE)) {// 手续费
					_value = new BigDecimal(money).multiply(new BigDecimal(value));
				} else if (StringUtils.equals(discount.getDiscountType(), PaymentOrderDiscount.DISCOUNT_TYPE)) {// 折扣
					_value = new BigDecimal(money).multiply(new BigDecimal(value)).multiply(new BigDecimal(-1));
				}

			} else if (StringUtils.equals(discount.getCalcStandard(), PaymentOrderDiscount.FIXED_STANDARD)) {// 固定金额
				if (StringUtils.equals(discount.getDiscountType(), PaymentOrderDiscount.FEE_TYPE)) {// 手续费
					_value = new BigDecimal(value);
				} else if (StringUtils.equals(discount.getDiscountType(), PaymentOrderDiscount.DISCOUNT_TYPE)) {// 折扣
					_value = new BigDecimal(value).multiply(new BigDecimal(-1));
				}
			}
			if(min==null) {
				min=_value;
				data=discount;
			}else	if(_value.compareTo(min)<0) {
				min=_value;
				data=discount;
			}
		
		}
		result.put("data", data);  
		result.put("value", min==null?new BigDecimal(0):min);
		return result;
 	}
	
	
 
	 
 
	protected 	Map<String,Object > discountOrder(long merchantId, long paymentId,AbstractPayment actualPayment,	String orderMoney ,Map<String, Object> extendParams ) {
		Map<String,Object >result=new HashMap<String,Object>();
		Map<String,Object > discountMap=null;
		String  money=new BigDecimal(orderMoney).setScale(2,	BigDecimal.ROUND_HALF_UP).toString();
  		 
			discountMap = discount(merchantId, paymentId, money);

		 
 
		PaymentOrderDiscount discount = (PaymentOrderDiscount) discountMap.get("data");
 
		
		if(discount!=null) {
			BigDecimal moneyChange = discountMap.get("value")==null? new BigDecimal(0):(BigDecimal) discountMap.get("value");

			BigDecimal payMoney = new BigDecimal(money).add(moneyChange );
			// 最低支付1分钱
			if (payMoney.compareTo(new BigDecimal(0.01)) < 0) {
				payMoney = new BigDecimal(0.01);
			}
			
			result.put("discount", discount);			
			result.put("orderMoney", Float.valueOf(payMoney.floatValue()));

		}else {
			result.put("discount", discount);			
			result.put("orderMoney", Float.valueOf(money));
		}
	
		return  result;

	}
	
	
	
	/**
	 * 创建订单，保存第三方回调地址，第三方前台地址，第三方订单号
	 * @param pOrderNo
	 * @param merchantId
	 * @param paymentId
	 * @param money
	 * @param productName
	 * @param account
	 * @param gameId
	 * @param imprestMode
	 * @param clientIp
	 * @param extendParams  
	 * @param body
	 * @param goodsDetail
	 * @param terminalType
	 * @param timeoutExpress
	 * @return
	 */
	public Map<String, Object> createOrder(String pOrderNo,long merchantId, long paymentId,
			String money, String productName,long aid,String account,String loginAccount, long gameId,
			String imprestMode, String clientIp,Map<String, Object> extendParams,String moneyCurrency,String body,String goodsDetail,String terminalType,String timeoutExpress) {
		
		Map<String, Object> outParams = null;
		Platform platform=null;
		PaymentOrder paymentOrder=null;
		try {

			// 1.查询支付平台信息
			platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId));
			platformService.validatePaymentPlatform(platform);
			
			// 2.根据平台扩展判断是否需要调用远程服务
			String merchantExt = platform.getExtend();
			// 是否调用远端服务
			Boolean callPayRemoteFlag = false;
			if(StringUtils.isNotEmpty(merchantExt)){
				JSONObject extJson = JSONObject.parseObject(merchantExt);
				// callPayRemoteFlag 0 不要远程服务，1 需要远程服务
				if(extJson.containsKey("callPayRemote") && StringUtils.isNotEmpty(extJson.getString("callPayRemote"))){
					if(extJson.getString("callPayRemote").equals("1")){
						callPayRemoteFlag = true;
					};
				}
			}
			
			
			// 3.获取实际支付平台对象
			AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentId);
			Assert.notNull(actualPayment,"抽象支付平台配置不正确,查询平台为空,paymentId:" + paymentId);
			
			
			Map<String,Object > discountOrder=discountOrder(merchantId, paymentId, actualPayment, money, extendParams);//计算手续费

			// 4.判断订单是否生成
			String defaultbank = ObjectUtils.toString(extendParams.get("defaultbank"));
			paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(pOrderNo,Long.valueOf(merchantId));
			if (paymentOrder == null) {
				MemcacheCluster.getInstance().setList(pOrderNo, "新订单支付");

				String backendUrl = ObjectUtils.toString(extendParams.get("backendurl"));
				String fontendurl = ObjectUtils.toString(extendParams.get("fontendurl"));
				
				paymentOrder = new PaymentOrder();
				// a.查询帐号及判断
				// 通过收银台申请商户号是否开启账号功能判断是否保存账号信息
				if(StringUtils.isNotBlank(platform.getValidAccount()) &&
						platform.getValidAccount().equals(Platform.VALID_ACCOUNT)){
					if(aid>0){
						paymentOrder.setAid(aid);
					}else{
						
						Map<String, Object> result= ErrorCode.put(ErrorCode.getErrorCode(10001), ErrorCode.TIP_INFO,ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO));
						MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));

						return result;
					 
					}
				}else{
					if(StringUtils.isNotBlank(account)){
						//不验证account
						if(aid>0){
							paymentOrder.setAid(aid);
						}else{
							Passport passport = null;
							try {
								passport = corePassportService.queryPassport(account);
							} catch (Exception e) {
								
								Map<String, Object> result=ErrorCode.put(ErrorCode.getErrorCode(20110), ErrorCode.TIP_INFO, e.getMessage());
								MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));

								return result;
								  
							}
							corePassportService.validatepassport(passport);
							paymentOrder.setAid(passport.getId());
						}
						String ext = ObjectUtils.toString(extendParams.get("ext"));
						if(StringUtils.isNotBlank(ext)){
							JSONObject extend = JSONObject.parseObject(ext);
							extend.put("account", account);
							extendParams.put("ext", extend);
						}else{
							JSONObject extend = new JSONObject();
							extend.put("account", account);
							extendParams.put("ext", extend);
						}
					}else{
						//不验证account
						if(aid>0){
							paymentOrder.setAid(aid);
						}else{
							paymentOrder.setAid(-1L);
						}
					}
				}
				
				//登录账号
				if(StringUtils.isNotBlank(loginAccount) && !"0".equals(loginAccount)){
					// 切换帐号库
					Passport passport = null;
					try {
						passport = corePassportService.queryPassport(loginAccount);
					} catch (Exception e) {
						Map<String, Object> result=ErrorCode.put(ErrorCode.getErrorCode(20110), ErrorCode.TIP_INFO, e.getMessage());
						MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));

						return result;
					 
					}
					corePassportService.validatepassport(passport);
					paymentOrder.setLoginAid(passport.getId());
				}else{
					paymentOrder.setLoginAid(0L);
				}
				
				paymentOrder.setAmount(1);
				paymentOrder.setPayPlatformId(platform.getPlatformId());
				paymentOrder.setCardTypeId(0L);
				paymentOrder.setIp(IpUtils.ipToLong(clientIp));
				paymentOrder.setMerchantId(merchantId);

				if(gameId != 0L){
					// b.查询游戏及判断
					Game game = gameManagerService.queryGameById(gameId);
					Assert.notNull(game, "充值所属游戏不存在");
					Assert.assertEquals("充值游戏被禁用", Game.GAME_STATE_OPEN,game.getState());
					
					paymentOrder.setCurrency(game.getCurrency()); // 游戏币种
					paymentOrder.setGameId(game.getId());
				} else {
					paymentOrder.setCurrency("0"); // 游戏币种
					paymentOrder.setGameId(0L);
				}
				
				paymentOrder.setGareaId(0L); // 表示充往中心
				paymentOrder.setGiftGareaId(0L); // 道具送往分区
				paymentOrder.setMoney(Float.valueOf(money));
				
				paymentOrder.setMoney((Float)discountOrder.get("orderMoney"));				
				PaymentOrderDiscount		discount=(PaymentOrderDiscount)discountOrder.get("discount");
				PaymentOrderDiscountRecord discountRecord=null;
				if(discount!=null) {
					paymentOrder.setMoneyDiscountBefore(Float.valueOf(money));
					paymentOrder.setMoneyDiscountAfter(paymentOrder.getMoney());
					paymentOrder.setMoneyDiscountChange(paymentOrder.getMoneyDiscountAfter()-paymentOrder.getMoneyDiscountBefore());
					
					
					discountRecord = new PaymentOrderDiscountRecord();
					discountRecord.setDiscountId(discount.getId());
					discountRecord.setMoney(paymentOrder.getMoneyDiscountChange());
					discountRecord.setPaymentId(paymentId);
					
					
				}else {
					paymentOrder.setMoneyDiscountBefore(paymentOrder.getMoney());
					paymentOrder.setMoneyDiscountAfter(paymentOrder.getMoney());
					paymentOrder.setMoneyDiscountChange(0f);
				 
				}
				
				

				paymentOrder.setState(PaymentOrder.IMPREST_STATE_NOT_COMPLETED); // 状态
				paymentOrder.setPayState(PaymentOrder.PAYMENT_STATE_CREATED);

//				paymentOrder.setMoneyCurrency(actualPayment.getMoneyCurrency()); // 实际支付币种
				paymentOrder.setMoneyCurrency(moneyCurrency);
				paymentOrder.setImprestMode(imprestMode); // 存储在订单中的支付模式
				
				paymentOrder.setPaypartnerBackendCall(backendUrl);
				paymentOrder.setPaypartnerFrontCall(fontendurl);
				paymentOrder.setPaypartnerOtherOrderNo(pOrderNo);
				
				paymentOrder.setProductname(productName);//产品名称,订单标题
				paymentOrder.setBody(body);//交易说明
				paymentOrder.setGoodsDetail(goodsDetail);//商品详细说明
				paymentOrder.setTerminalType(StringUtils.isBlank(terminalType)?PaymentConstant.TERMINALTYPE_PC:terminalType);//终端类型
				
				//增加扩展参数,处理手游走消息推送逻辑,ext必须是json格式
				String ext = ObjectUtils.toString(extendParams.get("ext"));
				if(StringUtils.isNotBlank(ext)){
					JSONObject extend = JSONObject.parseObject(ext);
					paymentOrder.setInfo(ObjectUtils.toString(extend));
					
					//增加设置分区id
					if(extend.containsKey("serverId") && StringUtils.isNotBlank(ObjectUtils.toString(extend.get("serverId")))){
						if(StringUtils.isNumeric(ObjectUtils.toString(extend.get("serverId")))){
							paymentOrder.setGareaId(NumberUtils.toLong(ObjectUtils.toString(extend.get("serverId"))));
						}
					}
				}
				
				//增加渠道商户号
				paymentOrder.setMerchantNo(platform.getMerchantNo());
				paymentOrder.setMerchantName(platform.getMerchantName());
				
				paymentOrderService.createOrderAndGenOrderNo(paymentOrder,7L,timeoutExpress,discountRecord);
			} else {
				MemcacheCluster.getInstance().setList(pOrderNo, "接口再次请求支付");

				//modified by fuzl 充值方式无法切换问题解决
//				Assert.assertEquals("金额与订单中的金额不一致，请重新核对",paymentOrder.getMoney(), Float.valueOf(money));
				Assert.assertEquals("金额与订单中的金额不一致，请重新核对",paymentOrder.getMoney(), Float.valueOf((Float)discountOrder.get("orderMoney")));

//				Assert.assertEquals("支付平台ID与订单中平台ID不一致，请重新核对",paymentOrder.getPlatformId(), paymentId);
				if(PaymentOrder.PAYMENT_STATE_PAYED.equals(paymentOrder.getPayState())){
					throw new OrderIsSuccessException("订单已支付，请重新核对");
				} else if(PaymentConstant.PAYMENT_STATE_CANCEL.equals(paymentOrder.getPayState())){
					throw new IllegalArgumentException("订单已取消，请重新核对");
				} 
//				else if(PaymentConstant.PAYMENT_STATE_PROCESS.equals(paymentOrder.getPaymentState())){
//					throw new IllegalArgumentException("订单已提交，请重新核对");
//				}
				
				//modified by  fuzl 20161103
				//fixed 切换平台账号无法保存,兔兔币支付单号格式修改
				// a.查询帐号及判断
				if(StringUtils.isNotBlank(platform.getValidAccount()) &&
						platform.getValidAccount().equals(Platform.VALID_ACCOUNT)){
					if(aid>0){
						paymentOrder.setAid(aid);
					}else{
						
						Map<String, Object> result=ErrorCode.put(ErrorCode.getErrorCode(10001), ErrorCode.TIP_INFO,ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO));
						MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));

						return result;
					 
					}
				}else{
					if(StringUtils.isNotBlank(account)){
						//不验证account
						if(aid>0){
							paymentOrder.setAid(aid);
						}else{
							Passport passport = null;
							try {
								passport = corePassportService.queryPassport(account);
							} catch (Exception e) {
								
								Map<String, Object> result=ErrorCode.put(ErrorCode.getErrorCode(20110), ErrorCode.TIP_INFO, e.getMessage());
								MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));

								return result;
							 
							}
							corePassportService.validatepassport(passport);
							paymentOrder.setAid(passport.getId());
						}
						String ext = ObjectUtils.toString(extendParams.get("ext"));
						if(StringUtils.isNotBlank(ext)){
							JSONObject extend = JSONObject.parseObject(ext);
							extend.put("account", account);
							extendParams.put("ext", extend);
						}else{
							JSONObject extend = new JSONObject();
							extend.put("account", account);
							extendParams.put("ext", extend);
						}
					}else{
						//不验证account
						if(aid>0){
							paymentOrder.setAid(aid);
						}else{
							paymentOrder.setAid(-1L);
						}
					}
				}
				
				//登录账号
				if(StringUtils.isNotBlank(loginAccount) && !"0".equals(loginAccount)){
					// 切换帐号库
					Passport passport = null;
					try {
						passport = corePassportService.queryPassport(loginAccount);
					} catch (Exception e) {
						
						
						
						Map<String, Object> result=ErrorCode.put(ErrorCode.getErrorCode(20110), ErrorCode.TIP_INFO, e.getMessage());
						MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));

						return result;
 					}
					corePassportService.validatepassport(passport);
					paymentOrder.setLoginAid(passport.getId());
				}else{
					paymentOrder.setLoginAid(0L);
				}
				
				if(gameId != 0L){
					// b.查询游戏及判断
//					DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
					Game game = gameManagerService.queryGameById(gameId);
					Assert.notNull(game, "充值所属游戏不存在");
					Assert.assertEquals("充值游戏被禁用", Game.GAME_STATE_OPEN,game.getState());
					
					paymentOrder.setCurrency(game.getCurrency()); // 游戏币种
					paymentOrder.setGameId(game.getId());
				} else {
					paymentOrder.setCurrency("0"); // 游戏币种
					paymentOrder.setGameId(0L);
				}
				
				if(paymentOrder.getPayPlatformId() != 4001 && paymentId == 4001){
					//4001.兔兔币支付，4002.翡翠币web，4003.翡翠币wap
					String newPattern = paymentOrder.getMerchantId().toString()+ "-" + ObjectUtils.toString(paymentId);
					
					String oldOrderNo = paymentOrder.getOrderNo();
					
					String oldPattern = oldOrderNo.substring(oldOrderNo.indexOf("-")+1, oldOrderNo.indexOf("-")+5);
					// 新的订单号
					String newOrderNo = org.springframework.util.StringUtils.replace(oldOrderNo, oldPattern, newPattern);
					
					paymentOrder.setOrderNo(newOrderNo);
					
				}
				
				if((paymentOrder.getPayPlatformId() != 4011 && paymentId == 4011) || (paymentOrder.getPayPlatformId() != 4012 && paymentId == 4012) 
						|| (paymentOrder.getPayPlatformId() != 4013 && paymentId == 4013) || (paymentOrder.getPayPlatformId() != 4014 && paymentId == 4014)){
					//4011.PC兔兔币支付,4012.wap兔兔币支付,4013.android兔兔币支付,4014.ios兔兔币支付
					String newPattern = paymentOrder.getMerchantId().toString()+ "-" + ObjectUtils.toString(paymentId);
					String oldOrderNo = paymentOrder.getOrderNo();
					String oldPattern = oldOrderNo.substring(oldOrderNo.indexOf("-")+1, oldOrderNo.indexOf("-")+5);
					// 新的订单号
					String newOrderNo = org.springframework.util.StringUtils.replace(oldOrderNo, oldPattern, newPattern);
					paymentOrder.setOrderNo(newOrderNo);
				}
				
				paymentOrder.setMoneyCurrency(moneyCurrency); // 支付币种
				
				paymentOrder.setPayPlatformId(paymentId);// 更换支付方式
				//增加渠道商户号
				paymentOrder.setMerchantNo(platform.getMerchantNo());
				paymentOrder.setMerchantName(platform.getMerchantName());
				
				//增加扩展参数,处理手游走消息推送逻辑,ext必须是json格式
				String ext = ObjectUtils.toString(extendParams.get("ext"));
				if(StringUtils.isNotBlank(ext)){
					JSONObject extend = JSONObject.parseObject(ext);
					paymentOrder.setInfo(ObjectUtils.toString(extend));
					
					//增加设置分区id
					if(extend.containsKey("serverId") && StringUtils.isNotBlank(ObjectUtils.toString(extend.get("serverId")))){
						if(StringUtils.isNumeric(ObjectUtils.toString(extend.get("serverId")))){
							paymentOrder.setGareaId(NumberUtils.toLong(ObjectUtils.toString(extend.get("serverId"))));
						}
					}
				}
				
				paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_CREATED ,PaymentOrder.IMPREST_STATE_NOT_COMPLETED);
			}

			// 4.封装支付
			// a.结果信息map
			// b.提交给对方平台地址 - payUrl 和 支付编码 - acceptCharset
			// c.提交给对方平台信息 - paymentParams
			// d.用于前台显示信息，如游戏-分区-订单-充值平台-直充银行等等
			Map<String, Object> inParams = new HashMap<String, Object>();
			inParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
			inParams.put(PaymentConstant.PAYMENT_PLATFORM, platform);
			inParams.put("productName", productName); // 产品名称
			inParams.put("defaultbank", defaultbank);
//			inParams.put("account", passport.getAccount());
			inParams.put(PaymentConstant.CLIENT_IP, clientIp);
			inParams.putAll(extendParams);

			Map<String, Object> paymentParams = null;
			if(callPayRemoteFlag){
				//5.请求远端服务,提交cbss-api处理第三方平台支付请求,
				inParams.put("extendParams",extendParams);
				MemcacheCluster.getInstance().setList(pOrderNo, "开始远程调用Payment,"+ContentUtils.safeLogJson(inParams));

				paymentParams = actualPayment.callPay(inParams);
			}else{
	 		    MemcacheCluster.getInstance().setList(pOrderNo, "开始本地调用Payment,"+ContentUtils.safeLogJson(inParams));

				paymentParams = actualPayment.orderedParams(inParams);
			}
 		    MemcacheCluster.getInstance().setList(pOrderNo, "调用Payment返回:"+ContentUtils.safeLogJson(paymentParams));

			String oppositeOrderNo=paymentParams!=null?(String)paymentParams.get(PaymentConstant.OPPOSITE_ORDERNO):null;
			String oppositeCurrency=paymentParams!=null?(String)paymentParams.get(PaymentConstant.OPPOSITE_CURRENCY):null;
			boolean flag=false;
			if(StringUtils.isNotBlank(oppositeOrderNo)){
				paymentOrder.setOtherOrderNo(oppositeOrderNo);
				flag=true;
			}

			if(StringUtils.isNotBlank(oppositeCurrency)){
				paymentOrder.setMoneyCurrency(oppositeCurrency);
				flag=true;
			}
			if(flag) {
				paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_CREATED ,PaymentOrder.IMPREST_STATE_NOT_COMPLETED);

			}
			// 操作成功，用于给上层判断
			outParams = ErrorCode.getErrorCode(1);

			// 提交给对方平台的URL
			outParams.put("payUrl", paymentParams.get("payUrl"));
			// 提交给对方平台的编码 - 默认为UTF-8
			String charset = (String) paymentParams.get("acceptCharset");
			outParams.put("acceptCharset",StringUtils.isBlank(charset) ? "utf-8" : charset);
			// 从提交给支付平台参数中去除提交第三方的编码
			// 银行直连银行名称
			outParams.put("defaultBankName",(String) paymentParams.get("defaultBankName")); // 支付宝直连的银行名称
			outParams.put("method", paymentParams.get("method"));

			// 从提交给支付平台参数中去除无用参数
			if( actualPayment instanceof AlipayWapAppPayment){
				//不需要删除此参数,新增pName
				outParams.put("pName", "AlipayWapAppPayment");
			}else{
				paymentParams.remove("method");
			}
			paymentParams.remove("payUrl");
			paymentParams.remove("acceptCharset");
			paymentParams.remove("defaultBankName");
			if(ObjectUtils.equals(paymentParams.get(ErrorCode.TIP_CODE), ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE))){
				paymentParams.remove(ErrorCode.TIP_CODE);
				paymentParams.remove(ErrorCode.TIP_INFO);
			}

			outParams.put("paymentParams", paymentParams); // 返回给支付平台的参数
			outParams.put("paymentPlatform", platform); // 支付平台POJO
			outParams.put("paymentOrder", paymentOrder); // 订单信息

		} catch (DataAccessException e) {
			logger.error("数据库操作异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(14102), ErrorCode.ERROR_INFO, e.getMessage());
			
			 
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			return result;
 		} catch(OrderIsSuccessException e){
			logger.error("OrderIsSuccessException异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(56014), ErrorCode.TIP_INFO, e.getMessage());
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			
		 
			
			result.put("paymentPlatform", platform); // 支付平台POJO
			result.put("paymentOrder", paymentOrder); // 订单信息
			return result;
		} catch (IllegalArgumentException e) {
			logger.error("validation异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(14101), ErrorCode.TIP_INFO, e.getMessage());
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			return result;
		} catch (Exception e) {
			logger.error("未知异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(10002), ErrorCode.ERROR_INFO, e.getMessage());
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			return result;
		}
		return outParams;
	}
	
	/**
	 * <pre>
	 * 	<h1>组合支付</h1>
	 *  eg. 支付宝 + 余额 
	 *  
	 *  组合支付流程 ：
	 *  1.先充值到余额
	 *  2.扣除余额
	 * </pre>
	 * 
	 * @param pOrderNo
	 * @param gameId
	 * @param account
	 * @param money
	 * @param paymentId
	 * @param productName
	 * @param imprestMode
	 * @param clientIp
	 * @param extendParams
	 * @return
	 */
	public Map<String, Object> createOrder(String pOrderNo,long merchantId, long paymentId,
			String money, String yueCurrency,String yueMoney, String productName,String account, long gameId,
			String imprestMode, String clientIp,Map<String, Object> extendParams,String body,String goodsDetail,String terminalType,String timeoutExpress) {
		
		Map<String, Object> outParams = null;
		PaymentOrder paymentOrder=null;
		Platform platform=null;
		try {

			// 1.查询支付平台信息
			platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId));
			platformService.validatePaymentPlatform(platform);

			// 2.获取实际支付平台对象
			AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentId);
			Assert.notNull(actualPayment,"抽象支付平台配置不正确,查询平台为空,paymentId:" + paymentId);
			
			DecimalFormat decimalFormat = new DecimalFormat("0.00");

			String orderMoney =decimalFormat.format(Float.valueOf(money) - Float.valueOf(yueMoney));

			
			Map<String,Object> discountOrder=discountOrder(merchantId, paymentId, actualPayment, orderMoney, extendParams);//计算手续费

			// 3.判断订单是否生成
			String defaultbank = ObjectUtils.toString(extendParams.get("defaultbank"));
		    paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(pOrderNo,Long.valueOf(merchantId));
			if (paymentOrder == null) {
				MemcacheCluster.getInstance().setList(pOrderNo, "新订单支付");
				String backendUrl = ObjectUtils.toString(extendParams.get("backendurl"));
				String fontendurl = ObjectUtils.toString(extendParams.get("fontendurl"));
				
				paymentOrder = new PaymentOrder();
				// a.查询帐号及判断
				if(StringUtils.isNotBlank(account)){
					// 切换帐号库
//					DataSourceHolder.setDataSourceType(DataSourceConstants.DS_ACCOUNT_DB);
					Passport passport = null;
					try {
						passport = corePassportService.queryPassport(account);
					} catch (Exception e) {
						
						
						Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(20110), ErrorCode.TIP_INFO, e.getMessage());
						MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
						return result;
					}
					corePassportService.validatepassport(passport);
					paymentOrder.setAid(passport.getId());
				} else {
					paymentOrder.setAid(0L);
				}
				
				paymentOrder.setAmount(1);
				paymentOrder.setPayPlatformId(platform.getPlatformId());
				paymentOrder.setCardTypeId(0L);
				paymentOrder.setIp(IpUtils.ipToLong(clientIp));
				paymentOrder.setMerchantId(merchantId);

				if(gameId != 0L){
					// b.查询游戏及判断
//					DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
					Game game = gameManagerService.queryGameById(gameId);
					Assert.notNull(game, "充值所属游戏不存在");
					Assert.assertEquals("充值游戏被禁用", Game.GAME_STATE_OPEN,game.getState());
					
					paymentOrder.setCurrency(game.getCurrency()); // 游戏币种
					paymentOrder.setGameId(game.getId());
				} else {
					paymentOrder.setCurrency("0"); // 游戏币种
					paymentOrder.setGameId(0L);
				}
				
				paymentOrder.setGareaId(0L); // 表示充往中心
				paymentOrder.setGiftGareaId(0L); // 道具送往分区
//				DecimalFormat decimalFormat = new DecimalFormat("0.00");
//				Float orderMoney = Float.valueOf(decimalFormat.format(Float.valueOf(money) - Float.valueOf(yueMoney)));
//				paymentOrder.setMoney(orderMoney);
				
				paymentOrder.setMoney((Float)discountOrder.get("orderMoney"));				
				PaymentOrderDiscount		discount=(PaymentOrderDiscount)discountOrder.get("discount");
				PaymentOrderDiscountRecord discountRecord=null;
				if(discount!=null) {
					paymentOrder.setMoneyDiscountBefore(Float.valueOf(decimalFormat.format(orderMoney)));
					paymentOrder.setMoneyDiscountAfter(paymentOrder.getMoney());
					paymentOrder.setMoneyDiscountChange(paymentOrder.getMoneyDiscountAfter()-paymentOrder.getMoneyDiscountBefore());
					
					
					discountRecord = new PaymentOrderDiscountRecord();
					discountRecord.setDiscountId(discount.getId());
					discountRecord.setMoney(paymentOrder.getMoneyDiscountChange());
					discountRecord.setPaymentId(paymentId);
					
					
				}else {
					paymentOrder.setMoneyDiscountBefore(paymentOrder.getMoney());
					paymentOrder.setMoneyDiscountAfter(paymentOrder.getMoney());
					paymentOrder.setMoneyDiscountChange(0f);
				 
				}
 				
				
				
				paymentOrder.setYueMoney(Float.valueOf(yueMoney));//余额支付金额
				paymentOrder.setYueCurrency(yueCurrency);//余额币种
				paymentOrder.setYuePayState(PaymentOrder.PAYMENT_STATE_CREATED);//余额支付支付状态

				paymentOrder.setState(PaymentOrder.IMPREST_STATE_NOT_COMPLETED); // 状态
				paymentOrder.setPayState(PaymentOrder.PAYMENT_STATE_CREATED);

				paymentOrder.setMoneyCurrency(actualPayment.getMoneyCurrency()); // 实际支付币种
				paymentOrder.setImprestMode(imprestMode); // 存储在订单中的支付模式
				
				paymentOrder.setPaypartnerBackendCall(backendUrl);
				paymentOrder.setPaypartnerFrontCall(fontendurl);
				paymentOrder.setPaypartnerOtherOrderNo(pOrderNo);
				
				paymentOrder.setProductname(productName);//产品名称,订单标题
				paymentOrder.setBody(body);//交易说明
				paymentOrder.setGoodsDetail(goodsDetail);//商品详细说明
				paymentOrder.setTerminalType(StringUtils.isBlank(terminalType)?PaymentConstant.TERMINALTYPE_PC:terminalType);//终端类型
	
				//增加渠道商户号,
				paymentOrder.setMerchantNo(platform.getMerchantNo());
				paymentOrder.setMerchantName(platform.getMerchantName());
				
				paymentOrderService.createOrderAndGenOrderNo(paymentOrder,7L,timeoutExpress,discountRecord);
			} else {
				
				MemcacheCluster.getInstance().setList(pOrderNo, "接口再次请求支付");
				//modified by fuzl 充值方式无法切换问题解决
//				DecimalFormat decimalFormat = new DecimalFormat("0.00");
//				Float orderMoney = Float.valueOf(decimalFormat.format(Float.valueOf(money) - Float.valueOf(yueMoney)));
//				Assert.assertEquals("金额与订单中的金额不一致，请重新核对",paymentOrder.getMoney(), orderMoney);
 				Assert.assertEquals("金额与订单中的金额不一致，请重新核对",paymentOrder.getMoney(),  Float.valueOf(orderMoney));

//				Assert.assertEquals("支付平台ID与订单中平台ID不一致，请重新核对",paymentOrder.getPlatformId(), paymentId);
				if(PaymentOrder.PAYMENT_STATE_PAYED.equals(paymentOrder.getPayState())){
					throw new OrderIsSuccessException("订单已支付，请重新核对");
				}
				paymentOrder.setPayPlatformId(paymentId);//更换支付方式
				//增加渠道商户号,
				paymentOrder.setMerchantNo(platform.getMerchantNo());
				
				paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_CREATED ,PaymentOrder.IMPREST_STATE_NOT_COMPLETED);
				
			}

			// 4.封装支付
			// a.结果信息map
			// b.提交给对方平台地址 - payUrl 和 支付编码 - acceptCharset
			// c.提交给对方平台信息 - paymentParams
			// d.用于前台显示信息，如游戏-分区-订单-充值平台-直充银行等等
			Map<String, Object> inParams = new HashMap<String, Object>();
			inParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
			inParams.put(PaymentConstant.PAYMENT_PLATFORM, platform);
			inParams.put("productName", productName); // 产品名称
			inParams.put("defaultbank", defaultbank);
//			inParams.put("account", passport.getAccount());
			inParams.put(PaymentConstant.CLIENT_IP, clientIp);
			inParams.putAll(extendParams);

		    MemcacheCluster.getInstance().setList(pOrderNo, "开始本地调用Payment,"+ContentUtils.safeLogJson(inParams));

			Map<String, Object> paymentParams = actualPayment.orderedParams(inParams);
				 
			MemcacheCluster.getInstance().setList(pOrderNo, "调用Payment返回:"+ContentUtils.safeLogJson(paymentParams));
			String oppositeOrderNo = paymentParams != null ? (String) paymentParams.get("oppositeOrderNo") : null;
			if (StringUtils.isNotBlank(oppositeOrderNo)) {
				paymentOrder.setOtherOrderNo(oppositeOrderNo);
				paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_CREATED, PaymentOrder.IMPREST_STATE_NOT_COMPLETED);

			}
			// 操作成功，用于给上层判断
			outParams = ErrorCode.getErrorCode(1);

			// 提交给对方平台的URL
			outParams.put("payUrl", paymentParams.get("payUrl"));
			// 提交给对方平台的编码 - 默认为UTF-8
			String charset = (String) paymentParams.get("acceptCharset");
			outParams.put("acceptCharset",StringUtils.isBlank(charset) ? "utf-8" : charset);
			// 从提交给支付平台参数中去除提交第三方的编码
			// 银行直连银行名称
			outParams.put("defaultBankName",(String) paymentParams.get("defaultBankName")); // 支付宝直连的银行名称
			outParams.put("method", paymentParams.get("method"));

			// 从提交给支付平台参数中去除无用参数
			if( actualPayment instanceof AlipayWapAppPayment){
				//不需要删除此参数
			}else{
				paymentParams.remove("method");
			}
			
			paymentParams.remove("payUrl");
			paymentParams.remove("acceptCharset");
			paymentParams.remove("defaultBankName");

			outParams.put("paymentParams", paymentParams); // 返回给支付平台的参数
			outParams.put("paymentPlatform", platform); // 支付平台POJO
			outParams.put("paymentOrder", paymentOrder); // 订单信息

		} catch (DataAccessException e) {
			logger.error("数据库操作异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(14102), ErrorCode.ERROR_INFO, e.getMessage());
			
			 
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			return result;
		} catch (OrderIsSuccessException e) {
			logger.error("OrderIsSuccessException异常", e);
			Map<String, Object> result = ErrorCode.put(ErrorCode.getErrorCode(56014), ErrorCode.TIP_INFO, e.getMessage());

			String msg = ObjectUtils.toString(result.get(ErrorCode.TIP_CODE)) + ":" + ObjectUtils.toString(ErrorCode.getErrorCode(56014).get(ErrorCode.TIP_INFO)) + "OrderIsSuccessException异常";
			logger.info(pOrderNo + ":" + msg);
			MemcacheCluster.getInstance().setList(pOrderNo, msg);
			result.put("paymentPlatform", platform); // 支付平台POJO
			result.put("paymentOrder", paymentOrder); // 订单信息
			return result;
 		}catch (IllegalArgumentException e) {
			logger.error("validation异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(14101), ErrorCode.TIP_INFO, e.getMessage());
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			return result;
		} catch (Exception e) {
			logger.error("未知异常", e);
			Map<String,Object> result= ErrorCode.put(ErrorCode.getErrorCode(10002), ErrorCode.ERROR_INFO, e.getMessage());
			MemcacheCluster.getInstance().setList(pOrderNo, ObjectUtils.toString(result.get(ErrorCode.TIP_CODE))+":"+ObjectUtils.toString(ErrorCode.getErrorCode(10001).get(ErrorCode.TIP_INFO)));
			return result;
		}
		return outParams;
	}
	
	public void processPaymentBack(PaymentProcess paymentProcess, Map<String, Object> inParams){
		paymentProcess.doPay(inParams);
	}
	
	public Map<String,Object> checkOrder(PaymentProcess paymentProcess,String orderNo,Long merchantId){
		Map<String, Object> retMap = paymentProcess.doOrderCheck(orderNo,merchantId);
		PaymentOrder queryOrder = (PaymentOrder) retMap.get(PaymentConstant.PAYMENT_ORDER);
		if(queryOrder == null){
			queryOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo,merchantId);
			if(queryOrder == null){
				queryOrder = paymentOrderService.queryOrder(orderNo);
			}
		}
		retMap.put(PaymentConstant.PAYMENT_ORDER, queryOrder);
		return retMap;
	}

	public Map<String, Object> processPaymentFont(AbstractPayment actualImprestPayment, HttpServletRequest request) {
		Map<String, Object> result = ErrorCode.getErrorCode(1);
		
		// 1.获取渠道平台信息
		Platform platform = (Platform) request.getAttribute(PaymentConstant.PAYMENT_PLATFORM);
		// 是否调用远端服务,默认调用本地
		Boolean callPayRemoteFlag = false;
		//前端跳转后是否需要验证等操作处理
		boolean frontProcess = false;
		if(null != platform){
			// 2.根据平台扩展判断是否需要调用远程服务,add by fuzl@mysnail.com
			String merchantExt = platform.getExtend();
			if(StringUtils.isNotEmpty(merchantExt)){
				JSONObject extJson = JSONObject.parseObject(merchantExt);
				// callPayRemoteFlag 0 不要远程服务，1 需要远程服务
				if(extJson.containsKey("callPayRemote") && StringUtils.isNotEmpty(extJson.getString("callPayRemote"))){
					if(extJson.getString("callPayRemote").equals("1")){
						callPayRemoteFlag = true;
					};
				}
				if("1".equals(extJson.getString("frontProcess"))){
					frontProcess = true;
				};
			}
		}
		
		String orderNo = "";
		PaymentOrder order = null;
 		Boolean tag=(Boolean) request.getAttribute(Constant.FORWARD_TO_FONT);

		 if(BooleanUtils.isTrue(tag)) {
				Map<String, Object> infoMap = (Map<String, Object>) request.getAttribute("infoMap");
				if(infoMap!=null&&infoMap.get("paymentOrder")!=null) {
					order=(PaymentOrder)infoMap.get("paymentOrder");
				}
		 }  
		if (order == null) {

			if (callPayRemoteFlag) {
				// 调用远程服务
				orderNo = actualImprestPayment.callPayGetOrderNoFromRequest(platform, request);
			} else {
				// 调用本地服务
				if (frontProcess) {
					orderNo = actualImprestPayment.getOrderNoFromRequest(request, platform);
				} else {
					orderNo = actualImprestPayment.getOrderNoFromRequest(request);
				}
			}

			if (StringUtils.isBlank(orderNo)) {
				return ErrorCode.getErrorCode(14105);
			}
			try {
				order = paymentOrderService.queryOrder(orderNo);
			} catch (DataAccessException e) {
				logger.error("充值订单查询异常", e);
				result = ErrorCode.getErrorCode(14102);
				return result;
			}
		}

	
		
		if (order != null) {
			result.put(PaymentConstant.PAYMENT_ORDER,order);
//			Passport pp = corePassportService.queryPassport(order.getAid());
//			if (pp != null && pp.getAccount() != null) {
//				result.put("userName", pp.getAccount());
//			}
		} else {
			result = ErrorCode.getErrorCode(14105);
		}
		return result;
	}
	
	public Map<String,Object> cancelOrder(PaymentProcess paymentProcess, String orderNo, String merchantId,String platformId,Map<String,Object> extParams){
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		
		try{
			retMap = paymentProcess.cancelOrder(orderNo,Long.valueOf(merchantId),Long.valueOf(platformId),extParams);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			retMap = ErrorCode.getErrorCode(53110);
		}
		
		return retMap;
	}
}
