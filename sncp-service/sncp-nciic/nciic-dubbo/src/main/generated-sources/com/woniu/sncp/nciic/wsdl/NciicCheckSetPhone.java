//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.11 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.05.26 时间 04:53:18 PM CST 
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
 *         &lt;element name="fileObject" type="{http://domain.webservices.serv.nciic.com}FileObjectSetPhone"/&gt;
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
    "fileObject"
})
@XmlRootElement(name = "nciicCheckSetPhone")
public class NciicCheckSetPhone {

    @XmlElement(required = true, nillable = true)
    protected FileObjectSetPhone fileObject;

    /**
     * 获取fileObject属性的值。
     * 
     * @return
     *     possible object is
     *     {@link FileObjectSetPhone }
     *     
     */
    public FileObjectSetPhone getFileObject() {
        return fileObject;
    }

    /**
     * 设置fileObject属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link FileObjectSetPhone }
     *     
     */
    public void setFileObject(FileObjectSetPhone value) {
        this.fileObject = value;
    }

}
