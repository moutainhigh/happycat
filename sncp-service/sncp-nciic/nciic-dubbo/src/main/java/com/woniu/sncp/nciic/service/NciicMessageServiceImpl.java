package com.woniu.sncp.nciic.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.utils.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.woniu.sncp.exception.BusinessException;
import com.woniu.sncp.nciic.dto.NciicMessageOut;
import com.woniu.sncp.nciic.dto.NciicMessageIn;

public class NciicMessageServiceImpl implements NciicMessageService {

	private Logger logger = LoggerFactory.getLogger(NciicMessageServiceImpl.class);

	@Autowired
	private NciicClient nciicClient;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/**
	 * 服务地址
	 */
	@Value("${nciic.license}")
	private String license;

	@Value("${nciic.sbm}")
	private String sbm;

	@Value("${nciic.maxInDay}")
	private Long maxInDay;

	private String buildXml(List<NciicMessageIn> nciicMessageTos) {
		Document document = DocumentHelper.createDocument();
		Element ROWS = document.addElement("ROWS");
		Element INFO = ROWS.addElement("INFO");
		INFO.addElement("SBM").addText(sbm);
		Element ROW = ROWS.addElement("ROW");
		ROW.addElement("GMSFHM").addText("公民身份号码");
		ROW.addElement("XM").addText("姓名");
		for (NciicMessageIn nciicMessageTo : nciicMessageTos) {
			ROW = ROWS.addElement("ROW");
			ROW.addAttribute("FSD", "215000");
			ROW.addAttribute("YWLX", "215000");
			ROW.addElement("GMSFHM").addText(nciicMessageTo.getIdentityNo().trim());
			ROW.addElement("XM").addText(nciicMessageTo.getUserName().trim());
		}
		return document.asXML();
	}

	@SuppressWarnings("unchecked")
	private List<NciicMessageOut> unbuildString(String response)
			throws NciicException {
		try {
			Document document = DocumentHelper.parseText(response);
			Node errorCode = document.selectSingleNode("//ROWS/ROW/ErrorCode");
			Node errorMsg = document.selectSingleNode("//ROWS/ROW/ErrorMsg");
			if (errorCode != null || errorMsg != null) {
				throw new BusinessException(((Element) errorCode).getText() + ":" + ((Element) errorMsg).getText());
			}
			List<NciicMessageOut> outs = new ArrayList<NciicMessageOut>();
			List<Element> rows = document.selectNodes("//ROWS/ROW");
			for (Element row : rows) {
				Node errormesage = row.selectSingleNode("OUTPUT/ITEM/errormesage");
				Node errormesagecol = row.selectSingleNode("OUTPUT/ITEM/errormesagecol");
				Node no = row.selectSingleNode("@no");

				String xm = (row.selectSingleNode("INPUT/xm")).getText();
				String sfhm = (row.selectSingleNode("INPUT/gmsfhm")).getText();
				NciicMessageOut messageOut = new NciicMessageOut(xm, sfhm);
				outs.add(messageOut);

				if (errormesage != null && errormesagecol != null) {
					messageOut.setErrorInfo(no.getText() + ":" + ((Element) errormesagecol).getText() + ":" + ((Element) errormesage).getText());
					continue;
				}

				String idnoresult = (row.selectSingleNode("OUTPUT/ITEM/result_gmsfhm")).getText();
				String nameresult = (row.selectSingleNode("OUTPUT/ITEM/result_xm")).getText();

				if (NciicMessageOut.SUCC_SAME.equals(idnoresult)) {
					messageOut.setIdentityNoResult(NciicMessageOut.SUCC_SAME);
				} else {
					messageOut.setIdentityNoResult(idnoresult);
				}
				if (NciicMessageOut.SUCC_SAME.equals(nameresult)) {
					messageOut.setUserNameResult(NciicMessageOut.SUCC_SAME);
				} else {
					messageOut.setUserNameResult(nameresult);
				}
			}
			return null;
		} catch (Exception e) {
			throw new NciicException();
		}
	}

	private void vaildMaxInDay(int len)
			throws NciicMaxCountInADayException {
		String day = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
		Long size = redisTemplate.opsForValue().increment("NciicMessageService-checkRealNameIdentityNo-" + day, len);
		if (maxInDay != null && maxInDay.compareTo(0L) > 0) {
			if (maxInDay.compareTo(size) < 0) {
				throw new NciicMaxCountInADayException("maxInDay=" + maxInDay + ",actual in " + day + " =" + size);
			}
		} else {
			throw new NciicMaxCountInADayException("maxInDay=" + maxInDay + ",actual in " + day + " =" + size);
		}
	}

	@Override
	public List<NciicMessageOut> checkRealNameIdentityNo(List<NciicMessageIn> nciicMessageTo)
			throws NciicException {
		vaildMaxInDay(nciicMessageTo.size());
		String response = nciicClient.nciicCheckResponse(buildXml(nciicMessageTo), license).getOut();
		return unbuildString(response);
	}

	@Override
	public NciicMessageOut checkRealNameIdentityNo(NciicMessageIn nciicMessageTo)
			throws NciicException {

		vaildMaxInDay(1);

		ArrayList<NciicMessageIn> list = new ArrayList<NciicMessageIn>(1);
		list.add(nciicMessageTo);

		String response = nciicClient.nciicCheckResponse(buildXml(list), license).getOut();

		return unbuildString(response).get(0);
	}
}
