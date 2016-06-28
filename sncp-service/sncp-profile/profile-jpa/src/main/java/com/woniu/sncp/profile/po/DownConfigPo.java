package com.woniu.sncp.profile.po;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author luzz
 *
 */
@Entity
@Table(name="PP_DOWN_CONF",schema="SN_PROFILE")
public class DownConfigPo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4964984689810401188L;
	
	@Id
	@Column(name="n_id")
	private Long id;
	
	@Column(name="s_type")
	private String type; //应用分类
	
	@Column(name="s_name")
	private String name;//应用名称
	
	@Column(name="s_ico")
	private String ico;//图标地址
	
	@Column(name="s_ico_name")
	private String icoName;//图标名称
	
	@Column(name="s_size")
	private String size;//应用大小
	
	@Column(name="s_os_type")
	private String osType;//应用系统
	
	@Column(name="s_down_url")
	private String downUrl;//下载地址
	
	@Column(name="n_level")
	private Integer level;//推荐等级
	
	@Column(name="n_down")
	private Long down;//下载数量
	
	@Column(name="n_sort")
	private Integer sort;//排序
	
	@Column(name="s_property")
	private String property;//属性
	
	@Column(name="s_desc")
	private String desc;//描述
	
	@Column(name="s_state")
	private String state;//状态1启用,0禁用
	
	@Column(name="d_create")
	private Date create;//创建时间
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIco() {
		return ico;
	}

	public void setIco(String ico) {
		this.ico = ico;
	}

	public String getIcoName() {
		return icoName;
	}

	public void setIcoName(String icoName) {
		this.icoName = icoName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}
	
	public Long getDown() {
		return down;
	}

	public void setDown(Long down) {
		this.down = down;
	}
	
	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
	}

}
