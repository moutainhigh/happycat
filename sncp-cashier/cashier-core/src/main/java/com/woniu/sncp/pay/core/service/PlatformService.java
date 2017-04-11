package com.woniu.sncp.pay.core.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.ocp.utils.ProxoolUtil;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.http.IpUtils;
import com.woniu.sncp.pay.core.service.monitor.MonitorMessageService;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.tools.IPRangeValidator;

/**
 * 支付平台服务类
 * 
 * @author luzz
 *
 */
@Service("platformService")
public class PlatformService {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private BaseSessionDAO sessionDao;
	
	@Resource
	private MonitorMessageService monitorMessageService;
	
	@Resource
	private PaymentMerchantService paymentMerchantService;
	
	/**
	 * 根据申请支付号和平台id查询平台相关信息
	 * 
	 * @param merchantId 申请支付号
	 * @param platformId 平台id
	 * @return
	 */
	public Platform queryPlatform(long merchantId,long platformId){
		Assert.assertNotSame("非法支付申请号", 0L, merchantId);
		Assert.assertNotSame("非法支付平台号", 0L, platformId);
		
		String querySql = "SELECT P.N_ID,P.S_NAME,P.S_TYPE,P.S_PAY_URL,P.S_PAY_CHECK_URL,P.S_PAY_REFUND_URL "+
							",P.S_BEHIND_URL,P.S_FRONT_URL,P.S_LIMIT_IP "+
							",D.S_MERCHANT_NO,D.S_MANAGE_USER,D.S_BACKEND_KEY,D.S_PAY_KEY,D.S_QUERY_KEY,D.S_REFOUND_KEY "+
							",D.S_PRIVATE_PWD,D.S_PUBLIC_PWD,D.S_QUERY_PWD,D.S_REFUND_PWD "+
							",D.S_PRIVATE_URL,D.S_PUBLIC_URL,D.S_QUERY_PRI_URL,D.S_REFUND_PRI_URL,D.S_QUERY_PUB_URL,D.S_REFUND_PUB_URL "+
							",D.S_STATUS,MP.S_OPERATOR_TYPE,D.N_TRANS_TIMEOUT,D.S_INFO,P.S_EXT,M.S_NAME MERCHANT_NAME,M.S_VALID_ACCOUNT "+
							"FROM SN_PAY.PAY_PLATFORM P,SN_PAY.PAY_PLATFORM_DTL D,SN_PAY.PAY_MERCHANT_PLATFORM MP,SN_PAY.PAY_MERCHANT M "+
							"WHERE P.N_ID = D.N_PLATFORM_ID "+
							"      AND D.N_ID = MP.N_PLATFORM_DTL_ID "+
							"      AND M.N_ID = MP.N_MERCHANT_ID "+
							"      AND P.S_STATUS = '1' "+
							"      AND D.S_STATUS = '1' "+
							"      AND MP.S_STATUS = '1' "+
							"      AND MP.N_MERCHANT_ID = ?  "+
							"      AND D.N_PLATFORM_ID = ? ";
		
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
		List<Platform> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{merchantId,platformId},new RowMapper<Platform>() {
            @Override
            public Platform mapRow(ResultSet rs, int rowNum) throws SQLException {
            	Platform ret = new Platform();
            	ret.setPlatformId(rs.getLong("N_ID"));
            	ret.setName(rs.getString("S_NAME"));
            	ret.setType(rs.getString("S_TYPE"));
            	ret.setPayUrl(rs.getString("S_PAY_URL"));
            	ret.setPayCheckUrl(rs.getString("S_PAY_CHECK_URL"));
            	ret.setPayRefundUrl(rs.getString("S_PAY_REFUND_URL"));
            	
            	ret.setBehindUrl(rs.getString("S_BEHIND_URL"));
            	ret.setFrontUrl(rs.getString("S_FRONT_URL"));
            	ret.setLimitIp(rs.getString("S_LIMIT_IP"));
            	
            	ret.setMerchantNo(rs.getString("S_MERCHANT_NO"));
            	ret.setManageUser(rs.getString("S_MANAGE_USER"));
            	ret.setBackendKey(rs.getString("S_BACKEND_KEY"));
            	ret.setPayKey(rs.getString("S_PAY_KEY"));
            	ret.setQueryKey(rs.getString("S_QUERY_KEY"));
            	ret.setRefundKey(rs.getString("S_REFOUND_KEY"));
            	
            	ret.setPrivatePassword(rs.getString("S_PRIVATE_PWD"));
            	ret.setPublicPassword(rs.getString("S_PUBLIC_PWD"));
            	ret.setQueryPassword(rs.getString("S_QUERY_PWD"));
            	ret.setRefundPassword(rs.getString("S_REFUND_PWD"));
            	
            	ret.setPrivateUrl(rs.getString("S_PRIVATE_URL"));
            	ret.setPublicUrl(rs.getString("S_PUBLIC_URL"));
            	ret.setQueryPrivateUrl(rs.getString("S_QUERY_PRI_URL"));
            	ret.setRefundPrivateUrl(rs.getString("S_REFUND_PRI_URL"));
            	ret.setQueryPublicUrl(rs.getString("S_QUERY_PUB_URL"));
            	ret.setRefundPublicUrl(rs.getString("S_REFUND_PUB_URL"));
            	
            	ret.setStatus(rs.getString("S_STATUS"));
            	
            	ret.setOperatorType(rs.getString("S_OPERATOR_TYPE"));
            	
            	ret.setTransTimeout(rs.getLong("N_TRANS_TIMEOUT"));//订单支付超时时间
            	ret.setExtend(rs.getString("S_INFO"));//商户信息扩展字段
            	ret.setPlatformExt(rs.getString("S_EXT"));//渠道技术信息扩展字段
            	ret.setMerchantName(rs.getString("MERCHANT_NAME"));//商户名称
            	ret.setValidAccount(rs.getString("S_VALID_ACCOUNT"));//有值且为1 校验账号信息；空或其他不校验
                return ret;
            }
        });
		
		return result.isEmpty()?null:result.get(0);
	}
	
	/**
	 * 验证平台信息
	 * 
	 * @param platform
	 * @throws ValidationException
	 */
	public void validatePaymentPlatform(Platform platform) throws ValidationException{
		Assert.notNull(platform, "支付平台不存在或被禁用");
	}
	
	/**
	 * 验证远程服务器IP
	 * 
	 * @param limitIp
	 * @param remoteIp
	 * @throws ValidationException
	 */
	public void validateIp(long platformId,String limitIp, String remoteIp) throws ValidationException{
		if (StringUtils.isNotBlank(limitIp)
                && !IPRangeValidator.isValid(limitIp, remoteIp)) {
            if (logger.isInfoEnabled())
                logger.info("平台:" + platformId + "IP受限,legalIp:" + limitIp + ",remoteIp:" + remoteIp);

            //modified by fuzl@snail.com 增加内网ip和当前应用域名信息
    		String localIp = IpUtils.getLoaclAddr();// 对方服务器IP
    		String serverName = "";
    		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    		if(null!=request){
    			serverName = request.getServerName();//当前应用域名
    		}else{
    			serverName = "cashier.woniu.com"; 
    		}
            String alertMsg = "所在应用:["+serverName+"],服务器:["+localIp +":"+ ProxoolUtil.getTomcatPort() + "@" + ProxoolUtil.getPid() +"],\n收银台支付平台ID：" + platformId + "访问IP受限,调用者IP:" + remoteIp;
            logger.info(alertMsg);
            monitorMessageService.sendMsg(alertMsg);
            throw new ValidationException("支付平台IP受限");
        }
	}
	
	/**
	 * 验证远程服务器IP
	 * @param inParams
	 * @param limitIp
	 * @param remoteIp
	 * @throws ValidationException
	 */
	public void validateIp(Map<String,Object> inParams,long platformId,String limitIp, String remoteIp) throws ValidationException{
		if (StringUtils.isNotBlank(limitIp)
				&& !IPRangeValidator.isValid(limitIp, remoteIp)) {
			
			String merchantId = String.valueOf(inParams.get("merchantid"));
			//获取商户信息
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(Long.valueOf(merchantId));
			if(payemntMerchnt == null){
				throw new ValidationException("非法支付申请号");
			}
			
			if (logger.isInfoEnabled())
				logger.info("商户号:"+merchantId+",联系人:"+payemntMerchnt.getContact()+",平台:" + platformId + "IP受限,legalIp:" + limitIp + ",remoteIp:" + remoteIp);
			
			//modified by fuzl@mysnail.com 增加内网ip和当前应用域名信息
			String localIp = IpUtils.getLoaclAddr();// 对方服务器IP
			String serverName = "";
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
			if(null!=request){
				serverName = request.getServerName();//当前应用域名
			}else{
				serverName = "cashier.woniu.com"; 
			}
			String alertMsg = "所在应用:["+serverName+"],服务器:["+localIp +":"+ ProxoolUtil.getTomcatPort() + "@" + ProxoolUtil.getPid() +"],\n收银台商户号:"+merchantId+",联系人:"+payemntMerchnt.getContact()+",支付平台ID：" + platformId + "访问IP受限,调用者IP:" + remoteIp;
			logger.info(alertMsg);
			monitorMessageService.sendMsg(alertMsg);
			throw new ValidationException("支付平台IP受限");
		}
	}
	
	
	
	/**
	 * 根据申请支付号和平台id和订单表渠道商户号查询平台相关信息
	 * 
	 * @param merchantId 申请支付号
	 * @param platformId 平台id
	 * @param merchantNo 渠道商户号
	 * @return
	 */
	public Platform queryPlatform(long merchantId,long platformId,String merchantNo){
		Assert.assertNotSame("非法支付申请号", 0L, merchantId);
		Assert.assertNotSame("非法支付平台号", 0L, platformId);
		Assert.assertNotSame("非法渠道商户号", "", merchantNo);
		
		String querySql = "SELECT P.N_ID,P.S_NAME,P.S_TYPE,P.S_PAY_URL,P.S_PAY_CHECK_URL,P.S_PAY_REFUND_URL "+
							",P.S_BEHIND_URL,P.S_FRONT_URL,P.S_LIMIT_IP "+
							",D.S_MERCHANT_NO,D.S_MANAGE_USER,D.S_BACKEND_KEY,D.S_PAY_KEY,D.S_QUERY_KEY,D.S_REFOUND_KEY "+
							",D.S_PRIVATE_PWD,D.S_PUBLIC_PWD,D.S_QUERY_PWD,D.S_REFUND_PWD "+
							",D.S_PRIVATE_URL,D.S_PUBLIC_URL,D.S_QUERY_PRI_URL,D.S_REFUND_PRI_URL,D.S_QUERY_PUB_URL,D.S_REFUND_PUB_URL "+
							",D.S_STATUS,MP.S_OPERATOR_TYPE,D.N_TRANS_TIMEOUT,D.S_INFO,P.S_EXT "+
							"FROM SN_PAY.PAY_PLATFORM P,SN_PAY.PAY_PLATFORM_DTL D,SN_PAY.PAY_MERCHANT_PLATFORM MP "+
							"WHERE P.N_ID = D.N_PLATFORM_ID "+
							"      AND D.N_ID = MP.N_PLATFORM_DTL_ID "+
							"      AND P.S_STATUS = '1' "+
							"      AND D.S_STATUS = '1' "+
							"      AND MP.N_MERCHANT_ID = ?  "+
							"      AND D.N_PLATFORM_ID = ? "+
							"      AND D.S_MERCHANT_NO = ? ";
		
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
		List<Platform> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{merchantId,platformId,merchantNo},new RowMapper<Platform>() {
            @Override
            public Platform mapRow(ResultSet rs, int rowNum) throws SQLException {
            	Platform ret = new Platform();
            	ret.setPlatformId(rs.getLong("N_ID"));
            	ret.setName(rs.getString("S_NAME"));
            	ret.setType(rs.getString("S_TYPE"));
            	ret.setPayUrl(rs.getString("S_PAY_URL"));
            	ret.setPayCheckUrl(rs.getString("S_PAY_CHECK_URL"));
            	ret.setPayRefundUrl(rs.getString("S_PAY_REFUND_URL"));
            	
            	ret.setBehindUrl(rs.getString("S_BEHIND_URL"));
            	ret.setFrontUrl(rs.getString("S_FRONT_URL"));
            	ret.setLimitIp(rs.getString("S_LIMIT_IP"));
            	
            	ret.setMerchantNo(rs.getString("S_MERCHANT_NO"));
            	ret.setManageUser(rs.getString("S_MANAGE_USER"));
            	ret.setBackendKey(rs.getString("S_BACKEND_KEY"));
            	ret.setPayKey(rs.getString("S_PAY_KEY"));
            	ret.setQueryKey(rs.getString("S_QUERY_KEY"));
            	ret.setRefundKey(rs.getString("S_REFOUND_KEY"));
            	
            	ret.setPrivatePassword(rs.getString("S_PRIVATE_PWD"));
            	ret.setPublicPassword(rs.getString("S_PUBLIC_PWD"));
            	ret.setQueryPassword(rs.getString("S_QUERY_PWD"));
            	ret.setRefundPassword(rs.getString("S_REFUND_PWD"));
            	
            	ret.setPrivateUrl(rs.getString("S_PRIVATE_URL"));
            	ret.setPublicUrl(rs.getString("S_PUBLIC_URL"));
            	ret.setQueryPrivateUrl(rs.getString("S_QUERY_PRI_URL"));
            	ret.setRefundPrivateUrl(rs.getString("S_REFUND_PRI_URL"));
            	ret.setQueryPublicUrl(rs.getString("S_QUERY_PUB_URL"));
            	ret.setRefundPublicUrl(rs.getString("S_REFUND_PUB_URL"));
            	
            	ret.setStatus(rs.getString("S_STATUS"));
            	
            	ret.setOperatorType(rs.getString("S_OPERATOR_TYPE"));
            	
            	ret.setTransTimeout(rs.getLong("N_TRANS_TIMEOUT"));//订单支付超时时间
            	ret.setExtend(rs.getString("S_INFO"));//商户信息扩展字段
            	ret.setPlatformExt(rs.getString("S_EXT"));//渠道技术信息扩展字段
                return ret;
            }
        });
		
		return result.isEmpty()?null:result.get(0);
	}
}
