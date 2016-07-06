package com.woniu.sncp.profile.service.ploy;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PresentsPloyContext {
	@Resource
	private PresentsPloy presentsPloy_S;

	@Resource
	private PresentsPloy presentsPloy_C;

	@Resource
	private PresentsPloy presentsPloy_d;

	@Resource
	private PresentsPloy presentsPloy_e;
	
	@Resource
	private PresentsPloy presentsPloy_q;

	@Resource
	private PresentsPloy presentsPloy_K;

	@Resource
	private PresentsPloy presentsPloy_L;

	@Resource
	private PresentsPloy presentsPloy_M;
	
	@Resource
	private PresentsPloy presentsPloy_z;
	
	@Resource
	private PresentsPloy presentsPloy_y;

	@Resource
	private PresentsPloy presentsPloy_a1;
	@Resource
	private PresentsPloy presentsPloy_a2;
	@Resource
	private PresentsPloy presentsPloy_a3;
    @Resource
    private PresentsPloy presentsPloy_k;
    @Resource
    private PresentsPloy presentsPloy_mz;
    @Resource
    private PresentsPloy presentsPloy_sc;
    @Resource
    private PresentsPloy presentsPloy_YDC;
    @Resource
    private PresentsPloy presentsPloy_YDS;
    @Resource
    private PresentsPloy presentsPloy_fd;

	private final Logger logger = Logger.getLogger(this.getClass());

	public PresentsPloy getPresentsPloyByType(String type) {
		if (StringUtils.isEmpty(type))
			logger.error("活动类型不能为空");
		if ("S".equals(type)) {
			return presentsPloy_S;
		} else if ("C".equals(type)) {
			return presentsPloy_C;
		} else if ("d".equals(type)) {
			return presentsPloy_d;
		} else if ("e".equals(type)) {
			return presentsPloy_e;
		} else if ("K".equals(type)) {
			return presentsPloy_K;
		} else if ("L".equals(type)) {
			return presentsPloy_L;
		} else if ("M".equals(type)) {
			return presentsPloy_M;
		}else if ("q".equals(type)) {
			return presentsPloy_q;
		}else if("z".equals(type)){
			return presentsPloy_z;
		}else if("y".equals(type)){
			return presentsPloy_y;
		}else if ("a1".equals(type)) {
			return presentsPloy_a1;
		}else if ("a2".equals(type)) {
			return presentsPloy_a2;
		}else if ("a3".equals(type)) {
			return presentsPloy_a3;
		}else if ("mz".equals(type)) {
			return presentsPloy_mz;
		}else if ("sc".equals(type)) {
			return presentsPloy_sc;
		}else if ("fd".equals(type)) {
			return presentsPloy_fd;
		}else if ("YDC".equals(type)) {
			return presentsPloy_YDC;
		}else if ("YDS".equals(type)) {
			return presentsPloy_YDS;
		} else {
			logger.error("活动类型不合法,type=" + type);
			return null;
		}
	}
}
