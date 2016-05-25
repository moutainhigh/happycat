//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.11 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.05.25 时间 01:50:35 PM CST 
//


package com.woniu.sncp.nciic.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="inLicense" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="inConditions" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "inLicense",
    "inConditions"
})
@XmlRootElement(name = "nciicCombineSearch")
public class NciicCombineSearch {

    @XmlElement(required = true, nillable = true)
    protected String inLicense;
    @XmlElement(required = true, nillable = true)
    protected String inConditions;

    /**
     * 获取inLicense属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInLicense() {
        return inLicense;
    }

    /**
     * 设置inLicense属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInLicense(String value) {
        this.inLicense = value;
    }

    /**
     * 获取inConditions属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInConditions() {
        return inConditions;
    }

    /**
     * 设置inConditions属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInConditions(String value) {
        this.inConditions = value;
    }

}
