package com.woniu.sncp.cbss.core.model.access;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * webservice接口访问权限表 - SN_PASSPORT.PP_SECURITY_RESOURCE
 * 
 * @author zhangweipeng
 * @since 1.0
 */
public class SecurityResource {

	/**
	 * 序列化值
	 */
	private static final long serialVersionUID = 1L;
	private SecurityResourcePK id;

	/**
	 * 版本 - S_VERSION
	 */
	private String version;

	/**
	 * 资料填写时间 - D_CREATE
	 */
	private Date createTime;
	/**
	 * 状态 - S_STATE
	 */
	private String state;

	/**
	 * 提示信息 - S_NOTE
	 */
	private String note;

	/**
	 * 当status为SERVER_FUTURE_STOPED或SERVER_FUTURE_MAINTAIN时，此值会出现一个时间点格式:yyyy-MM
	 * -dd HH:mm:ss,表示在此时间点会进行维护或停服务
	 */
	private String futureTime = "";
	/**
	 * 当status为DOMAINNAME_CHANGE时，此值一个新域名或逗号分隔的多个域名，使用人按照顺序逐个调用直到调用成功或每个都使用过，如域名
	 * :a.b.c,a1.b.c,a2.b.c,表示3个域名轮询调用
	 */
	private String domaneName = "";

	/**
	 * 签名规则，默认0
	 */
	private int signType = 0;
	
	public int getSignType() {
		return signType;
	}

	public void setSignType(int signType) {
		this.signType = signType;
	}

	public String getFutureTime() {
		return StringUtils.isBlank(futureTime) ? "" : futureTime;
	}

	public void setFutureTime(String futuretime) {
		this.futureTime = futuretime;
	}

	public String getDomaneName() {
		return StringUtils.isBlank(domaneName) ? "" : domaneName;
	}

	public void setDomaneName(String domaneName) {
		this.domaneName = domaneName;
	}

	public SecurityResourcePK getId() {
		return id;
	}

	public void setId(SecurityResourcePK id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Map getNoteFirst() {
		String[] ns = StringUtils.split(this.note, "\\|\\|");
		return JSONObject.parseObject(ns[0]);
	}

	public List<Map> getNoteMore() {
		List<Map> datas = new ArrayList<Map>();
		String[] ns = StringUtils.split(this.note, "\\|\\|");
		for (String n : ns) {
			datas.add(JSONObject.parseObject(ns[0]));
		}
		return datas;
	}

}
