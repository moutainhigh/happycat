package com.woniu.sncp.pay.core.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.woniu.pay.pojo.CreditStage;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pay.dao.PaymentMerchantDao;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentMerchantDetail;

@Service("paymentMerchantService")
public class PaymentMerchantService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BaseSessionDAO sessionDao;
	
	@Autowired
	PaymentMerchantDao paymentMerchantDao;
	
	public PaymentMerchant queryPayemntMerchnt(long merchantId){
		return paymentMerchantDao.queryPayemntMerchnt(merchantId);
	}
	
	public List<PaymentMerchantDetail> queryPaymentMerchantDtl(long merchantId){
		Assert.assertNotSame("非法支付申请号", 0L, merchantId);
		return paymentMerchantDao.queryPaymentMerchantDtlById(merchantId);
	}
	
	public List<CreditStage> queryCreditStage(String bankCode){
		Assert.notNull(bankCode);
		String querySql = "select * from SN_PAY.PAY_BANK_STAGE s where s.BANK_CODE = ? and s.S_STATE = '1'";
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("bankCode", bankCode);
		List<CreditStage> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{bankCode},new RowMapper<CreditStage>() {
            @Override
            public CreditStage mapRow(ResultSet rs, int rowNum) throws SQLException {
            	CreditStage ret = new CreditStage();
            	ret.setBankCode(rs.getString("BANK_CODE"));
            	ret.setStagePlan(rs.getString("STAGE_PLAN"));
            	ret.setStageNum(rs.getInt("STAGE_NUM"));
            	ret.setState(rs.getString("S_STATE"));
            	ret.setPlanPoundage(rs.getFloat("PLAN_POUNDAGE"));
                return ret;
            }
        });
		
		return result;
	}

}
