package cbss.core.model.access;

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
