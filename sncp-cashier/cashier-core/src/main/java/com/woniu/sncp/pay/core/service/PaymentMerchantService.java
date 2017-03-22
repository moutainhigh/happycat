package com.woniu.sncp.pay.core.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.dao.PaymentMerchantDao;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentMerchantDetail;

@Service("paymentMerchantService")
public class PaymentMerchantService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	PaymentMerchantDao paymentMerchantDao;
	
	public PaymentMerchant queryPayemntMerchnt(long merchantId){
		return null;
	}
	
	public List<PaymentMerchantDetail> queryPaymentMerchantDtl(long merchantId){
		Assert.assertNotSame("非法支付申请号", 0L, merchantId);
		return paymentMerchantDao.queryPaymentMerchantDtlById(merchantId);
	}
	
//	@Autowired
//	private BaseSessionDAO sessionDao;
	
//	public PaymentMerchant queryPayemntMerchnt(long merchantId){
//		Assert.assertNotSame("非法支付申请号", 0L, merchantId);
//		
//		String querySql = "SELECT * FROM SN_IMPREST.PAY_MERCHANT M WHERE M.S_STATUS = '1' AND M.N_ID = ?";
//		
//		Map<String,Object> paramMap = new HashMap<String,Object>();
//		paramMap.put("merchantId", merchantId);
//		
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
//		List<PaymentMerchant> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{merchantId},new RowMapper<PaymentMerchant>() {
//            @Override
//            public PaymentMerchant mapRow(ResultSet rs, int rowNum) throws SQLException {
//            	PaymentMerchant ret = new PaymentMerchant();
//            	ret.setId(rs.getLong("N_ID"));
//            	ret.setName(rs.getString("S_NAME"));
//            	ret.setKeyType(rs.getString("S_KEY_TYPE"));
//            	ret.setKey(rs.getString("S_MERCHANT_KEY"));
//            	ret.setPrivateKey(rs.getString("S_PRIVATE_KEY"));
//            	ret.setPublicKey(rs.getString("S_PUBLIC_KEY"));
//            	ret.setStatus(rs.getString("S_STATUS"));
//                return ret;
//            }
//        });
//		
//		return result.isEmpty()?null:result.get(0);
//	}
	
//	public List<PaymentMerchantDetail> queryPaymentMerchantDtl(long merchantId){
//		Assert.assertNotSame("非法支付申请号", 0L, merchantId);
//		
//		String querySql = "SELECT D.*,(SELECT P.S_NAME FROM SN_IMPREST.PAY_PLATFORM P WHERE  P.N_ID  = IFNULL(D.S_BANK_PLATFORM_ID,D.S_CONTENT)) S_NAME " +
//				" FROM SN_IMPREST.PAY_MERCHANT_DTL D WHERE D.N_MERCHANT_ID = ? AND D.S_STATUS = '1' ORDER BY D.N_SORT,D.S_TYPE";
//		
//		Map<String,Object> paramMap = new HashMap<String,Object>();
//		paramMap.put("merchantId", merchantId);
//		
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
//		List<PaymentMerchantDetail> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{merchantId},new RowMapper<PaymentMerchantDetail>() {
//            @Override
//            public PaymentMerchantDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
//            	PaymentMerchantDetail ret = new PaymentMerchantDetail();
//            	ret.setMerchantId(rs.getLong("N_ID"));
//            	ret.setMerchantId(rs.getLong("N_MERCHANT_ID"));
//            	ret.setType(rs.getString("S_TYPE"));
//            	ret.setDispFlag(rs.getString("S_DISP_FLAG"));
//            	ret.setContent(rs.getString("S_CONTENT"));
//            	ret.setBankPlatformId(rs.getString("S_BANK_PLATFORM_ID"));
//            	ret.setStatus(rs.getString("S_STATUS"));
//            	ret.setName(rs.getString("S_NAME"));
//            	ret.setDebitPayType(rs.getString("S_DEBIT_PAY_TYPE"));
//            	ret.setCreditPayType(rs.getString("S_CREDIT_PAY_TYPE"));
//                return ret;
//            }
//        });
//		
//		return result;
//	}
	
//	public List<CreditStage> queryCreditStage(String bankCode){
//		Assert.notNull(bankCode);
//		String querySql = "select * from sn_imprest.pay_bank_stage s where s.bank_code = ? and s.s_state = '1'";
//		
//		Map<String,Object> paramMap = new HashMap<String,Object>();
//		paramMap.put("bankCode", bankCode);
//		
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
//		List<CreditStage> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{bankCode},new RowMapper<CreditStage>() {
//            @Override
//            public CreditStage mapRow(ResultSet rs, int rowNum) throws SQLException {
//            	CreditStage ret = new CreditStage();
//            	ret.setBankCode(rs.getString("BANK_CODE"));
//            	ret.setStagePlan(rs.getString("STAGE_PLAN"));
//            	ret.setStageNum(rs.getInt("STAGE_NUM"));
//            	ret.setState(rs.getString("S_STATE"));
//            	ret.setPlanPoundage(rs.getFloat("PLAN_POUNDAGE"));
//                return ret;
//            }
//        });
//		
//		return result;
//	}

}
