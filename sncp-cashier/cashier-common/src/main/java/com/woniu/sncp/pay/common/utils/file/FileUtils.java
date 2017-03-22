package com.woniu.sncp.pay.common.utils.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * 读取文件的内容
	 * 
	 * @param file
	 * @return
	 */
	public static String readText(String file) {
		String ret = "";
		try {
			ret = IOUtils.toString(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.error("读取文件["+file+"]时未发现该文件,"+e.getMessage(), e);
		} catch (IOException e) {
			logger.error("读取文件["+file+"]时异常，"+e.getMessage(), e);
		}
		return ret;
	}
}
