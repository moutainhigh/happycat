package com.woniu.sncp.cbss.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PidUtil {

	private static final Logger logger = LoggerFactory.getLogger(PidUtil.class);

	public static String procCpuShell = "top -b -n 1 -p $pid | sed '$d' | sed -n '$p' | awk '{print $9}' ";

	/**
	 * 是否是window系统
	 * 
	 * @return
	 */
	public static boolean isWin() {
		String osName = (String) System.getProperties().get("os.name");
		if (osName.contains("Win")) {
			return true;
		}
		return false;
	}

	/**
	 * 仅支持linux
	 * 
	 * 如果是window系统返回-1
	 * 
	 * @param pid
	 * @return
	 */
	public static double getProcCpu(int pid) {
		Process process = null;
		BufferedReader br = null;
		try {

			if (isWin()) {
				return -1;
			}

			if ("".equals(procCpuShell))
				return 0;
			procCpuShell = procCpuShell.replaceAll("\\$pid", pid + "");
			String[] cmd = new String[] { "/bin/sh", "-c", procCpuShell };
			process = Runtime.getRuntime().exec(cmd);
			int resultCode = process.waitFor();
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				Double cpu = Double.parseDouble(line);
				if (cpu > 100)
					cpu = cpu / 10;// 刚启动会出现CPU100多情况，则处理除于10
				return cpu;
			}
		} catch (Exception e) {
			logger.error("执行获取进程CPU使用率错误", e);
		} finally {
			try {
				if (process != null)
					process.destroy();
				if (br != null)
					br.close();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

		}
		return 0.0;
	}
}
