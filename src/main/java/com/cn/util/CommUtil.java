package com.cn.util;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @ClassName: UUIDUtil
 * @Description: TODO
 * @Author devfzm@gmail.com
 * @Date 2016年5月25日 下午12:54:16
 */

public class CommUtil {

	private static Logger logger = Logger.getLogger(CommUtil.class);

	/**
	 * @Title: randomUUID
	 * @Description: 随机产生一个UUID字符串
	 * @return String
	 * @throws
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * @Title: getIpAddr
	 * @Description: 获取访问者的IP地址
	 * @param request
	 * @return String
	 * @throws
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ipAddress = null;

		ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0
				|| "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0
				|| "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0
				|| "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1")
					|| ipAddress.equals("0:0:0:0:0:0:0:1")) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (Exception e) {
					logger.error("Get IP error: " + e);
				}
				ipAddress = inet.getHostAddress();
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15) {
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

	/**
	 * @Title: readFile
	 * @Description: 读取文件
	 * @param path
	 * @return String
	 * @throws
	 */
	public static String readFile(String path) {
		File file = new File(path);
		Scanner scanner = null;
		StringBuilder buffer = new StringBuilder();
		try {
			scanner = new Scanner(file, "utf-8");
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine());
			}
		} catch (Exception e) {
			logger.error("Read File error: " + e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return buffer.toString();
	}

	/**
	 * @Title: readProperties
	 * @Description: 读取properties文件
	 * @param name resources下的文件名
	 * @return Properties
	 * @throws
	 */
	public static Properties readProperties(String name) {
		Properties prop = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		try {
			prop.load(in);
			in.close();
		} catch (Exception e) {
			logger.error("Read properties error: " + e);
		}
		return prop;
	}
	
}
