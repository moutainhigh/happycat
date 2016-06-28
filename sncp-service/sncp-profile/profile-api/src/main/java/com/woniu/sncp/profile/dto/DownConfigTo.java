package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @author luzz
 *
 */
public class DownConfigTo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4964984689810401188L;
	
	private Long id;
	
	private String type; //应用分类
	
	private String name;//应用名称
	
	private String ico;//图标地址
	
	private String icoName;//图标名称
	
	private String size;//应用大小
	
	private String osType;//应用系统
	
	private String downUrl;//下载地址
	
	private int level;//推荐等级
	
	private Long down;//下载数量
	
	private int sort;//排序
	
	private String property;//属性
	
	private String desc;//描述
	
	private String state;//状态1启用,0禁用
	
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Long getDown() {
		return down;
	}

	public void setDown(Long down) {
		this.down = down;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
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
