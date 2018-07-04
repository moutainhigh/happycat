package com.woniu.sncp.pay.common.utils.xml;


import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.ValidationException;
/**
 * @author: gexinying
 * Date: 12-7-4
 * Time: 上午11:54
 */
public class XmlConvertUtil {


    private static Logger logger = LoggerFactory.getLogger(XmlConvertUtil.class);

    /*
      * 由map转换成以下格式的XML <?xml version="1.0" encoding="utf-8"?> <ITEMS> <ITEM
      * name="nTotalPrice" value="8"/> <ITEM name="sAccount" value="527053081"/>
      * <ITEM name="iGameID" value="9"/> </ITEMS>
      */

    @SuppressWarnings("rawtypes")
    public static String mapToXml(Map<String, Object> map) {
        StringBuffer retString = new StringBuffer();
        if (map == null || map.isEmpty() || map.size() <= 0) {
            return retString.toString();
        }
        retString.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        int msgcode = 0;
        String info = "";
        try {
            msgcode = Integer.parseInt(String.valueOf(map.get("msgcode")));
        } catch (Exception e) {
            logger.error("msgcode:" + map.get("msgcode"), e);
        }
        try {
            info = (String) map.get("message");
        } catch (Exception e) {
            logger.error("message", e);
        }
        retString.append("<RESULT msgcode=\"" + msgcode + "\" message=\"" + info + "\">");
        retString.append("<ITEMS>");
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry != null && entry.getKey() != null && entry.getValue() != null
                    && !"msgcode".equals(entry.getKey()) && !"message".equals(entry.getKey())) {
                retString.append("<ITEM name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\" />");
            }
        }
        retString.append("</ITEMS></RESULT>");
        String retString2 = retString.toString().replace("<ITEMS></ITEMS>", "");
        logger.debug("最后返回 ： " + retString2);
        return retString2;
    }

    public static Map StringToMap(String mapToStringOfString) throws DocumentException {
        Document document = DocumentHelper.parseText(mapToStringOfString);
        Map<String, Object> rtn = new HashMap<String, Object>();
        rtn.put("msgcode", document.getRootElement().attributeValue("msgcode"));
        rtn.put("message", document.getRootElement().attributeValue("message"));
        List<Node> items = document.selectNodes("//ITEMS/ITEM");
        for (Node node : items) {
            if (node instanceof Element) {
                Element element = (Element) node;
                rtn.put(element.attributeValue("name"), element.attributeValue("value"));
            }
        }
        return rtn;
    }

    public static String convertByType(String returnType, Map<String, Object> retMap) {
        String result = "";
        if (retMap != null && retMap.size() > 0) {
            if (StringUtils.isNotBlank(returnType) && "json".equals(returnType)) {
                result = JsonUtils.toJson(retMap);
                if(result == null){
                    result = JsonUtils.toJson(retMap);
                }
                logger.info("result:" + result);
            } else {
                result = XmlConvertUtil.mapToXml(retMap);
            }
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static String convert(List list) {
        return JsonUtils.toJson(list);
    }

    public static Map<String, Object> getPageString(int pageSize, int pageNum, int total) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (pageNum < 0 || total < 0) {
            return null;
        }
        int pagesCount = 0;
        if (total % pageSize == 0) { // 总数/分页数
            pagesCount = total / pageSize;
        } else {
            pagesCount = total / pageSize + 1;
        }
        retMap.put("totalPage", pagesCount); // 总页数
        retMap.put("pageNum", pageNum); // 当前页起始数
        retMap.put("currentPage", pageNum); // 当前页
        retMap.put("pageSize", pageSize); // 页面长度
        retMap.put("totalCount", total); // 总记录数
        return retMap;
    }
    
    /**
	 * 增加防止部实体注入逻辑 <功能详细描述>
	 * 
	 * @param reader
	 * @throws SAXException
	 * @see [类、类#方法、类#成员]
	 */
	public static void setReaderFeature(SAXReader reader) throws SAXException {
		//
		// 36: java.lang.NullPointerException
		// 37: at
		// com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor.startDTD(XMLDTDProcessor.java:685)
		// reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
		// true);
		reader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
		reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	}
	
	public static Document parseText(String text) throws DocumentException, SAXException {
		Document result = null;

		SAXReader reader = new SAXReader();
		setReaderFeature(reader);

		String encoding = getEncoding(text);

		InputSource source = new InputSource(new StringReader(text));
		source.setEncoding(encoding);

		result = reader.read(source);

		// if the XML parser doesn't provide a way to retrieve the encoding,
		// specify it manually
		if (result.getXMLEncoding() == null) {
			result.setXMLEncoding(encoding);
		}

		return result;
	}
    
    
    public static String readXmlNode(String resData,String nodePath) {
		Document doc = null;
		try {
			doc = parseText(resData);
		} catch (Exception e) {
			throw new ValidationException("xml转换异常");
		}  
		Node node = doc.selectSingleNode(nodePath);
		String nodeText = node.getText();
		return nodeText;
	}
    
    public static Map<String,Object> XmlNodesToMap(String resData,String xpath) {
		Document doc = null;
		try {
			doc = parseText(resData);
		} catch (Exception e) {
			throw new ValidationException("xml转换异常");
		}  
		List selectNodes = doc.selectNodes(xpath);
		Map<String,Object> treeMap = new TreeMap<String,Object>();
		for (Object object : selectNodes) {
			Node node = (Node) object;
			treeMap.put(node.getName(), node.getText());
		}
		
		return treeMap;
	}
    
    private static String getEncoding(String text) {
        String result = null;

        String xml = text.trim();

        if (xml.startsWith("<?xml")) {
            int end = xml.indexOf("?>");
            String sub = xml.substring(0, end);
            StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken();
                    }

                    break;
                }
            }
        }

        return result;
    }
}
